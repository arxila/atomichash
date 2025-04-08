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

final class Node implements Serializable {

    private static final long serialVersionUID = 5892440116260222326L;

    private static final int NEG_MASK = 1 << 31; // will be used for turning 0..63 int positions into negative
    private static final Node[] EMPTY_NODES = new Node[0];
    private static final Entry[] EMPTY_ENTRIES = new Entry[0];

    final int level;
    final int size;
    final long nodesBitMap;
    final Node[] nodes;
    final long entriesBitMap;
    final Entry[] entries;



    Node() {
        super();
        this.level = 0;
        this.size = 0;
        this.nodesBitMap = 0L;
        this.nodes = EMPTY_NODES;
        this.entriesBitMap = 0L;
        this.entries = EMPTY_ENTRIES;
    }


    private Node(final int level, final int size, final long nodesBitMap, final Node[] nodes, final long entriesBitMap, final Entry[] entries) {
        super();
        this.level = level;
        this.size = size;
        this.nodesBitMap = nodesBitMap;
        this.nodes = nodes;
        this.entriesBitMap = entriesBitMap;
        this.entries = entries;
    }


    private Node(final int level, final DataEntry dataEntry0, final DataEntry dataEntry1) {

        super();

        this.level = level;
        this.size = 2;

        final long mask0 = Hash.mask(dataEntry0.hash, this.level);
        final long mask1 = Hash.mask(dataEntry1.hash, this.level);

        if (mask0 != mask1) {

            final int index0 = Hash.index(dataEntry0.hash, this.level);
            final int index1 = Hash.index(dataEntry1.hash, this.level);

            this.nodesBitMap = 0L;
            this.nodes = EMPTY_NODES;

            this.entriesBitMap = (mask0 | mask1);
            this.entries = (index0 < index1) ? new Entry[] { dataEntry0, dataEntry1 } : new Entry[] { dataEntry1, dataEntry0 };

        } else {
            // We have an index match at this level, so we will need to (try) to create a new level
            if (this.level == Hash.MAX_LEVEL) {
                // We have no more levels, so we need a CollisionEntry

                this.nodesBitMap = 0L;
                this.nodes = EMPTY_NODES;

                this.entriesBitMap = mask0;
                this.entries = new Entry[] { dataEntry0.add(dataEntry1) };

            } else {
                // We need an additional level to further differentiate entries

                this.nodesBitMap = mask0;
                this.nodes = new Node[]{ new Node(this.level + 1, dataEntry0, dataEntry1) };

                this.entriesBitMap = 0L;
                this.entries = EMPTY_ENTRIES;

            }
        }
    }

    // TODO There is still need for a constructor that takes a KeyValue[] as an argument. This will
    //      be executed outside the critical path so it won't be as important that it's fast, but nevertheless
    //      its performance should be cared for.
    //
    //  1. Create DataEntry for all keyvalues
    //  2. Sort dataentries by means of the comparator
    //  3. Create MultiDataEntry's where needed, remove key-duplicates
    //  4. 


    /*
     * Computes the position in a compact array by using a bitmap. This bitmap will contain 1's for
     * every position of the possible 64 (max size of the values array) that can contain an element. The
     * position in the array will correspond to the number of positions occupied before the index, i.e. the
     * number of bits to the right of the bit corresponding to the index for this level.
     *
     * This algorithm benefits from the fact that Long.bitCount is an intrinsic candidate typically implemented
     * as a single "population count" CPU instruction, and thus the computation will be O(1).
     */
    static int pos(final long mask, final long bitMap) {
        final int pos = Long.bitCount(bitMap & (mask - 1L));
        return ((bitMap & mask) != 0L) ? pos : (pos ^ NEG_MASK); // positive if present, negative if absent
    }


    boolean contains(final Object key) {
        final int hash = Hash.hash(key);
        Node node = this; int level = 0; long mask;
        while(((mask = Hash.mask(hash, level++)) & node.nodesBitMap) != 0L) {
            node = node.nodes[pos(mask, node.nodesBitMap)];
        }
        if ((mask & node.entriesBitMap) != 0L) {
            return node.entries[pos(mask, node.entriesBitMap)].containsKey(hash, key);
        }
        return false;
    }


