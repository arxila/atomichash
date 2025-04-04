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
import java.util.Arrays;
import java.util.Objects;

final class CollisionEntry implements Entry, Serializable {

    private static final long serialVersionUID = 5847401436093096621L;

    final Hash hash;
    final KeyValue[] keyValues;


    // Should only be called from DataEntry or CollisionEntry
    CollisionEntry(final Hash hash, final KeyValue[] keyValues) {
        super();
        this.hash = hash;
        this.keyValues = keyValues;
    }


    @Override
    public int size() {
        return this.keyValues.length;
    }


    @Override
    public boolean containsKey(final Hash hash, final Object key) {
        if (this.hash.hash != hash.hash) {
            return false;
        }
        for (final KeyValue thisKeyValue : this.keyValues) {
            if (Objects.equals(thisKeyValue.key, key)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public KeyValue get(final Object key) {
        for (final KeyValue keyValue : this.keyValues) {
            if (Objects.equals(keyValue.key, key)) {
                return keyValue;
            }
        }
        return KeyValue.NOT_FOUND;
    }


    @Override
    public CollisionEntry add(final KeyValue keyValue) {
        final KeyValue[] newKeyValues = new KeyValue[this.keyValues.length + 1];
        System.arraycopy(this.keyValues, 0, newKeyValues, 0, this.keyValues.length);
        newKeyValues[this.keyValues.length] = keyValue;
        return new CollisionEntry(this.hash, newKeyValues);
    }


    @Override
    public Entry merge(final Entry other) {
        // hash is guaranteed to match

        if (other instanceof DataEntry) {

            final DataEntry otherDataEntry = (DataEntry) other;
            final KeyValue otherKeyValue = otherDataEntry.keyValue;
            for (int i = 0; i < this.keyValues.length; i++) {
                final KeyValue thisKeyValue = this.keyValues[i];
                if (Objects.equals(thisKeyValue.key, otherKeyValue.key)) {
                    if (thisKeyValue.key == otherKeyValue.key && thisKeyValue.value == otherKeyValue.value) {
                        return this;
                    }
                    final KeyValue[] newKeyValues = Arrays.copyOf(this.keyValues, this.keyValues.length, KeyValue[].class);
                    newKeyValues[i] = otherKeyValue;
                    return new CollisionEntry(this.hash, newKeyValues);
                }
            }
            return add(otherKeyValue);

        }

        final CollisionEntry otherCollisionEntry = (CollisionEntry) other;
        final KeyValue[] otherKeyValues = otherCollisionEntry.keyValues;

        final boolean[] processed = new boolean[otherKeyValues.length];
        Arrays.fill(processed, false);
        int toBeProcessed = processed.length;

        KeyValue[] newKeyValues = Arrays.copyOf(this.keyValues, this.keyValues.length, KeyValue[].class);
        for (int i = 0; i < this.keyValues.length && toBeProcessed > 0; i++) {
            for (int j = 0; j < processed.length && toBeProcessed > 0; j++) {
                if (!processed[j] && Objects.equals(this.keyValues[i].key, otherKeyValues[j].key)) {
                    // We have a match, so we will need to replace
                    newKeyValues[i] = otherKeyValues[j];
                    processed[j] = true;
                    toBeProcessed--;
                }
            }
        }
        if (toBeProcessed > 0) {
            newKeyValues = Arrays.copyOf(newKeyValues, newKeyValues.length + toBeProcessed, KeyValue[].class);
            for (int i = 0; i < processed.length; i++) {
                if (!processed[i]) {
                    newKeyValues[this.keyValues.length + i] = otherKeyValues[i];
                }
            }
        }

        return new CollisionEntry(this.hash, newKeyValues);

    }


}
