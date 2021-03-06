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
package org.eclipse.handly.examples.jmodel;

import org.eclipse.core.runtime.CoreException;

/**
 * Represents an import container. It is a child of a Java compilation unit
 * that contains all (and only) import declarations. If a compilation unit
 * has no import declarations, no import container will be present.
 */
public interface IImportContainer
    extends IJavaSourceConstruct
{
    @Override
    default ICompilationUnit getParent()
    {
        return (ICompilationUnit)IJavaSourceConstruct.super.getParent();
    }

    /**
     * Returns the import declaration in this import container
     * with the given name.
     * <p>
     * This is a handle-only method. The import declaration may or may not exist.
     * </p>
     *
     * @param name the given name (not <code>null</code>)
     * @return the import declaration in this import container
     *  with the given name (never <code>null</code>)
     */
    IImportDeclaration getImport(String name);

    /**
     * Returns the import declarations in this import container in the order
     * in which they appear in the source.
     *
     * @return the import declarations in this import container
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    IImportDeclaration[] getImports() throws CoreException;
}
