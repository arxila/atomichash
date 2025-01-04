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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AtomicHashStoreReadWriteTest {

    private AtomicHashStore<String,String> store;


    @BeforeEach
    public void initStore() {
        this.store = new AtomicHashStore<>();
    }


    @Test
    public void test00() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assertions.assertEquals(0, st.size());
        Assertions.assertTrue(st.isEmpty());
        Assertions.assertNull(st.get(null));
        st = add(st, "one", "ONE");
        Assertions.assertFalse(st.isEmpty());
        st = add(st, "one", "ONE");
        st = add(st, new String("one"), "ONE"); // Different String with same value checked on purpose
        Assertions.assertEquals(1, st.size());
        st = add(st, "two", "ANOTHER VALUE");
        st = add(st, "three", "A THIRD ONE");
        Assertions.assertEquals(3, st.size());
        st = add(st, "one", "ONE");
        st = add(st, "one", "ONE");
        Assertions.assertEquals(3, st.size());
        st = add(st, "pOe", "ONE COLLISION");
        Assertions.assertEquals(4, st.size());
        st = add(st, "q0e", "ANOTHER COLLISION");
        Assertions.assertEquals(5, st.size());
        st = add(st, "pOe", "ONE COLLISION");
        Assertions.assertEquals(5, st.size());
        st = add(st, "pOe", "ONE COLLISION, BUT NEW ENTRY");
        Assertions.assertEquals(5, st.size());
        st = add(st, new String("q0e"), "ANOTHER COLLISION");
        Assertions.assertEquals(5, st.size());
        st = remove(st, "one");
        Assertions.assertEquals(4, st.size());
        st = remove(st, "three");
        Assertions.assertEquals(3, st.size());
        st = add(st, "three", "A THIRD ONE");
        Assertions.assertEquals(4, st.size());
        st = remove(st, "three");
        Assertions.assertEquals(3, st.size());
        st = remove(st, "three");
        Assertions.assertEquals(3, st.size());
        st = remove(st, "pOe");
        Assertions.assertEquals(2, st.size());
        st = remove(st, "q0e");
        Assertions.assertEquals(1, st.size());

    }


    @Test
    public void test01() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assertions.assertEquals(0, st.size());
        st = remove(st, "one");
        st = add(st, "one", "ONE");
        Assertions.assertEquals(1, st.size());
        st = remove(st, "pOe");
        Assertions.assertEquals(1, st.size());

        st = add(st, "one", "ONE");
        Assertions.assertEquals(1, st.size());
        st = remove(st, "one");
        Assertions.assertEquals(0, st.size());

    }


    @Test
    public void test02() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assertions.assertEquals(0, st.size());
        st = remove(st, null);
        st = add(st, null, null);
        Assertions.assertEquals(1, st.size());
        st = remove(st, null);
        Assertions.assertEquals(0, st.size());

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
            Assertions.assertEquals(size, st.size());
        }

    }


    @Test
    public void test04() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assertions.assertTrue(st.isEmpty());
        st = add(st, "one", "ONE");
        Assertions.assertFalse(st.isEmpty());
        st = remove(st, "one");
        Assertions.assertTrue(st.isEmpty());

    }


    @Test
    public void test05() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assertions.assertTrue(st.isEmpty());
        st = st.clear();
        Assertions.assertTrue(st.isEmpty());

        st = add(st, "one", "ONE");
        st = add(st, "two", "TWO");
        st = add(st, "three", "THREE");
        st = add(st, "four", "FOUR");
        st = add(st, "five", "FIVE");

        Assertions.assertFalse(st.isEmpty());
        st = st.clear();
        Assertions.assertTrue(st.isEmpty());

    }


    @Test
    public void test06() throws Exception {

        AtomicHashStore<String,String> st = this.store;
        AtomicHashStore<String,String> st2 = null;

        TestUtils.ValueRef ref = new TestUtils.ValueRef();
        ref.b = true;

        Assertions.assertTrue(st.isEmpty());
        st = st.remove(null, (String)null);
        Assertions.assertTrue(st.isEmpty());

        st = st.remove(null, (String)null, b -> ref.b = b);
        Assertions.assertTrue(st.isEmpty());
        Assertions.assertFalse(ref.b);

        st = add(st, "one", "ONE");

        st2 = st.remove("one", "ONE");
        Assertions.assertTrue(st2.isEmpty());

        st2 = st.remove("one", "ONE", b -> ref.b = b);
        Assertions.assertTrue(st2.isEmpty());
        Assertions.assertTrue(ref.b);

        st2 = st.remove("one", "ON");
        Assertions.assertSame(st, st2);

        st2 = st.remove("one", "ON", b -> ref.b = b);
        Assertions.assertSame(st, st2);
        Assertions.assertFalse(ref.b);

        st2 = st.remove("one", (String)null);
        Assertions.assertSame(st, st2);

        st = st.put(null, "NULL");

        st2 = st.remove(null, (String)null);
        Assertions.assertEquals(2,st2.size());
        Assertions.assertSame(st, st2);

        st2 = st.remove(null, "NULL");
        Assertions.assertEquals(1,st2.size());

        st = st.put(null, null);
        Assertions.assertEquals(2,st.size());

        st2 = st.remove(null, "NULL");
        Assertions.assertEquals(2,st2.size());
        Assertions.assertSame(st, st2);

        st2 = st.remove(null, (String)null);
        Assertions.assertEquals(1,st2.size());


    }



    private static <K,V> AtomicHashStore<K,V> add(final AtomicHashStore<K,V> store, final K key, final V value) {

        AtomicHashStore<K,V> store2, store3;
        final TestUtils.ValueRef<V> ref = new TestUtils.ValueRef<>();

        final boolean oldContainsKey = store.containsKey(key);
        final boolean oldContainsValue = store.containsValue(value);
        final V oldValue = store.get(key);
        final int oldSize = store.size();

        if (!oldContainsKey) {
            Assertions.assertNull(oldValue);
        }

        final String snap11 = PrettyPrinter.prettyPrint(store);


        store2 = store.put(key, value, (v) -> ref.val = v);
        store3 = store.put(key, value);

        final String snap12 = PrettyPrinter.prettyPrint(store);
        Assertions.assertEquals(snap11, snap12);

        TestUtils.validateStoreWellFormed(store2);
        TestUtils.validateStoreWellFormed(store3);

        Assertions.assertTrue(store2.equals(store3));

        Assertions.assertEquals(oldValue, ref.val);

        final boolean newContainsKey = store2.containsKey(key);
        final boolean newContainsValue = store2.containsValue(value);
        final V newValue = store2.get(key);
        final int newSize = store2.size();

        if (oldContainsKey) {
            Assertions.assertEquals(oldSize, newSize);
            if (existsEntryByReference(store, key, value)) {
                Assertions.assertSame(store, store2);
                Assertions.assertSame(oldValue, newValue);
            } else {
                Assertions.assertNotSame(store, store2);
            }
        }

        Assertions.assertEquals(oldContainsKey, store.containsKey(key));
        Assertions.assertEquals(oldContainsValue, store.containsValue(value));
        Assertions.assertEquals(oldSize, store.size());
        Assertions.assertSame(oldValue, store.get(key));

        Assertions.assertTrue(newContainsKey);
        Assertions.assertTrue(newContainsValue);
        Assertions.assertEquals((oldContainsKey) ? oldSize : (oldSize + 1), newSize);
        Assertions.assertSame(value, newValue);

        final String snap21 = PrettyPrinter.prettyPrint(store2);

        store3 = store2.remove(key);

        final String snap22 = PrettyPrinter.prettyPrint(store2);
        Assertions.assertEquals(snap21, snap22);

        TestUtils.validateStoreWellFormed(store3);

        Assertions.assertTrue(store2.containsKey(key));
        Assertions.assertTrue(store2.containsValue(value));
        Assertions.assertEquals(newSize, store2.size());
        Assertions.assertSame(newValue, store2.get(key));

        Assertions.assertFalse(store3.containsKey(key));
        Assertions.assertEquals((oldContainsKey)? (oldSize - 1) : oldSize, store3.size());
        Assertions.assertNull(store3.get(key));


        return store2;

    }






    private static <K,V> AtomicHashStore<K,V> remove(final AtomicHashStore<K,V> store, final K key) {

        AtomicHashStore<K,V> store2, store3, store4, store5;

        final boolean oldContainsKey = store.containsKey(key);
        final V oldValue = store.get(key);
        final int oldSize = store.size();

        if (!oldContainsKey) {
            Assertions.assertNull(oldValue);
        }

        final String snap11 = PrettyPrinter.prettyPrint(store);

        store2 = store.remove(key);
        store3 = store.remove(key, oldValue);

        TestUtils.ValueRef<V> ref = new TestUtils.ValueRef();
        store4 = store.remove(key, (v) -> ref.val = v);
        if (!oldContainsKey) {
            Assertions.assertNull(ref.val);
        } else {
            Assertions.assertEquals(oldValue, ref.val);
        }

        store5 = store.remove(key, oldValue, (b) -> ref.b = b);

        Assertions.assertEquals(oldContainsKey, ref.b);

        Assertions.assertTrue(store2.equals(store3));
        Assertions.assertTrue(store2.equals(store4));
        Assertions.assertTrue(store2.equals(store5));

        final String snap12 = PrettyPrinter.prettyPrint(store);
        Assertions.assertEquals(snap11, snap12);

        TestUtils.validateStoreWellFormed(store2);
        TestUtils.validateStoreWellFormed(store3);

        final boolean newContainsKey = store2.containsKey(key);
        final V newValue = store2.get(key);
        final int newSize = store2.size();

        if (!oldContainsKey) {
            Assertions.assertSame(store, store2);
        }

        Assertions.assertEquals(oldContainsKey, store.containsKey(key));
        Assertions.assertEquals(oldSize, store.size());
        Assertions.assertSame(oldValue, store.get(key));

        Assertions.assertFalse(newContainsKey);
        Assertions.assertEquals((!oldContainsKey) ? oldSize : (oldSize - 1), newSize);
        Assertions.assertNull(newValue);

        final String snap21 = PrettyPrinter.prettyPrint(store2);

        store3 = store2.put(key, null);

        final String snap22 = PrettyPrinter.prettyPrint(store2);
        Assertions.assertEquals(snap21, snap22);

        TestUtils.validateStoreWellFormed(store3);

        Assertions.assertFalse(store2.containsKey(key));
        Assertions.assertEquals(newSize, store2.size());
        Assertions.assertNull(store2.get(key));

        Assertions.assertTrue(store3.containsKey(key));
        Assertions.assertEquals((oldContainsKey)? oldSize : (oldSize + 1), store3.size());
        Assertions.assertNull(store3.get(key));


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
