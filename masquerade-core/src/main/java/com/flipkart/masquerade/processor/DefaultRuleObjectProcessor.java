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

import java.util.Collection;
import java.util.Map;

import static com.flipkart.masquerade.util.Helper.getCollectionVariableName;
import static com.flipkart.masquerade.util.Helper.getMapVariableName;
import static com.flipkart.masquerade.util.Helper.getObjectArrayVariableName;
import static com.flipkart.masquerade.util.Strings.*;

/**
 * Created by shrey.garg on 23/07/17.
 */
public class DefaultRuleObjectProcessor extends RuleObjectProcessor {
    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder  Entry class under construction for the cycle
     */
    public DefaultRuleObjectProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        super(configuration, cloakBuilder);
    }

    @Override
    protected void handleReturnsForNullObjects(MethodSpec.Builder objectMaskBuilder) {
        objectMaskBuilder.addStatement("return");
    }

    @Override
    protected void handleRegisteredClasses(MethodSpec.Builder objectMaskBuilder) {
        objectMaskBuilder.addStatement("$L.$L($L, $L, this, $L)", MASKER_VARIABLE, INTERFACE_METHOD, OBJECT_PARAMETER, EVAL_PARAMETER, SET_PARAMETER);
    }

    @Override
    protected void handleMaps(Rule rule, MethodSpec.Builder objectMaskBuilder) {
        objectMaskBuilder.addStatement("$L.$L().$L(($T) $L, $L, this, $L)", SET_PARAMETER, getMapVariableName(rule), INTERFACE_METHOD, Map.class, OBJECT_PARAMETER, EVAL_PARAMETER, SET_PARAMETER);
    }

    @Override
    protected void handleCollections(Rule rule, MethodSpec.Builder objectMaskBuilder) {
        objectMaskBuilder.addStatement("$L.$L().$L(($T) $L, $L, this, $L)", SET_PARAMETER, getCollectionVariableName(rule), INTERFACE_METHOD, Collection.class, OBJECT_PARAMETER, EVAL_PARAMETER, SET_PARAMETER);
    }

    @Override
    protected void handleObjectArrays(Rule rule, MethodSpec.Builder objectMaskBuilder) {
        objectMaskBuilder.addStatement("$L.$L().$L(($T) $L, $L, this, $L)", SET_PARAMETER, getObjectArrayVariableName(rule), INTERFACE_METHOD, ArrayTypeName.of(Object.class), OBJECT_PARAMETER, EVAL_PARAMETER, SET_PARAMETER);
    }

    @Override
    protected void handlePrimitiveArrays(Rule rule, MethodSpec.Builder objectMaskBuilder) {
        // Nothing needs to be done in case of primitive arrays in this case
    }

    @Override
    protected void handleReturns(MethodSpec.Builder objectMaskBuilder) {
        // Returns nothing in this case
    }
}
