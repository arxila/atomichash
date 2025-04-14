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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AtomicHashStoreMergeTest {

    private AtomicHashStore<String,String> store;


    @BeforeEach
    public void initStore() {
        this.store = new AtomicHashStore<>();
    }


    @Test
    public void test00() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assertions.assertEquals(0, st.size());
        Assertions.assertNull(st.get(null));
        st = add(st, "one", "ONE");
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

    }


    @Test
    public void test01() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        Assertions.assertEquals(0, st.size());
        st = add(st, "one", "ONE");
        Assertions.assertEquals(1, st.size());

        st = add(st, "one", "ONE");
        Assertions.assertEquals(1, st.size());

    }


    @Test
    public void test02() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        try {
            st = add(st, null, null);
            Assertions.assertTrue(false);
        } catch (final NullPointerException e) {
            Assertions.assertTrue(true);
        } catch (final Throwable t) {
            Assertions.assertTrue(false);
        }

    }



    private static <K,V> AtomicHashStore<K,V> add(final AtomicHashStore<K,V> store, final K key, final V value) {

        AtomicHashStore<K,V> store2;

        final boolean oldContainsKey = store.containsKey(key);
        final V oldValue = store.get(key);

        if (!oldContainsKey) {
            Assertions.assertNull(oldValue);
        }

        final String snap11 = PrettyPrinter.print(store);

        store2 = store.merge(key, value, (oldv,newv) -> (V)((String)oldv+(String)newv));

        String expected = (oldContainsKey) ? (String) oldValue + (String) value : (String)value;

        Assertions.assertEquals(expected, store2.get(key));


        store2 = store.merge(key, value, (oldv,newv) -> (V)((String)oldv+(String)newv));


        final String snap12 = PrettyPrinter.print(store);
        Assertions.assertEquals(snap11, snap12);

        TestUtils.validate(store2);

        Assertions.assertEquals(expected, store2.get(key));

        return store2;

    }



    private static <K,V> boolean existsEntryByReference(final AtomicHashStore<K,V> store, final K key, final V value) {
        for (final Map.Entry<K,V> entry : store.entrySet()) {
            if (key == entry.getKey() && value == entry.getValue()) {
                return true;
            }
        }
        return false;
    }

}
