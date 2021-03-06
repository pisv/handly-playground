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
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.context.Contexts.with;
import static org.eclipse.handly.model.Elements.BASE_SNAPSHOT;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.model.impl.ISourceElementImpl;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.handly.util.TextRange;

/**
 * A "trait-like" interface providing a skeletal implementation of {@link
 * ISourceElementImpl} to minimize the effort required to implement that
 * interface.
 * <p>
 * In general, the members first defined in this interface are not intended
 * to be referenced outside the subtype hierarchy.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceElementImplSupport
    extends IElementImplSupport, ISourceElementImpl
{
    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link #getBody_(IContext,
     * IProgressMonitor)}; it is assumed that the body implements
     * {@link ISourceElementInfo}.
     * </p>
     * @throws CoreException {@inheritDoc}
     * @throws OperationCanceledException {@inheritDoc}
     */
    @Override
    default ISourceElementInfo getSourceElementInfo_(IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        return (ISourceElementInfo)getBody_(context, monitor);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation obtains the source element info for this element and
     * delegates to {@link #getSourceElementAt_(int, ISourceElementInfo, IContext,
     * IProgressMonitor)} if the given position is within the source range of
     * this element as reported by {@link #checkInRange(int, ISourceElementInfo,
     * IContext)}; otherwise, <code>null</code> is returned.
     * </p>
     * @throws CoreException {@inheritDoc}
     * @throws StaleSnapshotException {@inheritDoc}
     * @throws OperationCanceledException {@inheritDoc}
     */
    @Override
    default ISourceElement getSourceElementAt_(int position, IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
        ISourceElementInfo info = getSourceElementInfo_(context,
            subMonitor.split(1));
        if (!checkInRange(position, info, context))
            return null;
        return getSourceElementAt_(position, info, context, subMonitor.split(
            1));
    }

    /**
     * Returns the smallest element within this element that includes the
     * given source position, which is known to be within the source range of
     * this element as recorded by the given element info. If no finer grained
     * element is found at the position, this element itself is returned.
     * <p>
     * Implementations are encouraged to support the following standard options,
     * which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link org.eclipse.handly.model.Elements#BASE_SNAPSHOT BASE_SNAPSHOT} -
     * A snapshot on which the given position is based, or <code>null</code>
     * if the snapshot is unknown or does not matter.
     * </li>
     * </ul>
     *
     * @param position a source position (0-based)
     * @param info an {@link ISourceElementInfo} for this element
     *  (not <code>null</code>)
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor (not <code>null</code>).
     *  The caller must not rely on {@link IProgressMonitor#done()}
     *  having been called by the receiver
     * @return the innermost element enclosing the given source position
     *  (never <code>null</code>)
     * @throws CoreException if an exception occurs while accessing
     *  the element's corresponding resource
     * @throws StaleSnapshotException if snapshot inconsistency is detected
     * @throws OperationCanceledException if this method is canceled
     */
    default ISourceElement getSourceElementAt_(int position,
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
        for (ISourceElement child : children)
        {
            SubMonitor iterationMonitor = loopMonitor.split(1);
            ISourceElement found = Elements.getSourceElementAt(child, position,
                context, iterationMonitor);
            if (found != null)
                return found;
        }
        return this;
    }

    /**
     * Checks whether the given position is within the element's range
     * in the source snapshot as recorded by the given element info.
     * <p>
     * Supports the following standard options, which may be specified
     * in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link org.eclipse.handly.model.Elements#BASE_SNAPSHOT BASE_SNAPSHOT} -
     * A snapshot on which the given position is based, or <code>null</code>
     * if the snapshot is unknown or does not matter.
     * </li>
     * </ul>
     *
     * @param position a source position (0-based)
     * @param info an {@link ISourceElementInfo} (not <code>null</code>)
     * @param context the operation context (not <code>null</code>)
     * @return <code>true</code> if the given position is within the element's
     *  source range, and <code>false</code> otherwise
     * @throws StaleSnapshotException if snapshot inconsistency is detected
     */
    static boolean checkInRange(int position, ISourceElementInfo info,
        IContext context)
    {
        ISnapshot base = context.get(BASE_SNAPSHOT);
        if (base != null && !base.isEqualTo(info.getSnapshot()))
        {
            throw new StaleSnapshotException();
        }
        TextRange textRange = info.getFullRange();
        return textRange != null && textRange.covers(position);
    }
}
