/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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
package org.eclipse.handly.util;

import junit.framework.TestCase;

/**
 * <code>SimpleSynchronizer</code> tests.
 */
public class SimpleSynchronizerTest
    extends TestCase
{
    private SimpleSynchronizer synchronizer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        synchronizer = new SimpleSynchronizer();
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (synchronizer != null)
            synchronizer.dispose();
        super.tearDown();
    }

    public void test1() throws Exception
    {
        Thread thread = synchronizer.getThread();
        assertNotNull(thread);
        assertNotSame(Thread.currentThread(), thread);
        Thread[] threads = new Thread[1];
        synchronizer.syncExec(() ->
        {
            threads[0] = Thread.currentThread();
        });
        assertSame(thread, threads[0]);
    }
}
