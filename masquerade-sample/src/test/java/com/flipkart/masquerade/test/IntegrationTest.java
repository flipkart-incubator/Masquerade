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

import com.flipkart.masquerade.test.actual.Fallback;
import com.flipkart.masquerade.test.actual.Others;
import com.flipkart.masquerade.test.actual.others.Sample;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by shrey.garg on 15/07/17.
 */
public class IntegrationTest extends BaseTest {
    @Test
    public void testMiscellaneous() throws Exception {
        Others others = new Others();
        others.setA(32);
        others.setIs(true);
        others.setProductSuffix("something");
        others.setIsNotBoolean("not a boolean");
        others.setArrayList(new ArrayList<>());
        others.setUri(URI.create("/3/product/reviews?start=0&count=3&productId=SGLEHCBEZVHH5BBF&sortOrder=MOST_HELPFUL&ratings=ALL&reviewType=ALL&reviewerType=ALL&infiniteScroll=false"));
        others.setInts(new int[] { 1, 2, 3 });
        others.setChars(new char[] { 'a', 'b', 'c' });
        others.setCharacter('z');

        String serialized = cloak.hide(others, defaultEval);
        System.out.println(serialized);

        assertEquals(mapper.writeValueAsString(others), serialized);
    }

    @Test
    public void testFallback() throws Exception {
        Fallback fallback = new Fallback();
        fallback.setString("abc");
        fallback.setObjects(Arrays.asList(MissingEnum.B, 1));
        fallback.setNotIncluded(new NotIncluded(true, "false"));

        String serialized = cloak.hide(fallback, defaultEval);
        System.out.println(serialized);

        assertEquals(mapper.writeValueAsString(fallback), serialized);
    }

    @Test
    public void testSamples() throws Exception {
        Sample sample = new Sample(new BigInteger("1238123"));

        String serialized = cloak.hide(sample, defaultEval);
        System.out.println(serialized);

        assertEquals(mapper.writeValueAsString(sample), serialized);
    }
}
