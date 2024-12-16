/*
 * =========================================================================
 *
 *   Copyright (c) 2019-2024 Aquen (https://aquen.io)
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
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AtomicHashMapKeySetTest {


    @Test
    public void testKeySet() throws Exception {
        testKeySet(1);
        testKeySet(2);
        testKeySet(3);
        testKeySet(5);
        testKeySet(8);
        testKeySet(16);
        testKeySet(32);
        testKeySet(64);
        testKeySet(10000);
    }


    private void testKeySet(final int size) {

        AtomicHashMap<String,String> m = new AtomicHashMap<>();

        final KeyValue<String,String>[] kvs = TestUtils.generateStringStringKeyValues(size, 20, 0);

        for (int i = 0; i < kvs.length; i++) {
            m.put(kvs[i].getKey(), kvs[i].getValue());
        }

        final Set<String> keySet = m.keySet();
        Assertions.assertEquals(kvs.length, keySet.size());

        for (int i = 0; i < kvs.length; i++) {
            Assertions.assertTrue(keySet.contains(kvs[i].getKey()));
        }


        final int oldSize = keySet.size();
        m.put(null, "some null");
        // The keySet of a Store is not affected by modifications on that store (because it is immutable). Note this
        // is the contrary of what should happen with a Map
        Assertions.assertEquals(oldSize, keySet.size());

        testIterator(kvs, keySet);
    }



    private void testIterator(KeyValue<String,String>[] entries, final Set<String> keySet) {

        final List<KeyValue<String,String>> expectedEntries = new ArrayList<>(Arrays.asList(entries));
        expectedEntries.sort(TestUtils.HashComparator.INSTANCE);

        final List<String> expectedKeys = new ArrayList<>();
        for (final KeyValue<String,String> expectedEntry : expectedEntries) {
            expectedKeys.add(expectedEntry.getKey());
        }

        final List<String> obtainedKeys = new ArrayList<>();
        for (final String key : keySet) {
            obtainedKeys.add(key);
        }

        Assertions.assertEquals(expectedKeys, obtainedKeys);

    }

}
