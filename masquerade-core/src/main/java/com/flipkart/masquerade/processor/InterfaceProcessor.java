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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.lang.model.element.Modifier;

import static com.flipkart.masquerade.util.Helper.*;
import static com.flipkart.masquerade.util.Strings.*;

/**
 * Processor that creates an interface for a Mask
 * <p />
 * Created by shrey.garg on 12/05/17.
 */
public class InterfaceProcessor {
    private final Configuration configuration;
    private final TypeSpec.Builder cloakBuilder;

    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder Entry class under construction for the cycle
     */
    public InterfaceProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        this.configuration = configuration;
        this.cloakBuilder = cloakBuilder;
    }

    /**
     * @param rule The rule to generate the interface for
     * @return A fully constructed TypeSpec object for the interface
     */
    public TypeSpec generateInterface(Rule rule) {
        TypeSpec.Builder ruleInterface = TypeSpec.interfaceBuilder(getInterfaceName(rule));
        ruleInterface.addModifiers(Modifier.PUBLIC);
        ruleInterface.addTypeVariable(TypeVariableName.get("T"));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(INTERFACE_METHOD);
        methodBuilder.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC);
        methodBuilder.addParameter(TypeVariableName.get("T"), OBJECT_PARAMETER);
        methodBuilder.addParameter(rule.getEvaluatorClass(), EVAL_PARAMETER);
        methodBuilder.addParameter(getEntryClass(configuration), CLOAK_PARAMETER);
        methodBuilder.addParameter(getRepositoryClass(configuration), SET_PARAMETER);

        if (configuration.isNativeSerializationEnabled()) {
            methodBuilder.addParameter(StringBuilder.class, SERIALIZED_OBJECT);
        }

        ruleInterface.addMethod(methodBuilder.build());

        return ruleInterface.build();
    }
}
