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

package com.flipkart.masquerade.test.actual;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.masquerade.test.actual.subtypes.Base;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by shrey.garg on 15/07/17.
 */
public class Others {
    private boolean is;
    @JsonProperty("sProductSuffix")
    private String productSuffix;
    private Integer a;
    private String isNotBoolean;
    private char character;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ArrayList<String> arrayList;
    private Boolean isActiveSubscriber;
    private boolean value;
    private URI uri;
    private int[] ints;
    private char[] chars;
    private Base base;

    public boolean isIs() {
        return is;
    }

    public void setIs(boolean is) {
        this.is = is;
    }

    public String getProductSuffix() {
        return productSuffix;
    }

    public void setProductSuffix(String productSuffix) {
        this.productSuffix = productSuffix;
    }

    public Integer getA() {
        return a;
    }

    public void setA(Integer a) {
        this.a = a;
    }

    public String getIsNotBoolean() {
        return isNotBoolean;
    }

    public void setIsNotBoolean(String isNotBoolean) {
        this.isNotBoolean = isNotBoolean;
    }

    public ArrayList<String> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<String> arrayList) {
        this.arrayList = arrayList;
    }

    public Boolean getActiveSubscriber() {
        return isActiveSubscriber;
    }

    public void setActiveSubscriber(Boolean activeSubscriber) {
        isActiveSubscriber = activeSubscriber;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public int[] getInts() {
        return ints;
    }

    public void setInts(int[] ints) {
        this.ints = ints;
    }

    public char[] getChars() {
        return chars;
    }

    public void setChars(char[] chars) {
        this.chars = chars;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public Base getBase() {
        return base;
    }

    public void setBase(Base base) {
        this.base = base;
    }
}
