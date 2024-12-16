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
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AtomicHashMapValuesTest {


    @Test
    public void testValues() throws Exception {
        testValues(1);
        testValues(2);
        testValues(3);
        testValues(5);
        testValues(8);
        testValues(16);
        testValues(32);
        testValues(64);
        testValues(10000);
    }


    private void testValues(final int size) {

        AtomicHashMap<String,String> m = new AtomicHashMap<>();

        final KeyValue<String,String>[] kvs = TestUtils.generateStringStringKeyValues(size, 20, 0);

        for (int i = 0; i < kvs.length; i++) {
            m.put(kvs[i].getKey(), kvs[i].getValue());
        }

        final Collection<String> values = m.values();

        for (int i = 0; i < kvs.length; i++) {
            Assertions.assertTrue(values.contains(kvs[i].getValue()));
        }


        final int oldSize = values.size();
        m.put(null, "some null");
        // The values of a Store are not affected by modifications on that store (because it is immutable). Note this
        // is the contrary of what should happen with a Map
        Assertions.assertEquals(oldSize, values.size());

        testIterator(kvs, values);
    }



    private void testIterator(KeyValue<String,String>[] entries, final Collection<String> values) {

        final List<KeyValue<String,String>> expectedEntries = new ArrayList<>(Arrays.asList(entries));
        expectedEntries.sort(TestUtils.HashComparator.INSTANCE);

        final List<String> expectedValues = new ArrayList<>();
        for (final KeyValue<String,String> expectedEntry : expectedEntries) {
            expectedValues.add(expectedEntry.getValue());
        }

        final List<String> obtainedValues = new ArrayList<>();
        for (final String value : values) {
            obtainedValues.add(value);
        }

        Assertions.assertEquals(expectedValues, obtainedValues);

    }

}
