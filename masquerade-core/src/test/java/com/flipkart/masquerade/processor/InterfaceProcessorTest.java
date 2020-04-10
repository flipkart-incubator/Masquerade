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
import com.flipkart.masquerade.test.ConfigurationExtension;
import com.flipkart.masquerade.test.annotation.ConfigProvider;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static com.flipkart.masquerade.util.Helper.getEntryClass;
import static com.flipkart.masquerade.util.Helper.getRepositoryClass;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by shrey.garg on 20/05/17.
 */
@ExtendWith(ConfigurationExtension.class)
public class InterfaceProcessorTest {

    @Test
    public void generateInterface(@ConfigProvider Configuration configuration) {
        InterfaceProcessor processor = new InterfaceProcessor(configuration, null);
        List<Rule> rules = new ArrayList<>(configuration.getRules());

        TypeSpec interfaceSpec = processor.generateInterface(rules.get(0));

        assertEquals(1, interfaceSpec.typeVariables.size(), "Interface should have one generic variable");
        assertEquals(1, interfaceSpec.methodSpecs.size(), "The interface should have only one method");

        MethodSpec methodSpec = interfaceSpec.methodSpecs.get(0);
        assertEquals(4, methodSpec.parameters.size(), "The method should have 3 parameters");
        assertEquals("T", methodSpec.parameters.get(0).type.toString(), "First parameter is the generic variable");
        assertEquals(rules.get(0).getEvaluatorClass().getName(), methodSpec.parameters.get(1).type.toString(), "Second parameter is the Rule Evaluator");
        assertEquals(getEntryClass(configuration), methodSpec.parameters.get(2).type, "Third parameter is the entry class");
        assertEquals(getRepositoryClass(configuration), methodSpec.parameters.get(3).type, "Fourth parameter is the repository class");
    }

}