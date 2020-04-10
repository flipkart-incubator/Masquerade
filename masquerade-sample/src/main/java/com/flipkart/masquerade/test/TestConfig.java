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

package com.flipkart.masquerade.test;

import com.flipkart.masquerade.Configuration;
import com.flipkart.masquerade.rule.*;
import com.flipkart.masquerade.serialization.SerializationProperty;
import com.flipkart.masquerade.util.Fallback;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shrey.garg on 25/04/17.
 */
public class TestConfig implements Configuration {
    private static Set<Rule> rules = new HashSet<>();
    private static HashSet<SerializationProperty> serializationProperties = new HashSet<>();

    static {
        Rule rule = new Rule(
                "VaAn",
                ValidationAnnotation.class,
                Eval.class,
                new CompositeRule(
                        new BasicRule("name", Operator.EQUAL, "platform"),
                        new CompositeRule(Conjunction.OR,
                                new BasicRule("since", Operator.LESSER, "getVersion()"),
                                new BasicRule("till", Operator.GREATER, "getVersion()")
                        ),
                        new BasicRule("client", Operator.UNEQUAL, "getClient()")
                )
        );
        rules.add(rule);

        serializationProperties.add(SerializationProperty.SORT_PROPERTIES_ALPHABETICALLY);
    }

    @Override
    public List<String> getPackagesToScan() {
        return Arrays.asList("com.flipkart.masquerade.test.actual");
    }

    @Override
    public Set<Rule> getRules() {
        return rules;
    }

    @Override
    public String getCloakPackage() {
        return "org.test.veils";
    }

    @Override
    public boolean isNativeSerializationEnabled() {
        return true;
    }

    @Override
    public Set<SerializationProperty> serializationProperties() {
        return serializationProperties;
    }

    @Override
    public boolean isDebugMode() {
        return false;
    }

    @Override
    public Fallback fallback() {
        return new FallbackImpl();
    }
}
