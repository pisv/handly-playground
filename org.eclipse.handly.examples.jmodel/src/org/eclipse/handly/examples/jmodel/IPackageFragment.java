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
package org.eclipse.handly.examples.jmodel;

import org.eclipse.core.runtime.CoreException;

/**
 * A package fragment is a portion of the workspace corresponding to
 * an entire package, or to a portion thereof. The distinction between
 * a package fragment and a package is that a package with some name
 * is the union of all package fragments in the class path
 * which have the same name.
 */
public interface IPackageFragment
    extends IJavaElement
{
    @Override
    default IPackageFragmentRoot getParent()
    {
        return (IPackageFragmentRoot)IJavaElement.super.getParent();
    }

    /**
     * Returns the compilation unit with the specified name in this package
     * (for example, <code>"Object.java"</code>). The name must end with ".java".
     * <p>
     * This is a handle-only method. The compilation unit may or may not exist.
     * </p>
     *
     * @param name the given name (not <code>null</code>)
     * @return the compilation unit with the specified name in this package
     *  (never <code>null</code>)
     */
    ICompilationUnit getCompilationUnit(String name);

    /**
     * Returns all of the compilation units in this package fragment.
     *
     * @return all of the compilation units in this package fragment
     *  (never <code>null</code>). Clients <b>must not</b> modify the
     *  returned array.
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    ICompilationUnit[] getCompilationUnits() throws CoreException;

    /**
     * Returns the non-Java resources directly contained in this
     * package fragment.
     *
     * @return the non-Java resources directly contained  in this package fragment
     *  (never <code>null</code>). Clients <b>must not</b> modify the returned array.
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    Object[] getNonJavaResources() throws CoreException;

    /**
     * Returns whether this package fragment is a default package.
     * This is a handle-only method.
     *
     * @return <code>true</code> if this package fragment is a default package
     */
    boolean isDefaultPackage();

    /**
     * Returns whether this package fragment's name is a prefix of other
     * package fragments in this package fragment's root.
     *
     * @return <code>true</code> if this package fragment's name is a prefix
     *  of other package fragments in this package fragment's root
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    boolean hasSubpackages() throws CoreException;
}
