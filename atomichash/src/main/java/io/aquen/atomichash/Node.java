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

    private static final Node[] EMPTY_NODES = new Node[0];
    private static final Entry[] EMPTY_ENTRIES = new Entry[0];

    final int level;
    final long nodeBitMap;
    final Node[] nodes;
    final long entryBitMap;
    final Entry[] entries;


    Node(final int level, final long nodeBitMap, final Node[] nodes, final long entryBitMap, final Entry[] entries) {
        super();
        this.level = level;
        this.nodeBitMap = nodeBitMap;
        this.nodes = nodes;
        this.entryBitMap = entryBitMap;
        this.entries = entries;
    }


    private Node(final int level, final DataEntry dataEntry0, final DataEntry dataEntry1) {

        super();

        this.level = level;

        final long mask0 = dataEntry0.hash.mask(this.level);
        final long mask1 = dataEntry1.hash.mask(this.level);

        if (mask0 != mask1) {

            final int index0 = dataEntry0.hash.index(this.level);
            final int index1 = dataEntry1.hash.index(this.level);

            this.nodeBitMap = 0L;
            this.nodes = EMPTY_NODES;
            this.entryBitMap = mask0 | mask1;
            this.entries = (index0 < index1) ? new Entry[] { dataEntry0, dataEntry1 } : new Entry[] { dataEntry1, dataEntry0 };

        } else {
            // We have an index match at this level, so we will need to (try) to create a new level
            if (this.level == Hash.MAX_LEVEL) {
                // We have no more levels, so we need a CollisionEntry

                this.nodeBitMap = 0L;
                this.nodes = EMPTY_NODES;
                this.entryBitMap = mask0;
                this.entries = new Entry[] { dataEntry0.add(dataEntry1.keyValue) };

            } else {
                // We need an additional level to further differentiate entries

                this.nodeBitMap = mask0;
                this.nodes = new Node[]{ new Node(this.level + 1, dataEntry0, dataEntry1) };
                this.entryBitMap = 0L;
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


    boolean contains(final Hash hash) {
        // TODO Wrong! It only checks the current level
        return hash.pos(this.level, this.nodeBitMap) >= 0;
    }


    KeyValue get(final Hash hash, final Object key) {
        Node currentNode = this;
        int level = 0;
        long bitMap, hashMask = 0L;
        while (true) {
            level = currentNode.level;
            hashMask = hash.mask(level);
            if (((bitMap = currentNode.nodeBitMap) & hashMask) != 0) {
                currentNode = currentNode.nodes[hash.pos(level, bitMap)];
            } else if (((bitMap = currentNode.entryBitMap) & hashMask) != 0) {
                return currentNode.entries[hash.pos(level, bitMap)].get(key);
            } else {
                return null; // Not found
            }
        }
    }



    public Node put(final DataEntry dataEntry, final boolean replaceIfPresent) {

        final Hash hash = dataEntry.hash;
        final int nodePos = hash.pos(this.level, this.nodeBitMap);
        final int entryPos = hash.pos(this.level, this.entryBitMap);

        if (nodePos < 0 && entryPos < 0) {
            // There was nothing at the selected position: an entry will be created

            final int newEntryPos = (entryPos ^ Hash.NEG_MASK);

            final long newEntryBitMap = this.entryBitMap | hash.mask(this.level);
            final Entry[] newEntries = new Entry[this.entries.length + 1];
            System.arraycopy(this.entries, 0, newEntries, 0, newEntryPos);
            newEntries[newEntryPos] = dataEntry;
            System.arraycopy(this.entries, newEntryPos, newEntries, newEntryPos + 1, this.entries.length - newEntryPos);

            return new Node(this.level, this.nodeBitMap, this.nodes, newEntryBitMap, newEntries);

        }

        if (nodePos >= 0) {
            // There was a node at the selected position, therefore the put operation will be delegated

            final Node oldNode = this.nodes[nodePos];
            final Node newNode = oldNode.put(dataEntry, replaceIfPresent);

            if (oldNode == newNode) {
                return this;
            }

            final Node[] newNodes = Arrays.copyOf(this.nodes, this.nodes.length, Node[].class);
            newNodes[nodePos] = newNode;

            return new Node(this.level, this.nodeBitMap, newNodes, this.entryBitMap, this.entries);

        }

        // There was an entry at the selected position: either replace (if keys match) or create level / collision
        final Entry oldEntry = this.entries[entryPos];

        if (oldEntry.containsKey(hash, dataEntry.keyValue.key)) {
            // There is a match (key exists): entry needs to be replaced unless flagged not to do so

            if (!replaceIfPresent) {
                return this;
            }

            final Entry newEntry = oldEntry.set(dataEntry.keyValue);
            if (newEntry == oldEntry) {
                return this;
            }

            final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length, Entry[].class);
            newEntries[entryPos] = newEntry;

            return new Node(this.level, this.nodeBitMap, this.nodes, this.entryBitMap, newEntries);

        }

        if (this.level == Hash.MAX_LEVEL) {
            // No new levels can be created, so a CollisionEntry will be created or expanded

            final CollisionEntry newEntry = oldEntry.add(dataEntry.keyValue);

            final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length, Entry[].class);
            newEntries[entryPos] = newEntry;

            return new Node(this.level, this.nodeBitMap, this.nodes, this.entryBitMap, newEntries);

        }

        // A new level will be created, a node will replace the existing entry
        final Node newNode = new Node(this.level + 1, (DataEntry)oldEntry, dataEntry);
        final int newNodePos = (nodePos ^ Hash.NEG_MASK);

        final long hashMask = hash.mask(this.level);
        final long newNodeBitMap = this.nodeBitMap ^ hashMask;
        final long newEntryBitMap = this.entryBitMap ^ hashMask;

        final Node[] newNodes = new Node[this.nodes.length + 1];
        System.arraycopy(this.nodes, 0, newNodes, 0, newNodePos);
        newNodes[newNodePos] = newNode;
        System.arraycopy(this.nodes, newNodePos, newNodes, newNodePos + 1, this.nodes.length - newNodePos);

        final Entry[] newEntries = new Entry[this.entries.length - 1];
        System.arraycopy(this.entries, 0, newEntries, 0, entryPos);
        System.arraycopy(this.entries, entryPos + 1, newEntries, entryPos, this.entries.length - entryPos - 1);

        return new Node(this.level, newNodeBitMap, newNodes, newEntryBitMap, newEntries);

    }



//    Node putAll(final Node other) {
//        // this.level and other.level will always be the same
//
//        // The bitmap for the new compressed array will contain the bits in both nodes
//        final long newBitMap = this.nodeBitMap | other.nodeBitMap;
//        // We will need as many new values as bits set in the new bitmap
//        final Object[] newValues = new Object[Long.bitCount(newBitMap)];
//
//        // Three indices needed to traverse this, the other's and the new compressed array
//        int thisIndex = 0, otherIndex = 0, newIndex = 0;
//
//        // We need to iterate all the positions the array can have (64 bits in a long)
//        for (long mask = 1L; mask != 0; mask <<= 1) {
//
//            final boolean inThis = (this.nodeBitMap & mask) != 0;
//            final boolean inOther = (other.nodeBitMap & mask) != 0;
//
//            if (!inThis && !inOther) {
//                // Not found in any of the nodes, we should skip
//                continue;
//            }
//
//            final Object newValue;
//            if (inThis && inOther) {
//                // Both nodes contain an entry for this position so we need to merge
//
//                final Object thisValue = this.nodes[thisIndex++];
//                final Object otherValue = other.nodes[otherIndex++];
//
//                newValue = (this.level < Hash.MAX_LEVEL) ?
//                                computeNewValueForPutAllInterLevel(this.level, thisValue, otherValue)
//                              : computeNewValueForPutAllMaxLevel(thisValue, otherValue);
//
//            } else if (inThis) {
//                newValue = this.nodes[thisIndex++];
//            } else { // inOther
//                newValue = other.nodes[otherIndex++];
//            }
//            newValues[newIndex++] = newValue;
//
//        }
//
//        return new Node(this.level, newBitMap, newValues);
//
//    }
//
//
//    private static Object computeNewValueForPutAllInterLevel(
//            final int level, final Object thisValue, final Object otherValue) {
//        // At an intermediate level, Node values can exist but CollisionEntry values cannot
//
//        final Node thisNode = (thisValue instanceof Node) ? (Node) thisValue : null;
//        final Node otherNode = (otherValue instanceof Node) ? (Node) otherValue : null;
//
//        if (thisNode != null && otherNode != null) {
//            return thisNode.putAll(otherNode);
//        }
//
//        if (thisNode != null) {
//            final DataEntry otherEntry = (DataEntry)otherValue;
//            return thisNode.put(otherEntry, true);
//        }
//
//        if (otherNode != null) {
//            final DataEntry thisEntry = (DataEntry)thisValue;
//            return otherNode.put(thisEntry, false);  // !replaceIfPresent because "other" has precedence
//        }
//
//        // Both thisValue and otherValue are DataEntry
//        final DataEntry thisDataEntry = (DataEntry)thisValue;
//        final DataEntry otherDataEntry = (DataEntry)otherValue;
//
//        if (thisDataEntry.matches(otherDataEntry.keyValue)) {
//            return thisDataEntry.replaceKeyValue(otherDataEntry.keyValue, true);
//        }
//        return new Node(level + 1, thisDataEntry, otherDataEntry);
//
//    }
//
//
//
//
//    private static Object computeNewValueForPutAllMaxLevel(final Object thisValue, final Object otherValue) {
//        // At Hash.MAX_LEVEL level, CollisionEntry values can exist but Node values cannot
//
//        final CollisionEntry thisCollisionEntry = (thisValue instanceof CollisionEntry) ? (CollisionEntry) thisValue : null;
//        final CollisionEntry otherCollisionEntry = (otherValue instanceof CollisionEntry) ? (CollisionEntry) otherValue : null;
//
//        if (thisCollisionEntry != null && otherCollisionEntry != null) {
//            return thisCollisionEntry.addOrReplaceKeyValues(otherCollisionEntry.keyValues);
//        }
//
//        if (thisCollisionEntry != null) {
//            final DataEntry otherDataEntry = (DataEntry)otherValue;
//            return thisCollisionEntry.addOrReplaceKeyValue(otherDataEntry.keyValue, true);
//        }
//
//        if (otherCollisionEntry != null) {
//            final DataEntry thisDataEntry = (DataEntry)thisValue;
//            return otherCollisionEntry.addOrReplaceKeyValue(thisDataEntry.keyValue, false);  // !replaceIfPresent because "other" has precedence
//        }
//
//        // Both thisValue and otherValue are DataEntry
//        final DataEntry thisDataEntry = (DataEntry)thisValue;
//        final DataEntry otherDataEntry = (DataEntry)otherValue;
//
//        if (thisDataEntry.matches(otherDataEntry.keyValue)) {
//            return thisDataEntry.replaceKeyValue(otherDataEntry.keyValue, true);
//        }
//        return thisDataEntry.addKeyValue(otherDataEntry.keyValue);
//
//    }


}
