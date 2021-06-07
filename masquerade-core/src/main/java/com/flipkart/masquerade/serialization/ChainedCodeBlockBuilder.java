/*
 * Copyright 2020 Flipkart Internet, pvt ltd.
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
package com.flipkart.masquerade.serialization;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 * User: bageshwar.pn
 * Date: 09/04/20
 * Time: 3:12 PM
 * This class splits the method body into 1000 statements per method.
 *
 * Helpful when the generated method's body could potentially be really huge, which would fail during compilation
 * since JVM puts a hard limit of 64Kb for the method body.
 */
public class ChainedCodeBlockBuilder {

    private int nStatements;
    private List<CodeBlock.Builder> initializers;
    private CodeBlock.Builder currentInitializer;
    private String methodPrefix;
    private TypeSpec.Builder owningClass;
    private int maxStatementsInMethod;

    /**
     *
     * @param methodPrefix The prefix to be used to generate method names for the chained method.
     * @param maxStatementsInMethod The number of statements per chained method.
     * @param owningClass The class where the chained methods will be added.
     */
    public ChainedCodeBlockBuilder(String methodPrefix, int maxStatementsInMethod, TypeSpec.Builder owningClass){
        this.methodPrefix = methodPrefix;
        this.owningClass = owningClass;
        this.maxStatementsInMethod = maxStatementsInMethod;

        this.currentInitializer = CodeBlock.builder();
        this.initializers = new LinkedList<>();
        this.initializers.add(currentInitializer);
    }

    public void addStatement(String format, Object... args) {

        if(nStatements > maxStatementsInMethod){
            nStatements = 0;
            currentInitializer = CodeBlock.builder();
            initializers.add(currentInitializer);
        }

        nStatements++;
        currentInitializer.addStatement(format, args);
    }

    public CodeBlock build(){
        if(initializers.size() == 1){
            return currentInitializer.build();
        } else {
            // add n new methods and chain them together
            int nMethod = 1;
            CodeBlock.Builder chainedMethodInitializer =  CodeBlock.builder();
            for(CodeBlock.Builder initializer : initializers){
                String methodName = methodPrefix + (nMethod++);
                owningClass.addMethod(MethodSpec
                        .methodBuilder(methodName).returns(TypeName.VOID)
                        .addModifiers(Modifier.PRIVATE)
                        .addCode(initializer.build()).build());

                chainedMethodInitializer.addStatement("$L()", methodName);
            }

            return chainedMethodInitializer.build();
        }
    }
}
