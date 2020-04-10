/*
 * Copyright 2017 Flipkart Internet, pvt ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.masquerade.processor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flipkart.masquerade.Configuration;
import com.flipkart.masquerade.annotation.IgnoreCloak;
import com.flipkart.masquerade.rule.*;
import com.flipkart.masquerade.serialization.FieldMeta;
import com.flipkart.masquerade.util.EntryType;
import com.flipkart.masquerade.util.FieldDescriptor;
import com.flipkart.masquerade.util.RepositoryEntry;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.flipkart.masquerade.util.Helper.*;
import static com.flipkart.masquerade.util.Strings.OBJECT_PARAMETER;

/**
 * Processor that creates an implementation class for a Mask interface
 * <p />
 * Created by shrey.garg on 12/05/17.
 */
public abstract class OverrideProcessor extends BaseOverrideProcessor {
    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder  Entry class under construction for the cycle
     */
    public OverrideProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        super(configuration, cloakBuilder);
    }

    /**
     * @param rule The rule which is being processed
     * @param clazz The Class for which the Rule Mask interface will be implemented
     * @param repositoryEntries The Initialization block entries for which the map entry for the implementation will be added
     * @return A fully constructed TypeSpec object for the implementation
     */
    public Optional<TypeSpec> createOverride(Rule rule, Class<?> clazz, List<RepositoryEntry> repositoryEntries) {
        MethodSpec.Builder methodBuilder = generateOverrideMethod(rule, clazz);
        declareInitializeVariables(methodBuilder);

        /* Only consider fields for processing that are not static and remove fields that are to be ignored */
        List<Field> originalFields = getNonStaticFields(clazz).stream().filter(field -> !field.isAnnotationPresent(IgnoreCloak.class) && !field.isAnnotationPresent(JsonIgnore.class)).collect(Collectors.toList());
        List<FieldMeta> nonStaticFields = orderedFields(originalFields, clazz);
        addSyntheticFields(clazz, nonStaticFields);
        for (FieldMeta field : nonStaticFields) {
            if (skipProcessing(field)) {
                continue;
            }

            if (field.isSynthetic()) {
                handleSyntheticFields(field, methodBuilder);
                continue;
            }

            if (!skipAnnotationProcessing(field)) {
                Class<? extends Annotation> annotationClass = rule.getAnnotationClass();
                Annotation[] annotations = field.getField().getAnnotationsByType(annotationClass);
                if (annotations != null && annotations.length != 0) {
                    List<CodeBlock> operationBlocks = new ArrayList<>();
                    for (Annotation annotation : annotations) {
                        operationBlocks.add(constructOperation(rule, annotationClass, annotation));
                    }
                    constructFinalOperation(methodBuilder, field.getField(), clazz, operationBlocks);
                }
            }

            handleFieldKeys(clazz, field, methodBuilder);
            addRecursiveStatement(rule, clazz, field.getField(), methodBuilder, repositoryEntries);
            handleFieldValues(field, methodBuilder);
        }

        returns(methodBuilder);

        MethodSpec methodSpec = methodBuilder.build();
        if (methodSpec.code.isEmpty()) {
            addNoOpInitializerCode(rule, clazz, repositoryEntries);
            return Optional.empty();
        }

        addInitializerCode(rule, clazz, repositoryEntries);

        String implName = getImplementationName(rule, clazz);
        return Optional.ofNullable(generateImplementationType(rule, clazz, implName, methodSpec));
    }

    /**
     * Adds a call to the Entry class if the current field needs to be processed as well
     * @param field Current Field
     * @param methodBuilder Current method builder
     */
    private void addRecursiveStatement(Rule rule, Class<?> clazz, Field field, MethodSpec.Builder methodBuilder, List<RepositoryEntry> repositoryEntries) {
        if (!skipRecursiveCall(field)) {
            String getter = getGetterName(field.getName(), isBoolean(field.getType()), field.getType().isPrimitive());
            try {
                clazz.getMethod(getter);
            } catch (NoSuchMethodException e) {
                throw new UnsupportedOperationException("A cloak-able class should have a getter defined for all fields. Class: " + clazz.getName() + " Field: " + field.getName());
            }

            if (field.getType().isEnum()) {
                addEnumInitializerCode(rule, field.getType(), repositoryEntries);
            }

            recursiveStatement(rule, methodBuilder, field.getType(), getter);
        }
    }

    private void addNoOpInitializerCode(Rule rule, Class<?> clazz, List<RepositoryEntry> repositoryEntries) {
        repositoryEntries.add(new RepositoryEntry(rule, clazz, EntryType.NoOP));
    }

    private void addEnumInitializerCode(Rule rule, Class<?> clazz, List<RepositoryEntry> repositoryEntries) {
        repositoryEntries.add(new RepositoryEntry(rule, clazz, EntryType.ENUM));
    }

    private void addInitializerCode(Rule rule, Class<?> clazz, List<RepositoryEntry> repositoryEntries) {
        repositoryEntries.add(new RepositoryEntry(rule, clazz, EntryType.NEW));
    }

    private CodeBlock constructOperation(Rule rule, Class<? extends Annotation> annotationClass, Annotation annotation) {
        List<Object> operands = new ArrayList<>();

        CompositeRule baseRule = rule.getValueRule();
        String operation = constructBasicOperation(baseRule, baseRule.getConjunction(), annotationClass, annotation, operands);

        return CodeBlock.of(operation, operands.toArray());
    }

    private void constructFinalOperation(MethodSpec.Builder builder, Field field, Class<?> clazz, List<CodeBlock> operationBlocks) {
        String setter = getSetterName(field.getName(), isBoolean(field.getType()));
        try {
            clazz.getMethod(setter, field.getType());
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("A cloak-able class should have a setter defined for all fields. Class: " + clazz.getName() + " Field: " + field.getName());
        }

        StringBuilder operationBuilder = new StringBuilder();
        for (int i = 0; i < operationBlocks.size(); i++) {
            operationBuilder.append("($L)");
            if (i != operationBlocks.size() - 1) {
                operationBuilder.append(" || ");
            }
        }

        builder.beginControlFlow("if (" + operationBuilder.toString() + ")", operationBlocks.toArray());
        builder.addStatement("$L.$L(null)", OBJECT_PARAMETER, setter);
        builder.endControlFlow();
    }

    private String constructBasicOperation(CompositeRule compositeRule, Conjunction conjunction, Class<? extends Annotation> annotationClass, Annotation annotation, List<Object> operands) {
        StringBuilder operation = new StringBuilder();
        for (ValueRule valueRule : compositeRule.getValueRules()) {
            if (valueRule instanceof CompositeRule) {
                CompositeRule innerRule = (CompositeRule) valueRule;
                String constructedCompositeOperation = constructBasicOperation(innerRule, innerRule.getConjunction(), annotationClass, annotation, operands);
                if (constructedCompositeOperation.length() == 0) {
                    continue;
                }

                operation.append("(").append(constructedCompositeOperation).append(")").append(" ").append(conjunction.getSymbol()).append(" ");
                continue;
            }

            Object value;
            Object defaultValue;

            BasicRule basicRule = (BasicRule) valueRule;

            try {
                Method annotationValue = annotationClass.getDeclaredMethod(basicRule.getAnnotationMember());
                value = annotationValue.invoke(annotation);
                defaultValue = annotationValue.getDefaultValue();
            } catch (Exception e) {
                throw new UnsupportedOperationException("Please provide a annotation member that exists");
            }

            if (basicRule.isDefaultIgnored() && value.equals(defaultValue)) {
                continue;
            }

            FieldDescriptor descriptor = generateDescriptor(value);
            basicRule.getOperator().getGenerateOperation().accept(operation, descriptor);
            operation.append(" ").append(conjunction.getSymbol()).append(" ");

            String evalFunc = getEvaluationFunction(basicRule);
            value = handleEnum(descriptor, value);

            operands.add(evalFunc);
            operands.add(value);
        }

        return operation.length() < 4 ? operation.toString() : operation.delete(operation.length() - 4, operation.length()).toString();
    }

    private FieldDescriptor generateDescriptor(Object value) {
        boolean isPrimitive = value.getClass().isPrimitive() || getWrapperTypes().contains(value.getClass());
        return new FieldDescriptor(
                isPrimitive,
                isPrimitive || value.getClass().isEnum(),
                isPrimitive || Comparable.class.isAssignableFrom(value.getClass()),
                value.getClass().isEnum());
    }

    private Object handleEnum(FieldDescriptor descriptor, Object value) {
        if (!descriptor.isEnumeration()) {
            return value;
        }

        ClassName enumName = ClassName.get(value.getClass().getPackage().getName(), value.getClass().getSimpleName());
        return CodeBlock.of("$T.$L", enumName, value);
    }

    private List<FieldMeta> orderedFields(List<Field> fields, Class<?> clazz) {
        List<FieldMeta> fieldMetas = transform(fields, clazz);
        return enrichFieldMetas(fieldMetas, clazz);
    }

    private List<FieldMeta> transform(List<Field> fields, Class<?> clazz) {
        return fields.stream().map(field -> new FieldMeta(field, clazz)).collect(Collectors.toList());
    }

    protected abstract void declareInitializeVariables(MethodSpec.Builder methodBuilder);

    protected abstract List<FieldMeta> enrichFieldMetas(List<FieldMeta> fieldMetas, Class<?> clazz);

    protected abstract void addSyntheticFields(Class<?> clazz, List<FieldMeta> fields);

    protected abstract boolean skipProcessing(FieldMeta field);

    protected abstract void handleSyntheticFields(FieldMeta field, MethodSpec.Builder methodBuilder);

    protected abstract boolean skipAnnotationProcessing(FieldMeta field);

    protected abstract void handleFieldKeys(Class<?> clazz, FieldMeta field, MethodSpec.Builder methodBuilder);

    protected abstract void handleFieldValues(FieldMeta field, MethodSpec.Builder methodBuilder);

    protected abstract void returns(MethodSpec.Builder methodBuilder);

    protected abstract boolean skipRecursiveCall(Field field);

    protected abstract void recursiveStatement(Rule rule, MethodSpec.Builder methodBuilder, Class<?> clazz, String getterName);
}
