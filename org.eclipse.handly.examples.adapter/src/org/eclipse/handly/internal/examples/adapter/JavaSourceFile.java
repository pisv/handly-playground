/*******************************************************************************
 * Copyright (c) 2015, 2019 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.adapter;

import static org.eclipse.handly.model.Elements.CREATE_BUFFER;
import static org.eclipse.handly.model.Elements.FORCE_RECONCILING;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.ICoreTextFileBufferProvider;
import org.eclipse.handly.buffer.TextFileBuffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.impl.ISourceFileImpl;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Adapts a compilation unit to <code>ISourceFile</code>.
 */
class JavaSourceFile
    extends JavaSourceElement
    implements ISourceFileImpl
{
    /**
     * Constructs a <code>JavaSourceFile</code> for the given compilation unit.
     *
     * @param compilationUnit not <code>null</code>
     */
    public JavaSourceFile(ICompilationUnit compilationUnit)
    {
        super(compilationUnit);
    }

    /**
     * Returns the underlying compilation unit.
     *
     * @return the underlying compilation unit (not <code>null</code>)
     */
    public ICompilationUnit getCompilationUnit()
    {
        return (ICompilationUnit)getJavaElement();
    }

    @Override
    public IFile getFile_()
    {
        return (IFile)getCompilationUnit().getResource();
    }

    @Override
    public boolean isWorkingCopy_()
    {
        return getCompilationUnit().isWorkingCopy();
    }

    @Override
    public boolean needsReconciling_()
    {
        try
        {
            return !getCompilationUnit().isConsistent();
        }
        catch (JavaModelException e)
        {
            Activator.logError(e);
            return false;
        }
    }

    @Override
    public void reconcile_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        getCompilationUnit().reconcile(ICompilationUnit.NO_AST,
            context.getOrDefault(FORCE_RECONCILING), null/*use primary owner*/,
            monitor);
    }

    @Override
    public IBuffer getBuffer_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        IFile file = getFile_();
        if (file == null)
            throw new AssertionError("No underlying IFile for " + toString());
        ICoreTextFileBufferProvider provider =
            ICoreTextFileBufferProvider.forLocation(file.getFullPath(),
                LocationKind.IFILE, ITextFileBufferManager.DEFAULT);
        if (!context.getOrDefault(CREATE_BUFFER)
            && provider.getBuffer() == null)
        {
            return null;
        }
        return new TextFileBuffer(provider, monitor);
    }
}
