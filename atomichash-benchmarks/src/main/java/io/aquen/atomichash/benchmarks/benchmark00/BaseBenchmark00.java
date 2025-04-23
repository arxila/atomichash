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
package io.aquen.atomichash.benchmarks.benchmark00;


import io.aquen.atomichash.AtomicHashStore;
import io.aquen.atomichash.benchmarks.utils.KeyValue;
import io.aquen.atomichash.benchmarks.utils.TestUtils;
import java.util.HashMap;
import java.util.Map;

public class BaseBenchmark00 {

    public static final int NUM_ENTRIES = 100000;
    public static final int NUM_ACCESES = 100000;

    private final int numEntries;
    private final KeyValue<String,String>[] entriesArray;
    private final Map<String,String> entriesMap;
    private final int[] accessOrder;


    protected BaseBenchmark00() {
        super();
        this.numEntries = NUM_ENTRIES;
        this.entriesArray = TestUtils.generateStringStringKeyValues(this.numEntries,0,0);
        this.accessOrder = TestUtils.generateInts(NUM_ACCESES, 0, this.numEntries);
        this.entriesMap = new HashMap<>();
        for (int i = 0; i < this.entriesArray.length; i++) {
            this.entriesMap.put(this.entriesArray[i].getKey(), this.entriesArray[i].getValue());
        }
    }




    public String[] getAll(final AtomicHashStore<String,String> map) {

        AtomicHashStore<String,String> m = map;
        final String[] result = new String[this.accessOrder.length];

        for (int i = 0; i < this.accessOrder.length; i++) {
            result[i] = m.get(this.entriesArray[this.accessOrder[i]].getKey());
        }

        return result;

    }




    public void testGet() throws Exception{

        AtomicHashStore<String,String> map = new AtomicHashStore<>();
        map = map.putAll(this.entriesMap);

        System.out.println("All initialised. Now pausing.");

        Thread.sleep(10000L);

        System.out.println("Read operations starting.");

        for (int i = 0; i < 10000; i++) {
            getAll(map);
        }

    }





    public void testPutAll() throws Exception{

        System.out.println("All initialised. Now pausing.");

        Thread.sleep(10000L);

        System.out.println("PutAll operations starting.");

        for (int i = 0; i < 1000; i++) {
            AtomicHashStore<String,String> map = new AtomicHashStore<>();
            map = map.putAll(this.entriesMap);
        }

    }


    public void testPut() throws Exception{

        System.out.println("All initialised. Now pausing.");

        Thread.sleep(10000L);

        System.out.println("Put operations starting.");

        for (int i = 0; i < 1000; i++) {
            AtomicHashStore<String,String> map = new AtomicHashStore<>();
            for (final Map.Entry<String,String> entry : this.entriesMap.entrySet()) {
                map = map.put(entry.getKey(), entry.getValue());
            }
        }

    }

    public static void main(String[] args) throws Exception{

//        (new BaseBenchmark00()).testGet();
//        (new BaseBenchmark00()).testPut();
        (new BaseBenchmark00()).testPutAll();

    }


}
