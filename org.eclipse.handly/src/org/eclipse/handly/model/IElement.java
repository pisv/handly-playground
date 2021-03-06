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
 *     (inspired by Eclipse JDT work)
 *******************************************************************************/
package org.eclipse.handly.model;

/**
 * A marker interface for all elements of a Handly-based model.
 * <p>
 * Elements of a Handly-based model are exposed to clients as <i>handles</i>
 * to the actual underlying element. The model may hand out any number of
 * handles for each element. Handles are value objects; handles that refer to
 * the same element are guaranteed to be equal, but not necessarily identical.
 * </p>
 * <p>
 * Elements of a Handly-based model are safe for use by multiple threads.
 * </p>
 * @see Elements
 */
public interface IElement
{
    /*
     * Implementors of this interface must also implement IElementImpl.
     */

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();
}
