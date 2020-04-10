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

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.masquerade.test.actual.*;
import com.flipkart.masquerade.test.actual.collections.CollectOne;
import com.flipkart.masquerade.test.actual.collections.CollectThree;
import com.flipkart.masquerade.test.actual.maps.MapOne;
import com.flipkart.masquerade.test.actual.maps.MapTwo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by shrey.garg on 28/05/17.
 */
public class SampleTest extends BaseTest {
    @Test
    public void testSimpleCloaking() throws Exception {
        One one = new One();
        one.setT1("something");
        one.setT2(2);

        Eval eval = new Eval();
        eval.platform = Platform.ANDROID;
        eval.setClient("web");
        eval.setVersion(5);

        assertNotNull(one.getT2());
        String serialized = cloak.hide(one, eval);
        assertNull(one.getT2());
        assertEquals(mapper.writeValueAsString(one), serialized);

        One deSerialized = mapper.readValue(serialized, One.class);
        assertNotNull(deSerialized);
        assertNull(deSerialized.getT2());
        assertNull(deSerialized.getTwo());
        assertEquals("something", deSerialized.getT1());
    }

    @Test
    public void testSimpleNotCloaking() throws Exception {
        One one = new One();
        one.setT1("something");
        one.setT2(2);

        Eval eval = new Eval();
        eval.platform = Platform.MOBILE_WEB;
        eval.setClient("web");
        eval.setVersion(5);

        assertNotNull(one.getT2());
        String serialized = cloak.hide(one, eval);
        assertNotNull(one.getT2());
        assertEquals(mapper.writeValueAsString(one), serialized);

        One deSerialized = mapper.readValue(serialized, One.class);
        assertNotNull(deSerialized);
        assertNotNull(deSerialized.getT2());
        assertNull(deSerialized.getTwo());
        assertEquals("something", deSerialized.getT1());
    }

    @Test
    public void testGenericCloaking() throws Exception {
        One one = new One();
        one.setT1("something");
        one.setT2(2);

        Wrapper<One> wrapper = new Wrapper<>();
        wrapper.setResponse(one);

        Eval eval = new Eval();
        eval.platform = Platform.ANDROID;
        eval.setClient("web");
        eval.setVersion(5);

        assertNotNull(one.getT2());
        String serialized = cloak.hide(wrapper, eval);
        assertNull(one.getT2());
        assertEquals(mapper.writeValueAsString(wrapper), serialized);

        Wrapper<One> deSerialized = mapper.readValue(serialized, new TypeReference<Wrapper<One>>() {
        });
        assertNull(deSerialized.getResponse().getT2());
        assertNull(deSerialized.getResponse().getTwo());
        assertNotNull(deSerialized.getResponse().getT1());
    }

    @Test
    public void testComplexCloaking() throws Exception {
        One one = new One();
        one.setT1("something");
        one.setT2(2);

        Two two = new Two();
        two.setL1("else");
        two.setL2(7);
        two.setOne(one);

        one.setTwo(two);

        Eval eval = new Eval();
        eval.platform = Platform.ANDROID;
        eval.setClient("web");
        eval.setVersion(1);

        assertNotNull(one.getT2());
        assertNotNull(two.getL1());

        String serialized = cloak.hide(one, eval);

        assertNull(one.getT2());
        assertNull(two.getL1());
        assertEquals(mapper.writeValueAsString(one), serialized);

        One deSerialized = mapper.readValue(serialized, One.class);
        assertNotNull(deSerialized);
        assertNull(deSerialized.getT2());
        assertEquals("something", deSerialized.getT1());

        assertNotNull(deSerialized.getTwo());
        assertNull(deSerialized.getTwo().getL1());
        assertEquals(7, deSerialized.getTwo().getL2().intValue());
        assertNull(deSerialized.getTwo().getOne());
    }

    @Test
    public void testCollectionCloaking() throws Exception {
        CollectThree collectThree = new CollectThree();
        collectThree.setId(2);

        Three three1 = new Three(2, 53.125);
        Three three2 = new Three(624, 212.63);
        collectThree.setThrees(new Three[]{ three1, three2 });

        CollectOne collectOne = new CollectOne();
        collectOne.setCollectTwo(collectThree);

        Four four1 = new Four(1232.12324, 423.61);
        Four four2 = new Four(2643.12, 6943.255);
        Four four3 = new Four(4124.63, 90123.23);
        collectOne.setFours(Arrays.asList(four1, four2, four3));

        Eval eval = new Eval();
        eval.platform = Platform.MOBILE_WEB;
        eval.setVersion(1);

        assertNotNull(four1.getBbDouble());
        assertNotNull(four2.getBbDouble());
        assertNotNull(four3.getBbDouble());

        String serialized = cloak.hide(collectOne, eval);

        assertNull(four1.getBbDouble());
        assertNull(four2.getBbDouble());
        assertNull(four3.getBbDouble());

        assertEquals(mapper.writeValueAsString(collectOne), serialized);

        // TODO: 09/07/17 Fix this test once we add JsonSubTypesSupport
//        CollectOne deSerialized = mapper.readValue(serialized, CollectOne.class);
    }

