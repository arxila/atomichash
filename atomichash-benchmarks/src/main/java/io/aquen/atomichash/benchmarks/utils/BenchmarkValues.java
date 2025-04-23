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

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.RandomUtils;

public final class BenchmarkValues {

    private static final int COMPONENTS_ARRAY_SIZE = 100000;
    private static final int COMPONENTS_STRING_SIZE = 20;

    private final String[] componentsArray;

    private final AtomicInteger i = new AtomicInteger(0);
    private final AtomicInteger j = new AtomicInteger(0);
    private final AtomicInteger k = new AtomicInteger(0);
    private final AtomicInteger l = new AtomicInteger(0);

    private final int inci = RandomUtils.nextInt(0, 7);
    private final int incj = RandomUtils.nextInt(0, 7);
    private final int inck = RandomUtils.nextInt(0, 7);
    private final int incl = RandomUtils.nextInt(0, 7);


    public BenchmarkValues() {
        super();
        this.componentsArray = new String[COMPONENTS_ARRAY_SIZE];
        for (int i = 0; i < this.componentsArray.length; i++) {
            this.componentsArray[i] = TestUtils.generateString(COMPONENTS_STRING_SIZE);
        }
        reset();
    }


    public void reset() {
        this.i.set(RandomUtils.nextInt(0, COMPONENTS_ARRAY_SIZE));
        this.j.set(RandomUtils.nextInt(0, COMPONENTS_ARRAY_SIZE));
        this.k.set(RandomUtils.nextInt(0, COMPONENTS_ARRAY_SIZE));
        this.l.set(RandomUtils.nextInt(0, COMPONENTS_ARRAY_SIZE));
    }



    public KeyValue<String,String> produceKeyValue() {
        final int ival = computeVal(this.i, this.inci);
        final int jval = computeVal(this.j, this.incj);
        final int kval = computeVal(this.k, this.inck);
        final int lval = computeVal(this.l, this.incl);
        final String key = this.componentsArray[ival] + this.componentsArray[jval];
        final String value = this.componentsArray[kval] + this.componentsArray[lval];
        return new KeyValue<>(key, value);
    }

    private static int computeVal(final AtomicInteger atint, final int inc) {
        return (Math.abs(atint.getAndAdd(inc)) % COMPONENTS_ARRAY_SIZE);
    }



}
