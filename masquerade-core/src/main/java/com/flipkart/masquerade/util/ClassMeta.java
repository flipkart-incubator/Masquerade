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

import java.util.HashSet;
import java.util.Set;

/**
 * Created by shrey.garg on 25/07/17.
 */
public class ClassMeta<T> {
    private final Set<Class<? extends T>> subClasses = new HashSet<>();
    private final boolean abstractClass;
    private final boolean publicClass;

    public ClassMeta(Class<T> clazz) {
        this.abstractClass = Helper.isAbstract(clazz);
        this.publicClass = Helper.isPublic(clazz);
    }

    public Set<Class<? extends T>> getSubClasses() {
        return subClasses;
    }

    public boolean isAbstract() {
        return abstractClass;
    }

    public boolean isPublic() {
        return publicClass;
    }

    public void addSubClass(Class subClass) {
        subClasses.add(subClass);
    }
}