    @Test
    public void testMapCloaking() throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("b", 43.63);
        objectMap.put("a", false);
        objectMap.put("d", "Jack");
        objectMap.put("c", new Three(4, 621.1));

        MapTwo mapTwo = new MapTwo();
        mapTwo.setTbm("Jill");
        mapTwo.setObjectMap(objectMap);

        MapTwo mapTwo1 = new MapTwo();
        mapTwo1.setTbm("Went");
        mapTwo1.setObjectMap(new HashMap<>());

        Map<String, MapTwo> mapTwoMap = new HashMap<>();
        mapTwoMap.put("1", mapTwo);
        mapTwoMap.put("2", mapTwo1);

        MapOne mapOne = new MapOne();
        mapOne.setAbc("xyz");
        mapOne.setSomeBoolean(true);
        mapOne.setMapTwoMap(mapTwoMap);

        Eval eval = new Eval();
        eval.platform = Platform.ANDROID;
        eval.setVersion(12);

        assertNotNull(mapTwo.getTbm());
        assertNotNull(mapTwo1.getTbm());

        String serialized = cloak.hide(mapOne, eval);

        assertNull(mapTwo.getTbm());
        assertNull(mapTwo1.getTbm());

        assertEquals(mapper.writeValueAsString(mapOne), serialized);

        MapOne deSerialized = mapper.readValue(serialized, MapOne.class);
        assertEquals("xyz", deSerialized.getAbc());
        assertEquals(true, deSerialized.isSomeBoolean());
        assertNotNull(deSerialized.getMapTwoMap());

        Map<String, MapTwo> deSerializedMapTwoMap = deSerialized.getMapTwoMap();

        MapTwo deSerializedMapTwo = deSerializedMapTwoMap.get("1");
        assertNotNull(deSerializedMapTwo);
        assertNull(deSerializedMapTwo.getTbm());
        assertNotNull(deSerializedMapTwo.getObjectMap());
        assertEquals(4, deSerializedMapTwo.getObjectMap().size());
        assertEquals(false, deSerializedMapTwo.getObjectMap().get("a"));
        assertEquals(43.63, deSerializedMapTwo.getObjectMap().get("b"));
        assertEquals("Jack", deSerializedMapTwo.getObjectMap().get("d"));

        Three deSerializedThree = mapper.convertValue(deSerializedMapTwo.getObjectMap().get("c"), Three.class);
        assertNotNull(deSerializedThree);
        assertEquals(4, deSerializedThree.getA());
        assertEquals(621.1, deSerializedThree.getB());

        MapTwo deSerializedMapTwo1 = deSerializedMapTwoMap.get("2");
        assertNotNull(deSerializedMapTwo1);
        assertNull(deSerializedMapTwo1.getTbm());
        assertNotNull(deSerializedMapTwo1.getObjectMap());
        assertTrue(deSerializedMapTwo1.getObjectMap().isEmpty());
    }

    @Test
    public void testOthers() throws Exception {
        Others others = new Others();
        others.setA(32);
        others.setIs(true);
        others.setProductSuffix("something");
        others.setIsNotBoolean("not a boolean");
        others.setArrayList(new ArrayList<>());
        others.setProductSuffix("<p>\n" +
                "\t\t\t\t\t\t\t\tMoto brings to you the E4 Plus - a smartphone that & rsquo;s powered by a long - lasting 5000 mAh battery.Packed with a 13 MP rear camera and a 5 MP front camera,\n" +
                "\t\t\t\t\t\t\t\tthe Moto E4 Plus helps you take flaunt - worthy photos.It runs on Android 7.1 Nougat,\n" +
                "\t\t\t\t\t\t\t\tand \\s features Google Assistant\n" +
                "\t\t\t\t\t\t\t\tfor a seamless user experience.The 13.97 cm(5.5) HD display of this smartphone breathes life into all your visuals.Its fingerprint scanner not only helps you unlock this smartphone quickly,\n" +
                "\t\t\t\t\t\t\t\tbut also keeps your private data truly private\\.With a 1.3 GHz quad - core processor and 4 G support,\n" +
                "\t\t\t\t\t\t\t\tthis dual - SIM smartphone offers a powerful performance.Adding further to your convenience is the Moto Display feature that helps you check your notifications and updates quickly even when the phone is locked. < /p>");

        String serialized = cloak.hide(others, defaultEval);

        assertEquals(mapper.writeValueAsString(others), serialized);
        System.out.println(serialized);
    }
}
