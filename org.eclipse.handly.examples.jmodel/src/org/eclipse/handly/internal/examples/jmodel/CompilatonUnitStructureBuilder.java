/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel;

import java.util.List;
import java.util.Map;

import org.eclipse.handly.examples.jmodel.IJavaSourceConstruct;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.support.SourceElementBody;
import org.eclipse.handly.model.impl.support.StructureHelper;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Builds the inner structure for a compilation unit.
 */
class CompilatonUnitStructureBuilder
{
    private static final IJavaSourceConstruct[] NO_CHILDREN =
        new IJavaSourceConstruct[0];

    private final Map<IElement, Object> newElements;
    private final StructureHelper helper = new StructureHelper();

    /**
     * Constructs a new compilation unit structure builder.
     *
     * @param newElements the map to populate with structure elements
     *  (not <code>null</code>)
     */
    CompilatonUnitStructureBuilder(Map<IElement, Object> newElements)
    {
        if (newElements == null)
            throw new IllegalArgumentException();
        this.newElements = newElements;
    }

    /**
     * Builds the structure for the given compilation unit based on its AST.
     *
     * @param handle the handle to a compilation unit (not <code>null</code>)
     * @param cu the AST of the compilation unit (not <code>null</code>)
     */
    void buildStructure(CompilationUnit handle,
        org.eclipse.jdt.core.dom.CompilationUnit cu)
    {
        SourceElementBody body = new SourceElementBody();

        org.eclipse.jdt.core.dom.PackageDeclaration pkg = cu.getPackage();
        if (pkg != null)
            buildStructure(handle, body, pkg);

        @SuppressWarnings("unchecked")
        List<org.eclipse.jdt.core.dom.ImportDeclaration> imports = cu.imports();
        if (!imports.isEmpty())
            buildStructure(handle, body, imports);

        @SuppressWarnings("unchecked")
        List<? extends AbstractTypeDeclaration> types = cu.types();
        for (AbstractTypeDeclaration type : types)
            buildStructure(handle, body, type);

        body.setChildren(helper.popChildren(body).toArray(
            CompilationUnit.NO_CHILDREN));
        newElements.put(handle, body);
    }

    private void buildStructure(CompilationUnit parent, Object parentBody,
        org.eclipse.jdt.core.dom.PackageDeclaration pkg)
    {
        PackageDeclaration handle = new PackageDeclaration(parent,
            pkg.getName().getFullyQualifiedName());
        helper.resolveDuplicates(handle);
        SourceElementBody body = new SourceElementBody();
        body.setFullRange(getTextRange(pkg));
        body.setIdentifyingRange(getTextRange(pkg.getName()));
        body.setChildren(NO_CHILDREN);
        newElements.put(handle, body);
        helper.pushChild(parentBody, handle);
    }

    private void buildStructure(CompilationUnit parent, Object parentBody,
        List<org.eclipse.jdt.core.dom.ImportDeclaration> imports)
    {
        ImportContainer handle = new ImportContainer(parent);
        SourceElementBody body = new SourceElementBody();
        org.eclipse.jdt.core.dom.ImportDeclaration firstImport = imports.get(0);
        org.eclipse.jdt.core.dom.ImportDeclaration lastImport = imports.get(
            imports.size() - 1);
        body.setFullRange(new TextRange(firstImport.getStartPosition(),
            lastImport.getStartPosition() + lastImport.getLength()
                - firstImport.getStartPosition()));
        for (org.eclipse.jdt.core.dom.ImportDeclaration importDecl : imports)
            buildStructure(handle, body, importDecl);
        body.setChildren(helper.popChildren(body).toArray(
            ImportContainer.NO_CHILDREN));
        newElements.put(handle, body);
        helper.pushChild(parentBody, handle);
    }

