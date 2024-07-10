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
import com.flipkart.masquerade.processor.type.ToStringInitializationProcessor;
import com.flipkart.masquerade.rule.Rule;
import com.flipkart.masquerade.serialization.ChainedCodeBlockBuilder;
import com.flipkart.masquerade.util.EntryType;
import com.flipkart.masquerade.util.RepositoryEntry;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.List;

import static com.flipkart.masquerade.util.Helper.*;
import static com.flipkart.masquerade.util.Strings.SET_CLASS;
import static com.flipkart.masquerade.util.Strings.SET_PARAMETER;

/**
 * Created by shrey.garg on 24/07/17.
 */
public class RepositoryProcessor {

    private static final String INIT_VC_MAP_METHOD_NAME = "initVersionControlMap";

    private final Configuration configuration;
    private final TypeSpec.Builder cloakBuilder;

    private final ToStringInitializationProcessor toStringInitializationProcessor;
    private final ReferenceMapProcessor mapProcessor;

    /**
     * @param configuration Configuration for the current processing cycle
     * @param cloakBuilder Entry class under construction for the cycle
     */
    public RepositoryProcessor(Configuration configuration, TypeSpec.Builder cloakBuilder) {
        this.configuration = configuration;
        this.cloakBuilder = cloakBuilder;
        this.toStringInitializationProcessor = new ToStringInitializationProcessor(configuration, cloakBuilder);
        this.mapProcessor = new ReferenceMapProcessor(configuration, cloakBuilder);
    }

