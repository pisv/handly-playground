/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
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
package org.eclipse.handly.xtext.ui.editor;

import org.eclipse.xtext.ui.editor.DirtyStateEditorSupport;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;

/**
 * Extends {@link DirtyStateEditorSupport} for Handly reconciling story.
 * <p>
 * Bind this class or its subclass in place of the default <code>DirtyStateEditorSupport</code>
 * if you have {@link HandlyXtextDocument} bound:
 * </p>
 * <pre>
 * public Class&lt;? extends DirtyStateEditorSupport&gt; bindDirtyStateEditorSupport() {
 *     return HandlyDirtyStateEditorSupport.class; // or its subclass
 * }</pre>
 *
 * @see HandlyJvmTypesAwareDirtyStateEditorSupport
 */
public class HandlyDirtyStateEditorSupport
    extends DirtyStateEditorSupport
{
    @Override
    public void initializeDirtyStateSupport(
        IDirtyStateEditorSupportClient client)
    {
        super.initializeDirtyStateSupport(client);
        IXtextDocument document = client.getDocument();
        if (document instanceof HandlyXtextDocument)
            ((HandlyXtextDocument)document).setDirtyStateEditorSupport(this);
    }

    @Override
    public void removeDirtyStateSupport(IDirtyStateEditorSupportClient client)
    {
        super.removeDirtyStateSupport(client);
        IXtextDocument document = client.getDocument();
        if (document instanceof HandlyXtextDocument)
            ((HandlyXtextDocument)document).setDirtyStateEditorSupport(null);
    }
}
