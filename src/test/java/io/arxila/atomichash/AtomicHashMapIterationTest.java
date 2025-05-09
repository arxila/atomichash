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

import org.junit.jupiter.api.Test;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class AtomicHashMapIterationTest {

    @Test
    void testKeySet() {
        // Empty map
        AtomicHashMap<Object, Object> emptyMap = new AtomicHashMap<>();
        assertTrue(emptyMap.keySet().isEmpty());

        // Map with 10 values
        AtomicHashMap<Object, Object> smallMap = createAtomicHashMap(10);
        assertEquals(10, smallMap.keySet().size());
        assertTrue(smallMap.keySet().containsAll(createKeys(10)));

        // Map with 100,000 values
        AtomicHashMap<Object, Object> largeMap = createAtomicHashMap(100_000);
        assertEquals(100_000, largeMap.keySet().size());
        assertTrue(largeMap.keySet().containsAll(createKeys(100_000)));
    }

    @Test
    void testValues() {
        // Empty map
        AtomicHashMap<Object, Object> emptyMap = new AtomicHashMap<>();
        assertTrue(emptyMap.values().isEmpty());

        // Map with 10 values
        AtomicHashMap<Object, Object> smallMap = createAtomicHashMap(10);
        assertEquals(10, smallMap.values().size());
        assertTrue(smallMap.values().containsAll(createValues(10)));

        // Map with 100,000 values
        AtomicHashMap<Object, Object> largeMap = createAtomicHashMap(100_000);
        assertEquals(100_000, largeMap.values().size());
    }

    @Test
    void testEntrySet() {
        // Empty map
        AtomicHashMap<Object, Object> emptyMap = new AtomicHashMap<>();
        assertTrue(emptyMap.entrySet().isEmpty());

        // Map with 10 values
        AtomicHashMap<Object, Object> smallMap = createAtomicHashMap(10);
        assertEquals(10, smallMap.entrySet().size());
        verifyEntrySetContents(smallMap, 10);

        // Map with 100,000 values
        AtomicHashMap<Object, Object> largeMap = createAtomicHashMap(100_000);
        assertEquals(100_000, largeMap.entrySet().size());
        verifyEntrySetContents(largeMap, 100_000);
    }

    private void verifyEntrySetContents(AtomicHashMap<Object, Object> map, int size) {
        Set<Map.Entry<Object, Object>> entrySet = map.entrySet();
        for (int i = 0; i < size; i++) {
            String key = "key" + i;
            String value = "value" + i;
            boolean found = entrySet.stream().anyMatch(entry -> entry.getKey().equals(key) && entry.getValue().equals(value));
            assertTrue(found, "EntrySet did not contain the expected key-value pair: " + key + " -> " + value);
        }
    }

    private AtomicHashMap<Object, Object> createAtomicHashMap(int size) {
        AtomicHashMap<Object, Object> map = new AtomicHashMap<>();
        for (int i = 0; i < size; i++) {
            map.put("key" + i, "value" + i);
        }
        return map;
    }

    private List<Object> createKeys(int size) {
        List<Object> keys = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            keys.add("key" + i);
        }
        return keys;
    }

    private List<Object> createValues(int size) {
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            values.add("value" + i);
        }
        return values;
    }
}