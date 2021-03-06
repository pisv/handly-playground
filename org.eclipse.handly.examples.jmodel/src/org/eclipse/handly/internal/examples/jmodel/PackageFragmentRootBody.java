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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.impl.support.Body;

/**
 * <code>Body</code> extension for the package fragment root.
 */
public class PackageFragmentRootBody
    extends Body
{
    private volatile Object[] nonJavaResources;

    public Object[] getNonJavaResources(PackageFragmentRoot root)
        throws CoreException
    {
        Object[] nonJavaResources = this.nonJavaResources;
        if (nonJavaResources == null)
        {
            nonJavaResources = computeNonJavaResources(root);
            this.nonJavaResources = nonJavaResources;
        }
        return nonJavaResources;
    }

    void setNonJavaResources(Object[] resources)
    {
        this.nonJavaResources = resources;
    }

    private Object[] computeNonJavaResources(PackageFragmentRoot root)
        throws CoreException
    {
        return PackageFragmentBody.computeNonJavaResources(
            root.getPackageFragment("")); //$NON-NLS-1$
    }
}
