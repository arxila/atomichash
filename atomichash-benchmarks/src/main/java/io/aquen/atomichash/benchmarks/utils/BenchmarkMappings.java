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

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public final class BenchmarkMappings {

    private final static String VALUES_INIT_FILENAME = "benchmark-values-init.properties";
    private final static String VALUES_MODIFIED_FILENAME = "benchmark-values-modified.properties";
    private final static String VALUES_NEW_FILENAME = "benchmark-values-new.properties";

    private final Map<String,String> initMappings;
    private final Map<String,String> modifiedMappings;
    private final Map<String,String> newMappings;


    public BenchmarkMappings(final int initMappings, final int modifiedMappings, final int newMappings) {

        super();

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final Properties initProperties = new Properties();
        final Properties modifiedProperties = new Properties();
        final Properties newProperties = new Properties();
        try {
            initProperties.load(classLoader.getResourceAsStream(VALUES_INIT_FILENAME));
            modifiedProperties.load(classLoader.getResourceAsStream(VALUES_MODIFIED_FILENAME));
            newProperties.load(classLoader.getResourceAsStream(VALUES_NEW_FILENAME));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        if (initMappings > initProperties.size()) {
            throw new IllegalArgumentException("initMappings is " + initMappings + " but total init properties are " + initProperties.size());
        }
        if (modifiedMappings > modifiedProperties.size()) {
            throw new IllegalArgumentException("modifiedMappings is " + modifiedMappings + " but total modified properties are " + modifiedProperties.size());
        }
        if (newMappings > newProperties.size()) {
            throw new IllegalArgumentException("newMappings is " + newMappings + " but total new properties are " + newProperties.size());
        }
        if (modifiedMappings > initMappings) {
            throw new IllegalArgumentException("modifiedMappings is " + modifiedMappings + " but initMappings is " + initMappings + " (must be <=)");
        }

        final KeyValue<String,String>[] allInitMappings =
                initProperties.entrySet().stream().map(e -> new KeyValue<>(e.getKey(), e.getValue())).toArray(KeyValue[]::new);
        final KeyValue<String,String>[] allModifiedMappings =
                modifiedProperties.entrySet().stream().map(e -> new KeyValue<>(e.getKey(), e.getValue())).toArray(KeyValue[]::new);
        final KeyValue<String,String>[] allNewMappings =
                newProperties.entrySet().stream().map(e -> new KeyValue<>(e.getKey(), e.getValue())).toArray(KeyValue[]::new);

        Arrays.sort(allInitMappings, Comparator.comparingInt(KeyValue::hashCode));
        Arrays.sort(allModifiedMappings, Comparator.comparingInt(KeyValue::hashCode));
        Arrays.sort(allNewMappings, Comparator.comparingInt(KeyValue::hashCode));

        this.initMappings = Arrays.stream(Arrays.copyOf(allInitMappings, initMappings)).collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));
        this.newMappings = Arrays.stream(Arrays.copyOf(allNewMappings, newMappings)).collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));

        this.modifiedMappings = new HashMap<>();
        int modifCount = 0, i = 0;
        do {
            final KeyValue<String,String> kv = allModifiedMappings[i];
            if (this.initMappings.containsKey(kv.getKey())) {
                this.modifiedMappings.put(kv.getKey(), kv.getValue());
                modifCount++;
            }
            i++;
        } while (modifCount < modifiedMappings);

    }



    public static void main(String[] args) {
        final BenchmarkMappings bm = new BenchmarkMappings(10, 4, 2);
        System.out.println(bm.initMappings);
    }

}
