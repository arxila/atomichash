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

import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BaseAtomicHashMapTest {


    @Test
    void testEmptyMap() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Test size
        assertEquals(0, map.size(), "Size of empty map should be 0");

        // Test isEmpty
        assertTrue(map.isEmpty(), "Empty map should report as empty");

        // Test get
        assertNull(map.get("key"), "Getting a key from an empty map should return null");
        assertFalse(map.containsKey("key"), "Empty map should not contain key");
        assertFalse(map.containsValue("value"), "Empty map should not contain value");

        // Test remove
        assertNull(map.remove("key"), "Removing a key from an empty map should return null");

    }


    @Test
    void testSize() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Initially the map is empty
        assertEquals(0, map.size());

        // Add a key-value pair
        map.put("key1", "value1");
        assertEquals(1, map.size(), "Size should increment after put");

        // Add another key-value pair
        map.put("key2", "value2");
        assertEquals(2, map.size(), "Size should reflect the number of keys");

        // Update an existing key
        map.put("key1", "newValue");
        assertEquals(2, map.size(), "Updating an existing key should not change the size");

        // Remove a key
        map.remove("key1");
        assertEquals(1, map.size(), "Size should decrement after remove");

        // Remove all keys
        map.remove("key2");
        assertEquals(0, map.size(), "Size should be 0 after all keys are removed");

    }


    @Test
    void testContainsKeyAndValue() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Initially, the map should not contain any key or value
        assertFalse(map.containsKey(null), "Empty map should not contain null key");
        assertFalse(map.containsValue(null), "Empty map should not contain null value");
        assertFalse(map.containsKey("key1"), "Empty map should not contain any key");
        assertFalse(map.containsValue("value1"), "Empty map should not contain any value");

        // Add null key with non-null value
        map.put(null, "value1");
        assertTrue(map.containsKey(null), "Map should contain null key after adding it");
        assertTrue(map.containsValue("value1"), "Map should contain value1 after adding it");
        assertFalse(map.containsValue(null), "Map should not contain null value if not explicitly added");

        // Add non-null key with null value
        map.put("key1", null);
        assertTrue(map.containsKey("key1"), "Map should contain key1 after adding it with a null value");
        assertTrue(map.containsValue(null), "Map should contain null value after adding it");
        assertFalse(map.containsKey("key2"), "Map should not contain key2 if not added");
        assertFalse(map.containsValue("value2"), "Map should not contain value2 if not added");

        // Add additional key-value pairs
        map.put("key2", "value2");
        assertTrue(map.containsKey("key2"), "Map should contain key2 after adding it");
        assertTrue(map.containsValue("value2"), "Map should contain value2 after adding it");

        // Remove key and check again
        map.remove(null);
        assertFalse(map.containsKey(null), "Map should not contain null key after removal");
        assertFalse(map.containsValue("value1"), "Map should not contain value1 after null key removal");
    }


    @Test
    void testPutAndGetWithNullKeysAndValues() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Insert a null key with a non-null value
        assertNull(map.put(null, "value1"), "Put should return null for a new key");
        assertEquals("value1", map.get(null), "Get should return the value for a null key");

        // Update a null key's value
        assertEquals("value1", map.put(null, "value2"), "Put should return the old value when updating");
        assertEquals("value2", map.get(null), "Get should return the updated value for a null key");

        // Insert a non-null key with a null value
        assertNull(map.put("key1", null), "Put should return null for a new key with a null value");
        assertNull(map.get("key1"), "Get should return null for a key with a null value");

        // Update the null value for an existing key
        assertNull(map.put("key1", "value1"), "Put should return the old null value");
        assertEquals("value1", map.get("key1"), "Get should return updated value for a key");

    }


    @Test
    void testRemove() {

        final Map<String, String> map = new AtomicHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        // Remove a key and check return value
        assertEquals("value1", map.remove("key1"), "Remove should return the value associated with the key");
        assertNull(map.get("key1"), "Key should no longer exist after being removed");

        // Removing a non-existent key should return null
        assertNull(map.remove("key3"), "Removing a non-existent key should return null");

        // Remove remaining keys
        assertEquals("value2", map.remove("key2"));
        assertTrue(map.isEmpty(), "Map should be empty after removing all keys");

    }


    @Test
    void testPutAndReplaceValues() {
        final Map<String, String> map = new AtomicHashMap<>();
        assertNull(map.put("key1", "value1"), "Put should return null for a new key");

        // Replace the value for an existing key
        assertEquals("value1", map.put("key1", "value2"), "Put should return the old value when replacing");
        assertEquals("value2", map.get("key1"), "Get should reflect the updated value");
    }


    @Test
    void testGetNonexistentKeys() {

        final Map<String, String> map = new AtomicHashMap<>();

        map.put("key1", "value1");

        // Access a nonexistent key
        assertNull(map.get("key2"), "Getting a nonexistent key should return null");

    }


    @Test
    void testHashCollisions() {
        // Use a custom object that always returns the same hash code
        class CollidingObject {
            private final String value;

            CollidingObject(String value) {
                this.value = value;
            }

            @Override
            public int hashCode() {
                return 1; // Forces a hash collision
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null || getClass() != obj.getClass()) return false;
                CollidingObject that = (CollidingObject) obj;
                return Objects.equals(this.value, that.value);
            }

            @Override
            public String toString() {
                return this.value;
            }
        }

        final Map<CollidingObject, String> map = new AtomicHashMap<>();
        CollidingObject key1 = new CollidingObject("key1");
        CollidingObject key2 = new CollidingObject("key2");
        CollidingObject key3 = new CollidingObject("key3");
        CollidingObject key4 = new CollidingObject("key4");

        // Insert all colliding keys
        map.put(key1, "value1");
        assertEquals(1, map.size(), "Size should be 1 after inserting key1");

        map.put(key2, "value2");
        assertEquals(2, map.size(), "Size should be 2 after inserting key2");

        map.put(key3, "value3");
        assertEquals(3, map.size(), "Size should be 3 after inserting key3");

        map.put(key4, "value4");
        assertEquals(4, map.size(), "Size should be 4 after inserting key4");

        // Ensure all keys are stored and retrievable
        assertEquals("value1", map.get(key1), "Should retrieve value associated with key1");
        assertEquals("value2", map.get(key2), "Should retrieve value associated with key2");
        assertEquals("value3", map.get(key3), "Should retrieve value associated with key3");
        assertEquals("value4", map.get(key4), "Should retrieve value associated with key4");

        // Remove one key and ensure the others remain
        map.remove(key1);
        assertEquals(3, map.size(), "Size should be 3 after removing key1");
        assertNull(map.get(key1), "Removed key should no longer exist");
        assertEquals("value2", map.get(key2), "Key2 should still be retrievable after collision");
        assertEquals("value3", map.get(key3), "Key3 should still be retrievable after collision");
        assertEquals("value4", map.get(key4), "Key4 should still be retrievable after collision");

        // Remove all keys one by one and validate map state
        map.remove(key2);
        assertEquals(2, map.size(), "Size should be 2 after removing key2");
        assertNull(map.get(key2), "Removed key should no longer exist");
        assertEquals("value3", map.get(key3), "Key3 should still be retrievable after collision");
        assertEquals("value4", map.get(key4), "Key4 should still be retrievable after collision");

        map.remove(key3);
        assertEquals(1, map.size(), "Size should be 1 after removing key3");
        assertNull(map.get(key3), "Removed key should no longer exist");
        assertEquals("value4", map.get(key4), "Key4 should still be retrievable after collision");

        map.remove(key4);
        assertEquals(0, map.size(), "Size should be 0 after removing all keys");
        assertNull(map.get(key4), "Removed key should no longer exist");
        assertTrue(map.isEmpty(), "Map should be empty after removing all keys");
    }

    @Test
    void testGetOrDefault() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Test default value for nonexistent key
        assertEquals("default", map.getOrDefault("key", "default"), "Should return default value for nonexistent key");

        // Add a value and test getOrDefault
        map.put("key", "value");
        assertEquals("value", map.getOrDefault("key", "default"), "Should return value for existing key");

    }

    @Test
    void testPutAll() {

        final Map<String, String> map1 = new AtomicHashMap<>();
        final Map<String, String> map2 = new AtomicHashMap<>();

        map1.put("key1", "value1");
        map1.put("key2", "value2");

        // Use putAll
        map2.putAll(map1);

        // Validate map2 contains all values from map1
        assertEquals(2, map2.size(), "Map2 size should match map1 after putAll");
        assertEquals("value1", map2.get("key1"), "Map2 should contain key1 with correct value");
        assertEquals("value2", map2.get("key2"), "Map2 should contain key2 with correct value");

    }

    @Test
    void testPutIfAbsent() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Add a new entry if absent
        assertNull(map.putIfAbsent("key1", "value1"), "Should return null for a new key");
        assertEquals("value1", map.get("key1"), "Key1 should be added with the correct value");

        // Test putIfAbsent for an existing key
        assertEquals("value1", map.putIfAbsent("key1", "value2"), "Should return existing value when key is already present");
        assertEquals("value1", map.get("key1"), "Value should not be updated for an existing key");

    }

    @Test
    void testClear() {

        final Map<String, String> map = new AtomicHashMap<>();

        map.put("key1", "value1");
        map.put("key2", "value2");

        assertFalse(map.isEmpty(), "Map should not be empty before clear");

        // Clear the map
        map.clear();
        assertTrue(map.isEmpty(), "Map should be empty after clear");

    }

    @Test
    void testRemoveKeyValue() {

        final Map<String, String> map = new AtomicHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        // Test remove with matching key and value
        assertTrue(map.remove("key1", "value1"), "Remove should return true for matching key and value");
        assertNull(map.get("key1"), "Key1 should be removed from the map");

        // Test remove with mismatched value
        assertFalse(map.remove("key2", "wrongValue"), "Remove should return false for mismatched key-value pair");
        assertEquals("value2", map.get("key2"), "Key2 should still exist with the correct value");

    }
}
