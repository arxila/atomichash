/*
 * =========================================================================
 *
 *   Copyright (c) 2019-2025 Arxila OSS (https://arxila.io)
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
package io.arxila.atomichash;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeavyLoadAtomicHashMapTest {

    private AtomicHashMap<String, String> heavyLoadInstance() {
        return heavyLoadInstance(2_000_000);
    }

    private AtomicHashMap<String, String> heavyLoadInstance(int size) {
        final AtomicHashMap<String, String> map = new AtomicHashMap<>();
        for (int i = 0; i < size; i++) {
            map.put("Key" + i, "Value" + i);
        }
        return map;
    }

    @Test
    public void testGetOperation() {
        final Map<String, String> map = heavyLoadInstance();
        assertEquals("Value100000", map.get("Key100000"));
        assertNull(map.get("NonExistentKey"));
    }

    @Test
    public void testPutOperation() {
        final Map<String, String> map = heavyLoadInstance();
        map.put("NewKey", "NewValue");
        assertEquals("NewValue", map.get("NewKey"));
    }

    @Test
    public void testRemoveOperation() {
        final Map<String, String> map = heavyLoadInstance();
        assertEquals("Value1000", map.remove("Key1000"));
        assertNull(map.get("Key1000"));
    }

    @Test
    public void testContainsKeyOperation() {
        final Map<String, String> map = heavyLoadInstance();
        assertTrue(map.containsKey("Key50000"));
        assertFalse(map.containsKey("NonExistentKey"));
    }

    @Test
    public void testKeySetSize() {
        final Map<String, String> map = heavyLoadInstance();
        assertEquals(2_000_000, map.keySet().size());
    }

    @Test
    public void testNodeValid0() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(0);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid1() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(1);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid10() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(10);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid100() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(100);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid500() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(500);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid1000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(1000);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid5000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(5000);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid10000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(10000);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid50000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(50000);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid100000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(100000);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid500000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(500000);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid1000000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(1000000);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid5000000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(5000000);
        TestUtils.validate(map.innerRoot());
        assertTrue(true);
    }

}
