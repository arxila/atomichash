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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AtomicHashStoreGetAllTest {

    @Test
    void getAll_ShouldReturnEmptyMap_WhenNoKeysProvided() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of("one", 1, "two", 2);

        Map<String, Integer> result = store.getAll();

        Assertions.assertTrue(result.isEmpty(), "Result should be empty when no keys are provided.");
    }

    @Test
    void getAll_ShouldReturnEmptyMap_WhenNullKeysProvided() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of("one", 1, "two", 2);

        Map<String, Integer> result = store.getAll((Object[]) null);

        Assertions.assertTrue(result.isEmpty(), "Result should be empty when null keys are provided.");
    }

    @Test
    void getAll_ShouldReturnCorrectValues_ForExistingKeys() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of("one", 1, "two", 2, "three", 3);

        Map<String, Integer> result = store.getAll("one", "three");

        Assertions.assertEquals(2, result.size(), "Result should contain entries for the existing keys.");
        Assertions.assertEquals(1, result.get("one"));
        Assertions.assertEquals(3, result.get("three"));
    }

    @Test
    void getAll_ShouldIgnoreNonExistingKeys() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of("one", 1, "two", 2);

        Map<String, Integer> result = store.getAll("one", "three");

        Assertions.assertEquals(1, result.size(), "Result should only contain entries for existing keys.");
        Assertions.assertEquals(1, result.get("one"));
        Assertions.assertNull(result.get("three"), "Non-existing keys should not be present in the result.");
    }

    @Test
    void getAll_ShouldReturnEmptyMap_WhenAllKeysAreNonExisting() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of("one", 1, "two", 2);

        Map<String, Integer> result = store.getAll("three", "four");

        Assertions.assertTrue(result.isEmpty(), "Result should be empty when no keys match.");
    }

    @Test
    void getAll_ShouldHandleNullKey() {
        AtomicHashStore<String, Integer> store = new AtomicHashStore<>();
        store = store.put("one", 1).put(null, 100);

        Map<String, Integer> result = store.getAll("one", null);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(1, result.get("one"));
        Assertions.assertEquals(100, result.get(null), "Result should include the entry for the null key.");
    }

    @Test
    void getAll_ShouldHandleDuplicateKeys() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of("one", 1, "two", 2);

        Map<String, Integer> result = store.getAll("one", "one", "two");

        Assertions.assertEquals(2, result.size(), "Result should not include duplicate entries.");
        Assertions.assertEquals(1, result.get("one"));
        Assertions.assertEquals(2, result.get("two"));
    }
}