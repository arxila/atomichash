package io.aquen.atomichash;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeavyLoadAtomicHashMapTest {

    private AtomicHashMap<String, String> heavyLoadInstance() {
        return heavyLoadInstance(2_000_000);
    }

    private AtomicHashMap<String, String> heavyLoadInstance(int size) {
        final AtomicHashMap<String, String> map = new AtomicHashMap<>();
        for (int i = 0; i < size; i++) {
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

    @Test
    public void testNodeValid0() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(0);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid1() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(1);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid10() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(10);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid100() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(100);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid500() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(500);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid1000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(1000);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid5000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(5000);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid10000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(10000);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid50000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(50000);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid100000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(100000);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid500000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(500000);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid1000000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(1000000);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

    @Test
    public void testNodeValid5000000() {
        final AtomicHashMap<String, String> map = heavyLoadInstance(5000000);
        TestUtils.validateNode(map.innerRoot());
        assertTrue(true);
    }

}
