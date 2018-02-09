/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.lsp;

import static org.eclipse.handly.model.IElementDeltaConstants.ADDED;
import static org.eclipse.handly.model.IElementDeltaConstants.F_CONTENT;
import static org.eclipse.handly.model.IElementDeltaConstants.F_MARKERS;
import static org.eclipse.handly.model.IElementDeltaConstants.F_MOVED_FROM;
import static org.eclipse.handly.model.IElementDeltaConstants.F_MOVED_TO;
import static org.eclipse.handly.model.IElementDeltaConstants.F_SYNC;
import static org.eclipse.handly.model.IElementDeltaConstants.F_UNDERLYING_RESOURCE;
import static org.eclipse.handly.model.IElementDeltaConstants.REMOVED;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.handly.examples.lsp.ILanguageElement;
import org.eclipse.handly.examples.lsp.ILanguageSourceFile;
import org.eclipse.handly.examples.lsp.LanguageCore;
import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.impl.IElementImplExtension;
import org.eclipse.handly.model.impl.support.Body;

/**
 * This class is used by the {@link ModelManager} to convert resource deltas
 * into language element deltas. It also does some processing on the language
 * elements involved (e.g. closing them).
 */
final class DeltaProcessor
    implements IResourceDeltaVisitor
{
    private final List<IElementDelta> deltas = new ArrayList<>();

    /**
     * Returns the element deltas built from the resource delta.
     *
     * @return element deltas (never <code>null</code>, may be empty;
     *  if not empty, each of the deltas must not be empty)
     */
    IElementDelta[] getDeltas()
    {
        return deltas.toArray(ElementDeltas.EMPTY_ARRAY);
    }

    @Override
    public boolean visit(IResourceDelta delta) throws CoreException
    {
        switch (delta.getResource().getType())
        {
        case IResource.PROJECT:
            return processProject(delta);

        case IResource.FILE:
            return processFile(delta);

        default:
            return true;
        }
    }

    private boolean processProject(IResourceDelta delta)
    {
        IProject project = (IProject)delta.getResource();
        if (delta.getKind() == IResourceDelta.REMOVED
            || (delta.getKind() == IResourceDelta.CHANGED && (delta.getFlags()
                & IResourceDelta.OPEN) != 0) && !project.isOpen())
        {
            ModelManager.INSTANCE.getServerManager().disconnect(project);
            return false;
        }
        return true;
    }

    private boolean processFile(IResourceDelta delta)
    {
        switch (delta.getKind())
        {
        case IResourceDelta.ADDED:
            return processAddedFile(delta);

        case IResourceDelta.REMOVED:
            return processRemovedFile(delta);

        case IResourceDelta.CHANGED:
            return processChangedFile(delta);

        default:
            return false;
        }
    }

    private boolean processAddedFile(IResourceDelta delta)
    {
        IFile file = (IFile)delta.getResource();
        ILanguageElement element = LanguageCore.create(file);
        if (element != null)
        {
            if (element instanceof ILanguageSourceFile)
                ModelManager.INSTANCE.getServerManager().updateUri(
                    (ILanguageSourceFile)element);

            if (!isWorkingCopy(element))
            {
                addToModel(element);
                translateAddedDelta(delta, element);
            }
            else
            {
                LanguageElementDelta result = new LanguageElementDelta(element);
                result.setKind(ADDED);
                result.setFlags(F_UNDERLYING_RESOURCE);
                deltas.add(result);
            }
        }
        return false;
    }

    private boolean processRemovedFile(IResourceDelta delta)
    {
        IFile file = (IFile)delta.getResource();
        ILanguageElement element = LanguageCore.create(file);
        if (element != null)
        {
            if (!isWorkingCopy(element))
            {
                removeFromModel(element);
                translateRemovedDelta(delta, element);
            }
            else
            {
                LanguageElementDelta result = new LanguageElementDelta(element);
                result.setKind(REMOVED);
                result.setFlags(F_UNDERLYING_RESOURCE);
                deltas.add(result);
            }
        }
        return false;
    }

    private boolean processChangedFile(IResourceDelta delta)
    {
        IFile file = (IFile)delta.getResource();
        ILanguageElement element = LanguageCore.create(file);
        if (element != null)
        {
            LanguageElementDelta result = new LanguageElementDelta(element);

            long flags = 0;

            boolean isWorkingCopy = isWorkingCopy(element);

            if (isWorkingCopy)
                flags |= F_UNDERLYING_RESOURCE;

            if ((delta.getFlags() & ~(IResourceDelta.MARKERS
                | IResourceDelta.SYNC)) != 0)
            {
                flags |= F_CONTENT;
                if (!isWorkingCopy)
                    close(element);
            }

            if ((delta.getFlags() & IResourceDelta.MARKERS) != 0)
            {
                flags |= F_MARKERS;
                result.setMarkerDeltas(delta.getMarkerDeltas());
            }

            if ((delta.getFlags() & IResourceDelta.SYNC) != 0)
                flags |= F_SYNC;

            result.setFlags(flags);

            deltas.add(result);
        }
        return false;
    }

    private void addToModel(ILanguageElement element)
    {
        Body parentBody = findBody(element.getParent());
        if (parentBody != null)
            parentBody.addChild(element);
        close(element);
    }

    private void removeFromModel(ILanguageElement element)
    {
        Body parentBody = findBody(element.getParent());
        if (parentBody != null)
            parentBody.removeChild(element);
        close(element);
    }

    private void translateAddedDelta(IResourceDelta delta,
        ILanguageElement element)
    {
        LanguageElementDelta result = new LanguageElementDelta(element);
        result.setKind(ADDED);
        if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0)
        {
            ILanguageElement movedFromElement = LanguageCore.create(getResource(
                delta.getMovedFromPath(), delta.getResource().getType()));
            if (movedFromElement != null)
            {
                result.setFlags(F_MOVED_FROM);
                result.setMovedFromElement(movedFromElement);
            }
        }
        deltas.add(result);
    }

    private void translateRemovedDelta(IResourceDelta delta,
        ILanguageElement element)
    {
        LanguageElementDelta result = new LanguageElementDelta(element);
        result.setKind(REMOVED);
        if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0)
        {
            ILanguageElement movedToElement = LanguageCore.create(getResource(
                delta.getMovedToPath(), delta.getResource().getType()));
            if (movedToElement != null)
            {
                result.setFlags(F_MOVED_TO);
                result.setMovedToElement(movedToElement);
            }
        }
        deltas.add(result);
    }

    private static boolean isWorkingCopy(ILanguageElement element)
    {
        return element instanceof ILanguageSourceFile
            && ((ILanguageSourceFile)element).isWorkingCopy();
    }

    private static Body findBody(ILanguageElement element)
    {
        if (element == null)
            return null;
        return (Body)((IElementImplExtension)element).findBody_();
    }

    private static void close(ILanguageElement element)
    {
        ((IElementImplExtension)element).close_();
    }

    private static IResource getResource(IPath fullPath, int resourceType)
    {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        switch (resourceType)
        {
        case IResource.ROOT:
            return root;

        case IResource.PROJECT:
            return root.getProject(fullPath.lastSegment());

        case IResource.FOLDER:
            return root.getFolder(fullPath);

        case IResource.FILE:
            return root.getFile(fullPath);

        default:
            return null;
        }
    }
}
