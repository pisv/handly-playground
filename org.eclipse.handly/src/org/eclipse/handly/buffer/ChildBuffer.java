/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC.
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
package org.eclipse.handly.buffer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * A child buffer is created on top of a parent {@link IBuffer} and inherits
 * the parent's contents initially, but is modified independently. Saving the
 * child buffer propagates its contents to the parent buffer and also to the
 * parent buffer's underlying resource.
 * <p>
 * An instance of this class is safe for use by multiple threads.
 * </p>
 */
public final class ChildBuffer
    extends Buffer
{
    private final IBuffer parent;
    private volatile ISnapshot base;

    /**
     * Creates a new child buffer instance on top of the given parent buffer and
     * initializes it with the parent's contents.
     * <p>
     * The child buffer takes an independent ownership of the parent buffer
     * to ensure that it is kept open as long as the child buffer is in use.
     * The client still owns the parent buffer, but may release it immediately.
     * </p>
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the created buffer after it is no longer needed.
     * </p>
     *
     * @param parent the parent buffer (not <code>null</code>)
     * @throws IllegalStateException if the child buffer could not be created
     *  because of an inappropriate state of the parent buffer
     */
    public ChildBuffer(IBuffer parent)
    {
        if ((this.parent = parent) == null)
            throw new IllegalArgumentException();
        NonExpiringSnapshot snapshot = new NonExpiringSnapshot(parent);
        initWithContents(snapshot.getContents());
        base = snapshot.getWrappedSnapshot();
        parent.addRef(); // should always be the last statement in the constructor
    }

    @Override
    public void addRef()
    {
        parent.addRef();
    }

    @Override
    public void release()
    {
        parent.release();
    }

    @Override
    protected void doSave(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        try
        {
            String baseContents = base.getContents();
            if (baseContents == null)
                throw new StaleSnapshotException();
            BufferChange change = new BufferChange(new ReplaceEdit(0,
                baseContents.length(), getDocument().get()));
            change.setBase(base);
            IBufferChange undoChange = parent.applyChange(change, monitor);
            base = undoChange.getBase();
        }
        catch (StaleSnapshotException e)
        {
            throw new CoreException(Activator.createErrorStatus(
                Messages.ChildBuffer_Parent_has_been_modified_and_may_not_be_overwritten,
                e));
        }
    }
}
