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

final class CollisionEntry implements Serializable {

    private static final long serialVersionUID = 5847401436093096621L;

    final Hash hash;
    final KeyValue[] keyValues;


    // Should only be called from DataEntry or CollisionEntry
    CollisionEntry(final Hash hash, final KeyValue[] keyValues) {
        super();
        this.hash = hash;
        this.keyValues = keyValues;
    }


    KeyValue get(final Object key) {
        for (final KeyValue keyValue : this.keyValues) {
            if (Objects.equals(keyValue.key, key)) {
                return keyValue;
            }
        }
        return null;
    }


    CollisionEntry addOrReplaceKeyValue(final KeyValue keyValue, final boolean replaceIfPresent) {
        for (int i = 0; i < this.keyValues.length; i++) {
            if (Objects.equals(this.keyValues[i].key, keyValue.key)) {
                // We have a match, so we will need to replace (if flagged to do so)
                if (!replaceIfPresent || (this.keyValues[i].key == keyValue.key && this.keyValues[i].value == keyValue.value)) {
                    return this;
                }
                final KeyValue[] newKeyValues = Arrays.copyOf(this.keyValues, this.keyValues.length, KeyValue[].class);
                newKeyValues[i] = keyValue;
                return new CollisionEntry(this.hash, newKeyValues);
            }
        }
        final KeyValue[] newKeyValues = new KeyValue[this.keyValues.length + 1];
        System.arraycopy(this.keyValues, 0, newKeyValues, 0, this.keyValues.length);
        newKeyValues[this.keyValues.length] = keyValue;
        return new CollisionEntry(this.hash, newKeyValues);
    }


    CollisionEntry addOrReplaceKeyValues(final KeyValue[] keyValues) {

        final boolean[] processed = new boolean[keyValues.length];
        Arrays.fill(processed, false);
        int toBeProcessed = processed.length;

        KeyValue[] newKeyValues = Arrays.copyOf(this.keyValues, this.keyValues.length, KeyValue[].class);
        for (int i = 0; i < this.keyValues.length && toBeProcessed > 0; i++) {
            for (int j = 0; j < processed.length && toBeProcessed > 0; j++) {
                if (!processed[j] && Objects.equals(this.keyValues[i].key, keyValues[j].key)) {
                    // We have a match, so we will need to replace
                    newKeyValues[i] = keyValues[j];
                    processed[j] = true;
                    toBeProcessed--;
                }
            }
        }
        if (toBeProcessed > 0) {
            newKeyValues = Arrays.copyOf(newKeyValues, newKeyValues.length + toBeProcessed, KeyValue[].class);
            for (int i = 0; i < processed.length; i++) {
                if (!processed[i]) {
                    newKeyValues[this.keyValues.length + i] = keyValues[i];
                }
            }
        }

        return new CollisionEntry(this.hash, newKeyValues);

    }


}
