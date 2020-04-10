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

import com.flipkart.masquerade.test.actual.Four;
import com.flipkart.masquerade.test.actual.Others;
import com.flipkart.masquerade.test.actual.Three;
import com.flipkart.masquerade.test.actual.subtypes.Base;
import com.flipkart.masquerade.test.actual.subtypes.SubOne;
import com.flipkart.masquerade.test.actual.subtypes.SubTwo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by shrey.garg on 10/07/17.
 */
public class SubTypesTest extends BaseTest {
    @Test
    public void testSubTypes() throws Exception {
        SubOne subOne = new SubOne();
        subOne.setAbc("abc");
        subOne.setDef("def");
        subOne.setFour(new Four(213.632, 42.12342));

        String serialized = cloak.hide(subOne, defaultEval);
        System.out.println(serialized);

        assertEquals(mapper.writeValueAsString(subOne), serialized);

        SubTwo subTwo = new SubTwo();
        subTwo.setAbc("abc");
        subTwo.setDef("def");
        subTwo.setThree(new Three(764323, 42.12342));

        String serializedTwo = cloak.hide(subTwo, defaultEval);
        System.out.println(serializedTwo);

        assertEquals(mapper.writeValueAsString(subTwo), serializedTwo);
    }

    @Test
    public void testSubTypesWithAbstractDeclaration() throws Exception {
        Base subOne = new SubOne();
        subOne.setAbc("abc");
        subOne.setDef("def");
//        subOne.setFour(new Four(213.632, 42.12342));

        String serialized = cloak.hide(subOne, defaultEval);
        System.out.println(serialized);

        assertEquals(mapper.writeValueAsString(subOne), serialized);

        Base subTwo = new SubTwo();
        subTwo.setAbc("abc");
        subTwo.setDef("def");
//        subTwo.setThree(new Three(764323, 42.12342));

        String serializedTwo = cloak.hide(subTwo, defaultEval);
        System.out.println(serializedTwo);

        assertEquals(mapper.writeValueAsString(subTwo), serializedTwo);
    }

    @Test
    public void testSubTypesInAnotherObject() throws Exception {
        Others others = new Others();
        SubOne subOne = new SubOne();
        subOne.setAbc("abc");
        subOne.setDef("def");
        subOne.setFour(new Four(213.632, 42.12342));
        others.setBase(subOne);

        String serialized = cloak.hide(others, defaultEval);
        System.out.println(serialized);

        assertEquals(mapper.writeValueAsString(others), serialized);

        SubTwo subTwo = new SubTwo();
        subTwo.setAbc("abc");
        subTwo.setDef("def");
        subTwo.setThree(new Three(764323, 42.12342));
        others.setBase(subTwo);

        String serializedTwo = cloak.hide(others, defaultEval);
        System.out.println(serializedTwo);

        assertEquals(mapper.writeValueAsString(others), serializedTwo);
    }

}
