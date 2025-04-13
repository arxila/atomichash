/*
 * =========================================================================
 *
 *   Copyright (c) 2019-2025 Aquen (https://aquen.io)
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AtomicHashMapMixedTest {

    private AtomicHashMap<String,String> map;


    @BeforeEach
    public void initStore() {
        this.map = new AtomicHashMap<>();
    }


    @Test
    public void test00() throws Exception {

        Assertions.assertEquals(0, map.size());
        Assertions.assertTrue(map.isEmpty());
        Assertions.assertFalse(map.containsKey("one"));
        map.forEach((k,v) -> Assertions.assertTrue(false));

        map.put("one", "ONE");
        Assertions.assertEquals("ONE", map.get("one"));
        Assertions.assertEquals("ONE", map.putIfAbsent("one","TWO"));
        Assertions.assertEquals("ONE", map.get("one"));

        Assertions.assertFalse(map.isEmpty());

        final Map<String,String> m = new HashMap<>();
        m.put("two", "TWO");
        m.put("three", "THREE");
        m.put("four", "FOUR");

        map.putAll(m);
        Assertions.assertEquals(4, map.size());
        Assertions.assertEquals("TWO", map.get("two"));
        Assertions.assertEquals("THREE", map.get("three"));
        Assertions.assertEquals("FOUR", map.get("four"));

        Assertions.assertEquals(map.store().hashCode(), map.hashCode());

        Assertions.assertTrue(map.containsKey("one"));
        Assertions.assertFalse(map.containsKey("five"));

        map.forEach((k,v) -> Assertions.assertTrue(map.containsKey(k)));
        map.forEach((k,v) -> Assertions.assertTrue(map.containsValue(v)));
        map.forEach((k,v) -> Assertions.assertSame(map.get(k), v));

        Assertions.assertEquals("FOUR", map.getOrDefault("four", "NOTHING"));
        Assertions.assertEquals("NOTHING", map.getOrDefault("nothing", "NOTHING"));

        Assertions.assertNull(map.putIfAbsent("five","FIVE"));
        Assertions.assertEquals("FIVE", map.get("five"));

        Assertions.assertNull(map.remove("six"));
        Assertions.assertEquals("FIVE", map.remove("five"));

        Assertions.assertFalse(map.remove("seven", "SEVEN"));
        Assertions.assertFalse(map.remove("four", "FIVE"));
        Assertions.assertTrue(map.remove("four", "FOUR"));
        map.put("four", "FOUR");

        Assertions.assertNull(map.replace("five", "FIVE"));
        Assertions.assertNull(map.get("five"));
        map.put("five", "FIVE");
        Assertions.assertEquals("FIVE", map.replace("five", "FIVER"));
        Assertions.assertEquals("FIVER", map.get("five"));

        Assertions.assertFalse(map.replace("five", "FIVE", "FIVEST"));
        Assertions.assertTrue(map.replace("five", "FIVER", "FIVEST"));
        Assertions.assertEquals("FIVEST", map.get("five"));

        Assertions.assertEquals("fiveFIVESTx", map.compute("five", (k,v) -> k + v + "x"));
        Assertions.assertEquals("fiveFIVESTx", map.get("five"));
        Assertions.assertNull(map.compute("five", (k,v) -> null));
        Assertions.assertFalse(map.containsKey("five"));

        Assertions.assertEquals("FOUR", map.computeIfAbsent("four", (k) -> k + "x"));
        Assertions.assertEquals("FOUR", map.get("four"));
        Assertions.assertNull(map.computeIfAbsent("five", (k) -> k + "x"));
        Assertions.assertEquals("fivex", map.get("five"));

        Assertions.assertNull(map.computeIfPresent("six", (k,v) -> k + v + "x"));
        Assertions.assertNull(map.get("six"));
        Assertions.assertEquals("fivefivexx", map.computeIfPresent("five", (k,v) -> k + v + "x"));
        Assertions.assertEquals("fivefivexx", map.get("five"));


        Assertions.assertEquals("SEVEN", map.merge("seven", "SEVEN", (v1,v2) -> v1+v2));
        Assertions.assertEquals("SEVEN", map.get("seven"));
        Assertions.assertEquals("SEVENELEVEN", map.merge("seven", "ELEVEN", (v1,v2) -> v1+v2));
        Assertions.assertEquals("SEVENELEVEN", map.get("seven"));

        map.clear();
        Assertions.assertEquals(0, map.size());
        Assertions.assertTrue(map.isEmpty());

    }


    @Test
    public void test01() throws Exception {

        this.map.put(null, null);
        Assertions.assertFalse(this.map.isEmpty());
        final AtomicInteger ai = new AtomicInteger(0);
        map.forEach((k,v) -> ai.incrementAndGet());
        Assertions.assertEquals(1, ai.get());

        this.map.put(null, "SOMETHING");
        Assertions.assertFalse(this.map.isEmpty());
        Assertions.assertEquals("SOMETHING", this.map.get(null));

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

        Assertions.assertEquals(map, map2);

        final HashMap<String,Integer> map3 = new HashMap<>();
        map3.put("one", 1);
        map3.put("two", 2);
        map3.put("three", 3);
        map3.put("four", 4);
        map3.put("five", 5);
        map3.put("six", null);

        AtomicHashMap<String,Integer> map4 = new AtomicHashMap<>(map3);

        Assertions.assertEquals(map, map4);
        Assertions.assertEquals(map3, map4);
        Assertions.assertEquals(map4, map3);

        map4.remove("five");

        Assertions.assertNotEquals(map, map4);
        Assertions.assertNotEquals(map3, map4);
        Assertions.assertNotEquals(map4, map3);

        map4.put("five", 6);

        Assertions.assertNotEquals(map, map4);
        Assertions.assertNotEquals(map3, map4);
        Assertions.assertNotEquals(map4, map3);

        map4.put("five", 5);

        Assertions.assertEquals(map, map4);
        Assertions.assertEquals(map3, map4);
        Assertions.assertEquals(map4, map3);

    }


}
