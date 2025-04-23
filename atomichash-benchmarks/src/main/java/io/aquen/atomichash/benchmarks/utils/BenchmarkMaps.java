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
package io.aquen.atomichash.benchmarks.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class BenchmarkMaps {

    // This class provides a source of clean Maps, initialised for each Benchmark execution, so that
    // the fact that a benchmark modifies the data in a Map does not influence the results of the subsequent
    // benchmarks (map modification times usually depend on the amount of data already present in the Map)

    private final boolean pool;

    private final Supplier<Map<String,String>> mapSupplier;
    private final Map<String,String>[] mapPool;
    private final int initialMapSize;
    private final BenchmarkValues values;

    private String[] mapKeys;

    final AtomicInteger m = new AtomicInteger(0);
    final AtomicInteger k = new AtomicInteger(0);


    public static BenchmarkMaps createSingleMap(int initialMapSize, final BenchmarkValues values, final Supplier<Map<String,String>> mapSupplier) {
        return new BenchmarkMaps(false, 1, initialMapSize, values, mapSupplier);
    }

    public static BenchmarkMaps createPool(int mapPoolSize, int initialMapSize,
                                    final BenchmarkValues values, final Supplier<Map<String,String>> mapSupplier) {
        return new BenchmarkMaps(true, mapPoolSize, initialMapSize, values, mapSupplier);
    }


    private BenchmarkMaps(final boolean pool, int mapPoolSize, int initialMapSize,
                         final BenchmarkValues values, final Supplier<Map<String,String>> mapSupplier) {
        super();
        this.pool = pool;
        this.initialMapSize = initialMapSize;
        this.mapPool = new Map[mapPoolSize];
        this.mapKeys = new String[initialMapSize];
        this.values = values;
        this.mapSupplier = mapSupplier;
        reset();
    }


    public void reset() {

        this.k.set(0);

        if (this.pool) {
            this.m.set(0);
            for (int i = 0; i < this.mapPool.length; i++) {
                this.mapPool[i] = this.mapSupplier.get();
            }
        } else {
            // If there is no pool, then there is only one map
            this.mapPool[0] = this.mapSupplier.get();
        }

        if (this.initialMapSize > 0) {
            final Map<String,String> entries = new HashMap<>();
            for (int j = 0; j < this.initialMapSize; j++) {
                final KeyValue<String, String> kv = this.values.produceKeyValue();
                entries.put(kv.key, kv.value);
                this.mapKeys[j] = kv.key;
            }

            this.mapPool[0].putAll(entries);

            for (int i = 1; i < this.mapPool.length; i++) {
                this.mapPool[i].putAll(entries);
            }

            final ArrayList<String> keys = new ArrayList<>(Arrays.asList(this.mapKeys));
            Collections.shuffle(keys);
            this.mapKeys = keys.toArray(this.mapKeys);
        }
    }


    public Map<String,String> getMap() {
        if (this.pool) {
            throw new IllegalStateException("Cannot get map. This is a pool");
        }
        return this.mapPool[0];
    }


    public Map<String,String> produceMap(final int mapRepeats, final int numThreads) {

        if (!this.pool) {
            throw new IllegalStateException("Cannot produce map with n repeats or threads. This is not a pool");
        }

        // We will try to send the same map to each thread once in order to have some contention. No guarantee though.
        final int m = ((this.m.getAndIncrement() / numThreads) / mapRepeats);
        if (m >= this.mapPool.length) {
            throw new IllegalStateException(
                    String.format(
                            "Your computer is too fast, or the map pool is too small. " +
                            "Benchmark required more map instances from the pool that were initialised " +
                            "(pool size = %d, and it should be at least: %d num operations in iteration / " +
                            "(%d num threads x %d num times the same map is to be repeated)). Increase the " +
                            "size of the Map pool or reduce the time for each benchmark execution so that less instances " +
                            "are needed. Map instances cannot be reused so that previous state modifications don't affect " +
                            "subsequent benchmark executions.", this.mapPool.length, numThreads));
        }
        return this.mapPool[m];

    }


    public String produceKey() {
        final int kval = (this.k.getAndIncrement() % this.initialMapSize);
        return this.mapKeys[kval];
    }

}
