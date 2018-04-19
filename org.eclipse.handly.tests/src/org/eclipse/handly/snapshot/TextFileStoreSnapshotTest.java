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
package org.eclipse.handly.snapshot;

import java.nio.charset.Charset;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.handly.junit.WorkspaceTestCase;

/**
 * <code>TextFileStoreSnapshot</code> tests.
 */
public class TextFileStoreSnapshotTest
    extends WorkspaceTestCase
{
    private IFileStore fileStore;
    private TextFileStoreSnapshot snapshot;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IProject p = setUpProject("Test002");
        IFile file = p.getFile("x.txt");
        fileStore = EFS.getStore(file.getLocationURI());
        snapshot = new TextFileStoreSnapshot(fileStore, Charset.forName(
            "UTF-8"));
    }

    public void test1() throws Exception
    {
        assertEquals("hello", snapshot.getContents());
        IFileInfo info = fileStore.fetchInfo();
        info.setLastModified(info.getLastModified() + 1000);
        fileStore.putInfo(info, EFS.SET_LAST_MODIFIED, null);
        assertNull(snapshot.getContents());
    }
}
