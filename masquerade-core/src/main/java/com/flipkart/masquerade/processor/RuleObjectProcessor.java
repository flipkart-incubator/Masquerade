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
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.Map;

import static com.flipkart.masquerade.util.Helper.getRuleInterface;
import static com.flipkart.masquerade.util.Strings.*;

/**
 * Processor which adds the entry point method in the Entry class
 * <p />
 * Created by shrey.garg on 12/05/17.
 */
public abstract class RuleObjectProcessor {
    private final Configuration configuration;
    private final TypeSpec.Builder cloakBuilder;
    private final DebugProcessor debugProcessor;
    private final FallbackProcessor fallbackProcessor;

    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder Entry class under construction for the cycle
     */
    public RuleObjectProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        this.configuration = configuration;
        this.cloakBuilder = cloakBuilder;
        this.debugProcessor = new DebugProcessor(configuration, cloakBuilder);
        this.fallbackProcessor = new FallbackProcessor(configuration, cloakBuilder);
    }

    /**
     * @param rule Rule for which the entry point will be added
     */
    public void addEntry(Rule rule) {
        MethodSpec.Builder objectMaskBuilder = MethodSpec.methodBuilder(ENTRY_METHOD);
        objectMaskBuilder.addModifiers(Modifier.PUBLIC);
        /* Suppress warnings as the generated code will never cause a CLassCastException */
        objectMaskBuilder.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unchecked").build());
        /* Adds an Object class parameter for which all the process at runtime will happen */
        objectMaskBuilder.addParameter(Object.class, OBJECT_PARAMETER);
        /* The second parameter refers to the Evaluator Object which will be used for comparisons */
        objectMaskBuilder.addParameter(rule.getEvaluatorClass(), EVAL_PARAMETER);

        if (configuration.isNativeSerializationEnabled()) {
            objectMaskBuilder.addParameter(StringBuilder.class, SERIALIZED_OBJECT);
        }

        /* If a null Object is passed, return immediately */
        objectMaskBuilder.beginControlFlow("if ($L == null)", OBJECT_PARAMETER);
        handleReturnsForNullObjects(objectMaskBuilder);
        objectMaskBuilder.endControlFlow();

        /* Fetch the Mask implementation object from Map and assign it to an interface reference for the Rule */
        objectMaskBuilder.addStatement("$T $L = this.$L.get($L.getClass().getName())",
                getRuleInterface(configuration, rule),
                MASKER_VARIABLE,
                rule.getName(),
                OBJECT_PARAMETER);

        /* Check if the retrieved Object is present */
        objectMaskBuilder.beginControlFlow("if ($L != null)", MASKER_VARIABLE);
        /* If it is, then call the mask method for the Object */
        handleRegisteredClasses(objectMaskBuilder);

        objectMaskBuilder.nextControlFlow("else");
        /* Otherwise, check if the Object is an instance of Map */
        objectMaskBuilder.beginControlFlow("if ($L instanceof $T)", OBJECT_PARAMETER, Map.class);
        /* If it is, then recursively call this entry method with the List of map values */
        handleMaps(rule, objectMaskBuilder);

        /* If it's not a Map, then check if the Object is a collection */
        objectMaskBuilder.nextControlFlow("else if ($L instanceof $T)", OBJECT_PARAMETER, Collection.class);
        /* If it is, then iterate over the collection */
        handleCollections(rule, objectMaskBuilder);
        /* If it's not a Collection, then check if the Object is an array */
        objectMaskBuilder.nextControlFlow("else if ($L instanceof Object[])", OBJECT_PARAMETER);
        /* If it is, then iterate over the array */
        handleObjectArrays(rule, objectMaskBuilder);
        /* If it's not an Object[], then check if the Object is a primitive array, only if native serialization is enabled */
        handlePrimitiveArrays(rule, objectMaskBuilder);
        debugProcessor.addDebugCollector(objectMaskBuilder);
        fallbackProcessor.addFallbackCall(objectMaskBuilder);
        objectMaskBuilder.endControlFlow();
        objectMaskBuilder.endControlFlow();

        handleReturns(objectMaskBuilder);

        cloakBuilder.addMethod(objectMaskBuilder.build());

        if (configuration.isNativeSerializationEnabled()) {
            MethodSpec.Builder objectBasicMaskBuilder = MethodSpec.methodBuilder(ENTRY_METHOD);
            objectBasicMaskBuilder.addModifiers(Modifier.PUBLIC);
            /* Adds an Object class parameter for which all the process at runtime will happen */
            objectBasicMaskBuilder.addParameter(Object.class, OBJECT_PARAMETER);
            /* The second parameter refers to the Evaluator Object which will be used for comparisons */
            objectBasicMaskBuilder.addParameter(rule.getEvaluatorClass(), EVAL_PARAMETER);

            objectBasicMaskBuilder.addStatement("$T $L = new $T()", StringBuilder.class, SERIALIZED_OBJECT, StringBuilder.class);
            objectBasicMaskBuilder.addStatement("this.$L($L, $L, $L)", ENTRY_METHOD, OBJECT_PARAMETER, EVAL_PARAMETER, SERIALIZED_OBJECT);

            objectBasicMaskBuilder.returns(String.class);
            objectBasicMaskBuilder.addStatement("return $L.toString()", SERIALIZED_OBJECT);

            cloakBuilder.addMethod(objectBasicMaskBuilder.build());

            cloakBuilder.addField(
                    FieldSpec.builder(String.class, NULL_STRING, Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("$S", "null").build());
        }
    }

    protected abstract void handleReturnsForNullObjects(MethodSpec.Builder objectMaskBuilder);

    protected abstract void handleRegisteredClasses(MethodSpec.Builder objectMaskBuilder);

    protected abstract void handleMaps(Rule rule, MethodSpec.Builder objectMaskBuilder);

    protected abstract void handleCollections(Rule rule, MethodSpec.Builder objectMaskBuilder);

    protected abstract void handleObjectArrays(Rule rule, MethodSpec.Builder objectMaskBuilder);

    protected abstract void handlePrimitiveArrays(Rule rule, MethodSpec.Builder objectMaskBuilder);

    protected abstract void handleReturns(MethodSpec.Builder objectMaskBuilder);
}
