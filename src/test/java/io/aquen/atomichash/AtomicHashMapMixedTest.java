/*
 * =========================================================================
 *
 *   Copyright (c) 2019-2024, Aquen OSS (https://aquen.io)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *   implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 *
 * =========================================================================
 */
package io.aquen.atomichash;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AtomicHashMapMixedTest {

    private AtomicHashMap<String,String> map;


    @Before
    public void initStore() {
        this.map = new AtomicHashMap<>();
    }


    @Test
    public void test00() throws Exception {

        Assert.assertEquals(0, map.size());
        Assert.assertTrue(map.isEmpty());
        Assert.assertFalse(map.containsKey("one"));
        map.forEach((k,v) -> Assert.assertTrue(false));

        map.put("one", "ONE");
        Assert.assertEquals("ONE", map.get("one"));
        Assert.assertEquals("ONE", map.putIfAbsent("one","TWO"));
        Assert.assertEquals("ONE", map.get("one"));

        Assert.assertFalse(map.isEmpty());

        final Map<String,String> m = new HashMap<>();
        m.put("two", "TWO");
        m.put("three", "THREE");
        m.put("four", "FOUR");

        map.putAll(m);
        Assert.assertEquals(4, map.size());
        Assert.assertEquals("TWO", map.get("two"));
        Assert.assertEquals("THREE", map.get("three"));
        Assert.assertEquals("FOUR", map.get("four"));

        Assert.assertEquals(map.store().hashCode(), map.hashCode());

        Assert.assertTrue(map.containsKey("one"));
        Assert.assertFalse(map.containsKey("five"));

        map.forEach((k,v) -> Assert.assertTrue(map.containsKey(k)));
        map.forEach((k,v) -> Assert.assertTrue(map.containsValue(v)));
        map.forEach((k,v) -> Assert.assertTrue(map.get(k) == v));

        Assert.assertEquals("FOUR", map.getOrDefault("four", "NOTHING"));
        Assert.assertEquals("NOTHING", map.getOrDefault("nothing", "NOTHING"));

        Assert.assertNull("FIVE", map.putIfAbsent("five","FIVE"));
        Assert.assertEquals("FIVE", map.get("five"));

        Assert.assertNull(map.remove("six"));
        Assert.assertEquals("FIVE", map.remove("five"));

        Assert.assertFalse(map.remove("seven", "SEVEN"));
        Assert.assertFalse(map.remove("four", "FIVE"));
        Assert.assertTrue(map.remove("four", "FOUR"));
        map.put("four", "FOUR");

        Assert.assertNull(map.replace("five", "FIVE"));
        Assert.assertNull(map.get("five"));
        map.put("five", "FIVE");
        Assert.assertEquals("FIVE", map.replace("five", "FIVER"));
        Assert.assertEquals("FIVER", map.get("five"));

        Assert.assertFalse(map.replace("five", "FIVE", "FIVEST"));
        Assert.assertTrue(map.replace("five", "FIVER", "FIVEST"));
        Assert.assertEquals("FIVEST", map.get("five"));

        Assert.assertEquals("fiveFIVESTx", map.compute("five", (k,v) -> k + v + "x"));
        Assert.assertEquals("fiveFIVESTx", map.get("five"));
        Assert.assertNull(map.compute("five", (k,v) -> null));
        Assert.assertFalse(map.containsKey("five"));

        Assert.assertEquals("FOUR", map.computeIfAbsent("four", (k) -> k + "x"));
        Assert.assertEquals("FOUR", map.get("four"));
        Assert.assertEquals("fivex", map.computeIfAbsent("five", (k) -> k + "x"));
        Assert.assertEquals("fivex", map.get("five"));

        Assert.assertNull(map.computeIfPresent("six", (k,v) -> k + v + "x"));
        Assert.assertNull(map.get("six"));
        Assert.assertEquals("fivefivexx", map.computeIfPresent("five", (k,v) -> k + v + "x"));
        Assert.assertEquals("fivefivexx", map.get("five"));


        Assert.assertEquals("SEVEN", map.merge("seven", "SEVEN", (v1,v2) -> v1+v2));
        Assert.assertEquals("SEVEN", map.get("seven"));
        Assert.assertEquals("SEVENELEVEN", map.merge("seven", "ELEVEN", (v1,v2) -> v1+v2));
        Assert.assertEquals("SEVENELEVEN", map.get("seven"));

        map.clear();
        Assert.assertEquals(0, map.size());
        Assert.assertTrue(map.isEmpty());

    }


    @Test
    public void test01() throws Exception {

        this.map.put(null, null);
        Assert.assertFalse(this.map.isEmpty());
        final AtomicInteger ai = new AtomicInteger(0);
        map.forEach((k,v) -> ai.incrementAndGet());
        Assert.assertEquals(1, ai.get());

        this.map.put(null, "SOMETHING");
        Assert.assertFalse(this.map.isEmpty());
        Assert.assertEquals("SOMETHING", this.map.get(null));

    }


    @Test
    public void test02() throws Exception {

        final AtomicHashMap<String,Integer> map = new AtomicHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("four", 4);
        map.put("five", 5);
        map.put("six", null);

        final AtomicHashMap<String,Integer> map2 = new AtomicHashMap<>(map);

        Assert.assertEquals(map, map2);

        final HashMap<String,Integer> map3 = new HashMap<>();
        map3.put("one", 1);
        map3.put("two", 2);
        map3.put("three", 3);
        map3.put("four", 4);
        map3.put("five", 5);
        map3.put("six", null);

        AtomicHashMap<String,Integer> map4 = new AtomicHashMap<>(map3);

        Assert.assertEquals(map, map4);
        Assert.assertEquals(map3, map4);
        Assert.assertEquals(map4, map3);

        map4.remove("five");

        Assert.assertNotEquals(map, map4);
        Assert.assertNotEquals(map3, map4);
        Assert.assertNotEquals(map4, map3);

        map4.put("five", 6);

        Assert.assertNotEquals(map, map4);
        Assert.assertNotEquals(map3, map4);
        Assert.assertNotEquals(map4, map3);

        map4.put("five", 5);

        Assert.assertEquals(map, map4);
        Assert.assertEquals(map3, map4);
        Assert.assertEquals(map4, map3);

    }


}
