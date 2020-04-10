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

import com.flipkart.masquerade.Configuration;
import com.flipkart.masquerade.rule.Rule;
import com.flipkart.masquerade.serialization.FieldMeta;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.lang.reflect.Field;
import java.util.List;

import static com.flipkart.masquerade.util.Helper.getWrapperTypes;
import static com.flipkart.masquerade.util.Strings.*;

/**
 * Created by shrey.garg on 23/07/17.
 */
public class DefaultOverrideProcessor extends OverrideProcessor {
    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder  Entry class under construction for the cycle
     */
    public DefaultOverrideProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        super(configuration, cloakBuilder);
    }

    @Override
    protected void declareInitializeVariables(MethodSpec.Builder methodBuilder) {
        // Nothing needs to be initialized in this case
    }

    @Override
    protected List<FieldMeta> enrichFieldMetas(List<FieldMeta> fieldMetas, Class<?> clazz) {
        return fieldMetas;
    }

    @Override
    protected void addSyntheticFields(Class<?> clazz, List<FieldMeta> fields) {
        // No extra fields need to be added here
    }

    @Override
    protected boolean skipProcessing(FieldMeta field) {
        return field.getType().isPrimitive();
    }

    @Override
    protected void handleSyntheticFields(FieldMeta field, MethodSpec.Builder methodBuilder) {
        // No synthetic fields in this case
    }

    @Override
    protected boolean skipAnnotationProcessing(FieldMeta field) {
        return field.getType().isPrimitive();
    }

    @Override
    protected void handleFieldKeys(Class<?> clazz, FieldMeta field, MethodSpec.Builder methodBuilder) {
        // No processing required in this case
    }

    @Override
    protected void handleFieldValues(FieldMeta field, MethodSpec.Builder methodBuilder) {
        // No processing required in this case
    }

    @Override
    protected void returns(MethodSpec.Builder methodBuilder) {
        // Returns nothing
    }

    @Override
    protected boolean skipRecursiveCall(Field field) {
        /* Does not add the statement if the field is primitive, primitive wrapper, String or an Enum */
        return field.getType().isPrimitive() ||
                getWrapperTypes().contains(field.getType()) ||
                String.class.isAssignableFrom(field.getType()) ||
                field.getType().isEnum();
    }

    @Override
    protected void recursiveStatement(Rule rule, MethodSpec.Builder methodBuilder, Class<?> clazz, String getterName) {
        methodBuilder.addStatement("$L.$L($L.$L(), $L)", CLOAK_PARAMETER, ENTRY_METHOD, OBJECT_PARAMETER, getterName, EVAL_PARAMETER);
    }
}
