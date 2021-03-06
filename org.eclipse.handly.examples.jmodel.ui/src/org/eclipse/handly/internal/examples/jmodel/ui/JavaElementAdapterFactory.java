/*******************************************************************************
 * Copyright (c) 2015, 2017 Codasip Ltd and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ondrej Ilcik (Codasip) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.handly.examples.jmodel.ICompilationUnit;
import org.eclipse.handly.examples.jmodel.IJavaElement;
import org.eclipse.handly.examples.jmodel.IJavaModel;
import org.eclipse.handly.examples.jmodel.IJavaProject;
import org.eclipse.handly.examples.jmodel.IPackageFragment;
import org.eclipse.handly.examples.jmodel.IPackageFragmentRoot;
import org.eclipse.handly.examples.jmodel.IType;
import org.eclipse.ui.IContainmentAdapter;
import org.eclipse.ui.IPersistableElement;

/**
 * Adapts Java elements to various other types.
 */
public class JavaElementAdapterFactory
    implements IAdapterFactory
{
    private static final Class<?>[] ADAPTER_LIST = new Class[] {
        IResource.class, IPersistableElement.class, IContainmentAdapter.class };

    private static final IContainmentAdapter CONTAINMENT_ADAPTER =
        new JavaElementContainmentAdapter();

    @Override
    public Class<?>[] getAdapterList()
    {
        return ADAPTER_LIST;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object element,
        Class<T> adapterType)
    {
        if (adapterType == IResource.class)
        {
            return (T)getResource((IJavaElement)element);
        }
        else if (adapterType == IPersistableElement.class)
        {
            return (T)new PersistableJavaElementFactory((IJavaElement)element);
        }
        else if (adapterType == IContainmentAdapter.class)
        {
            return (T)CONTAINMENT_ADAPTER;
        }
        return null;
    }

    private IResource getResource(IJavaElement element)
    {
        if (element instanceof IType)
        {
            // top level types behave like the CU
            IJavaElement parent = element.getParent();
            if (parent instanceof ICompilationUnit)
                return parent.getResource();
        }
        else if (element instanceof ICompilationUnit
            || element instanceof IPackageFragment
            || element instanceof IPackageFragmentRoot
            || element instanceof IJavaProject || element instanceof IJavaModel)
        {
            return element.getResource();
        }
        return null;
    }
}
