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
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AtomicHashMapComputeTest {

    private Map<Object, String> map;

    @BeforeEach
    void setUp() {
        map = new AtomicHashMap<>();
    }

    // ----- Test for compute -----

    @Test
    void testCompute_KeyExists() {
        map.put("key1", "value1");

        String computedValue = map.compute("key1", (key, value) -> value + "-computed");

        assertEquals("value1-computed", computedValue, "The value should be updated based on the remapping function");
        assertEquals("value1-computed", map.get("key1"), "The remapped value should be in the map");
    }

    @Test
    void testCompute_KeyDoesNotExist() {
        String computedValue = map.compute("key1", (key, value) -> key + "-computed");

        assertEquals("key1-computed", computedValue, "A new value should be computed for the key");
        assertEquals("key1-computed", map.get("key1"), "The computed value should be saved in the map");
    }

    @Test
    void testCompute_RemoveKeyByReturningNull() {
        map.put("key1", "value1");

        String computedValue = map.compute("key1", (key, value) -> null);

        assertNull(computedValue, "The computed value is null, so the key should be removed");
        assertFalse(map.containsKey("key1"), "The key should be removed from the map");
    }

    @Test
    void testCompute_NullKey() {
        map.put(null, "value1");

        String computedValue = map.compute(null, (key, value) -> value + "-computed");

        assertEquals("value1-computed", computedValue, "The null key value should be updated");
        assertEquals("value1-computed", map.get(null), "The updated value should be associated with the null key");
    }

    // ----- Tests for computeIfAbsent -----

    @Test
    void testComputeIfAbsent_KeyDoesNotExist() {
        String value = map.computeIfAbsent("key1", key -> key + "-default");

        assertEquals("key1-default", value, "A new value should be computed for the absent key");
        assertEquals("key1-default", map.get("key1"), "The computed value should be stored in the map");
    }

    @Test
    void testComputeIfAbsent_KeyExists() {
        map.put("key1", "value1");

        String value = map.computeIfAbsent("key1", key -> "default-value");

        assertEquals("value1", value, "The value of the existing key should not be replaced");
        assertEquals("value1", map.get("key1"), "The original value should remain unchanged in the map");
    }

    @Test
    void testComputeIfAbsent_NullKey() {
        map.put("key1", "value1");

        String value = map.computeIfAbsent(null, key -> "null-default");

        assertEquals("null-default", value, "The null key should be computed and assigned");
        assertEquals("null-default", map.get(null), "The newly computed value should be stored for the null key");
    }

    // ----- Tests for computeIfPresent -----

    @Test
    void testComputeIfPresent_KeyExists() {
        map.put("key1", "value1");

        String computedValue = map.computeIfPresent("key1", (key, value) -> value + "-updated");

        assertEquals("value1-updated", computedValue, "The value should be updated using the remapping function");
        assertEquals("value1-updated", map.get("key1"), "The updated value should be present in the map");
    }

    @Test
    void testComputeIfPresent_KeyDoesNotExist() {
        String computedValue = map.computeIfPresent("key1", (key, value) -> value + "-updated");

        assertNull(computedValue, "The value should remain unchanged when the key does not exist");
        assertFalse(map.containsKey("key1"), "The key should not be present in the map");
    }

    @Test
    void testComputeIfPresent_RemoveKeyByReturningNull() {
        map.put("key1", "value1");

        String computedValue = map.computeIfPresent("key1", (key, value) -> null);

        assertNull(computedValue, "The key should be removed when the remapping function returns null");
        assertFalse(map.containsKey("key1"), "The key should no longer exist in the map");
    }

    @Test
    void testComputeIfPresent_NullKey() {
        map.put(null, "value1");

        String computedValue = map.computeIfPresent(null, (key, value) -> value + "-updated");

        assertEquals("value1-updated", computedValue, "The null key value should be updated");
        assertEquals("value1-updated", map.get(null), "The updated value should be associated with the null key");
    }

    // ----- Tests for maps with collisions -----

    static class CustomKey {
        private final String value;

        public CustomKey(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CustomKey other = (CustomKey) obj;
            return Objects.equals(this.value, other.value);
        }

        @Override
        public int hashCode() {
            return 42; // Deliberate collision: all keys will have the same hash
        }
    }

    @Test
    void testComputeWithCollision() {
        CustomKey key1 = new CustomKey("key1");
        CustomKey key2 = new CustomKey("key2");

        map.put(key1, "value1");

        String computedValue = map.compute(key2, (key, value) -> value == null ? "new-value" : value + "-updated");

        assertEquals("new-value", computedValue, "The new key with the same hash should compute its value");
        assertEquals("value1", map.get(key1), "The value of the original key should remain unchanged");
        assertEquals("new-value", map.get(key2), "The new key should have the computed value");
    }

    // ----- Test for large maps -----

    @Test
    void testComputeOnLargeMap() {
        Map<Integer, Integer> largeMap = new AtomicHashMap<>();
        for (int i = 0; i < 1_000_000; i++) {
            largeMap.put(i, i);
        }

        largeMap.compute(500_000, (key, value) -> value + 1);

        assertEquals(500_001, largeMap.get(500_000), "The value for the key should be updated");
        assertEquals(1_000_000, largeMap.size(), "The size of the map should remain unchanged");
    }

    @Test
    void testComputeWithCollisionsInLargeMap() {
        // Create a large map with collision-prone custom keys
        Map<CustomKey, String> largeMap = new AtomicHashMap<>();
        for (int i = 0; i < 50_000; i++) {
            largeMap.put(new CustomKey("key" + i), "value" + i);
        }

        CustomKey collisionKey = new CustomKey("collision");
        largeMap.put(collisionKey, "original");

        String computedValue = largeMap.compute(collisionKey, (key, value) -> value + "-updated");

        assertEquals("original-updated", computedValue, "The value for the colliding key should be updated");
        assertEquals(50_001, largeMap.size(), "The size of the map should remain correct");
    }
}