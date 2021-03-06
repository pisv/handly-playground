/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel.ui.editor;

import org.eclipse.handly.ui.IWorkingCopyManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Configuration for a source viewer which shows Java code.
 */
public class JavaSourceViewerConfiguration
    extends TextSourceViewerConfiguration
{
    private final ITextEditor editor;
    private final IWorkingCopyManager manager;

    /**
     * Creates a new Java source viewer configuration for viewers in the given
     * editor using the given preference store.
     *
     * @param preferenceStore the preference store, can be read-only
     * @param editor the editor in which the configured viewer(s) will reside,
     *  or <code>null</code> if none
     * @param manager the working copy manager
     */
    public JavaSourceViewerConfiguration(IPreferenceStore preferenceStore,
        ITextEditor editor, IWorkingCopyManager manager)
    {
        super(preferenceStore);
        this.editor = editor;
        this.manager = manager;
    }

    @Override
    public IReconciler getReconciler(ISourceViewer sourceViewer)
    {
        if (editor != null && editor.isEditable())
        {
            return new JavaReconciler(editor, manager);
        }
        return null;
    }
}