    public void createReference() {
        ClassName className = getRepositoryClass(configuration);
        cloakBuilder.addField(FieldSpec
                .builder(className, SET_PARAMETER, Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", className).build());
        cloakBuilder.addInitializerBlock(CodeBlock.builder()
                .addStatement("$L.$L()", SET_PARAMETER, INIT_VC_MAP_METHOD_NAME).build());
        cloakBuilder.addMethod(MethodSpec
                .methodBuilder(getRepositoryGetter()).returns(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return $L", SET_PARAMETER).build());
    }

    public TypeSpec createRepository(List<RepositoryEntry> repositoryEntries) {
        /* Start construction of the repository class which will be used by the user */
        final TypeSpec.Builder repositoryBuilder = TypeSpec.classBuilder(SET_CLASS);
        repositoryBuilder.addModifiers(Modifier.PUBLIC);

        final ChainedCodeBlockBuilder initializer = new ChainedCodeBlockBuilder(configuration.methodPrefixForChainedMethods(), configuration.maxStatementsInMethod(), repositoryBuilder);

        for (Rule rule : configuration.getRules()) {
            /* Creates a Map of Class name and Mask */
            mapProcessor.addMap(rule, repositoryBuilder);

            handleEnumEntry(repositoryBuilder, rule);
            handlePrimitiveEntries(repositoryBuilder, rule, initializer);
            handleStringEntry(repositoryBuilder, rule, initializer);
            handleToStringEntries(repositoryBuilder, rule, initializer);
            handleProcessedEntries(repositoryBuilder, initializer, repositoryEntries);
            handleMapEntry(repositoryBuilder, rule);
            handleCollectionEntry(repositoryBuilder, rule);
            handleObjectArrayEntry(repositoryBuilder, rule);
            if (configuration.isNativeSerializationEnabled()) {
                handlePrimitiveArrayEntries(repositoryBuilder, rule);
                handleCharacterPrimitiveArrayEntries(repositoryBuilder, rule);
                handleNumericalEntries(repositoryBuilder, rule, initializer);
            } else {
                handleNoOpEntry(repositoryBuilder, rule);
            }
        }

        repositoryBuilder.addMethod(MethodSpec
                .methodBuilder(INIT_VC_MAP_METHOD_NAME).returns(TypeName.VOID)
                .addModifiers(Modifier.PUBLIC)
                .addCode(initializer.build()).build());
        return repositoryBuilder.build();
    }

    private void handleEnumEntry(TypeSpec.Builder repositoryBuilder, Rule rule) {
        handleEntry(repositoryBuilder,
                getEnumImplementationClass(configuration, rule), getEnumVariableName(rule));
    }

    private void handleNoOpEntry(TypeSpec.Builder repositoryBuilder, Rule rule) {
        handleEntry(repositoryBuilder,
                getNoOpImplementationClass(configuration, rule), getNoOpVariableName(rule));
    }

    private void handleToStringEntries(TypeSpec.Builder repositoryBuilder, Rule rule, ChainedCodeBlockBuilder initializer) {
        handleEntry(repositoryBuilder,
                getToStringImplementationClass(configuration, rule), getToStringVariableName(rule));

        toStringInitializationProcessor.generateToStringEntries(rule)
                .forEach(re -> initializer.addStatement("$L.put($S, $L)", rule.getName(), re.getClazz().getName(), getToStringVariableName(rule)));
    }

    private void handlePrimitiveEntries(TypeSpec.Builder repositoryBuilder, Rule rule, ChainedCodeBlockBuilder initializer) {
        for (Class<?> wrapperType : getWrapperTypes()) {
            handleEntry(repositoryBuilder,
                    getPrimitiveImplementationClass(configuration, rule, wrapperType), getPrimitiveVariableName(rule, wrapperType));

            initializer.addStatement("$L.put($S, $L)", rule.getName(), wrapperType.getName(), getPrimitiveVariableName(rule, wrapperType));
        }
    }

    private void handleStringEntry(TypeSpec.Builder repositoryBuilder, Rule rule, ChainedCodeBlockBuilder initializer) {
        handleEntry(repositoryBuilder,
                getStringImplementationClass(configuration, rule), getStringVariableName(rule));

        initializer.addStatement("$L.put($S, $L)", rule.getName(), String.class.getName(), getStringVariableName(rule));
    }

    private void handleMapEntry(TypeSpec.Builder repositoryBuilder, Rule rule) {
        handleEntry(repositoryBuilder,
                getMapImplementationClass(configuration, rule), getMapVariableName(rule));
    }

    private void handleCollectionEntry(TypeSpec.Builder repositoryBuilder, Rule rule) {
        handleEntry(repositoryBuilder,
                getCollectionImplementationClass(configuration, rule), getCollectionVariableName(rule));
    }

    private void handleObjectArrayEntry(TypeSpec.Builder repositoryBuilder, Rule rule) {
        handleEntry(repositoryBuilder,
                getObjectArrayImplementationClass(configuration, rule), getObjectArrayVariableName(rule));
    }

    private void handlePrimitiveArrayEntries(TypeSpec.Builder repositoryBuilder, Rule rule) {
        for (Class<?> primitiveType : getPrimitivesTypes()) {
            handleEntry(repositoryBuilder,
                    getPrimitiveArrayImplementationClass(configuration, rule, primitiveType), getPrimitiveArrayVariableName(rule, primitiveType));
        }
    }

    private void handleCharacterPrimitiveArrayEntries(TypeSpec.Builder repositoryBuilder, Rule rule) {
        handleEntry(repositoryBuilder,
                getPrimitiveArrayImplementationClass(configuration, rule, Character.TYPE), getPrimitiveArrayVariableName(rule, Character.TYPE));
    }

    private void handleNumericalEntries(TypeSpec.Builder repositoryBuilder, Rule rule, ChainedCodeBlockBuilder initializer) {
        for (Class<?> wrapperType : configuration.numericalSerializableClasses()) {
            handleEntry(repositoryBuilder,
                    getPrimitiveImplementationClass(configuration, rule, wrapperType), getPrimitiveVariableName(rule, wrapperType));

            initializer.addStatement("$L.put($S, $L)", rule.getName(), wrapperType.getName(), getPrimitiveVariableName(rule, wrapperType));
        }
    }

    private void handleProcessedEntries(TypeSpec.Builder repositoryBuilder, ChainedCodeBlockBuilder initializer, List<RepositoryEntry> repositoryEntries) {
        for (RepositoryEntry entry : repositoryEntries) {
            if (entry.getEntryType() == EntryType.NEW) {
                String implName = getImplementationName(entry.getRule(), entry.getClazz());
                ClassName cloakName = ClassName.get(getImplementationPackage(configuration, entry.getClazz()), implName);
                String variableName = getVariableName(cloakName);
                /* Refer to the added field while adding to map */
                initializer.addStatement("$L.put($S, $L)", entry.getRule().getName(), entry.getClazz().getName(), variableName);
                handleEntry(repositoryBuilder, cloakName, variableName);
            } else if (entry.getEntryType() == EntryType.ENUM) {
                initializer.addStatement("$L.put($S, $L)", entry.getRule().getName(), entry.getClazz().getName(), getEnumVariableName(entry.getRule()));
            } else if (entry.getEntryType() == EntryType.NoOP) {
                initializer.addStatement("$L.put($S, $L)", entry.getRule().getName(), entry.getClazz().getName(), getNoOpVariableName(entry.getRule()));
            }
        }
    }

    private void handleEntry(TypeSpec.Builder repositoryBuilder, ClassName className, String variableName) {
        FieldSpec fieldSpec = FieldSpec.builder(className, variableName, Modifier.PRIVATE)
                .initializer("new $T()", className).build();
        repositoryBuilder.addField(fieldSpec);
        /* Create a accessor for the field defined above */
        repositoryBuilder.addMethod(MethodSpec
                .methodBuilder(variableName).returns(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return $L", variableName).build());
    }
}