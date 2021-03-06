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
package org.eclipse.handly.examples.adapter;

import org.eclipse.handly.internal.examples.adapter.AdapterModelManager;
import org.eclipse.handly.internal.examples.adapter.JavaElement;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IElement;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Facade to the Handly adapter for the JDT Java model.
 */
public class JavaModelAdapter
{
    /**
     * Returns <code>IElement</code> that corresponds to the given JDT Java element.
     *
     * @param javaElement may be <code>null</code>
     * @return the corresponding {@link IElement}, or <code>null</code> if none
     */
    public static IElement adapt(IJavaElement javaElement)
    {
        return JavaElement.create(javaElement);
    }

    /**
     * Returns the JDT Java element corresponding to the given <code>IElement</code>.
     *
     * @param element {@link IElement} may be <code>null</code>
     * @return the corresponding JDT Java element, or <code>null</code> if none
     */
    public static IJavaElement getJavaElement(IElement element)
    {
        if (element instanceof JavaElement)
            return ((JavaElement)element).getJavaElement();
        return null;
    }

    /**
     * Adds the given listener for changes to elements in the adapter model.
     * Has no effect if an identical listener is already registered.
     * <p>
     * Once registered, a listener starts receiving notification of changes to
     * elements in the adapter model. The listener continues to receive
     * notifications until it is removed.
     * </p>
     *
     * @param listener the listener (not <code>null</code>)
     * @see #removeElementChangeListener(IElementChangeListener)
     */
    public static void addElementChangeListener(IElementChangeListener listener)
    {
        AdapterModelManager.INSTANCE.getNotificationManager().addElementChangeListener(
            listener);
    }

    /**
     * Removes the given element change listener.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener the listener (not <code>null</code>)
     */
    public static void removeElementChangeListener(
        IElementChangeListener listener)
    {
        AdapterModelManager.INSTANCE.getNotificationManager().removeElementChangeListener(
            listener);
    }

    private JavaModelAdapter()
    {
    }
}
