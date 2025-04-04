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


    Node(final int level, final int size, final long nodesBitMap, final Node[] nodes, final long entriesBitMap, final Entry[] entries) {
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

        final long mask0 = dataEntry0.hash.mask(this.level);
        final long mask1 = dataEntry1.hash.mask(this.level);

        if (mask0 != mask1) {

            final int index0 = dataEntry0.hash.index(this.level);
            final int index1 = dataEntry1.hash.index(this.level);

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
                this.entries = new Entry[] { dataEntry0.add(dataEntry1.keyValue) };

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


    boolean contains(final Hash hash, final Object key) {
        long bitMap, mask;
        Node node = this;
        while (true) {
            mask = hash.mask(node.level);
            if (((bitMap = node.nodesBitMap) & mask) != 0) {
                node = node.nodes[pos(mask, bitMap)];
            } else if (((bitMap = node.entriesBitMap) & mask) != 0) {
                return node.entries[pos(mask, bitMap)].containsKey(hash, key);
            } else {
                return false;
            }
        }
    }


    // May return KeyValue.NOT_FOUND if not found (so that it can be differentiated from a null value)
    KeyValue get(final Hash hash, final Object key) {
        long bitMap, mask;
        Node node = this;
        while (true) {
            mask = hash.mask(node.level);
            if (((bitMap = node.nodesBitMap) & mask) != 0) {
                node = node.nodes[pos(mask, bitMap)];
            } else if (((bitMap = node.entriesBitMap) & mask) != 0) {
                return node.entries[pos(mask, bitMap)].get(key);
            } else {
                return KeyValue.NOT_FOUND;
            }
        }
    }



    public Node put(final DataEntry dataEntry, final boolean replaceIfPresent) {

        final Hash hash = dataEntry.hash;
        final long mask = hash.mask(this.level);
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
            final Node newNode = oldNode.put(dataEntry, replaceIfPresent);

            if (oldNode == newNode) {
                return this;
            }

            final Node[] newNodeValues = Arrays.copyOf(this.nodes, this.nodes.length, Node[].class);
            newNodeValues[nodePos] = newNode;

            return new Node(this.level, this.size + (newNode.size - oldNode.size), this.nodesBitMap, newNodeValues, this.entriesBitMap, this.entries);

        }

        // There was an entry at the selected position: either replace (if keys match) or create level / collision
        final Entry oldEntry = this.entries[entryPos];

        if (oldEntry.containsKey(hash, dataEntry.keyValue.key)) {
            // There is a match (key exists): entry needs to be replaced unless flagged not to do so

            if (!replaceIfPresent) {
                return this;
            }

            final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length, Entry[].class);
            newEntries[entryPos] = dataEntry;

            return new Node(this.level, this.size, this.nodesBitMap, this.nodes, this.entriesBitMap, newEntries);

        }

        if (this.level == Hash.MAX_LEVEL) {
            // No new levels can be created, so a CollisionEntry will be created or expanded

            final CollisionEntry newEntry = oldEntry.add(dataEntry.keyValue);

            final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length, Entry[].class);
            newEntries[entryPos] = newEntry;

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
        System.arraycopy(this.entries, entryPos + 1, newEntries, entryPos, this.entries.length - entryPos - 1);

        return new Node(this.level, this.size + 1,newNodeBitMap, newNodes, newEntryBitMap, newEntries);

    }



    Node putAll(final Node other) {
        // this.level and other.level will always be the same

        // The new bitmaps will need to be considered temporary, as during processing some entries could turn to nodes
        long newNodesBitMap = this.nodesBitMap | other.nodesBitMap;
        long newEntriesBitMap = this.entriesBitMap | other.entriesBitMap;

        // New nodes and entries arrays will also be considered temporary, for the same reason as bitmaps
        // Nodes are given the maximum size because entries could be turned into nodes (the opposite does not happen)
        final int newNodesBitCount = Long.bitCount(newNodesBitMap);
        final int newEntriesBitCount = Long.bitCount(newEntriesBitMap);
        Node[] newNodes = new Node[newNodesBitCount + newEntriesBitCount];
        Entry[] newEntries = (newEntriesBitCount == 0)? EMPTY_ENTRIES : new Entry[newEntriesBitCount];

        // Three indices needed for nodes and other three for entries
        int thisNodesIndex = 0, otherNodesIndex = 0, newNodesIndex = 0;
        int thisEntriesIndex = 0, otherEntriesIndex = 0, newEntriesIndex = 0;

        // Iteration performed bit-by-bit for all the 64 possible bits in the bitmap
        boolean inThisNodes, inOtherNodes, inThisEntries, inOtherEntries;
        Node thisNode, otherNode;
        Entry thisEntry, otherEntry;
        for (long mask = 1L; mask != 0; mask <<= 1) {

            inThisNodes = (this.nodesBitMap & mask) != 0;
            inOtherNodes = (other.nodesBitMap & mask) != 0;
            inThisEntries = (this.entriesBitMap & mask) != 0;
            inOtherEntries = (other.entriesBitMap & mask) != 0;

            if (!inThisNodes && !inOtherNodes && !inThisEntries && !inOtherEntries) {
                // Not found in this or other: skip
                continue;
            }

            if (inThisNodes && inOtherNodes) {
                thisNode = this.nodes[thisNodesIndex++];
                otherNode = other.nodes[otherNodesIndex++];
                newNodes[newNodesIndex++] = thisNode.putAll(otherNode);
                continue;
            }

            if (inThisNodes && inOtherEntries) {
                thisNode = this.nodes[thisNodesIndex++];
                otherEntry = other.entries[otherEntriesIndex++];
                newNodes[newNodesIndex++] = thisNode.put((DataEntry)otherEntry, true);
                newEntriesBitMap ^= mask; // Remove from entries bitmap
                continue;
            }

            if (inThisEntries && inOtherNodes) {
                thisEntry = this.entries[thisEntriesIndex++];
                otherNode = other.nodes[otherNodesIndex++];
                newNodes[newNodesIndex++] = otherNode.put((DataEntry)thisEntry, false);
                newEntriesBitMap ^= mask; // Remove from entries bitmap
                continue;
            }

            if (inThisEntries && inOtherEntries) {

                thisEntry = this.entries[thisEntriesIndex++];
                otherEntry = other.entries[otherEntriesIndex++];

                if (this.level == Hash.MAX_LEVEL) {
                    // If level is max, this is a hash collision
                    newEntries[newEntriesIndex++] = thisEntry.merge(otherEntry);
                    continue;
                }

                // Not at max level: entries guaranteed to be DataEntry and deeper levels could be created
                final DataEntry thisDataEntry = (DataEntry)thisEntry;
                final DataEntry otherDataEntry = (DataEntry)otherEntry;

                if (thisEntry.containsKey(otherDataEntry.hash, otherDataEntry.keyValue.key)) {
                    newEntries[newEntriesIndex++] = otherEntry;
                    continue;
                }

                // A node for a deeper level will be created

                newNodesBitMap ^= mask; // Add to nodes bitmap
                newNodes[newNodesIndex++] = new Node(this.level + 1, thisDataEntry, otherDataEntry);
                newEntriesBitMap ^= mask; // Remove from entries bitmap
                continue;

            }

            if (inThisNodes) {
                newNodes[newNodesIndex++] = this.nodes[thisNodesIndex++];
                continue;
            }

            if (inOtherNodes) {
                newNodes[newNodesIndex++] = other.nodes[otherNodesIndex++];
                continue;
            }

            if (inThisEntries) {
                newEntries[newEntriesIndex++] = this.entries[thisEntriesIndex++];
                continue;
            }

            if (inOtherEntries) {
                newEntries[newEntriesIndex++] = other.entries[otherEntriesIndex++];
                continue;
            }


        }

        // The size of nodes and entries arrays may need to be adjusted after some entries may have been converted
        // to nodes (and also, newNodes was initialized to the max possible size)
        if (newNodesIndex != newNodes.length) {
            newNodes = (newNodesIndex == 0)? EMPTY_NODES : Arrays.copyOf(newNodes, newNodesIndex);
        }
        if (newEntriesIndex != newEntries.length) {
            newEntries = (newEntriesIndex == 0)? EMPTY_ENTRIES : Arrays.copyOf(newEntries, newEntriesIndex);
        }

        // Compute size based only on new existing nodes and entries
        int newSize = 0;
        for (final Node newNode : newNodes) {
            newSize += newNode.size;
        }
        for (final Entry newEntry : newEntries) {
            newSize += newEntry.size();
        }

        return new Node(this.level, newSize ,newNodesBitMap, newNodes, newEntriesBitMap, newEntries);

    }


}
