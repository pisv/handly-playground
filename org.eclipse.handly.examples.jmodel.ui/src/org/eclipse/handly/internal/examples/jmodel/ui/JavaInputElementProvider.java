/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.examples.jmodel.JavaModelCore;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.ui.IEditorInput;

/**
 * Java specific implementation of {@link IInputElementProvider}.
 */
public class JavaInputElementProvider
    implements IInputElementProvider
{
    /**
     * The sole instance of the {@link JavaInputElementProvider}.
     */
    public static final IInputElementProvider INSTANCE =
        new JavaInputElementProvider();

    @Override
    public IElement getElement(IEditorInput input)
    {
        if (input == null)
            return null;
        IFile file = (IFile)input.getAdapter(IFile.class);
        return JavaModelCore.create(file);
    }

    private JavaInputElementProvider()
    {
    }
}
