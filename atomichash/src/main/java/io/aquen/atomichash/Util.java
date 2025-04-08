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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

final class Util implements Serializable {

    private static final DataEntryHashComparator COMPARATOR = new DataEntryHashComparator();


    public static Node createNode(final KeyValue[] keyValues) {

        final List<DataEntry> dataEntries = new ArrayList<>(keyValues.length);
        for (KeyValue keyValue : keyValues) {
            dataEntries.add(new DataEntry(Hash.of(keyValue.key), keyValue));
        }
        // Sort the DataEntry objects by hash
        dataEntries.sort(COMPARATOR);

        return nodeForLevel(0, dataEntries);

    }


    private static Node nodeForLevel(final int level, final List<DataEntry> entries) {

        final BitmapList<Node> newNodes = new BitmapList<>(Node.class, entries.size());
        final BitmapList<Entry> newEntries = new BitmapList<>(Entry.class, entries.size());

        long entryMask, groupMask = 0L;
        final List<DataEntry> entryGroup = new ArrayList<>(entries.size() / 2);

        for (final DataEntry entry : entries) {

            entryMask = entry.hash.mask(level);
            if (groupMask == entryMask) {
                entryGroup.add(entry);
                continue;
            }
            if (!entryGroup.isEmpty()) {
                processEntryGroup(level, groupMask, entryGroup, newNodes, newEntries);
            }
            entryGroup.clear();
            entryGroup.add(entry);
            groupMask = entryMask;

        }

        if (!entryGroup.isEmpty()) {
            processEntryGroup(level, groupMask, entryGroup, newNodes, newEntries);
        }

        // Compute size based only on new existing nodes and entries
        int newSize = 0;
        for (final Node newNode : newNodes.values) {
            newSize += newNode.size;
        }
        for (final Entry newEntry : newEntries.values) {
            newSize += newEntry.size();
        }

        return new Node(level, newSize, newNodes.bitmap, newNodes.valuesToArray(), newEntries.bitmap, newEntries.valuesToArray());

    }


    private static void processEntryGroup(final int level, final long mask, final List<DataEntry> entryGroup,
                                   final BitmapList<Node> nodes, final BitmapList<Entry> entries) {
        if (entryGroup.size() == 1) {
            entries.bitmap |= mask;
            entries.values.add(entryGroup.get(0));
        } else {
            if (level == Hash.MAX_LEVEL) {
                Entry previous = null;
                Hash previousHash = null;
                for (final DataEntry entryInGroup : entryGroup) {
                    if (previousHash != null && previousHash.equals(entryInGroup.hash)) {
                        previous = previous.merge(entryInGroup);
                        entries.values.set(entries.values.size() - 1, previous); // Replace the last element
                    } else {
                        entries.bitmap |= mask;
                        entries.values.add(entryInGroup);
                        previous = entryInGroup;
                        previousHash = entryInGroup.hash;
                    }
                }
            } else {
                nodes.bitmap |= mask;
                nodes.values.add(nodeForLevel(level + 1, entryGroup));
            }
        }
    }



    private static class BitmapList<T> {

        private long bitmap;
        private final Class<T> clazz;
        private final List<T> values;

        private BitmapList(final Class<T> clazz, final int initSize) {
            super();
            this.clazz = clazz;
            this.values = new ArrayList<T>(initSize);
        }

        @SuppressWarnings("unchecked")
        private T[] valuesToArray() {
            return values.toArray((T[])Array.newInstance(this.clazz, this.values.size()));
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
