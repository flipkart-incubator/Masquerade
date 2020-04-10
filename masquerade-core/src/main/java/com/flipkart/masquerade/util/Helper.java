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

package com.flipkart.masquerade.util;

import com.flipkart.masquerade.Configuration;
import com.flipkart.masquerade.rule.BasicRule;
import com.flipkart.masquerade.rule.Rule;
import com.google.common.reflect.ClassPath;
import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static com.flipkart.masquerade.util.Strings.*;

/**
 * Created by shrey.garg on 25/04/17.
 */
public class Helper {
    private static final Set<Class<?>> wrapperTypes = new HashSet<>();
    private static final Set<Class<?>> primitiveTypes = new HashSet<>();
    private static final Map<Class<?>, ClassMeta<?>> classInformation = new HashMap<>();

    static {
        wrapperTypes.add(Boolean.class);
        wrapperTypes.add(Character.class);
        wrapperTypes.add(Byte.class);
        wrapperTypes.add(Short.class);
        wrapperTypes.add(Integer.class);
        wrapperTypes.add(Long.class);
        wrapperTypes.add(Float.class);
        wrapperTypes.add(Double.class);
        wrapperTypes.add(Void.class);

        primitiveTypes.add(Boolean.TYPE);
        primitiveTypes.add(Byte.TYPE);
        primitiveTypes.add(Short.TYPE);
        primitiveTypes.add(Integer.TYPE);
        primitiveTypes.add(Long.TYPE);
        primitiveTypes.add(Float.TYPE);
        primitiveTypes.add(Double.TYPE);
    }

    public static String getSetterName(String name, boolean isBoolean) {
        String capitalizedName = capitalize(name);
        String prefix = "set";

        if (isBoolean) {
            capitalizedName = handleIsPrefix(capitalizedName);
        }

        return prefix + capitalizedName;
    }

    public static String getGetterName(String name, boolean isBoolean, boolean isPrimitive) {
        String capitalizedName = capitalize(name);
        String prefix = "get";

        if (isBoolean) {
            capitalizedName = handleIsPrefix(capitalizedName);

            if (isPrimitive) {
                prefix = "is";
            }
        }

        return prefix + capitalizedName;
    }

    public static String handleIsPrefix(String name) {
        String capitalizedName = capitalize(name);

        if (capitalizedName.startsWith("Is")) {
            if (capitalizedName.length() > 2 && Character.isUpperCase(capitalizedName.charAt(2))) {
                capitalizedName = capitalizedName.substring(2);
            }
        }

        return capitalizedName;
    }

    public static Set<Class<?>> getWrapperTypes() {
        return wrapperTypes;
    }

    public static Set<Class<?>> getPrimitivesTypes() {
        return primitiveTypes;
    }

    public static Set<Class<?>> getEmptiableTypes() {
        Set<Class<?>> ret = new HashSet<>();
        ret.add(Map.class);
        ret.add(Collection.class);
        ret.add(String.class);
        return ret;
    }

