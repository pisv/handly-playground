/*******************************************************************************
 * Copyright (c) 2014, 2020 1C-Soft LLC and others.
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

import static org.eclipse.handly.buffer.IBufferListener.BUFFER_SAVED;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.TextFileBufferSnapshot;
import org.eclipse.handly.util.UiSynchronizer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.text.edits.MalformedTreeException;

/**
 * Implementation of {@link IBuffer} backed by an {@link ITextFileBuffer}.
 * <p>
 * An instance of this class is safe for use by multiple threads, provided that
 * the underlying <code>ITextFileBuffer</code> and its document are thread-safe.
 * </p>
 * <p>
 * This class has an optional dependency on {@link IFile} and can safely be used
 * even when <code>org.eclipse.core.resources</code> bundle is not available.
 * </p>
 */
public final class TextFileBuffer
    implements IBuffer
{
    private final Object location;
    private ICoreTextFileBufferProvider coreTextFileBufferProvider;
    private int refCount = 1;
    private ListenerList<IBufferListener> listeners;
    private FileBufferListener fileBufferListener;

    /**
     * Returns a {@link TextFileBuffer} for the given file location.
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the buffer after it is no longer needed.
     * </p>
     *
     * @param location not <code>null</code>
     * @param locationKind not <code>null</code>
     * @return a buffer for the given file location (never <code>null</code>)
     * @throws CoreException if the buffer could not be created
     */
    public static TextFileBuffer forLocation(IPath location,
        LocationKind locationKind) throws CoreException
    {
        return new TextFileBuffer(ICoreTextFileBufferProvider.forLocation(
            location, locationKind, ITextFileBufferManager.DEFAULT), null);
    }

    /**
     * Returns a {@link TextFileBuffer} for the given file store.
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the buffer after it is no longer needed.
     * </p>
     *
     * @param fileStore not <code>null</code>
     * @return a buffer for the given file store (never <code>null</code>)
     * @throws CoreException if the buffer could not be created
     */
    public static TextFileBuffer forFileStore(IFileStore fileStore)
        throws CoreException
    {
        return new TextFileBuffer(ICoreTextFileBufferProvider.forFileStore(
            fileStore, ITextFileBufferManager.DEFAULT), null);
    }

    /**
     * Returns a {@link TextFileBuffer} for the given file resource.
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the buffer after it is no longer needed.
     * </p>
     *
     * @param file not <code>null</code>
     * @return a buffer for the given file resource (never <code>null</code>)
     * @throws CoreException if the buffer could not be created
     */
    public static TextFileBuffer forFile(IFile file) throws CoreException
    {
        return forLocation(file.getFullPath(), LocationKind.IFILE);
    }

    /**
     * Creates a new text file buffer instance and connects it to an underlying
     * {@link ITextFileBuffer} via the given provider.
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the created buffer after it is no longer needed.
     * </p>
     *
     * @param provider a provider of the underlying <code>ITextFileBuffer</code>
     *  (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver.
     *  The progress monitor is only valid for the duration of the invocation
     *  of this constructor
     * @throws CoreException if the buffer could not be created
     * @throws OperationCanceledException if this constructor is canceled
     */
    public TextFileBuffer(ICoreTextFileBufferProvider provider,
        IProgressMonitor monitor) throws CoreException
    {
        if ((this.coreTextFileBufferProvider = provider) == null)
            throw new IllegalArgumentException();

        provider.connect(monitor);

        boolean f = false;
        try
        {
            ITextFileBuffer buffer = provider.getBuffer();
            Object location = buffer.getLocation();
            if (location == null)
                location = buffer.getFileStore();
            this.location = location;
            f = true;
        }
        finally
        {
            if (!f)
                provider.disconnect(null);
        }
    }

    /**
     * Returns the provider of the underlying {@link ITextFileBuffer}
     * for this buffer.
     *
     * @return the underlying buffer's provider (never <code>null</code>)
     * @throws IllegalStateException if this buffer is no longer accessible
     */
    public ICoreTextFileBufferProvider getCoreTextFileBufferProvider()
    {
        ICoreTextFileBufferProvider result = coreTextFileBufferProvider;
        if (result == null)
            throw new IllegalStateException(
                "Attempt to access a disconnected TextFileBuffer for " //$NON-NLS-1$
                    + location);
        return result;
    }

    @Override
    public IDocument getDocument()
    {
        return getCoreTextFileBufferProvider().getBuffer().getDocument();
    }

    @Override
    public IAnnotationModel getAnnotationModel()
    {
        return getCoreTextFileBufferProvider().getBuffer().getAnnotationModel();
    }

    @Override
    public ISnapshot getSnapshot()
    {
        ICoreTextFileBufferProvider provider = getCoreTextFileBufferProvider();
        return new TextFileBufferSnapshot(provider.getBuffer(),
            provider.getBufferManager());
    }

    @Override
    public IBufferChange applyChange(IBufferChange change,
        IProgressMonitor monitor) throws CoreException
    {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        try
        {
            BufferChangeOperation operation = new BufferChangeOperation(this,
                change);
            if (!getCoreTextFileBufferProvider().getBuffer().isSynchronizationContextRequested())
                return operation.execute(monitor);

            UiSynchronizer synchronizer = UiSynchronizer.getDefault();
            if (synchronizer == null)
                throw new IllegalStateException(
                    "Synchronization context is requested, but synchronizer is not available"); //$NON-NLS-1$

            UiBufferChangeRunner runner = new UiBufferChangeRunner(synchronizer,
                operation);
            return runner.run(monitor);
        }
        catch (MalformedTreeException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
        catch (BadLocationException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
    }

    @Override
    public void save(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        getCoreTextFileBufferProvider().getBuffer().commit(monitor, false);
    }

    @Override
    public boolean isDirty()
    {
        return getCoreTextFileBufferProvider().getBuffer().isDirty();
    }

    @Override
    public int getSupportedListenerMethods()
    {
        return BUFFER_SAVED;
    }

    @Override
    public synchronized void addListener(IBufferListener listener)
    {
        if (listeners != null)
            listeners.add(listener);
        else
        {
            ICoreTextFileBufferProvider provider =
                getCoreTextFileBufferProvider();
            listeners = new ListenerList<>();
            listeners.add(listener);
            fileBufferListener = new FileBufferListener(provider.getBuffer());
            provider.getBufferManager().addFileBufferListener(
                fileBufferListener);
        }
    }

    @Override
    public synchronized void removeListener(IBufferListener listener)
    {
        if (listeners == null)
            return;
        listeners.remove(listener);
        if (listeners.isEmpty())
        {
            getCoreTextFileBufferProvider().getBufferManager().removeFileBufferListener(
                fileBufferListener);
            fileBufferListener = null;
            listeners = null;
        }
    }

    @Override
    public void addRef()
    {
        synchronized (this)
        {
            ++refCount;
        }
    }

    @Override
    public void release()
    {
        ICoreTextFileBufferProvider provider;
        synchronized (this)
        {
            if (--refCount != 0)
                return;
            if (coreTextFileBufferProvider == null)
                return;
            provider = coreTextFileBufferProvider;
            coreTextFileBufferProvider = null;
            listeners = null;
        }
        try
        {
            if (fileBufferListener != null)
            {
                provider.getBufferManager().removeFileBufferListener(
                    fileBufferListener);
                fileBufferListener = null;
            }
        }
        finally
        {
            try
            {
                provider.disconnect(null);
            }
            catch (CoreException e)
            {
                Activator.logError(e);
            }
        }
    }

    private void fireBufferSaved()
    {
        ListenerList<IBufferListener> listeners;
        synchronized (this)
        {
            if (this.listeners == null)
                return;
            listeners = this.listeners;
        }
        for (IBufferListener listener : listeners)
        {
            SafeRunner.run(() -> listener.bufferSaved(this));
        }
    }

    private class FileBufferListener
        extends FileBufferListenerAdapter
    {
        private final ITextFileBuffer buffer;
        private long modificationStamp;

        FileBufferListener(ITextFileBuffer buffer)
        {
            this.buffer = buffer;
            modificationStamp = buffer.getModificationStamp();
        }

        @Override
        public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty)
        {
            if (buffer == this.buffer && !isDirty && buffer.isSynchronized()
                && modificationStamp != buffer.getModificationStamp())
            {
                modificationStamp = buffer.getModificationStamp();
                fireBufferSaved();
            }
        }
    }
}
