/*
 * =========================================================================
 *
 *   Copyright (c) 2019-2024, Aquen OSS (https://aquen.io)
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AtomicHashMapReadWriteTest {

    private AtomicHashMap<String,String> map;


    @Before
    public void initStore() {
        this.map = new AtomicHashMap<>();
    }


    @Test
    public void test00() throws Exception {

        AtomicHashMap<String,String> m = this.map;
        AtomicHashMap<String,String> m2 = new AtomicHashMap<>();
        Assert.assertTrue(m.equals(m2));
        Assert.assertTrue(m2.equals(m));

        Assert.assertEquals(0, m.size());
        Assert.assertTrue(m.isEmpty());
        Assert.assertNull(m.get(null));
        add(m, "one", "ONE");
        Assert.assertFalse(m.isEmpty());
        add(m, "one", "ONE");
        add(m, new String("one"), "ONE"); // Different String with same value checked on purpose
        Assert.assertEquals(1, m.size());
        add(m, "two", "ANOTHER VALUE");
        add(m, "three", "A THIRD ONE");
        Assert.assertEquals(3, m.size());
        add(m, "one", "ONE");
        add(m, "one", "ONE");
        Assert.assertEquals(3, m.size());
        add(m, "pOe", "ONE COLLISION");
        Assert.assertEquals(4, m.size());
        add(m, "q0e", "ANOTHER COLLISION");
        Assert.assertEquals(5, m.size());
        add(m, "pOe", "ONE COLLISION");
        Assert.assertEquals(5, m.size());
        add(m, "pOe", "ONE COLLISION, BUT NEW ENTRY");
        Assert.assertEquals(5, m.size());
        add(m, new String("q0e"), "ANOTHER COLLISION");
        Assert.assertEquals(5, m.size());
        remove(m, "one");
        Assert.assertEquals(4, m.size());
        remove(m, "three");
        Assert.assertEquals(3, m.size());
        add(m, "three", "A THIRD ONE");
        Assert.assertEquals(4, m.size());
        remove(m, "three");
        Assert.assertEquals(3, m.size());
        remove(m, "three");
        Assert.assertEquals(3, m.size());
        remove(m, "pOe");
        Assert.assertEquals(2, m.size());
        remove(m, "q0e");
        Assert.assertEquals(1, m.size());

    }


    @Test
    public void test01() throws Exception {

        AtomicHashMap<String,String> st = this.map;

        Assert.assertEquals(0, st.size());
        remove(st, "one");
        add(st, "one", "ONE");
        Assert.assertEquals(1, st.size());
        remove(st, "pOe");
        Assert.assertEquals(1, st.size());

        add(st, "one", "ONE");
        Assert.assertEquals(1, st.size());
        remove(st, "one");
        Assert.assertEquals(0, st.size());

    }


    @Test
    public void test02() throws Exception {

        AtomicHashMap<String,String> st = this.map;

        Assert.assertEquals(0, st.size());
        remove(st, null);
        add(st, null, null);
        Assert.assertEquals(1, st.size());
        remove(st, null);
        Assert.assertEquals(0, st.size());

    }


    @Test
    public void test03() throws Exception {

        final KeyValue<String,String>[] entries =
                TestUtils.generateStringStringKeyValues(1000, 30, 100);

        AtomicHashMap<String,String> st = this.map;

        for (int i = 0; i < entries.length; i++) {
            add(st, entries[i].getKey(), entries[i].getValue());
        }

        final int[] accesses = TestUtils.generateInts(1000, 0, entries.length);

        int pos;
        int size = st.size();
        boolean exists;
        for (int i = 0; i < accesses.length; i++) {
            pos = accesses[i];
            exists = st.containsKey(entries[pos].getKey());
            remove(st, entries[pos].getKey());
            if (exists) {
                size--;
            }
            Assert.assertEquals(size, st.size());
        }

    }


    @Test
    public void test04() throws Exception {

        AtomicHashMap<String,String> st = this.map;

        Assert.assertTrue(st.isEmpty());
        add(st, "one", "ONE");
        Assert.assertFalse(st.isEmpty());
        remove(st, "one");
        Assert.assertTrue(st.isEmpty());

    }


    @Test
    public void test05() throws Exception {

        AtomicHashMap<String,String> st = this.map;

        Assert.assertTrue(st.isEmpty());
        st.clear();
        Assert.assertTrue(st.isEmpty());

        add(st, "one", "ONE");
        add(st, "two", "TWO");
        add(st, "three", "THREE");
        add(st, "four", "FOUR");
        add(st, "five", "FIVE");

        Assert.assertFalse(st.isEmpty());
        st.clear();
        Assert.assertTrue(st.isEmpty());

    }



    private static <K,V> void add(final AtomicHashMap<K,V> map, final K key, final V value) {

        final boolean oldContainsKey = map.containsKey(key);
        final V oldValue = map.get(key);
        final int oldSize = map.size();

        if (!oldContainsKey) {
            Assert.assertNull(oldValue);
        }

        V x = map.put(key, value);

        Assert.assertEquals(x,oldValue);

        final boolean newContainsKey = map.containsKey(key);
        final boolean newContainsValue = map.containsValue(value);
        final V newValue = map.get(key);
        final int newSize = map.size();

        if (oldContainsKey) {
            Assert.assertEquals(oldSize, newSize);
        }

        Assert.assertTrue(newContainsKey);
        Assert.assertTrue(newContainsValue);
        Assert.assertEquals((oldContainsKey) ? oldSize : (oldSize + 1), newSize);
        Assert.assertSame(value, newValue);

        final AtomicHashMap<K,V> map2 = AtomicHashMap.copyOf(map);
        x = map2.remove(key);

        Assert.assertEquals(value, x);

        Assert.assertFalse(map2.containsKey(key));
        Assert.assertEquals(newSize - 1, map2.size());
        Assert.assertNull(map2.get(key));

    }






    private static <K,V> void remove(final AtomicHashMap<K,V> map, final K key) {

        final boolean oldContainsKey = map.containsKey(key);
        final V oldValue = map.get(key);
        final int oldSize = map.size();

        if (!oldContainsKey) {
            Assert.assertNull(oldValue);
        }

        AtomicHashMap<K,V> map2 = AtomicHashMap.copyOf(map);

        V x1 = map.remove(key);
        boolean x2 = map2.remove(key, oldValue);

        Assert.assertEquals(oldValue, x1);
        Assert.assertEquals(oldContainsKey, x2);

        Assert.assertTrue(map.equals(map2));
        Assert.assertTrue(map2.equals(map));

        final boolean newContainsKey = map.containsKey(key);
        final V newValue = map.get(key);
        final int newSize = map.size();

        Assert.assertFalse(newContainsKey);
        Assert.assertEquals((!oldContainsKey) ? oldSize : (oldSize - 1), newSize);
        Assert.assertNull(newValue);

    }


}
