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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.flipkart.masquerade.Configuration;
import com.flipkart.masquerade.rule.Rule;
import com.flipkart.masquerade.serialization.FieldMeta;
import com.flipkart.masquerade.serialization.SerializationProperty;
import com.google.common.base.Defaults;
import com.google.common.primitives.Primitives;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.lang.reflect.Field;
import java.util.*;

import static com.flipkart.masquerade.util.Helper.*;
import static com.flipkart.masquerade.util.Strings.*;

/**
 * Created by shrey.garg on 23/07/17.
 */
public class SerializationOverrideProcessor extends OverrideProcessor {
    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder  Entry class under construction for the cycle
     */
    public SerializationOverrideProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        super(configuration, cloakBuilder);
    }

    @Override
    protected void declareInitializeVariables(MethodSpec.Builder methodBuilder) {
        methodBuilder.addStatement("$L.append($S)", SERIALIZED_OBJECT, "{");
    }

    @Override
    protected List<FieldMeta> enrichFieldMetas(List<FieldMeta> fieldMetas, Class<?> clazz) {
        List<FieldMeta> sortedFields = new ArrayList<>();
        JsonPropertyOrder propertyOrder = getAnnotation(clazz, JsonPropertyOrder.class);
        if (propertyOrder != null && propertyOrder.value().length > 0) {
            for (String name : propertyOrder.value()) {
                int index = findField(name, fieldMetas);
                if (index >= 0) {
                    sortedFields.add(fieldMetas.remove(index));
                }
            }
        }

        boolean sortedAlphabetically = configuration.serializationProperties().contains(SerializationProperty.SORT_PROPERTIES_ALPHABETICALLY);
        if (!sortedAlphabetically) {
            sortedFields.addAll(fieldMetas);
            return sortedFields;
        }

        fieldMetas.sort(Comparator.comparing(FieldMeta::getSerializableName));
        sortedFields.addAll(fieldMetas);
        return sortedFields;
    }

    @Override
    protected void addSyntheticFields(Class<?> clazz, List<FieldMeta> fields) {
        JsonTypeInfo jsonTypeInfo = getAnnotation(clazz, JsonTypeInfo.class);
        JsonSubTypes jsonSubTypes = getAnnotation(clazz, JsonSubTypes.class);
        if (jsonTypeInfo == null || jsonSubTypes == null) {
            return;
        }

        if (!jsonTypeInfo.include().equals(JsonTypeInfo.As.PROPERTY)) {
            return;
        }

        Optional<JsonSubTypes.Type> subType = Arrays.stream(jsonSubTypes.value()).filter(t -> t.value().equals(clazz)).findFirst();
        if (!subType.isPresent()) {
            return;
        }

        fields.add(0, new FieldMeta(jsonTypeInfo.property(), clazz, subType.get().name()));
    }

    @Override
    protected boolean skipProcessing(FieldMeta field) {
        return false;
    }

    @Override
    protected void handleSyntheticFields(FieldMeta field, MethodSpec.Builder methodBuilder) {
        methodBuilder.addStatement("$L.append($S)", SERIALIZED_OBJECT, QUOTES + field.getSerializableName() + QUOTES + ":" + QUOTES + field.getSyntheticValue() + QUOTES);
        methodBuilder.addStatement("$L.append($S)", SERIALIZED_OBJECT, ",");
    }

    @Override
    protected boolean skipAnnotationProcessing(FieldMeta field) {
        return field.getType().isPrimitive();
    }

    @Override
    protected void handleFieldKeys(Class<?> clazz, FieldMeta field, MethodSpec.Builder methodBuilder) {
        resolveInclusionLevel(clazz, field);
        if (field.getInclusionLevel() != JsonInclude.Include.ALWAYS) {
            CodeBlock inclusionCondition = constructInclusionCondition(field);
            if (field.isMaskable()) {
                methodBuilder.beginControlFlow("$L", inclusionCondition);
            }
        }
        methodBuilder.addStatement("$L.append($S)", SERIALIZED_OBJECT, QUOTES + field.getSerializableName() + QUOTES + ":");
    }

    @Override
    protected void handleFieldValues(FieldMeta field, MethodSpec.Builder methodBuilder) {
        methodBuilder.addStatement("$L.append($S)", SERIALIZED_OBJECT, ",");
        if (field.getInclusionLevel() != JsonInclude.Include.ALWAYS && field.isMaskable()) {
            methodBuilder.endControlFlow();
        }
    }

    @Override
    protected void returns(MethodSpec.Builder methodBuilder) {
        methodBuilder.beginControlFlow("if ($L.charAt($L.length() - 1) == ',')", SERIALIZED_OBJECT, SERIALIZED_OBJECT);
        methodBuilder.addStatement("$L.deleteCharAt($L.length() - 1)", SERIALIZED_OBJECT, SERIALIZED_OBJECT);
        methodBuilder.endControlFlow();

        methodBuilder.addStatement("$L.append($S)", SERIALIZED_OBJECT, "}");
    }

    @Override
    protected boolean skipRecursiveCall(Field field) {
        return false;
    }

    @Override
    protected void recursiveStatement(Rule rule, MethodSpec.Builder methodBuilder, Class<?> clazz, String getterName) {
        if (clazz.isArray()) {
            if (clazz.getComponentType().isPrimitive()) {
                addRecursiveStatement(methodBuilder, getPrimitiveArrayVariableName(rule, clazz.getComponentType()), getterName);
            } else {
                addRecursiveStatement(methodBuilder, getObjectArrayVariableName(rule), getterName);
            }
        } else if (String.class.isAssignableFrom(clazz)) {
            addRecursiveStatement(methodBuilder, getStringVariableName(rule), getterName);
        } else if (Map.class.isAssignableFrom(clazz)) {
            addRecursiveStatement(methodBuilder, getMapVariableName(rule), getterName);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            addRecursiveStatement(methodBuilder, getCollectionVariableName(rule), getterName);
        } else if (clazz.isPrimitive() || getWrapperTypes().contains(clazz)) {
            addRecursiveStatement(methodBuilder, getPrimitiveVariableName(rule, Primitives.wrap(clazz)), getterName);
        } else if (configuration.toStringSerializableClasses().contains(clazz)) {
            addRecursiveStatement(methodBuilder, getToStringVariableName(rule), getterName);
        } else if (clazz.isEnum()) {
            addRecursiveStatement(methodBuilder, getEnumVariableName(rule), getterName);
        } else if (getClassInformation(clazz) != null && getClassInformation(clazz).getSubClasses().isEmpty()) {
            addRecursiveStatement(methodBuilder, getVariableName(configuration, rule, clazz), getterName);
        } else {
            addDefaultRecursiveStatement(methodBuilder, getterName);
        }
    }

    private void addRecursiveStatement(MethodSpec.Builder methodBuilder, String methodName, String getterName) {
        methodBuilder.addStatement("$L.$L().$L($L.$L(), $L, $L, $L, $L)", SET_PARAMETER, methodName, INTERFACE_METHOD, OBJECT_PARAMETER, getterName, EVAL_PARAMETER, CLOAK_PARAMETER, SET_PARAMETER, SERIALIZED_OBJECT);
    }

    private void addDefaultRecursiveStatement(MethodSpec.Builder methodBuilder, String getterName) {
        methodBuilder.addStatement("$L.$L($L.$L(), $L, $L)", CLOAK_PARAMETER, ENTRY_METHOD, OBJECT_PARAMETER, getterName, EVAL_PARAMETER, SERIALIZED_OBJECT);
    }

    private int findField(String name, List<FieldMeta> fields) {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private void resolveInclusionLevel(Class<?> clazz, FieldMeta fieldMeta) {
        JsonSerialize classJsonSerialize = getAnnotation(clazz, JsonSerialize.class);
        JsonInclude classJsonInclude = getAnnotation(clazz, JsonInclude.class);

        JsonInclude.Include classInclusion = null;
        if (classJsonInclude == null && classJsonSerialize != null) {
            classInclusion = mapJsonSerialize(classJsonSerialize.include());
        } else if (classJsonInclude != null) {
            classInclusion = classJsonInclude.value();
        }

        JsonSerialize fieldJsonSerialize = fieldMeta.getField().getAnnotation(JsonSerialize.class);
        JsonInclude fieldJsonInclude = fieldMeta.getField().getAnnotation(JsonInclude.class);

        JsonInclude.Include fieldInclusion = null;
        if (fieldJsonInclude == null && fieldJsonSerialize != null) {
            fieldInclusion = mapJsonSerialize(fieldJsonSerialize.include());
        } else if (fieldJsonInclude != null) {
            fieldInclusion = fieldJsonInclude.value();
        }

        fieldMeta.setInclusionLevel(Optional.ofNullable(fieldInclusion).orElse(Optional.ofNullable(classInclusion).orElse(JsonInclude.Include.ALWAYS)));
    }

    private JsonInclude.Include mapJsonSerialize(JsonSerialize.Inclusion inclusion) {
        switch (inclusion) {
            case NON_NULL:
                return JsonInclude.Include.NON_NULL;
            case NON_EMPTY:
                return JsonInclude.Include.NON_EMPTY;
            case NON_DEFAULT:
                return JsonInclude.Include.NON_DEFAULT;
            case DEFAULT_INCLUSION:
            case ALWAYS:
            default:
                return JsonInclude.Include.ALWAYS;
        }
    }

    private CodeBlock constructInclusionCondition(FieldMeta fieldMeta) {
        Field field = fieldMeta.getField();
        String getterName = getGetterName(field.getName(), isBoolean(field.getType()), field.getType().isPrimitive());
        if (field.getType().isPrimitive()) {
            if (fieldMeta.getInclusionLevel() == JsonInclude.Include.NON_DEFAULT) {
                Object value = Defaults.defaultValue(field.getType());
                return CodeBlock.of("if ($L.$L() != $L)", OBJECT_PARAMETER, getterName, value);
            }
            fieldMeta.setMaskable(false);
            return null;
        } else {
            switch (fieldMeta.getInclusionLevel()) {
                case NON_DEFAULT:
                case NON_NULL:
                    return CodeBlock.of("if ($L.$L() != null)", OBJECT_PARAMETER, getterName);
                case NON_EMPTY:
                    if (field.getType().isArray()) {
                        return CodeBlock.of("if ($L.$L() != null && $L.$L().length > 0)", OBJECT_PARAMETER, getterName, OBJECT_PARAMETER, getterName);
                    } else {
                        if (getEmptiableTypes().stream().noneMatch(t -> t.isAssignableFrom(field.getType()))) {
                            fieldMeta.setMaskable(false);
                            return null;
                        }
                        return CodeBlock.of("if ($L.$L() != null && !$L.$L().isEmpty())", OBJECT_PARAMETER, getterName, OBJECT_PARAMETER, getterName);
                    }
                default:
                    fieldMeta.setMaskable(false);
                    return null;
            }
        }
    }
}
