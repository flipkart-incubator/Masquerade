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
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.flipkart.masquerade.util.Helper.*;
import static com.flipkart.masquerade.util.Strings.*;

/**
 * Created by shrey.garg on 23/07/17.
 */
public class SerializationRuleObjectProcessor extends RuleObjectProcessor {
    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder  Entry class under construction for the cycle
     */
    public SerializationRuleObjectProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        super(configuration, cloakBuilder);
    }

    @Override
    protected void handleReturnsForNullObjects(MethodSpec.Builder objectMaskBuilder) {
        objectMaskBuilder.addStatement("$L.append($L)", SERIALIZED_OBJECT, NULL_STRING);
        objectMaskBuilder.addStatement("return");
    }

    @Override
    protected void handleRegisteredClasses(MethodSpec.Builder objectMaskBuilder) {
        objectMaskBuilder.addStatement("$L.$L($L, $L, this, $L, $L)", MASKER_VARIABLE, INTERFACE_METHOD, OBJECT_PARAMETER, EVAL_PARAMETER, SET_PARAMETER, SERIALIZED_OBJECT);
    }

    @Override
    protected void handleMaps(Rule rule, MethodSpec.Builder objectMaskBuilder) {
        addMethodCall(objectMaskBuilder, getMapVariableName(rule), Map.class);
    }

    @Override
    protected void handleCollections(Rule rule, MethodSpec.Builder objectMaskBuilder) {
        addMethodCall(objectMaskBuilder, getCollectionVariableName(rule), Collection.class);
    }

    @Override
    protected void handleObjectArrays(Rule rule, MethodSpec.Builder objectMaskBuilder) {
        addMethodCall(objectMaskBuilder, getObjectArrayVariableName(rule), ArrayTypeName.of(Object.class));
    }

    private void addMethodCall(MethodSpec.Builder objectMaskBuilder, String methodName, Object clazz) {
        objectMaskBuilder.addStatement("$L.$L().$L(($T) $L, $L, this, $L, $L)", SET_PARAMETER, methodName, INTERFACE_METHOD, clazz, OBJECT_PARAMETER, EVAL_PARAMETER, SET_PARAMETER, SERIALIZED_OBJECT);
    }

    @Override
    protected void handlePrimitiveArrays(Rule rule, MethodSpec.Builder objectMaskBuilder) {
        objectMaskBuilder.nextControlFlow("else if ($L.getClass().isArray())", OBJECT_PARAMETER);
        /* Handle char[] separately */
        objectMaskBuilder.beginControlFlow("if ($L instanceof $T[])", OBJECT_PARAMETER, Character.TYPE);
        addMethodCall(objectMaskBuilder, getPrimitiveArrayVariableName(rule, Character.TYPE), ArrayTypeName.of(Character.TYPE));
        /* Need to check for all primitive types individually */
        List<Class<?>> primitiveTypes = new ArrayList<>(getPrimitivesTypes());
        for (int i = 0; i < primitiveTypes.size(); i++) {
            Class<?> primitiveType = primitiveTypes.get(i);
            objectMaskBuilder.nextControlFlow("else if ($L instanceof $T[])", OBJECT_PARAMETER, primitiveType);
            addMethodCall(objectMaskBuilder, getPrimitiveArrayVariableName(rule, primitiveType), ArrayTypeName.of(primitiveType));
        }
        objectMaskBuilder.endControlFlow();
    }

    @Override
    protected void handleReturns(MethodSpec.Builder objectMaskBuilder) {
        // DO NOTHING
    }
}
