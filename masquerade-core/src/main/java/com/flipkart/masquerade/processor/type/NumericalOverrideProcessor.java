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

package com.flipkart.masquerade.processor.type;

import com.flipkart.masquerade.Configuration;
import com.flipkart.masquerade.processor.BaseOverrideProcessor;
import com.flipkart.masquerade.rule.Rule;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import static com.flipkart.masquerade.util.Helper.getPrimitiveImplementationName;
import static com.flipkart.masquerade.util.Helper.getWrapperTypes;
import static com.flipkart.masquerade.util.Strings.OBJECT_PARAMETER;
import static com.flipkart.masquerade.util.Strings.QUOTES;
import static com.flipkart.masquerade.util.Strings.SERIALIZED_OBJECT;

/**
 * Created by shrey.garg on 23/08/17.
 */
public class NumericalOverrideProcessor extends BaseOverrideProcessor {
    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder  Entry class under construction for the cycle
     */
    public NumericalOverrideProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        super(configuration, cloakBuilder);
    }

    /**
     * @param rule The rule which is being processed
     * @return A fully constructed TypeSpec object for numerical implementation
     */
    public List<TypeSpec> createOverrides(Rule rule) {
        List<TypeSpec> typeSpecs = new ArrayList<>();
        for (Class<?> numericalType : configuration.numericalSerializableClasses()) {
            String implName = getPrimitiveImplementationName(rule, numericalType);
            MethodSpec.Builder methodBuilder = generateOverrideMethod(rule, numericalType);

            if (configuration.isNativeSerializationEnabled()) {
                methodBuilder.addStatement("$L.append($L.toString())", SERIALIZED_OBJECT, OBJECT_PARAMETER);
            }

            typeSpecs.add(generateImplementationType(rule, numericalType, implName, methodBuilder.build()));
        }
        return typeSpecs;
    }
}
