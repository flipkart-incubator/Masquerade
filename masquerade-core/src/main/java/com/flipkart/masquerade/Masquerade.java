package com.flipkart.masquerade;

import com.flipkart.masquerade.processor.InterfaceProcessor;
import com.flipkart.masquerade.processor.OverrideProcessor;
import com.flipkart.masquerade.processor.ReferenceMapProcessor;
import com.flipkart.masquerade.processor.RuleObjectProcessor;
import com.flipkart.masquerade.rule.Rule;
import com.flipkart.masquerade.util.TypeSpecContainer;
import com.google.common.reflect.ClassPath;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

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

        /* The initialization block which initializes a Map for each Rule and populates it with all the Masks relevant to that Rule */
        CodeBlock.Builder staticCode = CodeBlock.builder();

        /* Initialize all the processors */
        ReferenceMapProcessor mapProcessor = new ReferenceMapProcessor(configuration, builder);
        InterfaceProcessor interfaceProcessor = new InterfaceProcessor(configuration, builder);
        RuleObjectProcessor ruleObjectProcessor = new RuleObjectProcessor(configuration, builder);
        OverrideProcessor overrideProcessor = new OverrideProcessor(configuration, builder);

        for (Rule rule : configuration.getRules()) {
            /* Creates a Map of Class name and Mask */
            mapProcessor.addMap(rule);
            /* Creates an interface for each Rule which is extended by each Mask for that Rule */
            specs.add(new TypeSpecContainer(configuration.getCloakPackage(), interfaceProcessor.generateInterface(rule)));
            /* Adds the entry method which takes an Object, resolves and executes an appropriate Mask */
            ruleObjectProcessor.addEntry(rule);
        }

        for (ClassPath.ClassInfo info : scannedClasses) {
            Class<?> clazz = Class.forName(info.getName(), true, classLoader);

            /* Skip processing if the class is an Enum, Interface, Abstract or not a public class */
            if (clazz.isEnum() || clazz.isInterface() || isAbstract(clazz) || !isPublic(clazz)) {
                continue;
            }

            for (Rule rule : configuration.getRules()) {
                /* Generate an implementation class for the Mask interface created earlier */
                specs.add(new TypeSpecContainer(getImplementationPackage(configuration, clazz), overrideProcessor.createOverride(rule, clazz, staticCode)));
            }
        }

        builder.addInitializerBlock(staticCode.build());

        specs.add(new TypeSpecContainer(configuration.getCloakPackage(), builder.build()));

        for (TypeSpecContainer container : specs) {
            JavaFile javaFile = JavaFile.builder(container.getPackagePath(), container.getSpec())
                    .indent("    ")
                    .skipJavaLangImports(true)
                    .build();
            javaFile.writeTo(destination);
        }
    }
}
