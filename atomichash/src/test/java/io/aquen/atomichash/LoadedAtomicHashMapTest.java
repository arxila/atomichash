package io.aquen.atomichash;

import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoadedAtomicHashMapTest {


    @Test
    void testContainsEmptyMapSize() {

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

        // Add 2000 entries
        for (int i = 0; i < 2000; i++) {
            map.put("key" + i, "value" + i);
        }

        // Validate map size
        assertEquals(2000, map.size(), "Size should be 2000 after adding entries");

        // Validate map is not empty
        assertFalse(map.isEmpty(), "Map should not be empty after adding entries");

        // Validate get, containsKey, and containsValue for inserted keys and values
        for (int i = 0; i < 2000; i++) {
            assertEquals("value" + i, map.get("key" + i), "Get should return correct value for key" + i);
            assertTrue(map.containsKey("key" + i), "Map should contain key key" + i);
            assertTrue(map.containsValue("value" + i), "Map should contain value value" + i);
        }

        // Validate non-existence of keys and values, including null keys and values
        assertNull(map.get("nonexistentKey"), "Get should return null for a nonexistent key");
        assertFalse(map.containsKey("nonexistentKey"), "Map should not contain nonexistent key");
        assertFalse(map.containsValue("nonexistentValue"), "Map should not contain nonexistent value");
        assertFalse(map.containsKey(null), "Map should not contain null key");
        assertFalse(map.containsValue(null), "Map should not contain null value");

    }


    @Test
    void testContainsKeyAndValue() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Prepopulate the map with 2,000 key-value pairs
        for (int i = 0; i < 2000; i++) {
            map.put("key" + i, "value" + i);
        }

        // Validate that the map contains all inserted keys and values
        for (int i = 0; i < 2000; i++) {
            assertTrue(map.containsKey("key" + i), "Map should contain key key" + i);
            assertTrue(map.containsValue("value" + i), "Map should contain value value" + i);
        }

        // Initially, the map should not contain any key or value
        assertFalse(map.containsKey(null), "Empty map should not contain null key");
        assertFalse(map.containsValue(null), "Empty map should not contain null value");
        assertFalse(map.containsKey("nonexistentKey"), "Map should not contain nonexistent key");
        assertFalse(map.containsValue("nonexistentValue"), "Map should not contain nonexistent value");

        // Add null key with non-null value
        map.put(null, "value1null");
        assertTrue(map.containsKey(null), "Map should contain null key after adding it");
        assertTrue(map.containsValue("value1null"), "Map should contain value1 after adding it");
        assertFalse(map.containsValue(null), "Map should not contain null value if not explicitly added");

        // Add non-null key with null value
        map.put("key2001", null);
        assertTrue(map.containsKey("key2001"), "Map should contain key2001 after adding it with a null value");
        assertTrue(map.containsValue(null), "Map should contain null value after adding it");
        assertFalse(map.containsKey("key2002"), "Map should not contain key2002 if not added");
        assertFalse(map.containsValue("value2002"), "Map should not contain value2002 if not added");

        // Add additional key-value pair
        map.put("key2002", "value2002");
        assertTrue(map.containsKey("key2002"), "Map should contain key2002 after adding it");
        assertTrue(map.containsValue("value2002"), "Map should contain value2002 after adding it");

        // Remove key and check again
        map.remove(null);
        assertFalse(map.containsKey(null), "Map should not contain null key after removal");
        assertFalse(map.containsValue("value1null"), "Map should not contain value1 after null key removal");
    }


    @Test
    void testPutAndGetWithNullKeysAndValues() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Prepopulate the map with 2,000 key-value pairs
        for (int i = 0; i < 2000; i++) {
            map.put("key" + i, "value" + i + "fill");
        }

        // Insert a null key with a non-null value
        assertNull(map.put(null, "value1"), "Put should return null for a new key");
        assertEquals("value1", map.get(null), "Get should return the value for a null key");

        // Update a null key's value
        assertEquals("value1", map.put(null, "value2"), "Put should return the old value when updating");
        assertEquals("value2", map.get(null), "Get should return the updated value for a null key");

        // Insert a non-null key with a null value
        assertEquals("value1fill", map.put("key1", null), "Put should return null for a new key with a null value");
        assertNull(map.get("key1"), "Get should return null for a key with a null value");

        // Update the null value for an existing key
        assertNull(map.put("key1", "value1"), "Put should return the old null value");
        assertEquals("value1", map.get("key1"), "Get should return updated value for a key");

    }


    @Test
    void testRemove() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Prepopulate the map with 2,000 key-value pairs
        for (int i = 0; i < 2000; i++) {
            map.put("key" + i, "value" + i + "fill");
        }

        map.put("key1x", "value1x");
        map.put("key2x", "value2x");

        // Remove a key and check return value
        assertEquals("value1x", map.remove("key1x"), "Remove should return the value associated with the key");
        assertNull(map.get("key1x"), "Key should no longer exist after being removed");

        // Removing a non-existent key should return null
        assertNull(map.remove("key3x"), "Removing a non-existent key should return null");

        // Remove remaining keys
        assertEquals("value2x", map.remove("key2x"));
        assertFalse(map.isEmpty(), "Map should not be empty after removing all keys");
        assertEquals(2000, map.size(), "Map should still contain all the fill entries");

    }


    @Test
    void testPutAndReplaceValues() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Prepopulate the map with 2,000 key-value pairs
        for (int i = 0; i < 2000; i++) {
            map.put("key" + i, "value" + i + "fill");
        }
        assertNull(map.put("key1x", "value1x"), "Put should return null for a new key");

        // Replace the value for an existing key
        assertEquals("value1x", map.put("key1x", "value2x"), "Put should return the old value when replacing");
        assertEquals("value2x", map.get("key1x"), "Get should reflect the updated value");

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

        // Prepopulate the map with 2,000 key-value pairs
        for (int i = 0; i < 2000; i++) {
            map.put(new CollidingObject("key" + i), "value" + i + "fill");
        }

        CollidingObject key1 = new CollidingObject("key1x");
        CollidingObject key2 = new CollidingObject("key2x");
        CollidingObject key3 = new CollidingObject("key3x");
        CollidingObject key4 = new CollidingObject("key4x");

        // Insert all colliding keys
        map.put(key1, "value1");
        assertEquals(2001, map.size(), "Size should be 2001 after inserting key1");

        map.put(key2, "value2");
        assertEquals(2002, map.size(), "Size should be 2002 after inserting key2");

        map.put(key3, "value3");
        assertEquals(2003, map.size(), "Size should be 2003 after inserting key3");

        map.put(key4, "value4");
        assertEquals(2004, map.size(), "Size should be 2004 after inserting key4");

        // Ensure all keys are stored and retrievable
        assertEquals("value1", map.get(key1), "Should retrieve value associated with key1");
        assertEquals("value2", map.get(key2), "Should retrieve value associated with key2");
        assertEquals("value3", map.get(key3), "Should retrieve value associated with key3");
        assertEquals("value4", map.get(key4), "Should retrieve value associated with key4");

        // Remove one key and ensure the others remain
        map.remove(key1);
        assertEquals(2003, map.size(), "Size should be 2003 after removing key1");
        assertNull(map.get(key1), "Removed key should no longer exist");
        assertEquals("value2", map.get(key2), "Key2 should still be retrievable after collision");
        assertEquals("value3", map.get(key3), "Key3 should still be retrievable after collision");
        assertEquals("value4", map.get(key4), "Key4 should still be retrievable after collision");

        // Remove all keys one by one and validate map state
        map.remove(key2);
        assertEquals(2002, map.size(), "Size should be 2002 after removing key2");
        assertNull(map.get(key2), "Removed key should no longer exist");
        assertEquals("value3", map.get(key3), "Key3 should still be retrievable after collision");
        assertEquals("value4", map.get(key4), "Key4 should still be retrievable after collision");

        map.remove(key3);
        assertEquals(2001, map.size(), "Size should be 2001 after removing key3");
        assertNull(map.get(key3), "Removed key should no longer exist");
        assertEquals("value4", map.get(key4), "Key4 should still be retrievable after collision");

        map.remove(key4);
        assertEquals(2000, map.size(), "Size should be 2000 after removing all keys");
        assertNull(map.get(key4), "Removed key should no longer exist");

    }


    @Test
    void testGetOrDefault() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Prepopulate the map with 2,000 key-value pairs
        for (int i = 0; i < 2000; i++) {
            map.put("key" + i, "value" + i + "fill");
        }

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

        // Prepopulate the map with 2,000 key-value pairs
        for (int i = 0; i < 2000; i++) {
            map2.put("key" + i, "value" + i + "fill");
        }

        map1.put("key1", "value1");
        map1.put("key2", "value2");
        map1.put("key3x", "value3x");

        // Use putAll
        map2.putAll(map1);

        // Validate map2 contains all values from map1
        assertEquals(2001, map2.size(), "Map2 size should match map1 after putAll");
        assertEquals("value1", map2.get("key1"), "Map2 should contain key1 with correct value");
        assertEquals("value2", map2.get("key2"), "Map2 should contain key2 with correct value");
        assertEquals("value3x", map2.get("key3x"), "Map2 should contain key3x with correct value");

    }


    @Test
    void testPutAllOnLoadedMap() {
        final Map<String, String> map = new AtomicHashMap<>();

        // Prepopulate the map with 2,000 entries
        for (int i = 0; i < 2000; i++) {
            map.put("key" + i, "value" + i);
        }

        // Create a map containing 3,000 entries (some overlapping with the existing keys)
        final Map<String, String> entriesToAdd = new AtomicHashMap<>();
        for (int i = 0; i < 3000; i++) {
            entriesToAdd.put("key" + i, "newValue" + i);
        }

        // Add all entries to the existing map
        map.putAll(entriesToAdd);

        // Verify size and correctness of the map
        assertEquals(3000, map.size(), "Map size should be 3,000 after putAll operation");
        for (int i = 0; i < 3000; i++) {
            assertEquals("newValue" + i, map.get("key" + i), "Map should contain updated values for all keys");
        }
    }


    @Test
    void testPutAllOnEmptyMap() {
        final Map<String, String> map = new AtomicHashMap<>();

        // Create a map containing 3,000 entries
        final Map<String, String> entriesToAdd = new AtomicHashMap<>();
        for (int i = 0; i < 3000; i++) {
            entriesToAdd.put("key" + i, "value" + i);
        }

        // Add all entries to the empty map
        map.putAll(entriesToAdd);

        // Verify size and correctness of the map
        assertEquals(3000, map.size(), "Map size should be 3,000 after putAll operation");
        for (int i = 0; i < 3000; i++) {
            assertEquals("value" + i, map.get("key" + i), "Map should contain correct values for all keys");
        }
    }


    @Test
    void testPutIfAbsent() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Prepopulate the map with 2,000 entries
        for (int i = 0; i < 2000; i++) {
            map.put("key" + i, "value" + i);
        }

        // Add a new entry if absent
        assertNull(map.putIfAbsent("key1x", "value1x"), "Should return null for a new key");
        assertEquals("value1x", map.get("key1x"), "Key1x should be added with the correct value");

        // Test putIfAbsent for an existing key
        assertEquals("value1x", map.putIfAbsent("key1x", "value2x"), "Should return existing value when key is already present");
        assertEquals("value1x", map.get("key1x"), "Value should not be updated for an existing key");

    }


    @Test
    void testClear() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Prepopulate the map with 2,000 entries
        for (int i = 0; i < 2000; i++) {
            map.put("key" + i, "value" + i);
        }

        assertFalse(map.isEmpty(), "Map should not be empty before clear");

        // Clear the map
        map.clear();
        assertTrue(map.isEmpty(), "Map should be empty after clear");

    }


    @Test
    void testRemoveKeyValue() {

        final Map<String, String> map = new AtomicHashMap<>();

        // Prepopulate the map with 2,000 entries
        for (int i = 0; i < 2000; i++) {
            map.put("key" + i, "value" + i);
        }

        // Test remove with matching key and value
        assertTrue(map.remove("key1", "value1"), "Remove should return true for matching key and value");
        assertNull(map.get("key1"), "Key1 should be removed from the map");

        // Test remove with mismatched value
        assertFalse(map.remove("key2", "wrongValue"), "Remove should return false for mismatched key-value pair");
        assertEquals("value2", map.get("key2"), "Key2 should still exist with the correct value");
        assertEquals(1999, map.size(), "Map should still contain all the entries except for key1");

    }
}
