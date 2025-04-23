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
package io.aquen.atomichash.benchmarks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.aquen.atomichash.benchmarks.utils.KeyValue;

public class Threads1ExpectedOutputTest {


    private static String[] buildExpectedOutput(final KeyValue<String,String>[] entries, final int[] accessOrder) {
        final String[] result = new String[accessOrder.length];
        for (int i = 0; i < accessOrder.length; i++) {
            result[i] = entries[accessOrder[i]].getValue();
        }
        return result;
    }


//    @Test
//    public void testAtomicHashStore() throws Exception {
//        final AtomicHashStoreBenchmark bench = new AtomicHashStoreBenchmark();
//        bench.setup();
//        final String[] expectedOutput = buildExpectedOutput(bench.getEntries(), bench.getAccessOrder());
//        Assert.assertArrayEquals(expectedOutput, bench.benchmark());
//    }
//
//
//    @Test
//    public void testConcurrentHashMap() throws Exception {
//        final ConcurrentHashMapBenchmark bench = new ConcurrentHashMapBenchmark();
//        bench.setup();
//        final String[] expectedOutput = buildExpectedOutput(bench.getEntries(), bench.getAccessOrder());
//        Assert.assertArrayEquals(expectedOutput, bench.benchmark());
//    }
//
//
//    @Test
//    public void testHashMap() throws Exception {
//        final HashMapBenchmark bench = new HashMapBenchmark();
//        bench.setup();
//        final String[] expectedOutput = buildExpectedOutput(bench.getEntries(), bench.getAccessOrder());
//        Assert.assertArrayEquals(expectedOutput, bench.benchmark());
//    }
//
//
//    @Test
//    public void testLinkedHashMap() throws Exception {
//        final LinkedHashMapBenchmark bench = new LinkedHashMapBenchmark();
//        bench.setup();
//        final String[] expectedOutput = buildExpectedOutput(bench.getEntries(), bench.getAccessOrder());
//        Assert.assertArrayEquals(expectedOutput, bench.benchmark());
//    }
//
//
//    @Test
//    public void testTreeMap() throws Exception {
//        final TreeMapBenchmark bench = new TreeMapBenchmark();
//        bench.setup();
//        final String[] expectedOutput = buildExpectedOutput(bench.getEntries(), bench.getAccessOrder());
//        Assert.assertArrayEquals(expectedOutput, bench.benchmark());
//    }
//
//



}
