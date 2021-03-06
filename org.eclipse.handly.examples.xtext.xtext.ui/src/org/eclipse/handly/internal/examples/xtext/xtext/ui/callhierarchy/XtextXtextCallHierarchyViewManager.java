/*******************************************************************************
 * Copyright (c) 2019 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.xtext.xtext.ui.callhierarchy;

import org.eclipse.handly.ui.callhierarchy.CallHierarchyViewManager;
import org.eclipse.handly.ui.callhierarchy.CallHierarchyViewOpener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

class XtextXtextCallHierarchyViewManager
    extends CallHierarchyViewManager
{
    static final XtextXtextCallHierarchyViewManager INSTANCE =
        new XtextXtextCallHierarchyViewManager();

    private final CallHierarchyViewOpener opener = new CallHierarchyViewOpener(
        XtextXtextCallHierarchyView.ID, this);

    IViewPart openView(IWorkbenchPage page, Object[] inputElements)
        throws PartInitException
    {
        return opener.openView(page, inputElements);
    }

    private XtextXtextCallHierarchyViewManager()
    {
    }
}
