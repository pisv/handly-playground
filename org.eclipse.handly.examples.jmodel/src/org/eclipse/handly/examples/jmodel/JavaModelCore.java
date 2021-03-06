/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC.
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
package org.eclipse.handly.examples.jmodel;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.handly.internal.examples.jmodel.JavaModelManager;
import org.eclipse.handly.internal.examples.jmodel.MementoTokenizer;

/**
 * Facade to the Java model.
 */
public class JavaModelCore
{
    /**
     * Returns the Java model element.
     *
     * @return the Java model element (never <code>null</code>)
     */
    public static IJavaModel getJavaModel()
    {
        return JavaModelManager.INSTANCE.getModel();
    }

    /**
     * Returns the Java element corresponding to the given handle identifier
     * that was generated by {@link IJavaElement#getHandleIdentifier()}.
     *
     * @param handleIdentifier the given handle identifier
     *  (may be <code>null</code>)
     * @return the Java element corresponding to the handle identifier,
     *  or <code>null</code> if unable to create the associated element
     */
    public static IJavaElement create(String handleIdentifier)
    {
        if (handleIdentifier == null)
            return null;
        return JavaModelManager.INSTANCE.getModel().getHandleFromMemento(
            new MementoTokenizer(handleIdentifier));
    }

    /**
     * Returns the Java element corresponding to the given resource,
     * or <code>null</code> if unable to associate the given resource
     * with a Java element.
     *
     * @param resource the given resource (may be <code>null</code>)
     * @return the Java element corresponding to the given resource,
     *  or <code>null</code> if unable to associate the given resource
     *  with a Java element
     */
    public static IJavaElement create(IResource resource)
    {
        if (resource == null)
            return null;

        if (resource instanceof IWorkspaceRoot)
            return getJavaModel();
        else if (resource instanceof IProject)
            return create((IProject)resource);
        else if (resource instanceof IFolder)
            return create((IFolder)resource);
        else if (resource instanceof IFile)
            return create((IFile)resource);
        else
            throw new AssertionError();
    }

    /**
     * Returns the Java project corresponding to the given project.
     * <p>
     * Note that no check is done at this time on the existence
     * or the nature of this project.
     * </p>
     *
     * @param project the given project (may be <code>null</code>)
     * @return the Java project corresponding to the given project,
     *  or <code>null</code> if the given project is <code>null</code>
     */
    public static IJavaProject create(IProject project)
    {
        if (project == null)
            return null;
        return getJavaModel().getJavaProject(project.getName());
    }

    /**
     * Returns the package fragment or package fragment root corresponding
     * to the given folder, or <code>null</code> if unable to associate
     * the given folder with a Java element.
     * <p>
     * Note that a package fragment root is returned rather than a default package.
     * </p>
     *
     * @param folder the given folder (may be <code>null</code>)
     * @return the package fragment or package fragment root corresponding
     *  to the given folder, or <code>null</code> if unable to associate
     *  the given folder with a Java element
     */
    public static IJavaElement create(IFolder folder)
    {
        if (folder == null)
            return null;
        IJavaProject javaProject = create(folder.getProject());
        IPackageFragment pkg = javaProject.findPackageFragment(folder);
        if (pkg != null && pkg.isDefaultPackage())
            return pkg.getParent();
        return pkg;
    }

    /**
     * Returns the Java element corresponding to the given file, or
     * <code>null</code> if unable to associate the given file
     * with a Java element.
     *
     * @param file the given file (may be <code>null</code>)
     * @return the Java element corresponding to the given file, or
     *  <code>null</code> if unable to associate the given file
     *  with a Java element
     */
    public static IJavaElement create(IFile file)
    {
        if (file == null)
            return null;
        // In this example model, we consider only Java source files
        if ("java".equals(file.getFileExtension())) //$NON-NLS-1$
            return createCompilationUnitFrom(file);
        return null;
    }

    /**
     * Returns the compilation unit corresponding to the given file,
     * or <code>null</code> if unable to associate the given file
     * with a compilation unit. The file must be a <code>.java</code> file.
     *
     * @param file the given file (may be <code>null</code>)
     * @return the compilation unit corresponding to the given file,
     *  or <code>null</code> if unable to associate the given file
     *  with a compilation unit
     */
    public static ICompilationUnit createCompilationUnitFrom(IFile file)
    {
        if (file == null)
            return null;
        IContainer parent = file.getParent();
        if (!(parent instanceof IFolder))
            return null; // in this example model, CU's parent must be a folder
        IJavaElement element = create((IFolder)parent);
        if (element == null)
            return null;
        IPackageFragment pkg = element instanceof IPackageFragment
            ? (IPackageFragment)element
            : ((IPackageFragmentRoot)element).getPackageFragment(""); //$NON-NLS-1$
        return pkg.getCompilationUnit(file.getName());
    }

    private JavaModelCore()
    {
    }
}
