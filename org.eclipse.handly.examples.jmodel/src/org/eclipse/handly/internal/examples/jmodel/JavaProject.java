/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.jmodel.IJavaProject;
import org.eclipse.handly.examples.jmodel.IPackageFragment;
import org.eclipse.handly.examples.jmodel.IPackageFragmentRoot;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

/**
 * Implementation of {@link IJavaProject}.
 */
public class JavaProject
    extends JavaElement
    implements IJavaProject
{
    private static final IPackageFragmentRoot[] NO_CHILDREN =
        new IPackageFragmentRoot[0];

    private final IProject project;

    /**
     * Constructs a handle for a Java project with the given parent element
     * and the given underlying workspace project.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param project the workspace project underlying the element
     *  (not <code>null</code>)
     */
    public JavaProject(JavaModel parent, IProject project)
    {
        super(parent, project.getName());
        if (parent == null)
            throw new IllegalArgumentException();
        this.project = project;
    }

    @Override
    public IProject getProject()
    {
        return project;
    }

    @Override
    public PackageFragmentRoot getPackageFragmentRoot(IResource resource)
    {
        // In this example model, only folders that are direct children of the project
        // can be viewed as a package fragment root (representing a source folder)
        if (resource != null && resource.getType() == IResource.FOLDER
            && resource.getParent().equals(project))
        {
            return new PackageFragmentRoot(this, resource);
        }
        return null;
    }

    @Override
    public IPackageFragmentRoot[] getPackageFragmentRoots() throws CoreException
    {
        return (IPackageFragmentRoot[])getChildren();
    }

    @Override
    public IResource[] getNonJavaResources() throws CoreException
    {
        return ((JavaProjectBody)getBody_()).getNonJavaResources(this);
    }

    @Override
    public IPackageFragment findPackageFragment(IResource resource)
    {
        if (!exists())
            return null;
        try
        {
            IClasspathEntry[] rawClasspath = getRawClasspath();
            return findPackageFragment(resource, rawClasspath);
        }
        catch (CoreException e)
        {
            return null;
        }
    }

    IPackageFragment findPackageFragment(IResource resource,
        IClasspathEntry[] classpath)
    {
        // In this example model, only folders can correspond to a package fragment
        if (resource.getType() != IResource.FOLDER)
            return null;

        IPath resourcePath = resource.getFullPath();
        for (IClasspathEntry entry : classpath)
        {
            int entryKind = entry.getEntryKind();

            // In this example model, only source folders are considered
            if (entryKind != IClasspathEntry.CPE_SOURCE)
                continue;

            IPath entryPath = entry.getPath();
            if (entryPath.equals(resourcePath))
            {
                PackageFragmentRoot root = getPackageFragmentRoot(resource);
                if (root == null)
                    return null;
                return root.getPackageFragment(""); //$NON-NLS-1$
            }
            else if (entryPath.isPrefixOf(resourcePath))
            {
                IResource rootFolder;
                if (entryPath.segmentCount() == 1)
                    rootFolder = project;
                else
                    rootFolder = project.getParent().getFolder(entryPath);

                PackageFragmentRoot root = getPackageFragmentRoot(rootFolder);
                if (root == null)
                    return null;

                IPath packagePath = resourcePath.removeFirstSegments(
                    entryPath.segmentCount());

                PackageFragment packageFragment = root.getPackageFragment(
                    packagePath.segments());
                if (!packageFragment.isValidPackageName())
                    return null;

                return packageFragment;
            }
        }
        return null;
    }

    PerProjectInfo getPerProjectInfo() throws CoreException
    {
        return JavaModelManager.INSTANCE.getPerProjectInfoCheckExistence(
            project);
    }

    /**
     * Returns the raw classpath for this project, as a list of classpath entries.
     *
     * @return the raw classpath for this project, as a list of classpath entries
     *  (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @see IClasspathEntry
     */
    public IClasspathEntry[] getRawClasspath() throws CoreException
    {
        return getPerProjectInfo().getRawClasspath();
    }

    /**
     * Returns the output location for this project, as a workspace-relative
     * absolute path.
     * <p>
     * The output location is where class files are generated (and resource
     * files, copied).
     * </p>
     *
     * @return the output location for this project, as a workspace-relative
     *  absolute path (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    public IPath getOutputLocation() throws CoreException
    {
        return getPerProjectInfo().getOutputLocation();
    }

    void resetRawClasspath()
    {
        PerProjectInfo info = JavaModelManager.INSTANCE.getPerProjectInfo(
            project, false);
        if (info != null)
            info.setRawClasspath(null, null);
    }

    /**
     * Helper method for returning one option value only. Equivalent to
     * <code>(String)this.getOptions(inheritJavaCoreOptions).get(optionName)</code>
     * Note that it may answer <code>null</code> if this option does not exist,
     * or if there is no custom value for it.
     *
     * @param optionName the name of the option
     * @param inheritJavaCoreOptions indicates whether JavaCore options
     *  should be inherited as well
     * @return the String value of the given option, or <code>null</code> if none
     */
    public String getOption(String optionName, boolean inheritJavaCoreOptions)
    {
        // Cheat and delegate directly to JDT
        return JavaCore.create(project).getOption(optionName,
            inheritJavaCoreOptions);
    }

    /**
     * Returns the table of the current custom options for this project.
     * Projects remember their custom options, in other words, only the options
     * different from the the JavaCore global options for the workspace.
     * A boolean argument allows to directly merge the project options
     * with global ones from <code>JavaCore</code>.
     *
     * @param inheritJavaCoreOptions indicates whether JavaCore options
     *  should be inherited as well
     * @return table of current settings of all options (never <code>null</code>)
     */
    public Map<String, String> getOptions(boolean inheritJavaCoreOptions)
    {
        // Cheat and delegate directly to JDT
        return JavaCore.create(project).getOptions(inheritJavaCoreOptions);
    }

    @Override
    public IResource getResource_()
    {
        return project;
    }

    @Override
    public void validateExistence_(IContext context) throws CoreException
    {
        if (!project.hasNature(NATURE_ID))
            throw newDoesNotExistException_();
    }

    @Override
    public void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        IClasspathEntry[] rawClasspath = getRawClasspath();
        List<IPackageFragmentRoot> roots = new ArrayList<>();
        IPath projectPath = getPath();
        for (IClasspathEntry entry : rawClasspath)
        {
            IPackageFragmentRoot root = null;
            IPath entryPath = entry.getPath();
            int entryKind = entry.getEntryKind();
            // In this example model, only source folders that are
            // direct children of the project resource are represented
            // as package fragment roots
            if (entryKind == IClasspathEntry.CPE_SOURCE)
            {
                if (projectPath.isPrefixOf(entryPath)
                    && entryPath.segmentCount() == 2)
                {
                    IResource resource = project.getParent().findMember(
                        entryPath);
                    if (resource != null
                        && resource.getType() == IResource.FOLDER)
                    {
                        root = new PackageFragmentRoot(this, resource);
                    }
                }
            }
            if (root != null)
                roots.add(root);
        }
        JavaProjectBody body = new JavaProjectBody();
        body.setChildren(roots.toArray(NO_CHILDREN));
        context.get(NEW_ELEMENTS).put(this, body);
    }

    @Override
    protected char getHandleMementoDelimiter()
    {
        return JEM_JAVAPROJECT;
    }

    @Override
    protected JavaElement getHandleFromMemento(String token,
        MementoTokenizer memento)
    {
        if (token == MementoTokenizer.PACKAGEFRAGMENTROOT)
        {
            String rootPath = ""; //$NON-NLS-1$
            token = null;
            if (memento.hasMoreTokens())
            {
                token = memento.nextToken();
                if (!MementoTokenizer.isDelimeter(token))
                {
                    rootPath = token;
                    token = null;
                }
            }
            PackageFragmentRoot root = getPackageFragmentRoot(
                getProject().getFolder(new Path(rootPath)));
            if (token == null)
                return root.getHandleFromMemento(memento);
            else
                return root.getHandleFromMemento(token, memento);
        }
        return null;
    }
}
