/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementDelta;

/**
 * Represents a change event described by element deltas.
 */
public class ElementChangeEvent
    implements IElementChangeEvent
{
    private final int type;
    private final IElementDelta[] deltas;

    /**
     * Constructs a change event with the given type and the given top-level
     * deltas.
     *
     * @param type the type of event being reported (model-specific)
     * @param deltas the top-level deltas describing the change
     *  (at least one delta is required)
     */
    public ElementChangeEvent(int type, IElementDelta... deltas)
    {
        if (deltas.length == 0)
            throw new IllegalArgumentException();
        this.type = type;
        this.deltas = deltas;
    }

    @Override
    public int getType()
    {
        return type;
    }

    @Override
    public IElementDelta[] getDeltas()
    {
        return deltas;
    }
}