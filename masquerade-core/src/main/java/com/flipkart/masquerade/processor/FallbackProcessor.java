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
import com.flipkart.masquerade.util.Fallback;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import static com.flipkart.masquerade.util.Strings.*;

/**
 * Created by shrey.garg on 18/07/17.
 */
public class FallbackProcessor {
    private final Configuration configuration;
    private final TypeSpec.Builder cloakBuilder;

    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder Entry class under construction for the cycle
     */
    public FallbackProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        this.configuration = configuration;
        this.cloakBuilder = cloakBuilder;
    }

    public void addFallbackCall(MethodSpec.Builder objectMaskBuilder) {
        if (!configuration.isNativeSerializationEnabled() || configuration.fallback() == null) {
            return;
        }

        cloakBuilder.addField(FieldSpec
                .builder(Fallback.class, FALLBACK_VARIABLE, Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", configuration.fallback().getClass()).build());

        objectMaskBuilder.nextControlFlow("else");
        objectMaskBuilder.addStatement("$L.append($L.$L($L))", SERIALIZED_OBJECT, FALLBACK_VARIABLE, FALLBACK_METHOD, OBJECT_PARAMETER);
    }
}