    // May return DataEntry.NOT_FOUND if not found (so that it can be differentiated from a null value)
    Object get(final Object key) {
        final int hash = Hash.hash(key);
        Node node = this; int level = 0; long mask;
        while(((mask = Hash.mask(hash, level++)) & node.nodesBitMap) != 0L) {
            node = node.nodes[pos(mask, node.nodesBitMap)];
        }
        if ((mask & node.entriesBitMap) != 0L) {
            return node.entries[pos(mask, node.entriesBitMap)].get(key);
        }
        return DataEntry.NOT_FOUND;
    }



    Node put(final Object key, final Object value) {
        return put(new DataEntry(key, value));
    }


    Node put(final DataEntry dataEntry) {

        final int hash = dataEntry.hash;
        final long mask = Hash.mask(hash, this.level);
        final int nodePos = pos(mask, this.nodesBitMap);
        final int entryPos = pos(mask, this.entriesBitMap);

        if (nodePos < 0 && entryPos < 0) {
            // There was nothing at the selected position: an entry will be created

            final int newEntryPos = (entryPos ^ NEG_MASK);

            final long newEntriesBitMap = this.entriesBitMap | mask;
            final Entry[] newEntries = new Entry[this.entries.length + 1];
            System.arraycopy(this.entries, 0, newEntries, 0, newEntryPos);
            newEntries[newEntryPos] = dataEntry;
            System.arraycopy(this.entries, newEntryPos, newEntries, newEntryPos + 1, this.entries.length - newEntryPos);

            return new Node(this.level, this.size + 1, this.nodesBitMap, this.nodes, newEntriesBitMap, newEntries);

        }

        if (nodePos >= 0) {
            // There was a node at the selected position, therefore the put operation will be delegated

            final Node oldNode = this.nodes[nodePos];
            final Node newNode = oldNode.put(dataEntry);

            final Node[] newNodeValues = Arrays.copyOf(this.nodes, this.nodes.length, Node[].class);
            newNodeValues[nodePos] = newNode;

            return new Node(this.level, this.size + (newNode.size - oldNode.size), this.nodesBitMap, newNodeValues, this.entriesBitMap, this.entries);

        }

        // There was an entry at the selected position: either replace (if keys match) or create level / collision
        final Entry oldEntry = this.entries[entryPos];

        if (oldEntry.containsKey(hash, dataEntry.key)) {
            // There is a match (key exists): entry needs to be replaced

            final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length, Entry[].class);
            newEntries[entryPos] = dataEntry;

            return new Node(this.level, this.size, this.nodesBitMap, this.nodes, this.entriesBitMap, newEntries);

        }

        if (this.level == Hash.MAX_LEVEL) {
            // No new levels can be created, so a CollisionEntry will be created or expanded

            final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length, Entry[].class);
            newEntries[entryPos] = oldEntry.add(dataEntry);

            return new Node(this.level, this.size + 1, this.nodesBitMap, this.nodes, this.entriesBitMap, newEntries);

        }

        // A new level will be created, a node will replace the existing entry
        final Node newNode = new Node(this.level + 1, (DataEntry)oldEntry, dataEntry);
        final int newNodePos = (nodePos ^ NEG_MASK);

        final long newNodeBitMap = this.nodesBitMap ^ mask;
        final long newEntryBitMap = this.entriesBitMap ^ mask;

        final Node[] newNodes = new Node[this.nodes.length + 1];
        System.arraycopy(this.nodes, 0, newNodes, 0, newNodePos);
        newNodes[newNodePos] = newNode;
        System.arraycopy(this.nodes, newNodePos, newNodes, newNodePos + 1, this.nodes.length - newNodePos);

        final Entry[] newEntries = new Entry[this.entries.length - 1];
        System.arraycopy(this.entries, 0, newEntries, 0, entryPos);
        System.arraycopy(this.entries, entryPos + 1, newEntries, entryPos, this.entries.length - (entryPos + 1));

        return new Node(this.level, this.size + 1, newNodeBitMap, newNodes, newEntryBitMap, newEntries);

    }


}
