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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AtomicHashMapEntrySetTest {


    @Test
    public void testEntrySet() throws Exception {
        testEntrySet(1);
        testEntrySet(2);
        testEntrySet(3);
        testEntrySet(5);
        testEntrySet(8);
        testEntrySet(16);
        testEntrySet(32);
        testEntrySet(64);
        testEntrySet(10000);
    }


    private void testEntrySet(final int size) {

        AtomicHashMap<String,String> m = new AtomicHashMap<>();

        final KeyValue<String,String>[] kvs = TestUtils.generateStringStringKeyValues(size, 20, 0);

        for (final KeyValue<String, String> kv : kvs) {
            m.put(kv.getKey(), kv.getValue());
        }

        final Set<Map.Entry<String,String>> entrySet = m.entrySet();
        Assertions.assertEquals(kvs.length, entrySet.size());

        for (final KeyValue<String, String> kv : kvs) {
            Assertions.assertTrue(entrySet.contains(kv));
        }

        final int oldSize = entrySet.size();
        m.put(null, "some null");
        // The entrySet of a Store is not affected by modifications on that store (because it is immutable). Note this
        // is the contrary of what should happen with a Map
        Assertions.assertEquals(oldSize, entrySet.size());

        testIterator(kvs, entrySet);
    }



    private void testIterator(final KeyValue<String,String>[] entries, final Set<Map.Entry<String,String>> entrySet) {

        final Set<KeyValue<String,String>> expectedEntries = new HashSet<>(Arrays.asList(entries));

        final Set<KeyValue<String,String>> obtainedEntries = new HashSet<>();
        for (final Map.Entry<String,String> entry : entrySet) {
            obtainedEntries.add(new KeyValue<>(entry.getKey(), entry.getValue()));
        }

        Assertions.assertEquals(expectedEntries, obtainedEntries);

    }



}
