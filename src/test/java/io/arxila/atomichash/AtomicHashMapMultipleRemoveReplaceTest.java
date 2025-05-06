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

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AtomicHashMapMultipleRemoveReplaceTest {

    private static final int INITIAL_ENTRIES = 500;

    // Helper method to initialize an AtomicHashMap with test data
    private AtomicHashMap<Integer, String> initializeMap() {
        AtomicHashMap<Integer, String> map = new AtomicHashMap<>();
        IntStream.range(0, INITIAL_ENTRIES).forEach(i -> map.put(i, "Value" + i));
        assertEquals(INITIAL_ENTRIES, map.size(), "Initial map size should match the expected value");
        TestUtils.validate(map.innerRoot());
        return map;
    }

    // Test for the `remove` method
    @Test
    public void testRemove() {
        AtomicHashMap<Integer, String> map = initializeMap();

        // Remove existing keys
        for (int i = 0; i < 100; i++) {
            assertNotNull(map.remove(i), "Removed key should have a non-null value");
            assertEquals(INITIAL_ENTRIES - (i + 1), map.size(), "Map size should decrease with each removal");
            TestUtils.validate(map.innerRoot());
        }

        // Attempt to remove already-removed keys
        for (int i = 0; i < 100; i++) {
            assertNull(map.remove(i), "Removing a non-existent key should return null");
            TestUtils.validate(map.innerRoot());
        }

        // Map integrity check
        map.forEach((key, value) -> {
            assertTrue(key >= 100, "Keys below 100 should have been removed");
            assertNotNull(value, "Values should not be null");
        });
    }

    // Test for the `replace` method
    @Test
    public void testReplace() {
        AtomicHashMap<Integer, String> map = initializeMap();

        // Replace values for existing keys
        for (int i = 0; i < 300; i++) {
            String oldValue = map.replace(i, "NewValue" + i);
            assertNotNull(oldValue, "Replacing an existing key should return the old value");
            assertEquals("Value" + i, oldValue, "Old value should match the initial one");
            assertEquals("NewValue" + i, map.get(i), "Replaced value should match the new one");
            TestUtils.validate(map.innerRoot());
        }

        // Attempt to replace non-existent keys
        for (int i = INITIAL_ENTRIES; i < INITIAL_ENTRIES + 100; i++) {
            String result = map.replace(i, "NewValue" + i);
            assertNull(result, "Replacing a non-existent key should return null");
            TestUtils.validate(map.innerRoot());
        }

        // Map integrity check
        map.forEach((key, value) -> {
            if (key < 300) {
                assertEquals("NewValue" + key, value, "Values for replaced keys should be updated");
            } else {
                assertEquals("Value" + key, value, "Values for non-replaced keys should remain unchanged");
            }
        });
    }

    // Test for combined operations (optional if required)
    @Test
    public void testCombinedOperations() {
        AtomicHashMap<Integer, String> map = initializeMap();

        // Perform a combination of removals and replacements
        for (int i = 0; i < 200; i++) {
            if (i % 2 == 0) {
                assertNotNull(map.remove(i), "Removed key should have a non-null value");
                TestUtils.validate(map.innerRoot());
            } else {
                String oldValue = map.replace(i, "UpdatedValue" + i);
                assertNotNull(oldValue, "Replacing an existing key should return the old value");
                assertEquals("Value" + i, oldValue, "Old value should match the initial one");
                TestUtils.validate(map.innerRoot());
            }
        }

        // Map integrity check
        map.forEach((key, value) -> {
            if (key % 2 == 0 && key < 200) {
                fail("Keys divisible by 2 below 200 should have been removed");
            } else if (key < 200) {
                assertEquals("UpdatedValue" + key, value, "Odd keys below 200 should have updated values");
            } else {
                assertEquals("Value" + key, value, "Keys above 200 should remain unchanged");
            }
        });

        // Verify size
        assertEquals(INITIAL_ENTRIES - 100, map.size(), "Map size should reflect the removal of even keys below 200");
    }
}