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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class BenchmarkMappings {

    private final static String VALUES_INIT_FILENAME = "benchmark-values-init.properties";
    private final static String VALUES_MODIFIED_FILENAME = "benchmark-values-modified.properties";
    private final static String VALUES_NEW_FILENAME = "benchmark-values-new.properties";

    private final String[] initKeys;
    private final String[] additionalKeys;
    private AtomicInteger keyIndex = new AtomicInteger(0);
    private AtomicInteger additionalKeyIndex = new AtomicInteger(0);
    private final Map<String,String> initMappings;
    private final Map<String,String> additionalMappings;




    public BenchmarkMappings(final int initMappingsCount, final int modifiedMappingsCount, final int newMappingsCount) {

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

        if (initMappingsCount > initProperties.size()) {
            throw new IllegalArgumentException("initMappings is " + initMappingsCount + " but total init properties are " + initProperties.size());
        }
        if (modifiedMappingsCount > modifiedProperties.size()) {
            throw new IllegalArgumentException("modifiedMappings is " + modifiedMappingsCount + " but total modified properties are " + modifiedProperties.size());
        }
        if (newMappingsCount > newProperties.size()) {
            throw new IllegalArgumentException("newMappings is " + newMappingsCount + " but total new properties are " + newProperties.size());
        }
        if (modifiedMappingsCount > initMappingsCount) {
            throw new IllegalArgumentException("modifiedMappings is " + modifiedMappingsCount + " but initMappings is " + initMappingsCount + " (must be <=)");
        }

        final KeyValue<String,String>[] allInitMappings =
                initProperties.entrySet().stream().map(e -> new KeyValue<>(e.getKey(), e.getValue())).toArray(KeyValue[]::new);
        final KeyValue<String,String>[] allModifiedMappings =
                modifiedProperties.entrySet().stream().map(e -> new KeyValue<>(e.getKey(), e.getValue())).toArray(KeyValue[]::new);
        final KeyValue<String,String>[] allNewMappings =
                newProperties.entrySet().stream().map(e -> new KeyValue<>(e.getKey(), e.getValue())).toArray(KeyValue[]::new);

        this.initMappings = Arrays.stream(Arrays.copyOf(allInitMappings, initMappingsCount)).collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));

        this.additionalMappings = new HashMap<>();
        int modifCount = 0, i = 0;
        do {
            final KeyValue<String,String> kv = allModifiedMappings[i];
            if (this.initMappings.containsKey(kv.getKey())) {
                this.additionalMappings.put(kv.getKey(), kv.getValue());
                modifCount++;
            }
            i++;
        } while (modifCount < modifiedMappingsCount);

        final Map<String,String> newMappings =
                Arrays.stream(Arrays.copyOf(allNewMappings, newMappingsCount)).collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));
        this.additionalMappings.putAll(newMappings);

        this.initKeys = this.initMappings.keySet().toArray(String[]::new);
        this.additionalKeys = this.additionalMappings.keySet().toArray(String[]::new);

    }


    public Map<String,String> getInitMappings() {
        return this.initMappings;
    }


    public String nextKey() {
        final int initKeysLen = this.initKeys.length;
        int index, newIndex;
        do {
            index = this.keyIndex.get();
            newIndex = (index + 1) % initKeysLen;
        } while (!this.keyIndex.compareAndSet(index, newIndex));
        return this.initKeys[index];
    }


    public Map.Entry<String,String> nextAdditionalMapping() {
        // This is meant to return a mapping corresponding to the keys of the "additionalMappings" map. This
        // map contains both new values for entries already existing in "initMappings" and also new entries.
        // To make sure the same value for the same key is not returned twice, the algorithm below will
        // allow iteration up to twice the length of the additionalKeys array. For the first half of such a length
        // mappings are retrieved from the "additionalMappings" map, but for the second half the retrieved keys
        // are checked against the "initMappings" so that a new value (the "init" one) is retrieved for the same
        // key that was already returned some iterations ago. If the selected key is a "new" one (which will have
        // no correspondence in the "init" map, then a new index is computed.

        final int modifiedKeysLen = this.additionalKeys.length;
        int oldIndex, index, newIndex;
        boolean useAdditionals;
        String key;
        do {
            do {
                oldIndex = this.additionalKeyIndex.get();
                index = oldIndex;
                if (index >= modifiedKeysLen) {
                    index -= modifiedKeysLen;
                    useAdditionals = false;
                } else {
                    useAdditionals = true;
                }
                newIndex = oldIndex + 1;
                if (newIndex >= (modifiedKeysLen * 2)) {
                    newIndex = 0;
                }
            } while (!this.additionalKeyIndex.compareAndSet(oldIndex, newIndex));
            key = this.additionalKeys[index];
        } while (!useAdditionals && !this.initMappings.containsKey(key));

        final Map<String,String> mappings = useAdditionals ? this.additionalMappings : this.initMappings;

        return new KeyValue<>(key, mappings.get(key));

    }



}
