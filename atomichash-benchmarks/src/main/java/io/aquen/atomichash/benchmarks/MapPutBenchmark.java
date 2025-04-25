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


import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.aquen.atomichash.AtomicHashMap;
import io.aquen.atomichash.benchmarks.utils.BenchmarkMappings;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@Fork(2)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MapPutBenchmark {


    @State(Scope.Benchmark)
    public static abstract class MapState {

        @Param({"10"})
        int mapInitialSize;

        final Supplier<Map<String,String>> mapSupplier;
        Map<String,String> map;
        BenchmarkMappings benchmarkMappings;

        protected MapState(final Supplier<Map<String,String>> mapSupplier) {
            super();
            this.mapSupplier = mapSupplier;
        }

        @Setup(value = Level.Iteration)
        public void initMaps() throws Exception {
            // We need to delay this until Trial (i.e. not do it in the constructor) because attributes annotated
            // with @Param are not guaranteed to have a value at State object constructor execution time.
            this.benchmarkMappings = new BenchmarkMappings(this.mapInitialSize, 0, 0);
            this.map = this.mapSupplier.get();
            this.map.putAll(this.benchmarkMappings.getInitMappings());
        }

        public Map<String,String> getMap() {
            return this.map;
        }

        public String nextKey() {
            return this.benchmarkMappings.nextKey();
        }

        public Map.Entry<String,String> nextAdditionalMapping() {
            return this.benchmarkMappings.nextAdditionalMapping();
        }

        // NOTE we are NOT using a @Setup method for providing a Map and/or a key to each operation (benchmark method
        // execution) because that would make JMH produce lots of timing requests to the system in order to subtract
        // the execution times of the setup methods from the total. This could cause wrong results as explained in
        // the Level.Invocation javadocs.

    }

    public static class AtomicHashMapState extends MapState {
        public AtomicHashMapState() {
            super(AtomicHashMap::new);
        }
    }

    public static class ConcurrentHashMapState extends MapState {
        public ConcurrentHashMapState() {
            super(ConcurrentHashMap::new);
        }
    }

    public static class HashMapState extends MapState {
        public HashMapState() {
            super(HashMap::new);
        }
    }

    public static class SynchronizedMapState extends MapState {
        public SynchronizedMapState() {
            super(() -> Collections.synchronizedMap(new HashMap<>()));
        }
    }

    public static class LinkedHashMapState extends MapState {
        public LinkedHashMapState() {
            super(LinkedHashMap::new);
        }
    }



    private static String doAtomicGet(final MapState state) {
        final Map<String,String> map = state.getMap();
        final String key = state.nextKey();
        return map.get(key);
    }

    private static String doSynchronizedGet(final MapState state) {
        final Map<String,String> map = state.getMap();
        final String key = state.nextKey();
        synchronized (map) {
            return map.get(key);
        }
    }

    private static String doControlGet(final MapState state) {
        return state.nextKey();
    }

    private static String doAtomicPut(final MapState state) {
        final Map<String,String> map = state.getMap();
        final Map.Entry<String,String> entry = state.nextAdditionalMapping();
        return map.put(entry.getKey(), entry.getValue());
    }

    private static String doSynchronizedPut(final MapState state) {
        final Map<String,String> map = state.getMap();
        final Map.Entry<String,String> entry = state.nextAdditionalMapping();
        synchronized (map) {
            return map.put(entry.getKey(), entry.getValue());
        }
    }

    private static Map.Entry<String,String> doControlPut(final MapState state) {
        return state.nextAdditionalMapping();
    }




    @Benchmark @Group("atomic") @GroupThreads(16)
    public String t10_atomicHashMapGet(final AtomicHashMapState state) {
        return doAtomicGet(state);
    }
    @Benchmark @Group("atomic") @GroupThreads(4)
    public String t10_atomicHashMapPut(final AtomicHashMapState state) {
        return doAtomicPut(state);
    }

    @Benchmark @Group("concurrent") @GroupThreads(16)
    public String t10_concurrentHashMapGet(final ConcurrentHashMapState state) {
        return doAtomicGet(state);
    }
    @Benchmark @Group("concurrent") @GroupThreads(4)
    public String t10_concurrentHashMapPut(final ConcurrentHashMapState state) {
        return doAtomicPut(state);
    }

    @Benchmark @Group("control") @GroupThreads(16)
    public String t10_controlHashMapGet(final ConcurrentHashMapState state) {
        return doControlGet(state);
    }
    @Benchmark @Group("control") @GroupThreads(4)
    public Map.Entry<String,String> t10_controlHashMapPut(final ConcurrentHashMapState state) {
        return doControlPut(state);
    }

/*
    @Benchmark @Threads(4)
    public String t4_concurrentHashMap(final ConcurrentHashMapState state) {
        return doAtomicGet(state);
    }


    @Benchmark @Threads(4)
    public String t4_hashMap(final HashMapState state) {
        return doSynchronizedGet(state);
    }


    @Benchmark @Threads(4)
    public String t4_synchronizedMap(final SynchronizedMapState state) {
        return doAtomicGet(state);
    }


    @Benchmark
    public String t1_linkedHashMap(final LinkedHashMapState state) {
        return doSynchronizedGet(state);
    }
    @Benchmark @Threads(2)
    public String t2_linkedHashMap(final LinkedHashMapState state) {
        return doSynchronizedGet(state);
    }
    @Benchmark @Threads(4)
    public String t4_linkedHashMap(final LinkedHashMapState state) {
        return doSynchronizedGet(state);
    }
*/


}