    private static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String deCapitalize(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static ClassName getRuleInterface(Configuration configuration, Rule rule) {
        return ClassName.get(configuration.getCloakPackage(), rule.getName() + INTERFACE_SUFFIX);
    }

    public static ClassName getRepositoryClass(Configuration configuration) {
        return ClassName.get(configuration.getCloakPackage(), SET_CLASS);
    }

    public static String getRepositoryGetter() {
        return "get" + SET_CLASS;
    }

    public static String getInterfaceName(Rule rule) {
        return rule.getName() + INTERFACE_SUFFIX;
    }

    public static String getImplementationName(Rule rule, Class<?> clazz) {
        return generateImplementationName(rule, clazz.getSimpleName());
    }

    public static String getNoOpImplementationName(Rule rule) {
        return generateImplementationName(rule, "NoOp");
    }

    public static String getEnumImplementationName(Rule rule) {
        return generateImplementationName(rule, "Enum");
    }

    public static String getToStringImplementationName(Rule rule) {
        return generateImplementationName(rule, "ToString");
    }

    public static String getMapImplementationName(Rule rule) {
        return generateImplementationName(rule, "Map");
    }

    public static String getStringImplementationName(Rule rule) {
        return generateImplementationName(rule, "String");
    }

    public static String getCollectionImplementationName(Rule rule) {
        return generateImplementationName(rule, "Collection");
    }

    public static String getObjectArrayImplementationName(Rule rule) {
        return generateImplementationName(rule, "ObjectArray");
    }

    public static String getPrimitiveArrayImplementationName(Rule rule, Class<?> clazz) {
        return generateImplementationName(rule, capitalize(clazz.getSimpleName()) + "ArrayPrimitive");
    }

    public static String getPrimitiveImplementationName(Rule rule, Class<?> clazz) {
        return generateImplementationName(rule, capitalize(clazz.getSimpleName()));
    }

    public static ClassName getNoOpImplementationClass(Configuration configuration, Rule rule) {
        return ClassName.get(configuration.getCloakPackage(), getNoOpImplementationName(rule));
    }

    public static ClassName getEnumImplementationClass(Configuration configuration, Rule rule) {
        return ClassName.get(configuration.getCloakPackage(), getEnumImplementationName(rule));
    }

    public static ClassName getToStringImplementationClass(Configuration configuration, Rule rule) {
        return ClassName.get(configuration.getCloakPackage(), getToStringImplementationName(rule));
    }

    public static ClassName getMapImplementationClass(Configuration configuration, Rule rule) {
        return ClassName.get(configuration.getCloakPackage(), getMapImplementationName(rule));
    }

    public static ClassName getCollectionImplementationClass(Configuration configuration, Rule rule) {
        return ClassName.get(configuration.getCloakPackage(), getCollectionImplementationName(rule));
    }

    public static ClassName getObjectArrayImplementationClass(Configuration configuration, Rule rule) {
        return ClassName.get(configuration.getCloakPackage(), getObjectArrayImplementationName(rule));
    }

    public static ClassName getStringImplementationClass(Configuration configuration, Rule rule) {
        return ClassName.get(configuration.getCloakPackage(), getStringImplementationName(rule));
    }

    public static ClassName getPrimitiveArrayImplementationClass(Configuration configuration, Rule rule, Class<?> clazz) {
        return ClassName.get(configuration.getCloakPackage(), getPrimitiveArrayImplementationName(rule, clazz));
    }

    public static ClassName getPrimitiveImplementationClass(Configuration configuration, Rule rule, Class<?> clazz) {
        return ClassName.get(configuration.getCloakPackage(), getPrimitiveImplementationName(rule, clazz));
    }

    private static String generateImplementationName(Rule rule, String prefix) {
        return prefix + rule.getName() + INTERFACE_SUFFIX;
    }

    public static String getImplementationPackage(Configuration configuration, Class<?> clazz) {
        return configuration.getCloakPackage() + "." + clazz.getPackage().getName();
    }

    public static ClassName getEntryClass(Configuration configuration) {
        return ClassName.get(configuration.getCloakPackage(), ENTRY_CLASS);
    }

    public static String getNoOpVariableName(Rule rule) {
        return "noOp" + rule.getName();
    }

    public static String getEnumVariableName(Rule rule) {
        return "enum" + rule.getName();
    }

    public static String getVariableName(ClassName className) {
        return className.reflectionName().replaceAll("\\.", "_");
    }

    public static String getVariableName(Configuration configuration, Rule rule, Class<?> clazz) {
        return getVariableName(
                ClassName.get(getImplementationPackage(configuration, clazz),
                        getImplementationName(rule, clazz)));
    }

    public static String getToStringVariableName(Rule rule) {
        return "toString" + rule.getName();
    }

    public static String getMapVariableName(Rule rule) {
        return "map" + rule.getName();
    }

    public static String getCollectionVariableName(Rule rule) {
        return "collection" + rule.getName();
    }

    public static String getObjectArrayVariableName(Rule rule) {
        return "objectArray" + rule.getName();
    }

    public static String getStringVariableName(Rule rule) {
        return "string" + rule.getName();
    }

    public static String getPrimitiveArrayVariableName(Rule rule, Class<?> clazz) {
        return clazz.getSimpleName().toLowerCase() + "ArrayPrimitive" + rule.getName();
    }

    public static String getPrimitiveVariableName(Rule rule, Class<?> clazz) {
        return clazz.getSimpleName().toLowerCase() + rule.getName();
    }

    public static Set<ClassPath.ClassInfo> getPackageClasses(ClassLoader classLoader, List<String> packagesToScan) throws IOException {
        ClassPath classpath = ClassPath.from(classLoader);
        Set<ClassPath.ClassInfo> classDescriptions = new HashSet<>();
        for (String basePackage : packagesToScan) {
            classDescriptions.addAll(classpath.getTopLevelClassesRecursive(basePackage));
        }
        return classDescriptions;
    }

    public static List<Field> getNonStaticFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(
                    Arrays.asList(c.getDeclaredFields()).stream()
                            .filter(field -> !Modifier.isStatic(field.getModifiers()))
                            .collect(Collectors.toList())
            );
        }
        return fields;
    }

    public static <T extends Annotation> T getAnnotation(Class<?> type, Class<T> annotationClass) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            T annotation = c.getAnnotation(annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    public static String getEvaluationFunction(BasicRule basicRule) {
        return EVAL_PARAMETER + "." + basicRule.getEvaluatorFunction();
    }

    public static boolean isAbstract(Class clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    public static boolean isPublic(Class clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }

    public static boolean isBoolean(Class<?> clazz) {
        return clazz.equals(Boolean.TYPE) || clazz.equals(Boolean.class);
    }

    public static void mapClasses(Set<ClassPath.ClassInfo> scannedClasses, ClassLoader classLoader) throws ClassNotFoundException {
        for (ClassPath.ClassInfo info : scannedClasses) {
            Class<?> clazz = Class.forName(info.getName(), true, classLoader);
            classInformation.put(clazz, new ClassMeta<>(clazz));
        }

        for (Class<?> clazz : classInformation.keySet()) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass == null || superclass.equals(Object.class)) {
                continue;
            }

            final ClassMeta<?> superClassMeta = classInformation.get(superclass);
            if (superClassMeta != null) {
                superClassMeta.addSubClass(clazz);
            }

        }
    }

    public static Map<Class<?>, ClassMeta<?>> getClassInformation() {
        return classInformation;
    }

    public static Set<Class<?>> getClasses() {
        return classInformation.keySet();
    }

    public static ClassMeta<?> getClassInformation(Class<?> clazz) {
        return classInformation.get(clazz);
    }
}
