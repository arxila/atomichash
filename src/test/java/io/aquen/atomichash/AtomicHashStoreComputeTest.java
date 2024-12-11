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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AtomicHashStoreComputeTest {

    private AtomicHashStore<String,String> store;


    @Before
    public void initStore() {
        this.store = new AtomicHashStore<>();
    }


    @Test
    public void test00() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assert.assertEquals(0, st.size());
        Assert.assertNull(st.get(null));
        st = add(st, "one", "ONE");
        st = add(st, "one", "ONE");
        st = add(st, new String("one"), "ONE"); // Different String with same value checked on purpose
        Assert.assertEquals(1, st.size());
        st = add(st, "two", "ANOTHER VALUE");
        st = add(st, "three", "A THIRD ONE");
        Assert.assertEquals(3, st.size());
        st = add(st, "one", "ONE");
        st = add(st, "one", "ONE");
        Assert.assertEquals(3, st.size());
        st = add(st, "pOe", "ONE COLLISION");
        Assert.assertEquals(4, st.size());
        st = add(st, "q0e", "ANOTHER COLLISION");
        Assert.assertEquals(5, st.size());
        st = add(st, "pOe", "ONE COLLISION");
        Assert.assertEquals(5, st.size());
        st = add(st, "pOe", "ONE COLLISION, BUT NEW ENTRY");
        Assert.assertEquals(5, st.size());
        st = add(st, new String("q0e"), "ANOTHER COLLISION");
        Assert.assertEquals(5, st.size());
        st = remove(st, "one");
        Assert.assertEquals(4, st.size());
        st = remove(st, "three");
        Assert.assertEquals(3, st.size());
        st = add(st, "three", "A THIRD ONE");
        Assert.assertEquals(4, st.size());
        st = remove(st, "three");
        Assert.assertEquals(3, st.size());
        st = remove(st, "three");
        Assert.assertEquals(3, st.size());
        st = remove(st, "pOe");
        Assert.assertEquals(2, st.size());
        st = remove(st, "q0e");
        Assert.assertEquals(1, st.size());

    }


    @Test
    public void test01() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assert.assertEquals(0, st.size());
        st = remove(st, "one");
        st = add(st, "one", "ONE");
        Assert.assertEquals(1, st.size());
        st = remove(st, "pOe");
        Assert.assertEquals(1, st.size());

        st = add(st, "one", "ONE");
        Assert.assertEquals(1, st.size());
        st = remove(st, "one");
        Assert.assertEquals(0, st.size());

    }


    @Test
    public void test02() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assert.assertEquals(0, st.size());
        st = remove(st, null);
        st = add(st, null, null);
        Assert.assertEquals(0, st.size());
        st = remove(st, null);
        Assert.assertEquals(0, st.size());

    }


    @Test
    public void test03() throws Exception {

        final KeyValue<String,String>[] entries =
                TestUtils.generateStringStringKeyValues(1000, 30, 100);

        AtomicHashStore<String,String> st = this.store;

        for (int i = 0; i < entries.length; i++) {
            st = add(st, entries[i].getKey(), entries[i].getValue());
        }

        final int[] accesses = TestUtils.generateInts(1000, 0, entries.length);

        int pos;
        int size = st.size();
        boolean exists;
        for (int i = 0; i < accesses.length; i++) {
            pos = accesses[i];
            exists = st.containsKey(entries[pos].getKey());
            st = remove(st, entries[pos].getKey());
            if (exists) {
                size--;
            }
            Assert.assertEquals(size, st.size());
        }

    }



    private static <K,V> AtomicHashStore<K,V> add(final AtomicHashStore<K,V> store, final K key, final V value) {

        AtomicHashStore<K,V> store2, store3;

        final boolean oldContainsKey = store.containsKey(key);
        final boolean oldContainsValue = store.containsValue(value);
        final V oldValue = store.get(key);
        final int oldSize = store.size();

        if (!oldContainsKey) {
            Assert.assertNull(oldValue);
        }

        final String snap11 = PrettyPrinter.prettyPrint(store);

        final Map<String,V> m = new HashMap<>();
        store2 = store.compute(key, (k,v) -> value, (v) -> { m.put("VALUE", v);});

        Assert.assertEquals(value, m.get("VALUE"));
        Assert.assertEquals(value, store2.get(key));


        store2 = store.compute(key, (k,v) -> value);


        final String snap12 = PrettyPrinter.prettyPrint(store);
        Assert.assertEquals(snap11, snap12);

        TestUtils.validateStoreWellFormed(store2);

        final boolean newContainsKey = store2.containsKey(key);
        final boolean newContainsValue = store2.containsValue(value);
        final V newValue = store2.get(key);
        final int newSize = store2.size();

        if (oldContainsKey) {
            Assert.assertEquals(oldSize, newSize);
            if (existsEntryByReference(store, key, value)) {
                Assert.assertSame(store, store2);
                Assert.assertSame(oldValue, newValue);
            } else if (oldValue == null) {
                Assert.assertNotSame(store, store2);
            }
        }

        Assert.assertEquals(oldContainsKey, store.containsKey(key));
        Assert.assertEquals(oldContainsValue, store.containsValue(value));
        Assert.assertEquals(oldSize, store.size());
        Assert.assertSame(oldValue, store.get(key));

        Assert.assertTrue(value == null || newContainsKey);
        if (!oldContainsKey && (!oldContainsValue || oldValue == null)) {
            Assert.assertTrue(value == null || newContainsValue);
            Assert.assertEquals((oldContainsKey || value == null) ? oldSize : (oldSize + 1), newSize);
            Assert.assertSame(value, newValue);
        }

        final String snap21 = PrettyPrinter.prettyPrint(store2);

        store3 = store2.remove(key);

        final String snap22 = PrettyPrinter.prettyPrint(store2);
        Assert.assertEquals(snap21, snap22);

        TestUtils.validateStoreWellFormed(store3);

        return store2;

    }






    private static <K,V> AtomicHashStore<K,V> remove(final AtomicHashStore<K,V> store, final K key) {

        AtomicHashStore<K,V> store2, store3;

        final boolean oldContainsKey = store.containsKey(key);
        final V oldValue = store.get(key);
        final int oldSize = store.size();

        if (!oldContainsKey) {
            Assert.assertNull(oldValue);
        }

        final String snap11 = PrettyPrinter.prettyPrint(store);

        store2 = store.remove(key);

        final String snap12 = PrettyPrinter.prettyPrint(store);
        Assert.assertEquals(snap11, snap12);

        TestUtils.validateStoreWellFormed(store2);

        final boolean newContainsKey = store2.containsKey(key);
        final V newValue = store2.get(key);
        final int newSize = store2.size();

        if (!oldContainsKey) {
            Assert.assertSame(store, store2);
        }

        Assert.assertEquals(oldContainsKey, store.containsKey(key));
        Assert.assertEquals(oldSize, store.size());
        Assert.assertSame(oldValue, store.get(key));

        Assert.assertFalse(newContainsKey);
        Assert.assertEquals((!oldContainsKey) ? oldSize : (oldSize - 1), newSize);
        Assert.assertNull(newValue);

        final String snap21 = PrettyPrinter.prettyPrint(store2);

        store3 = store2.put(key, null);

        final String snap22 = PrettyPrinter.prettyPrint(store2);
        Assert.assertEquals(snap21, snap22);

        TestUtils.validateStoreWellFormed(store3);

        Assert.assertFalse(store2.containsKey(key));
        Assert.assertEquals(newSize, store2.size());
        Assert.assertNull(store2.get(key));

        Assert.assertTrue(store3.containsKey(key));
        Assert.assertEquals((oldContainsKey)? oldSize : (oldSize + 1), store3.size());
        Assert.assertNull(store3.get(key));


        return store2;

    }



    private static <K,V> boolean existsEntryByReference(final AtomicHashStore<K,V> store, final K key, final V value) {
        for (final Map.Entry<K,V> entry : store) {
            if (key == entry.getKey() && value == entry.getValue()) {
                return true;
            }
        }
        return false;
    }

}
