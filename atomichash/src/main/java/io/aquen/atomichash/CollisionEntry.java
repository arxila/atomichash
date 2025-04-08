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

    final int hash;
    final DataEntry[] entries;


    // Should only be called from DataEntry or CollisionEntry
    CollisionEntry(final int hash, final DataEntry[] entries) {
        super();
        this.hash = hash;
        this.entries = entries;
    }


    @Override
    public int size() {
        return this.entries.length;
    }


    @Override
    public boolean containsKey(final int hash, final Object key) {
        if (this.hash != hash) {
            return false;
        }
        for (final DataEntry thisEntry : this.entries) {
            if (Objects.equals(thisEntry.key, key)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public Object get(final Object key) {
        for (final DataEntry thisEntry : this.entries) {
            if (Objects.equals(thisEntry.key, key)) {
                return thisEntry.value;
            }
        }
        return DataEntry.NOT_FOUND;
    }


    @Override
    public CollisionEntry add(final DataEntry entry) {
        final DataEntry[] newEntries = new DataEntry[this.entries.length + 1];
        System.arraycopy(this.entries, 0, newEntries, 0, this.entries.length);
        newEntries[this.entries.length] = entry;
        return new CollisionEntry(this.hash, newEntries);
    }


    @Override
    public Entry merge(final Entry other) {
        // hash is guaranteed to match

        if (other instanceof DataEntry) {

            final DataEntry otherEntry = (DataEntry) other;
            for (int i = 0; i < this.entries.length; i++) {
                final DataEntry thisEntry = this.entries[i];
                if (Objects.equals(thisEntry.key, otherEntry.key)) {
                    if (thisEntry.key == otherEntry.key && thisEntry.value == otherEntry.value) {
                        return this;
                    }
                    final DataEntry[] newEntries = Arrays.copyOf(this.entries, this.entries.length, DataEntry[].class);
                    newEntries[i] = otherEntry;
                    return new CollisionEntry(this.hash, newEntries);
                }
            }
            return add(otherEntry);

        }

        final CollisionEntry otherCollisionEntry = (CollisionEntry) other;
        final DataEntry[] otherEntries = otherCollisionEntry.entries;

        final boolean[] processed = new boolean[otherEntries.length];
        Arrays.fill(processed, false);
        int toBeProcessed = processed.length;

        DataEntry[] newEntries = Arrays.copyOf(this.entries, this.entries.length, DataEntry[].class);
        for (int i = 0; i < this.entries.length && toBeProcessed > 0; i++) {
            for (int j = 0; j < otherEntries.length && toBeProcessed > 0; j++) {
                if (!processed[j] && Objects.equals(this.entries[i].key, otherEntries[j].key)) {
                    // We have a match, so we will need to replace
                    newEntries[i] = otherEntries[j];
                    processed[j] = true;
                    toBeProcessed--;
                }
            }
        }
        if (toBeProcessed > 0) {
            newEntries = Arrays.copyOf(newEntries, this.entries.length + toBeProcessed, DataEntry[].class);
            int newEntriesIndex = this.entries.length;
            for (int i = 0; i < processed.length; i++) {
                if (!processed[i]) {
                    newEntries[newEntriesIndex++] = otherEntries[i];
                }
            }
        }

        return new CollisionEntry(this.hash, newEntries);

    }


}
