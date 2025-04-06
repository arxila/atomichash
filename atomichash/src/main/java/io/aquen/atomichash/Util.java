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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class Util implements Serializable {

    private static final DataEntryHashComparator COMPARATOR = new DataEntryHashComparator();


    public static Node createNode(final KeyValue[] keyValues) {

        final DataEntry[] dataEntries = new DataEntry[keyValues.length];
        for (int i = 0; i < keyValues.length; i++) {
            dataEntries[i] = new DataEntry(Hash.of(keyValues[i].key), keyValues[i]);
        }
        // Sort the DataEntry objects by hash
        Arrays.sort(dataEntries, COMPARATOR);

        // Transform the sorted list into a new list of DataEntry or MultiDataEntry objects
        final List<Entry> processedEntries = new ArrayList<Entry>(dataEntries.length);
        Entry previous = null;
        Hash previousHash = null;

        for (DataEntry current : dataEntries) {

            if (previousHash != null && previousHash.equals(current.hash)) {
                previous = previous.merge(current);
                processedEntries.set(processedEntries.size() - 1, previous); // Replace the last element
            } else {
                processedEntries.add(current);
                previous = current;
                previousHash = current.hash;
            }

        }

        // At this point processedEntries contains all new entries ordered and grouped into collisions if needed

        return null;

    }


    private List<Object> valuesForLevel(final int level, final List<DataEntry> entries) {
        int previousIndex = -1;
        final List<Object> valuesForLevel = new ArrayList<>();
        final List<DataEntry> entryGroup = new ArrayList<>();
        for (final DataEntry entry : entries) {
            final int entryIndex = entry.hash.index(level);
            if (entryIndex == previousIndex) {
                entryGroup.add(entry);
                continue;
            }
            if (!entryGroup.isEmpty()) {
                processEntryGroup(level, entryGroup, valuesForLevel);
            }
            entryGroup.clear();
            entryGroup.add(entry);
            previousIndex = entryIndex;
        }
        if (!entryGroup.isEmpty()) {
            processEntryGroup(level, entryGroup, valuesForLevel);
        }
        return valuesForLevel;
    }


    private void processEntryGroup(final int level, final List<DataEntry> entryGroup, final List<Object> valuesForLevel) {
        if (entryGroup.size() == 1) {
            valuesForLevel.add(entryGroup.get(0));
        } else {
            if (level == Hash.MAX_LEVEL) {
                Entry previous = null;
                Hash previousHash = null;
                for (final DataEntry entryInGroup : entryGroup) {
                    if (previousHash != null && previousHash.equals(entryInGroup.hash)) {
                        previous = previous.merge(entryInGroup);
                        valuesForLevel.set(valuesForLevel.size() - 1, previous); // Replace the last element
                    } else {
                        valuesForLevel.add(entryInGroup);
                        previous = entryInGroup;
                        previousHash = entryInGroup.hash;
                    }
                }
            } else {
                // TODO This should not add a List<Object> but a node created for it.
                valuesForLevel.add(valuesForLevel(level + 1, entryGroup));
            }
        }
    }



    /*
     * Will sort KeyValue objects entirely based on the hash-based ordering of their keys. Values are explicitly
     * ignored because this will only be used for segmenting entries during multi-insertion.
     *
     * Implementing a comparator here is preferable to making the KeyValue class directly implement the Comparable
     * interface because we are sorting only on the hash of the Key object.
     */
    static class DataEntryHashComparator implements java.util.Comparator<DataEntry> {

        @Override
        public int compare(final DataEntry o1, final DataEntry o2) {
            return Hash.hashCompare(o1.hash, o2.hash);
        }

    }


}
