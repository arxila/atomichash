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
import org.junit.Test;

public class AtomicHashStoreOfTest {

    private AtomicHashStore<String,String> store;


    @Test
    public void test00() throws Exception {

        final String[] keys =  new String[] { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten" };
        final Integer[] values =  new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        AtomicHashStore<String,Integer> st =
                AtomicHashStore.of();
        test(st, 0, keys, values);

        st = AtomicHashStore.of(
                keys[0], values[0]
        );
        test(st, 1, keys, values);

        st = AtomicHashStore.of(
                keys[0], values[0],
                keys[1], values[1]
        );
        test(st, 2, keys, values);

        st = AtomicHashStore.of(
                keys[0], values[0],
                keys[1], values[1],
                keys[2], values[2]
        );
        test(st, 3, keys, values);

        st = AtomicHashStore.of(
                keys[0], values[0],
                keys[1], values[1],
                keys[2], values[2],
                keys[3], values[3]
        );
        test(st, 4, keys, values);

        st = AtomicHashStore.of(
                keys[0], values[0],
                keys[1], values[1],
                keys[2], values[2],
                keys[3], values[3],
                keys[4], values[4]
        );
        test(st, 5, keys, values);

        st = AtomicHashStore.of(
                keys[0], values[0],
                keys[1], values[1],
                keys[2], values[2],
                keys[3], values[3],
                keys[4], values[4],
                keys[5], values[5]
        );
        test(st, 6, keys, values);

        st = AtomicHashStore.of(
                keys[0], values[0],
                keys[1], values[1],
                keys[2], values[2],
                keys[3], values[3],
                keys[4], values[4],
                keys[5], values[5],
                keys[6], values[6]
        );
        test(st, 7, keys, values);

        st = AtomicHashStore.of(
                keys[0], values[0],
                keys[1], values[1],
                keys[2], values[2],
                keys[3], values[3],
                keys[4], values[4],
                keys[5], values[5],
                keys[6], values[6],
                keys[7], values[7]
                );
        test(st, 8, keys, values);

        st = AtomicHashStore.of(
                keys[0], values[0],
                keys[1], values[1],
                keys[2], values[2],
                keys[3], values[3],
                keys[4], values[4],
                keys[5], values[5],
                keys[6], values[6],
                keys[7], values[7],
                keys[8], values[8]
        );
        test(st, 9, keys, values);

        st = AtomicHashStore.of(
                keys[0], values[0],
                keys[1], values[1],
                keys[2], values[2],
                keys[3], values[3],
                keys[4], values[4],
                keys[5], values[5],
                keys[6], values[6],
                keys[7], values[7],
                keys[8], values[8],
                keys[9], values[9]
        );
        test(st, 10, keys, values);

    }


    @Test
    public void test01() throws Exception {

        final String[] keys =  new String[] { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve" };
        final Integer[] values =  new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };

        for (int i = 0; i < keys.length; i++) {

            final AtomicHashStore.Entry<String,Integer>[] entries = new AtomicHashStore.Entry[i];
            for (int j = 0; j < entries.length; j++) {
                entries[j] = new DataEntry<>(keys[j], values[j]);
            }

            final AtomicHashStore<String,Integer> st = AtomicHashStore.ofEntries(entries);
            test(st, i, keys, values);

        }

    }


    @Test
    public void test02() throws Exception {

        final String[] keys =  new String[] { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve" };
        final Integer[] values =  new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };

        for (int i = 0; i < keys.length; i++) {

            final AtomicHashStore.Entry<String,Integer>[] entries = new AtomicHashStore.Entry[i];
            for (int j = 0; j < entries.length; j++) {
                entries[j] = new TestEntry<>(keys[j], values[j]);
            }

            final AtomicHashStore<String,Integer> st = AtomicHashStore.ofEntries(entries);
            test(st, i, keys, values);

        }

    }


    @Test
    public void test03() throws Exception {

        final String[] keys =  new String[] { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve" };
        final Integer[] values =  new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };

        for (int i = 0; i < keys.length; i++) {

            final AtomicHashStore.Entry<String,Integer>[] entries = new AtomicHashStore.Entry[i];
            for (int j = 0; j < entries.length; j++) {
                entries[j] = AtomicHashStore.entry(keys[j], values[j]);
            }

            final AtomicHashStore<String,Integer> st = AtomicHashStore.ofEntries(entries);
            test(st, i, keys, values);

        }

    }





    private static void test(final AtomicHashStore<String,Integer> st, final int size, final String[] keys, final Integer[] values) {

        Assert.assertEquals(size, st.size());
        for (int i = 0; i < size; i++) {
            Assert.assertEquals(values[i], st.get(keys[i]));
        }

        AtomicHashStore<String,Integer> aux = new AtomicHashStore<>();
        for (int i = 0; i < size; i++) {
            aux = aux.put(keys[i], values[i]);
        }

        Assert.assertEquals(aux, st);

    }



    private static class TestEntry<K,V> implements AtomicHashStore.Entry<K,V> {
        private final K key;
        private final V value;

        public TestEntry(final K key, final V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public Object setValue(final Object value) {
            // (Not) implemented, allowed at the java.util.Map.Entry specification
            throw new UnsupportedOperationException("Setting values is forbidden in this implementation");
        }

    }

}
