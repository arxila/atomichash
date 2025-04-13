package io.aquen.atomichash;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HeavyLoadAtomicHashMapTest {

    private Map<String, String> heavyLoadInstance() {
        final AtomicHashMap<String, String> map = new AtomicHashMap<>();
        for (int i = 0; i < 2_000_000; i++) {
            map.put("Key" + i, "Value" + i);
        }
        return map;
    }

    @Test
    public void testGetOperation() {
        final Map<String, String> map = heavyLoadInstance();
        assertEquals("Value100000", map.get("Key100000"));
        assertNull(map.get("NonExistentKey"));
    }

    @Test
    public void testPutOperation() {
        final Map<String, String> map = heavyLoadInstance();
        map.put("NewKey", "NewValue");
        assertEquals("NewValue", map.get("NewKey"));
    }

    @Test
    public void testRemoveOperation() {
        final Map<String, String> map = heavyLoadInstance();
        assertEquals("Value1000", map.remove("Key1000"));
        assertNull(map.get("Key1000"));
    }

    @Test
    public void testContainsKeyOperation() {
        final Map<String, String> map = heavyLoadInstance();
        assertTrue(map.containsKey("Key50000"));
        assertFalse(map.containsKey("NonExistentKey"));
    }

    @Test
    public void testKeySetSize() {
        final Map<String, String> map = heavyLoadInstance();
        assertEquals(2_000_000, map.keySet().size());
    }
}
