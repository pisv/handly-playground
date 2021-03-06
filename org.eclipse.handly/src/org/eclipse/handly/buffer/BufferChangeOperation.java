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
package org.eclipse.handly.buffer;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditProcessor;
import org.eclipse.text.edits.UndoEdit;

/**
 * Applies a given change to a given {@link IBuffer}.
 * This class is intended to be used in implementations of <code>IBuffer</code>.
 * General clients should use {@link IBuffer#applyChange(IBufferChange,
 * IProgressMonitor)} instead.
 */
public class BufferChangeOperation
{
    protected final IBuffer buffer;
    protected final IBufferChange change;

    /**
     * Creates a new operation that can apply the given change
     * to the given buffer.
     *
     * @param buffer must not be <code>null</code>
     * @param change must not be <code>null</code>
     */
    public BufferChangeOperation(IBuffer buffer, IBufferChange change)
    {
        if (buffer == null)
            throw new IllegalArgumentException();
        if (change == null)
            throw new IllegalArgumentException();

        this.buffer = buffer;
        this.change = change;
    }

    /**
     * Executes the buffer change.
     * <p>
     * Note that an update conflict may occur if the buffer's contents have
     * changed since the inception of the snapshot on which the change is based.
     * In that case, a {@link StaleSnapshotException} is thrown.
     * </p>
     *
     * @param monitor a progress monitor (not <code>null</code>).
     *  The caller must not rely on {@link IProgressMonitor#done()}
     *  having been called by the receiver
     * @return undo change, if requested by the change. Otherwise, <code>null</code>
     * @throws StaleSnapshotException if the buffer has changed
     *  since the inception of the snapshot on which the change is based
     * @throws CoreException if save is requested by the change but the buffer
     *  could not be saved
     * @throws MalformedTreeException if the change's edit tree is not
     *  in a valid state
     * @throws BadLocationException if one of the edits in the change's
     *  edit tree could not be executed
     */
    public IBufferChange execute(IProgressMonitor monitor) throws CoreException,
        BadLocationException
    {
        IDocument document = buffer.getDocument();

        if (!(document instanceof IDocumentExtension4))
            return applyChange(monitor);

        IDocumentExtension4 extension = (IDocumentExtension4)document;
        boolean isLargeEdit = RewriteSessionEditProcessor.isLargeEdit(
            change.getEdit());
        DocumentRewriteSessionType type = isLargeEdit
            ? DocumentRewriteSessionType.UNRESTRICTED
            : DocumentRewriteSessionType.UNRESTRICTED_SMALL;
        DocumentRewriteSession session = extension.startRewriteSession(type);
        try
        {
            return applyChange(monitor);
        }
        finally
        {
            extension.stopRewriteSession(session);
        }
    }

    protected IBufferChange applyChange(IProgressMonitor monitor)
        throws CoreException, BadLocationException
    {
        checkChange();

        IDocument document = buffer.getDocument();
        LinkedModeModel.closeAllModels(document);

        boolean saved = !buffer.isDirty();
        long stampToRestore = getModificationStampOf(document);

        UndoEdit undoEdit = applyTextEdit();

        if (change instanceof UndoChange)
        {
            setModificationStampOf(document,
                ((UndoChange)change).stampToRestore);
        }

        if (change.getSaveMode() == SaveMode.FORCE_SAVE || (saved
            && change.getSaveMode() == SaveMode.KEEP_SAVED_STATE))
        {
            buffer.save(EMPTY_CONTEXT, monitor);
        }

        return createUndoChange(undoEdit, stampToRestore);
    }

    protected void checkChange() throws CoreException
    {
        ISnapshot base = change.getBase();
        if (base != null && !base.isEqualTo(buffer.getSnapshot()))
            throw new StaleSnapshotException();
    }

    protected UndoEdit applyTextEdit() throws BadLocationException
    {
        return createTextEditProcessor().performEdits();
    }

    protected TextEditProcessor createTextEditProcessor()
    {
        return new ChangeEditProcessor();
    }

    protected IBufferChange createUndoChange(UndoEdit undoEdit,
        long stampToRestore)
    {
        if (undoEdit == null)
            return null;

        UndoChange undoChange = new UndoChange(undoEdit, stampToRestore);
        undoChange.setBase(buffer.getSnapshot());
        undoChange.setStyle(change.getStyle());
        undoChange.setSaveMode(change.getSaveMode());
        return undoChange;
    }

    protected static long getModificationStampOf(IDocument document)
    {
        long modificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
        if (document instanceof IDocumentExtension4)
        {
            modificationStamp =
                ((IDocumentExtension4)document).getModificationStamp();
        }
        return modificationStamp;
    }

    protected static void setModificationStampOf(IDocument document,
        long modificationStamp)
    {
        if (document instanceof IDocumentExtension4
            && modificationStamp != IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP)
        {
            try
            {
                ((IDocumentExtension4)document).replace(0, 0, "", //$NON-NLS-1$
                    modificationStamp);
            }
            catch (BadLocationException e)
            {
            }
        }
    }

    protected class ChangeEditProcessor
        extends TextEditProcessor
    {
        public ChangeEditProcessor()
        {
            super(buffer.getDocument(), change.getEdit(), change.getStyle());
        }

        @Override
        protected boolean considerEdit(TextEdit edit)
        {
            return change.contains(edit);
        }
    }

    protected static class UndoChange
        extends BufferChange
    {
        public final long stampToRestore;

        public UndoChange(TextEdit undoEdit, long stampToRestore)
        {
            super(undoEdit);
            this.stampToRestore = stampToRestore;
        }
    }
}
