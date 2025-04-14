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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AtomicHashMapReplaceTest {

    private Map<String, String> map;

    @BeforeEach
    void setUp() {
        map = new AtomicHashMap<>();
    }

    @Test
    void testReplaceKeyWithValue_KeyExists() {
        map.put("key1", "value1");

        String oldValue = map.replace("key1", "newValue");

        assertEquals("value1", oldValue, "The old value should be returned");
        assertEquals("newValue", map.get("key1"), "The value should be replaced with the new value");
    }

    @Test
    void testReplaceKeyWithValue_KeyDoesNotExist() {
        String oldValue = map.replace("key1", "newValue");

        assertNull(oldValue, "For non-existent keys, null should be returned");
        assertNull(map.get("key1"), "The map must remain unchanged for non-existent keys");
    }

    @Test
    void testReplaceKeyWithValue_NullKey() {
        map.put(null, "value1");

        String oldValue = map.replace(null, "newValue");

        assertEquals("value1", oldValue, "The old value for the null key should be returned");
        assertEquals("newValue", map.get(null), "The null key's value should be updated");
    }

    @Test
    void testReplaceKeyWithValue_NullValue() {
        map.put("key1", null);

        String oldValue = map.replace("key1", "newValue");

        assertNull(oldValue, "The old value should be null for keys with a null value");
        assertEquals("newValue", map.get("key1"), "The value should be replaced with the new value");
    }

    @Test
    void testReplaceKeyWithOldValueAndNewValue_Success() {
        map.put("key1", "value1");

        boolean replaced = map.replace("key1", "value1", "newValue");

        assertTrue(replaced, "The value should be replaced when the key and oldValue match");
        assertEquals("newValue", map.get("key1"), "The value should be updated to the newValue");
    }

    @Test
    void testReplaceKeyWithOldValueAndNewValue_Failure_WrongOldValue() {
        map.put("key1", "value1");

        boolean replaced = map.replace("key1", "wrongValue", "newValue");

        assertFalse(replaced, "The replace method should return false when the old value doesn't match");
        assertEquals("value1", map.get("key1"), "The value should not be changed");
    }

    @Test
    void testReplaceKeyWithOldValueAndNewValue_KeyDoesNotExist() {
        boolean replaced = map.replace("key1", "value1", "newValue");

        assertFalse(replaced, "The replace method should return false for non-existent keys");
        assertNull(map.get("key1"), "No value should be set for the non-existent key");
    }

    @Test
    void testReplaceKeyWithOldValueAndNewValue_NullKey() {
        map.put(null, "value1");

        boolean replaced = map.replace(null, "value1", "newValue");

        assertTrue(replaced, "Null keys should be supported and replaced when oldValue matches");
        assertEquals("newValue", map.get(null), "The value of the null key should be updated");
    }

    @Test
    void testReplaceKeyWithOldValueAndNewValue_NullValues() {
        map.put("key1", null);

        boolean replaced = map.replace("key1", null, "newValue");

        assertTrue(replaced, "Keys with null values should be replaceable using replace(key, null, newValue)");
        assertEquals("newValue", map.get("key1"), "Null value should be replaced by the newValue");
    }

    @Test
    void testReplaceAll_ApplyFunction() {
        map.put("key1", "value1");
        map.put("key2", "value2");

        map.replaceAll((key, value) -> key + "-" + value);

        assertEquals("key1-value1", map.get("key1"), "The value should be transformed by the function");
        assertEquals("key2-value2", map.get("key2"), "The value should be transformed by the function");
    }

    @Test
    void testReplaceAll_NullValues() {
        map.put("key1", null);
        map.put("key2", "value2");

        map.replaceAll((key, value) -> value == null ? "default" : key + "-" + value);

        assertEquals("default", map.get("key1"), "Null values should be transformed by the function");
        assertEquals("key2-value2", map.get("key2"), "Non-null values should be transformed by the function");
    }

    @Test
    void testReplaceAll_EmptyMap() {
        map.replaceAll((key, value) -> key + "-" + value);

        assertTrue(map.isEmpty(), "ReplaceAll on an empty map should leave it empty");
    }

    @Test
    void testReplaceKeyWithValue_LargeMap() {
        Map<Integer, String> largeMap = new HashMap<>();
        for (int i = 0; i < 1_000_000; i++) {
            largeMap.put(i, "value" + i);
        }

        largeMap.replace(500_000, "newValue");

        assertEquals("newValue", largeMap.get(500_000), "The value should be replaced for the specified key");
        assertEquals("value0", largeMap.get(0), "Other values should remain unchanged");
        assertEquals("value999999", largeMap.get(999_999), "Other values should remain unchanged");
    }

    @Test
    void testReplaceAll_LargeMap() {
        Map<Integer, String> largeMap = new HashMap<>();
        for (int i = 0; i < 1_000_000; i++) {
            largeMap.put(i, "value" + i);
        }

        largeMap.replaceAll((key, value) -> "updated_" + value);

        assertEquals("updated_value0", largeMap.get(0), "The values should be replaced with updated ones");
        assertEquals("updated_value999999", largeMap.get(999_999), "The values should be replaced with updated ones");
        assertEquals(1_000_000, largeMap.size(), "The size of the map should remain unchanged");
    }
}