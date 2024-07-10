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

/**
 * Created by shrey.garg on 25/04/17.
 */
public class Eval {
    public Platform platform;
    private int version;
    private String client;
    private boolean defaultNonNullInclusion;

    public Eval() {
    }

    public Eval(Platform platform, int version) {
        this.platform = platform;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public boolean isDefaultNonNullInclusion() {
        return defaultNonNullInclusion;
    }

    public boolean isNullKeySupported() {
        return defaultNonNullInclusion;
    }

    public void setDefaultNonNullInclusion(boolean defaultNonNullInclusion) {
        this.defaultNonNullInclusion = defaultNonNullInclusion;
    }
}
