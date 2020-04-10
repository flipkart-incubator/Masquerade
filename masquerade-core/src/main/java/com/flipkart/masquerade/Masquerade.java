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

package com.flipkart.masquerade;

import com.flipkart.masquerade.processor.*;
import com.flipkart.masquerade.rule.Rule;
import com.flipkart.masquerade.util.EntryType;
import com.flipkart.masquerade.util.RepositoryEntry;
import com.flipkart.masquerade.util.TypeSpecContainer;
import com.google.common.reflect.ClassPath;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.flipkart.masquerade.util.Helper.*;
import static com.flipkart.masquerade.util.Strings.ENTRY_CLASS;

/**
 * Created by shrey.garg on 24/04/17.
 */
public class Masquerade {
    private static final List<TypeSpecContainer> specs = new ArrayList<>();

    public static void initialize(Configuration configuration, File destination) throws IOException, ClassNotFoundException {
        initialize(configuration, ClassLoader.getSystemClassLoader(), destination);
    }

    public static void initialize(Configuration configuration, ClassLoader classLoader, File destination) throws IOException, ClassNotFoundException {
        if (configuration == null || classLoader == null || destination == null) {
            throw new NullPointerException("Masquerade does not accept any null parameters");
        }

        if (configuration.getRules() == null || configuration.getCloakPackage() == null || configuration.getPackagesToScan() == null) {
            throw new NullPointerException("Configuration cannot return any null objects");
        }

        /* Fetch all the classes in the configured packages */
        Set<ClassPath.ClassInfo> scannedClasses = getPackageClasses(classLoader, configuration.getPackagesToScan());

        /* Start construction of the entry class which will be used by the user */
        TypeSpec.Builder builder = TypeSpec.classBuilder(ENTRY_CLASS);
        builder.addModifiers(Modifier.PUBLIC);

        RuleProcessor ruleProcessor = new RuleProcessor(configuration, builder);
        OverrideProcessor overrideProcessor = configuration.isNativeSerializationEnabled() ? new SerializationOverrideProcessor(configuration, builder) : new DefaultOverrideProcessor(configuration, builder);

        RepositoryProcessor repositoryProcessor = new RepositoryProcessor(configuration, builder);
        repositoryProcessor.createReference();

        DebugProcessor debugProcessor = new DebugProcessor(configuration, builder);
        debugProcessor.addGetter();

        specs.addAll(ruleProcessor.generateRuleTypeSpecs());

        List<RepositoryEntry> repositoryEntries = new ArrayList<>();
        mapClasses(scannedClasses, classLoader);
        for (Class<?> clazz : getClasses()) {
            /* Skip processing if the class is an Enum, Interface, Abstract or not a public class */
            if (clazz.isEnum() || clazz.isInterface() || getClassInformation(clazz).isAbstract() || !getClassInformation(clazz).isPublic()) {
                if (clazz.isEnum()) {
                    configuration.getRules().forEach(rule -> repositoryEntries.add(new RepositoryEntry(rule, clazz, EntryType.ENUM)));
                }
                continue;
            }

            for (Rule rule : configuration.getRules()) {
                /* Generate an implementation class for the Mask interface created earlier */
                /* The override might be absent in case of terminal classes */
                overrideProcessor.createOverride(rule, clazz, repositoryEntries)
                        .ifPresent(typeSpec -> specs.add(new TypeSpecContainer(getImplementationPackage(configuration, clazz), typeSpec)));
            }
        }

        specs.add(new TypeSpecContainer(configuration.getCloakPackage(), builder.build()));
        specs.add(new TypeSpecContainer(configuration.getCloakPackage(), repositoryProcessor.createRepository(repositoryEntries)));

        for (TypeSpecContainer container : specs) {
            TypeSpec.Builder typeBuilder = container.getSpec().toBuilder().addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value", "$S", "com.flipkart.masquerade.Masquerade").build());
            JavaFile javaFile = JavaFile.builder(container.getPackagePath(), typeBuilder.build())
                    .indent("    ")
                    .skipJavaLangImports(true)
                    .build();
            javaFile.writeTo(destination);
        }
    }
}
