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
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.jmodel.IField;
import org.eclipse.handly.model.impl.support.SourceElementBody;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;

/**
 * Implementation of {@link IField}.
 */
public class Field
    extends Member
    implements IField
{
    /**
     * Creates a handle for a field with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param name the name of the element (not <code>null</code>)
     */
    public Field(Type parent, String name)
    {
        super(parent, name);
        if (name == null)
            throw new IllegalArgumentException();
    }

    @Override
    public Type getParent()
    {
        return (Type)super.getParent();
    }

    @Override
    public String getType() throws CoreException
    {
        return getSourceElementInfo().get(TYPE);
    }

    @Override
    public boolean isEnumConstant() throws CoreException
    {
        return Flags.isEnum(getFlags());
    }

    @Override
    public void toStringBody_(StringBuilder builder, Object body,
        IContext context)
    {
        if (body != null && body != NO_BODY)
        {
            SourceElementBody fieldBody = (SourceElementBody)body;
            String type = fieldBody.get(TYPE);
            builder.append(Signature.toString(type));
            builder.append(' ');
        }
        toStringName_(builder, context);
        if (body == null)
        {
            builder.append(" (not open)"); //$NON-NLS-1$
        }
    }

    @Override
    protected char getHandleMementoDelimiter()
    {
        return JEM_FIELD;
    }
}
