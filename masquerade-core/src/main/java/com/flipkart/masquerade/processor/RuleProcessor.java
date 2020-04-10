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
import com.flipkart.masquerade.processor.type.*;
import com.flipkart.masquerade.rule.Rule;
import com.flipkart.masquerade.util.TypeSpecContainer;
import com.flipkart.masquerade.util.Verifier;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Processor that processes Rule level implementations
 * <p />
 * Created by shrey.garg on 27/05/17.
 */
public class RuleProcessor {
    private final Configuration configuration;
    private final TypeSpec.Builder cloakBuilder;

    private final List<TypeSpecContainer> specs = new ArrayList<>();

    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder Entry class under construction for the cycle
     */
    public RuleProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        this.configuration = configuration;
        this.cloakBuilder = cloakBuilder;
    }

    public List<TypeSpecContainer> generateRuleTypeSpecs() {
        /* Initialize all the processors */
        ReferenceMapProcessor mapProcessor = new ReferenceMapProcessor(configuration, cloakBuilder);
        InterfaceProcessor interfaceProcessor = new InterfaceProcessor(configuration, cloakBuilder);
        RuleObjectProcessor ruleObjectProcessor = configuration.isNativeSerializationEnabled() ? new SerializationRuleObjectProcessor(configuration, cloakBuilder) : new DefaultRuleObjectProcessor(configuration, cloakBuilder);
        NoOpOverrideProcessor noOpOverrideProcessor = new NoOpOverrideProcessor(configuration, cloakBuilder);
        EnumOverrideProcessor enumOverrideProcessor = new EnumOverrideProcessor(configuration, cloakBuilder);
        ToStringProcessor toStringProcessor = new ToStringProcessor(configuration, cloakBuilder);
        MapOverrideProcessor mapOverrideProcessor = new MapOverrideProcessor(configuration, cloakBuilder);
        CollectionOverrideProcessor collectionOverrideProcessor = new CollectionOverrideProcessor(configuration, cloakBuilder);
        ObjectArrayOverrideProcessor objectArrayOverrideProcessor = new ObjectArrayOverrideProcessor(configuration, cloakBuilder);
        PrimitiveArrayOverrideProcessor primitiveArrayOverrideProcessor = new PrimitiveArrayOverrideProcessor(configuration, cloakBuilder);
        CharacterPrimitiveArrayOverrideProcessor characterPrimitiveArrayOverrideProcessor = new CharacterPrimitiveArrayOverrideProcessor(configuration, cloakBuilder);
        StringOverrideProcessor stringOverrideProcessor = new StringOverrideProcessor(configuration, cloakBuilder);
        PrimitiveOverrideProcessor primitiveOverrideProcessor = new PrimitiveOverrideProcessor(configuration, cloakBuilder);
        NumericalOverrideProcessor numericalOverrideProcessor = new NumericalOverrideProcessor(configuration, cloakBuilder);

        for (Rule rule : configuration.getRules()) {
            /* Verify if the Rule is constructed properly */
            Verifier.verifyEvaluationObject(rule);
            Verifier.verifyAnnotation(rule);
            Verifier.verifyTypes(rule);
            /* Creates a Map of Class name and Mask */
            mapProcessor.addMap(rule);
            /* Creates an interface for each Rule which is extended by each Mask for that Rule */
            specs.add(new TypeSpecContainer(configuration.getCloakPackage(), interfaceProcessor.generateInterface(rule)));
            /* Adds the entry method which takes an Object, resolves and executes an appropriate Mask */
            ruleObjectProcessor.addEntry(rule);
            /* Creates a NoOp implementation for each Rule which can be used for terminal classes */
            specs.add(new TypeSpecContainer(configuration.getCloakPackage(), noOpOverrideProcessor.createOverride(rule)));
            /* Creates a Enum implementation for each Rule which can be used for enums */
            specs.add(new TypeSpecContainer(configuration.getCloakPackage(), enumOverrideProcessor.createOverride(rule)));
            /* Creates a ToString implementation for each Rule which can be used for any class which needs to be serialized by calling toString() */
            specs.add(new TypeSpecContainer(configuration.getCloakPackage(), toStringProcessor.createOverride(rule)));
            /* Creates a Map implementation for each Rule which can be used for Maps */
            specs.add(new TypeSpecContainer(configuration.getCloakPackage(), mapOverrideProcessor.createOverride(rule)));
            /* Creates a Collection implementation for each Rule which can be used for Collections */
            specs.add(new TypeSpecContainer(configuration.getCloakPackage(), collectionOverrideProcessor.createOverride(rule)));
            /* Creates a object array implementation for each Rule */
            specs.add(new TypeSpecContainer(configuration.getCloakPackage(), objectArrayOverrideProcessor.createOverride(rule)));
            specs.addAll(primitiveArrayOverrideProcessor.createOverrides(rule).stream()
                    .map(t -> new TypeSpecContainer(configuration.getCloakPackage(), t)).collect(Collectors.toList()));
            specs.add(new TypeSpecContainer(configuration.getCloakPackage(), characterPrimitiveArrayOverrideProcessor.createOverride(rule)));
            specs.add(new TypeSpecContainer(configuration.getCloakPackage(), stringOverrideProcessor.createOverride(rule)));
            specs.addAll(primitiveOverrideProcessor.createOverrides(rule).stream()
                    .map(t -> new TypeSpecContainer(configuration.getCloakPackage(), t)).collect(Collectors.toList()));
            specs.addAll(numericalOverrideProcessor.createOverrides(rule).stream()
                    .map(t -> new TypeSpecContainer(configuration.getCloakPackage(), t)).collect(Collectors.toList()));
        }

        return specs;
    }
}
