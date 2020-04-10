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

import com.flipkart.masquerade.rule.Rule;
import com.flipkart.masquerade.serialization.SerializationProperty;
import com.flipkart.masquerade.util.Fallback;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shrey.garg on 25/04/17.
 */
public interface Configuration {
    Set<Class<?>> toStringSerializableClasses = new HashSet<>(Arrays.asList(URI.class));
    Set<Class<?>> numericalSerializableClasses = new HashSet<>(Arrays.asList(BigInteger.class, BigDecimal.class));

    List<String> getPackagesToScan();
    Set<Rule> getRules();
    String getCloakPackage();
    boolean isNativeSerializationEnabled();
    Set<SerializationProperty> serializationProperties();
    boolean isDebugMode();
    default Set<Class<?>> toStringSerializableClasses() {
        return toStringSerializableClasses;
    }
    default Set<Class<?>> numericalSerializableClasses() {
        return numericalSerializableClasses;
    }
    default Fallback fallback() {
        return null;
    }
}
