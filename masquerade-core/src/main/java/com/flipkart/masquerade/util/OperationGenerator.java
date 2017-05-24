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
public class OperationGenerator {
    public static void processEquals(StringBuilder operation, FieldDescriptor descriptor) {
        if (descriptor.isEquatable()) {
            operation.append("($L == $L)");
        } else {
            operation.append("($L.equals($S))");
        }
    }

    public static void processNotEquals(StringBuilder operation, FieldDescriptor descriptor) {
        if (descriptor.isEquatable()) {
            operation.append("($L != $L)");
        } else {
            operation.append("(!$L.equals($L))");
        }
    }

    public static void processGreaterThan(StringBuilder operation, FieldDescriptor descriptor) {
        if (descriptor.isPrimitive()) {
            operation.append("($L > $L)");
        } else if (descriptor.isComparable()) {
            operation.append("($L.compareTo($L) > 0)");
        } else {
            throw new UnsupportedOperationException("Cannot compare non-comparable types");
        }
    }

    public static void processGreaterThanEquals(StringBuilder operation, FieldDescriptor descriptor) {
        if (descriptor.isPrimitive()) {
            operation.append("($L >= $L)");
        } else if (descriptor.isComparable()) {
            operation.append("($L.compareTo($L) >= 0)");
        } else {
            throw new UnsupportedOperationException("Cannot compare non-comparable types");
        }
    }

    public static void processLesserThan(StringBuilder operation, FieldDescriptor descriptor) {
        if (descriptor.isPrimitive()) {
            operation.append("($L < $L)");
        } else if (descriptor.isComparable()) {
            operation.append("($L.compareTo($L) < 0)");
        } else {
            throw new UnsupportedOperationException("Cannot compare non-comparable types");
        }
    }

    public static void processLesserThanEquals(StringBuilder operation, FieldDescriptor descriptor) {
        if (descriptor.isPrimitive()) {
            operation.append("($L <= $L)");
        } else if (descriptor.isComparable()) {
            operation.append("($L.compareTo($L) <= 0)");
        } else {
            throw new UnsupportedOperationException("Cannot compare non-comparable types");
        }
    }
}
