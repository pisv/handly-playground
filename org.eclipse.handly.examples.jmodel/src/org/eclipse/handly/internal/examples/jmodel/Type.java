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

import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.context.Contexts.with;
import static org.eclipse.handly.model.Elements.BASE_SNAPSHOT;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.jmodel.IField;
import org.eclipse.handly.examples.jmodel.IMember;
import org.eclipse.handly.examples.jmodel.IMethod;
import org.eclipse.handly.examples.jmodel.IType;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.model.impl.support.ISourceElementImplSupport;
import org.eclipse.handly.model.impl.support.SourceElementBody;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jdt.core.Flags;

/**
 * Implementation of {@link IType}.
 */
public class Type
    extends Member
    implements IType
{
    static final IMember[] NO_CHILDREN = new IMember[0];

    /**
     * Creates a handle for a type with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param name the name of the element (not <code>null</code>)
     */
    public Type(JavaElement parent, String name)
    {
        super(parent, name);
        if (name == null)
            throw new IllegalArgumentException();
    }

    @Override
    public Field getField(String name)
    {
        return new Field(this, name);
    }

    @Override
    public IField[] getFields() throws CoreException
    {
        return getChildrenOfType(IField.class);
    }

    @Override
    public Method getMethod(String name, String[] parameterTypes)
    {
        return new Method(this, name, parameterTypes);
    }

    @Override
    public IMethod[] getMethods() throws CoreException
    {
        return getChildrenOfType(IMethod.class);
    }

    @Override
    public Type getType(String name)
    {
        return new Type(this, name);
    }

    @Override
    public IType[] getTypes() throws CoreException
    {
        return getChildrenOfType(IType.class);
    }

    @Override
    public String getSuperclassType() throws CoreException
    {
        return getSourceElementInfo().get(SUPERCLASS_TYPE);
    }

    @Override
    public String[] getSuperInterfaceTypes() throws CoreException
    {
        String[] result = getSourceElementInfo().get(SUPER_INTERFACE_TYPES);
        if (result == null)
            return NO_STRINGS;
        return result;
    }

    @Override
    public boolean isClass() throws CoreException
    {
        int flags = getFlags();
        return !(Flags.isEnum(flags) || Flags.isInterface(flags)
            || Flags.isAnnotation(flags));
    }

    @Override
    public boolean isEnum() throws CoreException
    {
        return Flags.isEnum(getFlags());
    }

    @Override
    public boolean isInterface() throws CoreException
    {
        return Flags.isInterface(getFlags());
    }

    @Override
    public boolean isAnnotation() throws CoreException
    {
        int flags = getFlags();
        return Flags.isInterface(flags) && Flags.isAnnotation(flags);
    }

    @Override
    public boolean isMember()
    {
        return getDeclaringType() != null;
    }

    @Override
    public ISourceElement getSourceElementAt_(int position,
        ISourceElementInfo info, IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        if (context.get(BASE_SNAPSHOT) == null)
        {
            ISnapshot snapshot = info.getSnapshot();
            if (snapshot != null)
                context = with(of(BASE_SNAPSHOT, snapshot), context);
        }
        ISourceElement[] children = info.getChildren();
        SubMonitor loopMonitor = SubMonitor.convert(monitor, children.length);
        for (int i = children.length - 1; i >= 0; i--)
        {
            SubMonitor iterationMonitor = loopMonitor.split(1);
            ISourceElement child = children[i];
            if (child instanceof IField)
            {
                ISourceElementInfo childInfo = Elements.getSourceElementInfo(
                    child);
                if (ISourceElementImplSupport.checkInRange(position, childInfo,
                    context))
                {
                    // check multi-declaration case (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=465410)
                    ISourceElement candidate = null;
                    do
                    {
                        // check name range
                        TextRange nameRange = childInfo.getIdentifyingRange();
                        if (nameRange != null
                            && position <= nameRange.getEndOffset())
                            candidate = child;
                        else
                            return candidate == null ? child : candidate;

                        if (--i < 0)
                            child = null;
                        else
                        {
                            child = children[i];
                            childInfo = Elements.getSourceElementInfo(child);
                        }
                    }
                    while (child != null
                        && ISourceElementImplSupport.checkInRange(position,
                            childInfo, context));
                    // position in field's type: use first field
                    return candidate;
                }
            }
            else
            {
                ISourceElement found = Elements.getSourceElementAt(child,
                    position, context, iterationMonitor);
                if (found != null)
                    return found;
            }
        }
        return this;
    }

    @Override
    public void toStringBody_(StringBuilder builder, Object body,
        IContext context)
    {
        if (body != null && body != NO_BODY)
        {
            SourceElementBody typeBody = (SourceElementBody)body;
            int flags = typeBody.get(FLAGS);
            if (Flags.isEnum(flags))
                builder.append("enum "); //$NON-NLS-1$
            else if (Flags.isAnnotation(flags))
                builder.append("@interface "); //$NON-NLS-1$
            else if (Flags.isInterface(flags))
                builder.append("interface "); //$NON-NLS-1$
            else
                builder.append("class "); //$NON-NLS-1$
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
        return JEM_TYPE;
    }

    @Override
    protected JavaElement getHandleFromMemento(String token,
        MementoTokenizer memento)
    {
        if (token == MementoTokenizer.COUNT)
        {
            return getHandleUpdatingCountFromMemento(memento);
        }
        else if (token == MementoTokenizer.TYPE
            || token == MementoTokenizer.FIELD
            || token == MementoTokenizer.METHOD)
        {
            String name = ""; //$NON-NLS-1$
            String nextToken = null;
            if (memento.hasMoreTokens())
            {
                nextToken = memento.nextToken();
                if (!MementoTokenizer.isDelimeter(nextToken))
                {
                    name = nextToken;
                    nextToken = null;
                }
            }
            JavaElement element;
            if (token == MementoTokenizer.TYPE)
                element = getType(name);
            else if (token == MementoTokenizer.FIELD)
                element = getField(name);
            else if (token == MementoTokenizer.METHOD)
            {
                ArrayList<String> parameterTypes = new ArrayList<>();
                while (memento.hasMoreTokens())
                {
                    if (nextToken == null)
                        nextToken = memento.nextToken();
                    if (nextToken != MementoTokenizer.METHOD)
                        break;
                    nextToken = null;
                    if (memento.hasMoreTokens())
                    {
                        nextToken = memento.nextToken();
                        if (MementoTokenizer.isDelimeter(nextToken))
                            break;
                        parameterTypes.add(nextToken);
                        nextToken = null;
                    }
                }
                element = getMethod(name, parameterTypes.toArray(
                    new String[0]));
            }
            else
                throw new AssertionError();
            if (nextToken == null)
                return element.getHandleFromMemento(memento);
            else
                return element.getHandleFromMemento(token, memento);
        }
        return null;
    }
}
