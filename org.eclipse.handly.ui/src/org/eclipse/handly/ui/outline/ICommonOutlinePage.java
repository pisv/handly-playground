/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.outline;

import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Represents a common outline page.
 *
 * @noimplement This interface is not intended to be implemented by clients
 *  directly. However, clients may extend the base implementation, class
 *  {@link CommonOutlinePage}.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICommonOutlinePage
    extends IContentOutlinePage, IPageBookViewPage
{
    /*
     * Note that if an abstract method is added to this interface,
     * an implementation for the method must be provided in CommonOutlinePage.
     */

    /**
     * Returns the tree viewer of this outline page.
     *
     * @return the tree viewer of this outline page,
     *  or <code>null</code> if it has not been created yet
     */
    TreeViewer getTreeViewer();

    /**
     * Initializes this outline page with its corresponding editor.
     * This method should be called by the editor shortly after
     * page construction. Specifically, it must be called before
     * the page's control is created.
     *
     * @param editor the editor which created this outline page
     *  (not <code>null</code>)
     */
    void init(IEditorPart editor);

    /**
     * Returns the editor which created this outline page.
     *
     * @return the editor which created this outline page,
     *  or <code>null</code> if it has not been set yet
     */
    IEditorPart getEditor();

    /**
     * Adds the given outline contribution. This method has no effect
     * if the contribution is already registered.
     *
     * @param contribution not <code>null</code>
     */
    void addOutlineContribution(IOutlineContribution contribution);

    /**
     * Removes the given outline contribution. This method has no effect
     * if the contribution was not already registered.
     *
     * @param contribution not <code>null</code>
     */
    void removeOutlineContribution(IOutlineContribution contribution);

    /**
     * Adds the given input change listener. This method has no effect
     * if the listener is already registered.
     *
     * @param listener not <code>null</code>
     */
    void addInputChangeListener(IOutlineInputChangeListener listener);

    /**
     * Removes the given input change listener. This method has no effect
     * if the listener was not already registered.
     *
     * @param listener not <code>null</code>
     */
    void removeInputChangeListener(IOutlineInputChangeListener listener);

    /**
     * Returns link-with-editor preference for this outline page.
     *
     * @return link-with-editor preference for this outline page,
     *  or <code>null</code> if the outline page does not support
     *  linking with editor
     */
    IBooleanPreference getLinkWithEditorPreference();

    /**
     * Returns lexical sort preference for this outline page.
     *
     * @return lexical sort preference for this outline page,
     *  or <code>null</code> if the outline page does not support
     *  lexical sorting
     */
    IBooleanPreference getLexicalSortPreference();
}
