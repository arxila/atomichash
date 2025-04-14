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

public class AtomicHashStoreIteratorTest {


    @Test
    public void testEntryIterator() throws Exception {
        testIterator(1);
        testIterator(2);
        testIterator(3);
        testIterator(5);
        testIterator(8);
        testIterator(16);
        testIterator(32);
        testIterator(64);
        testIterator(100000);
        testIterator(1000000);
    }


    private void testIterator(final int size) {

        AtomicHashStore<String,String> store = new AtomicHashStore<>();

        final KeyValue<String,String>[] entries = TestUtils.generateStringStringKeyValues(size, 20, 0);

        for (int i = 0; i < entries.length; i++) {
            store = store.put(entries[i].getKey(), entries[i].getValue());
        }

        final String snap11 = PrettyPrinter.print(store);

        TestUtils.validate(store);

        final Set<KeyValue<String,String>> expected = new HashSet<>(Arrays.asList(entries));

        final Set<KeyValue<String,String>> obtained = new HashSet<>();
        for (final Map.Entry<String,String> entry : store.entrySet()) {
            obtained.add(new KeyValue<>(entry.getKey(), entry.getValue()));
        }

        Assertions.assertEquals(expected, obtained);

        final String snap12 = PrettyPrinter.print(store);
        Assertions.assertEquals(snap11, snap12);

    }

}
