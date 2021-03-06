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
package org.eclipse.handly.model.impl.support;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IElement;

/**
 * A simple source file for tests.
 * Test clients can instantiate this class directly or subclass it.
 */
public class SimpleSourceFile
    extends SourceFile
{
    private final IFile file;
    private final IModelManager manager;

    /**
     * Constructs a handle for a source file with the given parameters.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element, or <code>null</code>
     *  if the element has no name
     * @param file the workspace file underlying the element, or <code>null</code>
     *  if the element has no underlying workspace file
     * @param manager the model manager for the element
     */
    public SimpleSourceFile(IElement parent, String name, IFile file,
        IModelManager manager)
    {
        super(parent, name);
        this.file = file;
        this.manager = manager;
    }

    /**
     * Returns a child element with the given name.
     * This is a handle-only method.
     *
     * @param name the name of the element
     * @return the child element with the given name
     */
    public SimpleSourceConstruct getChild(String name)
    {
        return new SimpleSourceConstruct(this, name);
    }

    @Override
    public IModelManager getModelManager_()
    {
        return manager;
    }

    @Override
    public IResource getResource_()
    {
        return file;
    }

    @Override
    public void buildSourceStructure_(IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        context.get(NEW_ELEMENTS).put(this, new SourceElementBody());
    }
}
