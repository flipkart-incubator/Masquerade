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

import com.flipkart.masquerade.rule.Rule;

/**
 * Created by shrey.garg on 23/07/17.
 */
public class RepositoryEntry {
    private final Rule rule;
    private final Class<?> clazz;
    private final EntryType entryType;

    public RepositoryEntry(Rule rule, Class<?> clazz, EntryType entryType) {
        this.rule = rule;
        this.clazz = clazz;
        this.entryType = entryType;
    }

    public Rule getRule() {
        return rule;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepositoryEntry)) return false;

        RepositoryEntry that = (RepositoryEntry) o;

        if (!rule.equals(that.rule)) return false;
        if (!clazz.equals(that.clazz)) return false;
        return entryType == that.entryType;
    }

    @Override
    public int hashCode() {
        int result = rule.hashCode();
        result = 31 * result + clazz.hashCode();
        result = 31 * result + entryType.hashCode();
        return result;
    }
}
