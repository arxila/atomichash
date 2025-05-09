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

public class AtomicHashStoreIterationTests {

    @Test
    void testKeySet() {
        // Empty store
        AtomicHashStore<Object, Object> emptyStore = new AtomicHashStore<>();
        assertTrue(emptyStore.keySet().isEmpty());

        // Store with 10 values
        AtomicHashStore<Object, Object> smallStore = createAtomicHashStore(10);
        assertEquals(10, smallStore.keySet().size());
        assertTrue(smallStore.keySet().containsAll(createKeys(10)));

        // Store with 100,000 values
        AtomicHashStore<Object, Object> largeStore = createAtomicHashStore(100_000);
        assertEquals(100_000, largeStore.keySet().size());
        assertTrue(largeStore.keySet().containsAll(createKeys(100_000)));
    }

    @Test
    void testValues() {
        // Empty store
        AtomicHashStore<Object, Object> emptyStore = new AtomicHashStore<>();
        assertTrue(emptyStore.values().isEmpty());

        // Store with 10 values
        AtomicHashStore<Object, Object> smallStore = createAtomicHashStore(10);
        assertEquals(10, smallStore.values().size());
        assertTrue(smallStore.values().containsAll(createValues(10)));

        // Store with 100,000 values
        AtomicHashStore<Object, Object> largeStore = createAtomicHashStore(100_000);
        assertEquals(100_000, largeStore.values().size());
    }

    @Test
    void testEntrySet() {
        // Empty store
        AtomicHashStore<Object, Object> emptyStore = new AtomicHashStore<>();
        assertTrue(emptyStore.entrySet().isEmpty());

        // Store with 10 values
        AtomicHashStore<Object, Object> smallStore = createAtomicHashStore(10);
        assertEquals(10, smallStore.entrySet().size());
        verifyEntrySetContents(smallStore, 10);

        // Store with 100,000 values
        AtomicHashStore<Object, Object> largeStore = createAtomicHashStore(100_000);
        assertEquals(100_000, largeStore.entrySet().size());
        verifyEntrySetContents(largeStore, 100_000);
    }

    private void verifyEntrySetContents(AtomicHashStore<Object, Object> store, int size) {
        Set<Map.Entry<Object, Object>> entrySet = store.entrySet();
        for (int i = 0; i < size; i++) {
            String key = "key" + i;
            String value = "value" + i;
            boolean found = entrySet.stream().anyMatch(entry -> entry.getKey().equals(key) && entry.getValue().equals(value));
            assertTrue(found, "EntrySet did not contain the expected key-value pair: " + key + " -> " + value);
        }
    }

    private AtomicHashStore<Object, Object> createAtomicHashStore(int size) {
        AtomicHashStore<Object, Object> store = new AtomicHashStore<>();
        for (int i = 0; i < size; i++) {
            store = store.put("key" + i, "value" + i);
        }
        return store;
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