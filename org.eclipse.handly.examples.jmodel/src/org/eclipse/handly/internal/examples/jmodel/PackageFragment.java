/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     Ondrej Ilcik (Codasip)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel;

import static org.eclipse.handly.util.ToStringOptions.FORMAT_STYLE;
import static org.eclipse.handly.util.ToStringOptions.INDENT_LEVEL;
import static org.eclipse.handly.util.ToStringOptions.INDENT_POLICY;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.LONG;

import java.util.HashSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.jmodel.ICompilationUnit;
import org.eclipse.handly.examples.jmodel.IPackageFragment;
import org.eclipse.handly.util.IndentPolicy;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;

/**
 * Implementation of {@link IPackageFragment}
 */
public class PackageFragment
    extends JavaElement
    implements IPackageFragment
{
    private static final ICompilationUnit[] NO_CHILDREN =
        new ICompilationUnit[0];

    private final String[] simpleNames;

    /**
     * Constructs a package fragment with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param name the name of the element (not <code>null</code>)
     */
    public PackageFragment(PackageFragmentRoot parent, String name)
    {
        super(parent, name);
        if (parent == null)
            throw new IllegalArgumentException();
        if (name == null)
            throw new IllegalArgumentException();
        simpleNames = Signature.getSimpleNames(name);
    }

    PackageFragment(PackageFragmentRoot parent, String[] simpleNames)
    {
        super(parent, Signature.toQualifiedName(simpleNames));
        if (parent == null)
            throw new IllegalArgumentException();
        this.simpleNames = simpleNames;
    }

    @Override
    public PackageFragmentRoot getParent()
    {
        return (PackageFragmentRoot)getParent_();
    }

    @Override
    public CompilationUnit getCompilationUnit(String name)
    {
        if (name == null)
            throw new IllegalArgumentException();
        return new CompilationUnit(this, ((IContainer)getResource()).getFile(
            new Path(name)), null);
    }

    @Override
    public ICompilationUnit[] getCompilationUnits() throws CoreException
    {
        return (ICompilationUnit[])getChildren();
    }

    @Override
    public Object[] getNonJavaResources() throws CoreException
    {
        // Note: non-java resources of the default package are assigned
        // to the package fragment root.
        if (isDefaultPackage())
            return PackageFragmentBody.NO_NON_JAVA_RESOURCES;
        else
            return ((PackageFragmentBody)getBody_()).getNonJavaResources(this);
    }

    @Override
    public boolean isDefaultPackage()
    {
        return getElementName().isEmpty();
    }

    @Override
    public boolean hasSubpackages() throws CoreException
    {
        IPackageFragment[] packages = getParent().getChildrenOfType(
            IPackageFragment.class);
        int namesLength = simpleNames.length;
        nextPackage: for (IPackageFragment pkg : packages)
        {
            String[] otherNames = ((PackageFragment)pkg).simpleNames;
            if (otherNames.length <= namesLength)
                continue nextPackage;
            for (int i = 0; i < namesLength; i++)
            {
                if (!simpleNames[i].equals(otherNames[i]))
                    continue nextPackage;
            }
            return true;
        }
        return false;
    }

    boolean isValidPackageName()
    {
        JavaProject javaProject = getParent().getParent();
        String sourceLevel = javaProject.getOption(JavaCore.COMPILER_SOURCE,
            true);
        String complianceLevel = javaProject.getOption(
            JavaCore.COMPILER_COMPLIANCE, true);
        for (String simpleName : simpleNames)
        {
            if (JavaConventions.validateIdentifier(simpleName, sourceLevel,
                complianceLevel).getSeverity() == IStatus.ERROR)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public IResource getResource_()
    {
        if (simpleNames.length == 0)
            return getParent().getResource();
        IPath path = Path.EMPTY;
        for (String simpleName : simpleNames)
            path = path.append(simpleName);
        return ((IContainer)getParent().getResource()).getFolder(path);
    }

    @Override
    public void validateExistence_(IContext context) throws CoreException
    {
        if (!isValidPackageName())
            throw newDoesNotExistException_();

        IResource resource = getResource();
        if (resource != null && !resource.isAccessible())
            throw newDoesNotExistException_();
    }

    @Override
    public void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        HashSet<ICompilationUnit> children = new HashSet<>();
        IResource[] members = ((IContainer)getResource()).members();
        if (members.length > 0)
        {
            JavaProject javaProject = getAncestorOfType(JavaProject.class);
            String sourceLevel = javaProject.getOption(JavaCore.COMPILER_SOURCE,
                true);
            String complianceLevel = javaProject.getOption(
                JavaCore.COMPILER_COMPLIANCE, true);
            for (IResource member : members)
            {
                if (member instanceof IFile)
                {
                    if (JavaConventions.validateCompilationUnitName(
                        member.getName(), sourceLevel,
                        complianceLevel).getSeverity() != IStatus.ERROR)
                    {
                        children.add(new CompilationUnit(this, (IFile)member,
                            null));
                    }
                }
            }
        }
        PackageFragmentBody body = new PackageFragmentBody();
        body.setChildren(children.toArray(NO_CHILDREN));
        context.get(NEW_ELEMENTS).put(this, body);
    }

    @Override
    public String toString_(IContext context)
    {
        if (context.getOrDefault(FORMAT_STYLE) == LONG)
        {
            StringBuilder builder = new StringBuilder();
            IndentPolicy indentPolicy = context.getOrDefault(INDENT_POLICY);
            int indentLevel = context.getOrDefault(INDENT_LEVEL);
            indentPolicy.appendIndent(builder, indentLevel);
            Object body = peekAtBody_();
            toStringBody_(builder, body, context);
            if (body != null)
                builder.append(" (...)"); //$NON-NLS-1$
            return builder.toString();
        }
        return super.toString_(context);
    }

    @Override
    public void toStringName_(StringBuilder builder, IContext context)
    {
        if (isDefaultPackage())
            builder.append("<default>"); //$NON-NLS-1$
        else
            super.toStringName_(builder, context);
    }

    @Override
    protected char getHandleMementoDelimiter()
    {
        return JEM_PACKAGEFRAGMENT;
    }

    @Override
    protected JavaElement getHandleFromMemento(String token,
        MementoTokenizer memento)
    {
        if (token == MementoTokenizer.COMPILATIONUNIT)
        {
            String name = ""; //$NON-NLS-1$
            token = null;
            if (memento.hasMoreTokens())
            {
                token = memento.nextToken();
                if (!MementoTokenizer.isDelimeter(token))
                {
                    name = token;
                    token = null;
                }
            }
            CompilationUnit compilationUnit = getCompilationUnit(name);
            if (token == null)
                return compilationUnit.getHandleFromMemento(memento);
            else
                return compilationUnit.getHandleFromMemento(token, memento);
        }
        return null;
    }
}
