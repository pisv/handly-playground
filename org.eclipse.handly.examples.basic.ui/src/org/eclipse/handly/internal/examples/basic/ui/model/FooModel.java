/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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
package org.eclipse.handly.internal.examples.basic.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.ApiLevel;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.impl.IModelImpl;
import org.eclipse.handly.model.impl.support.Body;
import org.eclipse.handly.model.impl.support.Element;

/**
 * Represents the root Foo element corresponding to the workspace.
 */
public class FooModel
    extends Element
    implements IFooModel, IFooElementInternal, IModelImpl
{
    private static final IFooProject[] NO_CHILDREN = new IFooProject[0];

    private final IWorkspace workspace;

    /**
     * Constructs a handle for the root Foo element corresponding to
     * the given workspace.
     *
     * @param workspace the workspace underlying the Foo Model
     *  (not <code>null</code>)
     */
    public FooModel(IWorkspace workspace)
    {
        super(null, ""); //$NON-NLS-1$
        if (workspace == null)
            throw new IllegalArgumentException();
        this.workspace = workspace;
    }

    @Override
    public void addElementChangeListener(IElementChangeListener listener)
    {
        FooModelManager.INSTANCE.getNotificationManager().addElementChangeListener(
            listener);
    }

    @Override
    public void removeElementChangeListener(IElementChangeListener listener)
    {
        FooModelManager.INSTANCE.getNotificationManager().removeElementChangeListener(
            listener);
    }

    @Override
    public IFooProject getFooProject(String name)
    {
        return new FooProject(this, workspace.getRoot().getProject(name));
    }

    @Override
    public IFooProject[] getFooProjects() throws CoreException
    {
        return (IFooProject[])getChildren();
    }

    @Override
    public IWorkspace getWorkspace()
    {
        return workspace;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FooModel other = (FooModel)obj;
        if (!workspace.equals(other.workspace))
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        return workspace.hashCode();
    }

    @Override
    public IContext getModelContext_()
    {
        return FooModelManager.INSTANCE.getModelContext();
    }

    @Override
    public int getModelApiLevel_()
    {
        return ApiLevel.CURRENT;
    }

    @Override
    public IResource getResource_()
    {
        return workspace.getRoot();
    }

    @Override
    public boolean exists_()
    {
        return true; // always exists
    }

    @Override
    public void validateExistence_(IContext context)
    {
        // always exists
    }

    @Override
    public void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        IProject[] projects = workspace.getRoot().getProjects();
        List<IFooProject> fooProjects = new ArrayList<>(projects.length);
        for (IProject project : projects)
        {
            if (project.isOpen() && project.hasNature(IFooProject.NATURE_ID))
            {
                fooProjects.add(new FooProject(this, project));
            }
        }
        Body body = new Body();
        body.setChildren(fooProjects.toArray(NO_CHILDREN));
        context.get(NEW_ELEMENTS).put(this, body);
    }

    @Override
    public void toStringName_(StringBuilder builder, IContext context)
    {
        builder.append("FooModel"); //$NON-NLS-1$
    }
}
