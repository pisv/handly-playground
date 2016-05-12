/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     (inspired by Eclipse JDT work)
 *******************************************************************************/
package org.eclipse.handly.model;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.handly.model.impl.IElementDeltaImpl;

/**
 * Provides methods for generic access to element deltas. Given a delta, clients
 * can access the element that has changed, and any children that have changed.
 * <p>
 * Deltas have a different status depending on the kind of change they represent.
 * The list below summarizes each status (as returned by {@link #getKind})
 * and its meaning (see individual constants for a more detailed description):
 * </p>
 * <ul>
 * <li>{@link IElementDeltaConstants#ADDED ADDED}
 * - The element described by the delta has been added.
 * <li>{@link IElementDeltaConstants#REMOVED REMOVED}
 * - The element described by the delta has been removed.
 * <li>{@link IElementDeltaConstants#CHANGED CHANGED}
 * - The element described by the delta has been changed in some way.
 * </ul>
 * <p>
 * Specification of the type of change is provided by {@link #getFlags}.
 * </p>
 * <p>
 * Move operations are indicated by special change flags. If element A is moved
 * to become B, the delta for the change in A will have status <code>REMOVED</code>,
 * with change flag <code>F_MOVED_TO</code>. In this case, {@link #getMovedToElement}
 * on delta A will return the handle for B. The delta for B will have status
 * <code>ADDED</code>, with change flag <code>F_MOVED_FROM</code>, and {@link
 * #getMovedFromElement} on delta B will return the handle for A. (Note,
 * the handle to A in this case represents an element that no longer exists).
 * Note that the move change flags only describe the changes to a single element,
 * they do not imply anything about the parent or children of the element.
 * </p>
 * <p>
 * Note that despite having a dependency on {@link IResourceDelta}
 * and {@link IMarkerDelta} this class can safely be used even when
 * <code>org.eclipse.core.resources</code> bundle is not available.
 * This is based on the "outward impression" of late resolution of
 * symbolic references a JVM must provide according to the JVMS.
 * </p>
 *
 * @see IElementDelta
 */
public class ElementDeltas
{
    /**
     * Returns the element that the delta describes a change to.
     *
     * @param delta not <code>null</code>
     * @return the element that the delta describes a change to
     *  (never <code>null</code>)
     */
    public static IElement getElement(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).hElement();
    }

    /**
     * Returns the kind of the delta - one of
     * {@link IElementDeltaConstants#ADDED ADDED},
     * {@link IElementDeltaConstants#REMOVED REMOVED},
     * or {@link IElementDeltaConstants#CHANGED CHANGED}.
     *
     * @param delta not <code>null</code>
     * @return the kind of the delta
     */
    public static int getKind(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).hKind();
    }

    /**
     * Returns the delta's flags that describe how the element has changed.
     * Such flags should be tested using the <code>&amp;</code> operator.
     * For example:
     * <pre>
     * if ((flags &amp; F_CONTENT) != 0)
     * {
     *     // indicates a content change
     * }</pre>
     * <p>
     * Some flags are meaningful for most models and predefined in
     * {@link IElementDeltaConstants}, while others are model-specific
     * and defined by the model implementor. The range for model-specific
     * flags starts from {@code 1L << 32} and includes the upper 32 bits of the
     * <code>long</code> value. The lower 32 bits are reserved for predefined
     * generic flags.
     * </p>
     *
     * @param delta not <code>null</code>
     * @return the delta's flags that describe how the element has changed
     */
    public static long getFlags(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).hFlags();
    }

    /**
     * Returns deltas for the affected (added, removed, or changed) children.
     *
     * @param delta not <code>null</code>
     * @return deltas for the affected (added, removed, or changed) children
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    public static IElementDelta[] getAffectedChildren(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).hAffectedChildren();
    }

    /**
     * Returns deltas for the children that have been added.
     *
     * @param delta not <code>null</code>
     * @return deltas for the children that have been added
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    public static IElementDelta[] getAddedChildren(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).hAddedChildren();
    }

    /**
     * Returns deltas for the children that have been removed.
     *
     * @param delta not <code>null</code>
     * @return deltas for the children that have been removed
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    public static IElementDelta[] getRemovedChildren(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).hRemovedChildren();
    }

    /**
     * Returns deltas for the children that have been changed.
     *
     * @param delta not <code>null</code>
     * @return deltas for the children that have been changed
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    public static IElementDelta[] getChangedChildren(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).hChangedChildren();
    }

    /**
     * Returns an element describing the delta's element before it was moved
     * to its current location, or <code>null</code> if the {@link
     * IElementDeltaConstants#F_MOVED_FROM F_MOVED_FROM} flag is not set.
     *
     * @param delta not <code>null</code>
     * @return an element describing the delta's element before it was moved
     *  to its current location, or <code>null</code> if the <code>F_MOVED_FROM</code>
     *  flag is not set
     */
    public static IElement getMovedFromElement(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).hMovedFromElement();
    }

    /**
     * Returns an element describing the delta's element in its new location,
     * or <code>null</code> if the {@link IElementDeltaConstants#F_MOVED_TO
     * F_MOVED_TO} flag is not set.
     *
     * @param delta not <code>null</code>
     * @return an element describing the delta's element in its new location,
     *  or <code>null</code> if the <code>F_MOVED_TO</code> flag is not set
     */
    public static IElement getMovedToElement(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).hMovedToElement();
    }

    /**
     * Returns the changes to markers on the corresponding resource of the
     * delta's element. Returns <code>null</code> if none. Note that this is
     * an exception to the general convention of returning an empty array
     * rather than <code>null</code>. This makes the method safe to call
     * even when <code>org.eclipse.core.resources</code> bundle is not
     * available.
     * <p>
     * Note that marker deltas, like element deltas, are generally only valid
     * for the dynamic scope of change notification. Clients <b>must not</b>
     * hang on to these objects.
     * </p>
     *
     * @param delta not <code>null</code>
     * @return the marker deltas, or <code>null</code> if none.
     *  Clients <b>must not</b> modify the returned array.
     */
    public static IMarkerDelta[] getMarkerDeltas(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).hMarkerDeltas();
    }

    /**
     * Returns the changes to children of the delta element's corresponding
     * resource that cannot be described in terms of element deltas. Returns
     * <code>null</code> if none. Note that this is an exception to the general
     * convention of returning an empty array rather than <code>null</code>.
     * This makes the method safe to call even when <code>org.eclipse.core.resources</code>
     * bundle is not available.
     * <p>
     * Note that resource deltas, like element deltas, are generally only valid
     * for the dynamic scope of change notification. Clients <b>must not</b>
     * hang on to these objects.
     * </p>
     *
     * @param delta not <code>null</code>
     * @return the resource deltas, or <code>null</code> if none.
     *  Clients <b>must not</b> modify the returned array.
     */
    public static IResourceDelta[] getResourceDeltas(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).hResourceDeltas();
    }

    /**
     * Debugging purposes. Returns a string representation of the delta.
     * Note that the specified style serves as a hint that implementations
     * may or may not fully support.
     *
     * @param delta not <code>null</code>
     * @param style style hint (not <code>null</code>)
     * @return a string representation of the delta (never <code>null</code>)
     */
    public static String toString(IElementDelta delta, ToStringStyle style)
    {
        return ((IElementDeltaImpl)delta).hToString(style);
    }

    private ElementDeltas()
    {
    }
}