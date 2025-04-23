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


import io.aquen.atomichash.AtomicHashMap;
import io.aquen.atomichash.benchmarks.utils.BenchmarkMaps;
import io.aquen.atomichash.benchmarks.utils.BenchmarkValues;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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
import org.openjdk.jmh.infra.Blackhole;

@Fork(2)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class GetBenchmark {


    @State(Scope.Benchmark)
    public static abstract class MapState {

        final Supplier<Map<String,String>> mapSupplier;

        @Param({"10", "100", "1000"}) int mapInitialSize;
        BenchmarkMaps benchmarkMaps;

        protected MapState(final Supplier<Map<String,String>> mapSupplier) {
            super();
            this.mapSupplier = mapSupplier;
        }

        @Setup(value = Level.Trial)
        public void initMaps() throws Exception {
            // We need to delay this until Trial (i.e. not do it in the constructor) because attributes annotated
            // with @Param are not guaranteed to have a value at State object constructor execution time.
            this.benchmarkMaps = BenchmarkMaps.createSingleMap(this.mapInitialSize, new BenchmarkValues(), this.mapSupplier);
        }

        @Setup(value = Level.Iteration)
        public void resetMaps() throws Exception {
            this.benchmarkMaps.reset();
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



    // We need the blackhole to consume state data in all benchmarks in order being able to subtract the control
    // times, as we need the benchmark methods to contain ALL the code in the control.

    private static String doAtomicGet(final Blackhole blackhole, final MapState state) {
        final Map<String,String> map = state.benchmarkMaps.getMap();
        final String key = state.benchmarkMaps.produceKey();
        final String result = map.get(key);
        blackhole.consume(map);
        blackhole.consume(key);
        return result;
    }

    private static String doSynchronizedGet(final Blackhole blackhole, final MapState state) {
        final Map<String,String> map = state.benchmarkMaps.getMap();
        final String key = state.benchmarkMaps.produceKey();
        final String result;
        synchronized (map) {
            result = map.get(key);
        }
        blackhole.consume(map);
        blackhole.consume(key);
        return result;
    }

    private static void doControl(final Blackhole blackhole, final MapState state) {
        // This control will allow us to subtract the time taken by the execution of the parts of the
        // benchmarks that were not what we were actually measuring, and could not be moved to a @Setup
        // method in the @State class because invocation-level setup methods are discouraged.
        // NOTE we are using a HashMapState for this purpose but we could be using any other one as
        // the code being executed here is exactly the same for all State classes.
        final Map<String,String> map = state.benchmarkMaps.getMap();
        final String key = state.benchmarkMaps.produceKey();
        blackhole.consume(map);
        blackhole.consume(key);
    }




    @Benchmark
    public String t1_atomicHashMap(final Blackhole blackhole, final AtomicHashMapState state) {
        return doAtomicGet(blackhole, state);
    }
    @Benchmark @Threads(2)
    public String t2_atomicHashMap(final Blackhole blackhole, final AtomicHashMapState state) {
        return doAtomicGet(blackhole, state);
    }
    @Benchmark @Threads(4)
    public String t4_atomicHashMap(final Blackhole blackhole, final AtomicHashMapState state) {
        return doAtomicGet(blackhole, state);
    }


    @Benchmark
    public String t1_concurrentHashMap(final Blackhole blackhole, final ConcurrentHashMapState state) {
        return doAtomicGet(blackhole, state);
    }
    @Benchmark @Threads(2)
    public String t2_concurrentHashMap(final Blackhole blackhole, final ConcurrentHashMapState state) {
        return doAtomicGet(blackhole, state);
    }
    @Benchmark @Threads(4)
    public String t4_concurrentHashMap(final Blackhole blackhole, final ConcurrentHashMapState state) {
        return doAtomicGet(blackhole, state);
    }


    @Benchmark
    public String t1_hashMap(final Blackhole blackhole, final HashMapState state) {
        return doSynchronizedGet(blackhole, state);
    }
    @Benchmark @Threads(2)
    public String t2_hashMap(final Blackhole blackhole, final HashMapState state) {
        return doSynchronizedGet(blackhole, state);
    }
    @Benchmark @Threads(4)
    public String t4_hashMap(final Blackhole blackhole, final HashMapState state) {
        return doSynchronizedGet(blackhole, state);
    }


    @Benchmark
    public String t1_synchronizedMap(final Blackhole blackhole, final SynchronizedMapState state) {
        return doAtomicGet(blackhole, state);
    }
    @Benchmark @Threads(2)
    public String t2_synchronizedMap(final Blackhole blackhole, final SynchronizedMapState state) {
        return doAtomicGet(blackhole, state);
    }
    @Benchmark @Threads(4)
    public String t4_synchronizedMap(final Blackhole blackhole, final SynchronizedMapState state) {
        return doAtomicGet(blackhole, state);
    }


    @Benchmark
    public String t1_linkedHashMap(final Blackhole blackhole, final LinkedHashMapState state) {
        return doSynchronizedGet(blackhole, state);
    }
    @Benchmark @Threads(2)
    public String t2_linkedHashMap(final Blackhole blackhole, final LinkedHashMapState state) {
        return doSynchronizedGet(blackhole, state);
    }
    @Benchmark @Threads(4)
    public String t4_linkedHashMap(final Blackhole blackhole, final LinkedHashMapState state) {
        return doSynchronizedGet(blackhole, state);
    }


    // NOTE it makes sense to also have one benchmark control method for each number of threads being tested
    // because control code accesses at least one type of shared resource (AtomicIntegers for counters), and so
    // control code might be affected by contention in multi-threaded environments. This effect has to be
    // measured in order to have a more accurate control time.

    @Benchmark
    public void t1_control(final Blackhole blackhole, final HashMapState state) {
        doControl(blackhole, state);
    }
    @Benchmark @Threads(2)
    public void t2_control(final Blackhole blackhole, final HashMapState state) {
        doControl(blackhole, state);
    }
    @Benchmark @Threads(4)
    public void t4_control(final Blackhole blackhole, final HashMapState state) {
        doControl(blackhole, state);
    }



}
