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

/**
 * Created by shrey.garg on 12/05/17.
 */
public class Strings {
    public static final String MASKER_VARIABLE = "masker";

    public static final String INTERFACE_SUFFIX = "Mask";

    public static final String ENTRY_CLASS = "Cloak";
    public static final String ENTRY_METHOD = "hide";
    public static final String SET_CLASS = "Trunk";

    public static final String INTERFACE_METHOD = INTERFACE_SUFFIX.toLowerCase();
    public static final String OBJECT_PARAMETER = Object.class.getSimpleName().toLowerCase();
    public static final String EVAL_PARAMETER = "eval";
    public static final String CLOAK_PARAMETER = ENTRY_CLASS.toLowerCase();
    public static final String SET_PARAMETER = SET_CLASS.toLowerCase();

    public static final String SERIALIZED_OBJECT = "serialized";

    public static final String QUOTES = "\"";
    public static final String NULL_STRING = "_null";

    public static final String DEBUG_LIST = "missingClasses";
    public static final String FALLBACK_VARIABLE = "fallback";
    public static final String FALLBACK_METHOD = "convertToString";
}
