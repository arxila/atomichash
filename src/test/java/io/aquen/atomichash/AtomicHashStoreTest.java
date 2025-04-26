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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AtomicHashStoreTest {

    private AtomicHashStore<String, String> store;

    @BeforeEach
    void setUp() {
        store = new AtomicHashStore<>();
    }

    // ----- Tests for get and getOrDefault -----

    @Test
    void testGet_ExistingKey() {
        store = store.put("key1", "value1");

        String value = store.get("key1");

        assertEquals("value1", value, "The value for the existing key should be returned");
    }

    @Test
    void testGet_NonExistingKey() {
        String value = store.get("key1");

        assertNull(value, "The value for a non-existent key should be null");
    }

    @Test
    void testGetOrDefault_ExistingKey() {
        store = store.put("key1", "value1");

        String value = store.getOrDefault("key1", "default");

        assertEquals("value1", value, "The value for the existing key should be returned instead of the default");
    }

    @Test
    void testGetOrDefault_NonExistingKey() {
        String value = store.getOrDefault("key1", "default");

        assertEquals("default", value, "The default value should be returned for a non-existent key");
    }

    // ----- Tests for put -----

    @Test
    void testPut_NewKey() {
        AtomicHashStore<String, String> newStore = store.put("key1", "value1");

        assertNotSame(store, newStore, "A new instance of AtomicHashStore should be returned");
        assertNull(store.get("key1"), "The original store should remain unaffected");
        assertEquals("value1", newStore.get("key1"), "The new store should contain the added key-value pair");
    }

    @Test
    void testPut_ExistingKey() {
        store = store.put("key1", "value1");
        AtomicHashStore<String, String> newStore = store.put("key1", "newValue");

        assertNotSame(store, newStore, "A new instance of AtomicHashStore should be returned");
        assertEquals("value1", store.get("key1"), "The original store should retain the old value");
        assertEquals("newValue", newStore.get("key1"), "The new store should contain the updated value for the key");
    }

    @Test
    void testPut_NullKeyAndValue() {
        AtomicHashStore<String, String> newStore = store.put(null, null);

        assertNotSame(store, newStore, "A new instance of AtomicHashStore should be returned");
        assertNull(newStore.get(null), "The new store should allow null keys and values");
    }

    // ----- Tests for putIfAbsent -----

    @Test
    void testPutIfAbsent_KeyDoesNotExist() {
        AtomicHashStore<String, String> newStore = store.putIfAbsent("key1", "value1");

        assertNotSame(store, newStore, "A new instance of AtomicHashStore should be returned");
        assertEquals("value1", newStore.get("key1"), "The key-value pair should be added to the new store");
    }

    @Test
    void testPutIfAbsent_KeyExists() {
        store = store.put("key1", "value1");
        AtomicHashStore<String, String> newStore = store.putIfAbsent("key1", "newValue");

        assertSame(store, newStore, "The same instance should be returned when the key already exists");
        assertEquals("value1", store.get("key1"), "The value for the existing key should remain unchanged");
    }

    // ----- Tests for remove -----

    @Test
    void testRemove_KeyExists() {
        store = store.put("key1", "value1");
        AtomicHashStore<String, String> newStore = store.remove("key1");

        assertNotSame(store, newStore, "A new instance of AtomicHashStore should be returned");
        assertNull(newStore.get("key1"), "The removed key should not be present in the new store");
        assertEquals("value1", store.get("key1"), "The original store should be unaffected");
    }

    @Test
    void testRemove_KeyDoesNotExist() {
        AtomicHashStore<String, String> newStore = store.remove("key1");

        assertSame(store, newStore, "The same instance should be returned when the key does not exist");
        assertNull(store.get("key1"), "The non-existent key should not affect the original store");
    }

    @Test
    void testRemove_KeyAndValueMatch() {
        store = store.put("key1", "value1");
        AtomicHashStore<String, String> newStore = store.remove("key1", "value1");

        assertNotSame(store, newStore, "A new instance of AtomicHashStore should be returned");
        assertNull(newStore.get("key1"), "The key-value pair should be removed in the new store");
    }

    @Test
    void testRemove_KeyAndValueDoNotMatch() {
        store = store.put("key1", "value1");
        AtomicHashStore<String, String> newStore = store.remove("key1", "wrongValue");

        assertSame(store, newStore, "The same instance should be returned when the value does not match");
        assertEquals("value1", store.get("key1"), "The value for the key should remain unchanged");
    }

    // ----- Tests for putAll -----

    @Test
    void testPutAll() {
        Map<String, String> additional = new HashMap<>();
        additional.put("key1", "value1");
        additional.put("key2", "value2");

        AtomicHashStore<String, String> newStore = store.putAll(additional);

        assertNotSame(store, newStore, "A new instance of AtomicHashStore should be returned");
        assertEquals("value1", newStore.get("key1"), "The key-value pairs from the map should be merged into the new store");
        assertEquals("value2", newStore.get("key2"), "All new entries should be present in the new store");
    }

    // ----- Tests for clear -----

    @Test
    void testClear() {
        store = store.put("key1", "value1").put("key2", "value2");
        AtomicHashStore<String, String> clearedStore = store.clear();

        assertNotSame(store, clearedStore, "A new instance of AtomicHashStore should be returned");
        assertTrue(clearedStore.isEmpty(), "The cleared store should not contain any entries");
        assertFalse(store.isEmpty(), "The original store should remain unaffected");
    }

    // ----- Tests for large stores (1M entries) -----

    @Test
    void testPut_LargeStore() {
        AtomicHashStore<Integer, Integer> largeStore = new AtomicHashStore<>();
        for (int i = 0; i < 1_000_000; i++) {
            largeStore = largeStore.put(i, i);
        }

        AtomicHashStore<Integer, Integer> newStore = largeStore.put(1_000_001, 1_000_001);

        assertNotSame(largeStore, newStore, "A new store instance should be returned for large stores");
        assertEquals(1_000_001, newStore.get(1_000_001), "The new key should be successfully added to the new store");
        assertNull(largeStore.get(1_000_001), "The original store should remain unchanged");
    }

    @Test
    void testClear_LargeStore() {
        AtomicHashStore<Integer, Integer> largeStore = new AtomicHashStore<>();
        for (int i = 0; i < 1_000_000; i++) {
            largeStore = largeStore.put(i, i);
        }

        AtomicHashStore<Integer, Integer> clearedStore = largeStore.clear();

        assertNotSame(largeStore, clearedStore, "A new store instance should be returned");
        assertTrue(clearedStore.isEmpty(), "The cleared store should contain no entries");
        assertEquals(1_000_000, largeStore.size(), "The original store should remain unaffected");
    }
}