    private void buildStructure(ImportContainer parent, Object parentBody,
        org.eclipse.jdt.core.dom.ImportDeclaration importDecl)
    {
        String name = importDecl.getName().getFullyQualifiedName();
        if (importDecl.isOnDemand())
            name += ".*"; //$NON-NLS-1$
        ImportDeclaration handle = new ImportDeclaration(parent, name);
        helper.resolveDuplicates(handle);
        SourceElementBody body = new SourceElementBody();
        body.setFullRange(getTextRange(importDecl));
        body.setIdentifyingRange(getTextRange(importDecl.getName()));
        body.setChildren(NO_CHILDREN);
        newElements.put(handle, body);
        helper.pushChild(parentBody, handle);
    }

    private void buildStructure(JavaElement parent, Object parentBody,
        AbstractTypeDeclaration type)
    {
        Type handle = new Type(parent, type.getName().getIdentifier());
        helper.resolveDuplicates(handle);
        SourceElementBody body = new SourceElementBody();
        body.setFullRange(getTextRange(type));
        body.setIdentifyingRange(getTextRange(type.getName()));
        int flags = type.getModifiers();
        if (type instanceof TypeDeclaration)
        {
            TypeDeclaration typeDeclaration = (TypeDeclaration)type;

            if (typeDeclaration.isInterface())
                flags |= Flags.AccInterface;

            org.eclipse.jdt.core.dom.Type superclassType =
                typeDeclaration.getSuperclassType();
            if (superclassType != null)
                body.set(Type.SUPERCLASS_TYPE, AstUtil.getSignature(
                    superclassType));
            @SuppressWarnings("unchecked")
            List<? extends org.eclipse.jdt.core.dom.Type> superInterfaceTypes =
                typeDeclaration.superInterfaceTypes();
            body.set(Type.SUPER_INTERFACE_TYPES, AstUtil.getSignatures(
                superInterfaceTypes));
        }
        else if (type instanceof EnumDeclaration)
        {
            EnumDeclaration enumDeclaration = (EnumDeclaration)type;

            flags |= Flags.AccEnum;

            @SuppressWarnings("unchecked")
            List<? extends org.eclipse.jdt.core.dom.Type> superInterfaceTypes =
                enumDeclaration.superInterfaceTypes();
            body.set(Type.SUPER_INTERFACE_TYPES, AstUtil.getSignatures(
                superInterfaceTypes));

            @SuppressWarnings("unchecked")
            List<EnumConstantDeclaration> enumConstants =
                enumDeclaration.enumConstants();
            for (EnumConstantDeclaration enumConstant : enumConstants)
                buildStructure(handle, body, enumDeclaration, enumConstant);
        }
        else if (type instanceof AnnotationTypeDeclaration)
        {
            flags |= Flags.AccInterface | Flags.AccAnnotation;
        }
        body.set(Type.FLAGS, flags);
        @SuppressWarnings("unchecked")
        List<? extends BodyDeclaration> bodyDeclarations =
            type.bodyDeclarations();
        for (BodyDeclaration bd : bodyDeclarations)
        {
            if (bd instanceof FieldDeclaration)
                buildStructure(handle, body, (FieldDeclaration)bd);
            else if (bd instanceof MethodDeclaration)
                buildStructure(handle, body, (MethodDeclaration)bd);
            else if (bd instanceof AbstractTypeDeclaration)
                buildStructure(handle, body, (AbstractTypeDeclaration)bd);
            else if (bd instanceof AnnotationTypeMemberDeclaration)
                buildStructure(handle, body,
                    (AnnotationTypeMemberDeclaration)bd);
        }
        body.setChildren(helper.popChildren(body).toArray(Type.NO_CHILDREN));
        newElements.put(handle, body);
        helper.pushChild(parentBody, handle);
    }

    private void buildStructure(Type parent, Object parentBody,
        FieldDeclaration field)
    {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = field.fragments();
        for (VariableDeclarationFragment fragment : fragments)
            buildStructure(parent, parentBody, field, fragment);
    }

