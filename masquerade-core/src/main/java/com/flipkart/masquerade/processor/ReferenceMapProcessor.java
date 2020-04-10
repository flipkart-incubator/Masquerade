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
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Map;

import static com.flipkart.masquerade.util.Helper.getRuleInterface;
import static com.flipkart.masquerade.util.Strings.SET_PARAMETER;

/**
 * Processor that adds and initializes a Map field mapping Class to Mask Object.
 * <p />
 * Created by shrey.garg on 12/05/17.
 */
public class ReferenceMapProcessor {
    private final Configuration configuration;
    private final TypeSpec.Builder cloakBuilder;

    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder Entry class under construction for the cycle
     */
    public ReferenceMapProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        this.configuration = configuration;
        this.cloakBuilder = cloakBuilder;
    }

    /**
     * Adds a private field and initializes it as well.
     *
     * @param rule The rule for which the Map will be generated
     */
    public void addMap(Rule rule, TypeSpec.Builder builder) {
        ParameterizedTypeName mapType = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), getRuleInterface(configuration, rule));
        builder.addField(FieldSpec.builder(mapType, rule.getName(), Modifier.PRIVATE).initializer("new $T<>()", HashMap.class).build());
        builder.addMethod(MethodSpec
                .methodBuilder(mapGetterName(rule))
                .returns(mapType)
                .addStatement("return $L", rule.getName())
                .build());
    }

    /**
     * Adds a private field and references the repository map.
     *
     * @param rule The rule for which the Map will be generated
     */
    public void addMap(Rule rule) {
        ParameterizedTypeName mapType = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), getRuleInterface(configuration, rule));
        cloakBuilder.addField(FieldSpec.builder(mapType, rule.getName(), Modifier.PRIVATE).initializer("$L.$L()", SET_PARAMETER, mapGetterName(rule)).build());
    }

    private String mapGetterName(Rule rule) {
        return "get" + rule.getName();
    }

}
