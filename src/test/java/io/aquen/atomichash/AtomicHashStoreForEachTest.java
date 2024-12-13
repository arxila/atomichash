/*
 * =========================================================================
 *
 *   Copyright (c) 2019-2024, Aquen (https://aquen.io)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AtomicHashStoreForEachTest {

    private AtomicHashStore<String,String> store;


    @BeforeEach
    public void initStore() {
        this.store = new AtomicHashStore<>();
    }


    @Test
    public void test00() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        final List<KeyValue<String,String>> vals = new ArrayList<>();
        st.forEach((k,v) -> vals.add(new KeyValue<>(k, v)));

        Assertions.assertTrue(vals.isEmpty());

        st = st.put("one", "ONE");

        st.forEach((k,v) -> vals.add(new KeyValue<>(k, v)));

        Assertions.assertEquals(1, vals.size());
        Assertions.assertTrue(vals.iterator().next().getKey().equals("one"));

        st = st.clear();

        st = st.put(null, null);

        vals.clear();
        st.forEach((k,v) -> vals.add(new KeyValue<>(k, v)));

        Assertions.assertEquals(1, vals.size());
        Assertions.assertTrue(vals.iterator().next().getKey() == null);

    }


    @Test
    public void test01() throws Exception {

        final KeyValue<String,String>[] entries =
                TestUtils.generateStringStringKeyValues(1000, 10, 0);

        final Map<String,String> entriesMap = new HashMap<>();
        for (int i = 0; i < entries.length; i++) {
            entriesMap.put(entries[i].getKey(), entries[i].getValue());
        }

        AtomicHashStore<String,String> st = this.store;

        st = st.putAll(entriesMap);

        Arrays.sort(entries, TestUtils.HashComparator.INSTANCE);

        final List<KeyValue<String,String>> iteratedKVs = new ArrayList<>();
        st.forEach((k,v) -> iteratedKVs.add(new KeyValue<>(k, v)));

        final KeyValue<String,String>[] iteratedKVsArr = iteratedKVs.toArray(new KeyValue[iteratedKVs.size()]);

        Assertions.assertTrue(Arrays.equals(entries, iteratedKVsArr));

    }



    @Test
    public void test02() throws Exception {

        AtomicHashStore<String,String> st = this.store;

        final List<KeyValue<String,String>> vals = new ArrayList<>();
        st.forEach((entry) -> vals.add(new KeyValue<>(entry.getKey(), entry.getValue())));

        Assertions.assertTrue(vals.isEmpty());

        st = st.put("one", "ONE");

        st.forEach((entry) -> vals.add(new KeyValue<>(entry.getKey(), entry.getValue())));

        Assertions.assertEquals(1, vals.size());
        Assertions.assertTrue(vals.iterator().next().getKey().equals("one"));

        st = st.clear();

        st = st.put(null, null);

        vals.clear();
        st.forEach((entry) -> vals.add(new KeyValue<>(entry.getKey(), entry.getValue())));

        Assertions.assertEquals(1, vals.size());
        Assertions.assertTrue(vals.iterator().next().getKey() == null);

    }


    @Test
    public void test03() throws Exception {

        final KeyValue<String,String>[] entries =
                TestUtils.generateStringStringKeyValues(1000, 10, 0);

        final Map<String,String> entriesMap = new HashMap<>();
        for (int i = 0; i < entries.length; i++) {
            entriesMap.put(entries[i].getKey(), entries[i].getValue());
        }

        AtomicHashStore<String,String> st = this.store;

        st = st.putAll(entriesMap);

        Arrays.sort(entries, TestUtils.HashComparator.INSTANCE);

        final List<KeyValue<String,String>> iteratedKVs = new ArrayList<>();
        st.forEach((entry) -> iteratedKVs.add(new KeyValue<>(entry.getKey(), entry.getValue())));

        final KeyValue<String,String>[] iteratedKVsArr = iteratedKVs.toArray(new KeyValue[iteratedKVs.size()]);

        Assertions.assertTrue(Arrays.equals(entries, iteratedKVsArr));

    }

}
