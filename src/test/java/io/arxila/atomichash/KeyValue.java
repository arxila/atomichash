/*
 * =========================================================================
 *
 *   Copyright (c) 2019-2025 Arxila OSS (https://arxila.io)
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
package io.arxila.atomichash;

import java.util.Map;
import java.util.Objects;

public class KeyValue<K,V> implements Map.Entry<K,V> {

    private final K key;
    private final V value;

    public KeyValue(final K key, final V value) {
        super();
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return this.key;
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Map.Entry<?,?>)) {
            return false;
        }
        final Map.Entry<?,?> otherEntry = (Map.Entry<?,?>) other;
        return Objects.equals(this.key, otherEntry.getKey()) && Objects.equals(this.value, otherEntry.getValue());

    }

    @Override
    public int hashCode() {
        return ((this.key == null) ? 0 : this.key.hashCode()) ^
                ((this.value == null) ? 0 : this.value.hashCode());
    }
}
