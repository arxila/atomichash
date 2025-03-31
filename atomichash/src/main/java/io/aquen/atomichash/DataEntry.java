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

import java.io.Serializable;

final class DataEntry implements Serializable {

    private static final long serialVersionUID = -5401891463106165174L;

    final Hash hash;
    final KeyValue keyValue;


    static DataEntry of(final Hash hash, final KeyValue keyValue) {
        return new DataEntry(hash, keyValue);
    }


    // Should only be called from DataEntry or MultiDataEntry
    DataEntry(final Hash hash, final KeyValue keyValue) {
        super();
        this.hash = hash;
        this.keyValue = keyValue;
    }


    KeyValue get(final Object key) {
        return eq(this.keyValue.key, key) ? this.keyValue : null;
    }


    boolean matches(final Object key) {
        return eq(this.keyValue.key, key);
    }


    DataEntry replaceKeyValue(final KeyValue keyValue, final boolean replaceIfPresent) {
        return (replaceIfPresent) ? new DataEntry(this.hash, keyValue) : this;
    }


    MultiDataEntry addKeyValue(final KeyValue keyValue) {
        return new MultiDataEntry(this.hash, new KeyValue[]{ this.keyValue, keyValue });
    }


    /*
     * Equivalent to Objects.equals(), but by being called only from
     * this class we might benefit from runtime profile information on the
     * type of o1. See java.util.AbstractMap#eq().
     *
     * Do not replace with Objects.equals() until JDK-8015417 is resolved.
     */
    private static boolean eq(final Object o1, final Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }


}