    private void buildStructure(Type parent, Object parentBody,
        FieldDeclaration field, VariableDeclarationFragment fragment)
    {
        Field handle = new Field(parent, fragment.getName().getIdentifier());
        helper.resolveDuplicates(handle);
        SourceElementBody body = new SourceElementBody();
        body.setFullRange(getTextRange(field));
        body.setIdentifyingRange(getTextRange(fragment.getName()));
        body.set(Field.FLAGS, field.getModifiers());
        body.set(Field.TYPE, Signature.createArraySignature(
            AstUtil.getSignature(field.getType()),
            fragment.getExtraDimensions()));
        body.setChildren(NO_CHILDREN);
        newElements.put(handle, body);
        helper.pushChild(parentBody, handle);
    }

    private void buildStructure(Type parent, Object parentBody,
        EnumDeclaration enumDeclaration, EnumConstantDeclaration enumConstant)
    {
        Field handle = new Field(parent,
            enumConstant.getName().getIdentifier());
        helper.resolveDuplicates(handle);
        SourceElementBody body = new SourceElementBody();
        body.setFullRange(getTextRange(enumConstant));
        body.setIdentifyingRange(getTextRange(enumConstant.getName()));
        body.set(Field.FLAGS, enumConstant.getModifiers() | Flags.AccEnum);
        body.set(Field.TYPE, Signature.createTypeSignature(
            enumDeclaration.getName().getIdentifier(), false));
        body.setChildren(NO_CHILDREN);
        newElements.put(handle, body);
        helper.pushChild(parentBody, handle);
    }

    private void buildStructure(Type parent, Object parentBody,
        MethodDeclaration method)
    {
        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> parameters = method.parameters();
        int numberOfParameters = parameters.size();
        String[] parameterTypes = new String[numberOfParameters];
        String[] parameterNames = new String[numberOfParameters];
        int i = 0;
        for (SingleVariableDeclaration parameter : parameters)
        {
            parameterTypes[i] = Signature.createArraySignature(
                AstUtil.getSignature(parameter.getType()),
                parameter.getExtraDimensions());
            parameterNames[i] = parameter.getName().getIdentifier();
            i++;
        }

        Method handle = new Method(parent, method.getName().getIdentifier(),
            parameterTypes);
        helper.resolveDuplicates(handle);
        SourceElementBody body = new SourceElementBody();
        body.setFullRange(getTextRange(method));
        body.setIdentifyingRange(getTextRange(method.getName()));
        body.set(Method.FLAGS, method.getModifiers());
        body.set(Method.PARAMETER_NAMES, parameterNames);
        org.eclipse.jdt.core.dom.Type returnType = method.getReturnType2();
        if (returnType != null)
        {
            body.set(Method.RETURN_TYPE, Signature.createArraySignature(
                AstUtil.getSignature(returnType), method.getExtraDimensions()));
        }
        @SuppressWarnings("unchecked")
        List<? extends org.eclipse.jdt.core.dom.Type> thrownExceptions =
            method.thrownExceptionTypes();
        body.set(Method.EXCEPTION_TYPES, AstUtil.getSignatures(
            thrownExceptions));
        if (method.isConstructor())
            body.set(Method.IS_CONSTRUCTOR, Boolean.TRUE);
        body.setChildren(NO_CHILDREN);
        newElements.put(handle, body);
        helper.pushChild(parentBody, handle);
    }

    private void buildStructure(Type parent, Object parentBody,
        AnnotationTypeMemberDeclaration annotationTypeMember)
    {
        Method handle = new Method(parent,
            annotationTypeMember.getName().getIdentifier(), Method.NO_STRINGS);
        helper.resolveDuplicates(handle);
        SourceElementBody body = new SourceElementBody();
        body.setFullRange(getTextRange(annotationTypeMember));
        body.setIdentifyingRange(getTextRange(annotationTypeMember.getName()));
        body.set(Method.FLAGS, annotationTypeMember.getModifiers());
        body.set(Method.RETURN_TYPE, AstUtil.getSignature(
            annotationTypeMember.getType()));
        body.setChildren(NO_CHILDREN);
        newElements.put(handle, body);
        helper.pushChild(parentBody, handle);
    }

    private static TextRange getTextRange(ASTNode node)
    {
        int startPosition = node.getStartPosition();
        if (startPosition == -1)
            return null;
        return new TextRange(startPosition, node.getLength());
    }
}
