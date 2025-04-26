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
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AtomicHashMapMergeTest {

    // Helper method to create a new map (can be replaced with a different Map implementation)
    private Map<String, String> createMap() {
        return new AtomicHashMap<>();
    }

    @Test
    void merge_AddsNewEntry_WhenKeyDoesNotExist() {
        Map<String, String> map = createMap();

        map.merge("key", "value", (existingValue, newValue) -> existingValue + newValue);

        assertEquals(1, map.size());
        assertEquals("value", map.get("key"));
    }

    @Test
    void merge_UpdatesExistingValue_WhenKeyAlreadyExists() {
        Map<String, String> map = createMap();
        map.put("key", "oldValue");

        map.merge("key", "newValue", (existingValue, newValue) -> existingValue + newValue);

        assertEquals(1, map.size());
        assertEquals("oldValuenewValue", map.get("key"));
    }

    @Test
    void merge_RemovesEntry_WhenFunctionReturnsNull() {
        Map<String, String> map = createMap();
        map.put("key", "existingValue");

        map.merge("key", "newValue", (existingValue, newValue) -> null);

        assertFalse(map.containsKey("key"));
        assertEquals(0, map.size());
    }

    @Test
    void merge_CreatesNewEntry_WhenValueIsNull() {
        Map<String, String> map = createMap();

        map.merge("key", "initialValue", (existingValue, newValue) -> Objects.requireNonNullElse(existingValue, "default"));

        assertEquals(1, map.size());
        assertEquals("initialValue", map.get("key"));
    }

    @Test
    void merge_ConcurrentMapBehavior_ThreadSafeExample() {
        Map<String, Integer> map = new ConcurrentHashMap<>();
        map.put("key", 10);

        map.merge("key", 5, Integer::sum);

        assertEquals(1, map.size());
        assertEquals(15, map.get("key"));
    }

    @Test
    void merge_DoesNotThrow_WhenKeyIsNull() {
        Map<String, String> map = createMap();

        assertDoesNotThrow(() -> map.merge(null, "value", (existingValue, newValue) -> "mergedValue"));
    }

    @Test
    void merge_ThrowsException_WhenFunctionIsNull() {
        Map<String, String> map = createMap();

        assertThrows(NullPointerException.class, () -> map.merge("key", "value", null));
    }

    @Test
    void merge_HandlesCollisionsCorrectly() {
        Map<String, String> map = createMap();
        map.put("key", "value1");

        map.merge("key", "value2", (existingValue, newValue) -> existingValue + "-" + newValue);

        assertEquals("value1-value2", map.get("key"));
    }

}