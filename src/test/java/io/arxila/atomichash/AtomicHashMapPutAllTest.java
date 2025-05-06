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

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AtomicHashMapPutAllTest {

    private AtomicHashMap<String, String> map;

    @BeforeEach
    public void initMap() {
        this.map = new AtomicHashMap<>();
    }

    @Test
    public void testPutAllSingleEntries() {
        AtomicHashMap<String, String> m = this.map;

        Assertions.assertEquals(0, m.size());
        Assertions.assertNull(m.get(null));

        putAll(m, "one", "ONE");
        putAll(m, "one", "ONE");
        putAll(m, new String("one"), "ONE"); // Different String with the same content
        Assertions.assertEquals(1, m.size(), "Should only have one unique key at this point");

        putAll(m, "two", "VALUE_TWO");
        putAll(m, "three", "VALUE_THREE");
        Assertions.assertEquals(3, m.size(), "Should contain three unique keys");

        putAll(m, "pOe", "COLLISION_ONE");
        Assertions.assertEquals(4, m.size(), "Collision should occupy separate entry");
        putAll(m, "q0e", "COLLISION_TWO");
        Assertions.assertEquals(5, m.size(), "Another collision added");

        putAll(m, "pOe", "COLLISION_ONE_NEW");
        Assertions.assertEquals(5, m.size(), "Updating an existing key should not change size");
    }

    @Test
    public void testPutAllMultipleEntries() {
        AtomicHashMap<String, String> m = this.map;

        Assertions.assertEquals(0, m.size());
        Assertions.assertNull(m.get(null));

        // Add multiple entries at once
        putAll(m, "one", "ONE", "one", "ONE", "two", "VALUE_TWO", "three", "VALUE_THREE");
        Assertions.assertEquals(3, m.size(), "Should contain three unique keys");

        putAll(m, "pOe", "COLLISION_ONE", "q0e", "COLLISION_TWO");
        Assertions.assertEquals(5, m.size(), "Two new keys added, collisions included");

        putAll(m, "three", "NEW_VALUE_THREE", "q0e", "COLLISION_TWO");
        Assertions.assertEquals(5, m.size(), "Replacing one key and updating another collision should not affect size");
    }

    @Test
    public void testPutAllWithNullValues() {
        AtomicHashMap<String, String> m = this.map;

        Assertions.assertEquals(0, m.size());

        putAll(m, null, null, "one", "ONE", "two", "TWO");

        Assertions.assertEquals(3, m.size(), "Should contain three entries, including the null key");
        Assertions.assertTrue(m.containsKey(null), "Should contain null key");
        Assertions.assertNull(m.get(null), "Null key should have a null value");
        Assertions.assertTrue(m.containsKey("one"));
        Assertions.assertTrue(m.containsKey("two"));
    }

    @Test
    public void testPutAllWithLargeDataSet() {
        // Generate a large data set
        final KeyValue<String, String>[] entries = TestUtils.generateStringStringKeyValues(10000, 30, 100);
        final Map<String, String> entriesMap = new HashMap<>();
        for (KeyValue<String, String> entry : entries) {
            entriesMap.put(entry.getKey(), entry.getValue());
        }

        AtomicHashMap<String, String> m = this.map;

        // Perform bulk putAll operation
        m.putAll(entriesMap);
        TestUtils.validate(m.innerRoot());

        // Verify all keys were added
        Assertions.assertEquals(entriesMap.size(), m.size(), "All entries should be added to the map");
        for (Map.Entry<String, String> entry : entriesMap.entrySet()) {
            Assertions.assertTrue(m.containsKey(entry.getKey()), "Map should contain key: " + entry.getKey());
            Assertions.assertEquals(entry.getValue(), m.get(entry.getKey()), "Key should map to the correct value");
        }
    }


    @SafeVarargs
    private static <K, V> void putAll(AtomicHashMap<K, V> map, Object... keyValues) {
        Map<K, V> tempMap = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            @SuppressWarnings("unchecked")
            K key = (K) keyValues[i];
            @SuppressWarnings("unchecked")
            V value = (V) keyValues[i + 1];
            tempMap.put(key, value);
        }
        map.putAll(tempMap);
        TestUtils.validate(map.innerRoot());
    }

}