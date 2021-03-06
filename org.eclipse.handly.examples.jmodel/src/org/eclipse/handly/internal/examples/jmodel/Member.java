/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.examples.jmodel.IMember;
import org.eclipse.handly.examples.jmodel.IType;

/**
 * Implementation of {@link IMember}.
 */
public abstract class Member
    extends JavaSourceConstruct
    implements IMember
{
    static final String[] NO_STRINGS = new String[0];

    /**
     * Creates a handle for a member with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param name the name of the element, or <code>null</code>
     *  if the element has no name
     */
    public Member(JavaElement parent, String name)
    {
        super(parent, name);
    }

    @Override
    public IType getDeclaringType()
    {
        if (getParent() instanceof IType)
            return (IType)getParent();
        return null;
    }

    @Override
    public int getFlags() throws CoreException
    {
        return getSourceElementInfo().get(FLAGS);
    }
}
