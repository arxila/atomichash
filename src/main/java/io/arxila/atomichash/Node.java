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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class Node implements Serializable {
    // NOTE that this class is meant to be totally immutable so that, in future versions, it can become
    // a record (JDK17) and even a value type (Valhalla)

    private static final long serialVersionUID = 5892440116260222326L;

    static final int MAX_LEVEL = 5;
    static final int[] HASH_SHIFTS = new int[] { 0, 6, 12, 18, 24, 30 };
    static final int HASH_MASK = 0b111111;
    static final int NEG_MASK = 1 << 31; // will be used for turning 0..63 int positions into negative

    static final Node[] EMPTY_NODES = new Node[0];
    static final Entry[] EMPTY_ENTRIES = new Entry[0];
    static final Node EMPTY_NODE = new Node(0, 0, 0L, EMPTY_NODES, 0L, EMPTY_ENTRIES);


    final int level;
    final int size;
    final long nodesBitMap;
    final Node[] nodes;
    final long entriesBitMap;
    final Entry[] entries;



    Node(final int level, final int size,
         final long nodesBitMap, final Node[] nodes,
         final long entriesBitMap, final Entry[] entries) {
        super();
        this.level = level;
        this.size = size;
        this.nodesBitMap = nodesBitMap;
        this.nodes = nodes;
        this.entriesBitMap = entriesBitMap;
        this.entries = entries;
    }



    static Node createNewLevel(final int level, final Entry entry0, final Entry entry1) {

        final long newNodesBitMap;
        final long newEntriesBitMap;
        final Node[] newNodes;
        final Entry[] newEntries;

        final long mask0 = mask(entry0.hash, level);
        final long mask1 = mask(entry1.hash, level);

        if (mask0 != mask1) {

            final int index0 = index(entry0.hash, level);
            final int index1 = index(entry1.hash, level);

            newNodesBitMap = 0L;
            newNodes = EMPTY_NODES;

            newEntriesBitMap = (mask0 | mask1);
            newEntries = (index0 < index1) ? new Entry[] {entry0, entry1} : new Entry[] {entry1, entry0};

        } else {
            // We have an index match at this level, so we will need to (try) to create a new level
            if (level == MAX_LEVEL) {
                // We have no more levels, so we need an Entry with collisions

                newNodesBitMap = 0L;
                newNodes = EMPTY_NODES;

                newEntriesBitMap = mask0;
                newEntries = new Entry[] { entry0.add(entry1) };

            } else {
                // We need an additional level to further differentiate entries

                newNodesBitMap = mask0;
                newNodes = new Node[] { createNewLevel(level + 1, entry0, entry1) };

                newEntriesBitMap = 0L;
                newEntries = EMPTY_ENTRIES;

            }
        }

        return new Node(level, 2, newNodesBitMap, newNodes, newEntriesBitMap, newEntries);

    }


    static int index(final int hash, final int level) {
        return (hash >>> HASH_SHIFTS[level]) & HASH_MASK;
    }


    static long mask(final int hash, final int level) {
        return 1L << ((hash >>> HASH_SHIFTS[level]) & HASH_MASK);
    }


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


    boolean containsKey(final Object key) {
        final int hash = Entry.hash(key);
        Node node = this; long mask;
        while(((mask = mask(hash, node.level)) & node.nodesBitMap) != 0L) {
            node = node.nodes[pos(mask, node.nodesBitMap)];
        }
        if ((mask & node.entriesBitMap) != 0L) {
            return node.entries[pos(mask, node.entriesBitMap)].containsKey(hash, key);
        }
        return false;
    }


    boolean containsValue(final Object value) {

        Node[] nodeStack = null;
        int[] posStack = null;

        Node node = this;
        int nodeLevel = 0;

        do {

            if (node.entriesBitMap != 0L) {
                for (final Entry entry : node.entries) {
                    if (entry.containsValue(value)) {
                        return true;
                    }
                }
            }

            if (node.nodesBitMap != 0L) {
                if (nodeStack == null) {
                    nodeStack = new Node[MAX_LEVEL];
                    posStack = new int[MAX_LEVEL];
                }
                nodeStack[nodeLevel] = node;
                posStack[nodeLevel] = 0;
            } else {
                while (--nodeLevel >= 0 && (++posStack[nodeLevel] >= nodeStack[nodeLevel].nodes.length));
            }

            if (nodeLevel >= 0) {
                node = nodeStack[nodeLevel].nodes[posStack[nodeLevel]];
                nodeLevel++;
            }

        } while (nodeLevel >= 0);

        return false;

    }


    // May return Entry.NOT_FOUND if not found (so that it can be differentiated from a null value)
    Object get(final Object key) {
        final int hash = Entry.hash(key);
        Node node = this; long mask;
        while(((mask = mask(hash, node.level)) & node.nodesBitMap) != 0L) {
            node = node.nodes[pos(mask, node.nodesBitMap)];
        }
        if ((mask & node.entriesBitMap) != 0L) {
            return node.entries[pos(mask, node.entriesBitMap)].get(key);
        }
        return Entry.NOT_FOUND;
    }



    Node put(final Entry entry) {

        final int hash = entry.hash;

        Node[] nodeStack = null;
        int[] posStack = null;
        int stackIdx = -1;

        Node node = this;
        long mask = mask(hash, node.level);

        if ((mask & node.nodesBitMap) != 0L) {
            // There is a node at the selected position: nodes will be stacked until reaching the level
            // at which the data is or should be

            nodeStack = new Node[MAX_LEVEL];
            posStack = new int[MAX_LEVEL];

            int pos;
            do {
                stackIdx++;
                nodeStack[stackIdx] = node;
                posStack[stackIdx] = pos = pos(mask, node.nodesBitMap);
                node = node.nodes[pos];
            } while(((mask = mask(hash, node.level)) & node.nodesBitMap) != 0L);

        }

        Node newNode;
        final int entryPos = pos(mask, node.entriesBitMap);

        if (entryPos < 0) {
            // There is nothing at the selected position: an entry will be created

            final int newEntryPos = (entryPos ^ NEG_MASK); // Turn negative entryPos positive

            final long newEntriesBitMap = node.entriesBitMap | mask;
            final Entry[] newEntries = new Entry[node.entries.length + 1];
            System.arraycopy(node.entries, 0, newEntries, 0, newEntryPos);
            System.arraycopy(node.entries, newEntryPos, newEntries, newEntryPos + 1, node.entries.length - newEntryPos);
            newEntries[newEntryPos] = entry;

            newNode = new Node(node.level, node.size + 1, node.nodesBitMap, node.nodes, newEntriesBitMap, newEntries);

        } else {
            // There is an entry at the selected position: either replace (if keys match) or create level / collision

            final Entry oldEntry = node.entries[entryPos];

            if (oldEntry.containsKey(hash, entry.key)) {
                // There is a match (key exists): entry needs to be replaced

                final Entry newEntry = oldEntry.set(entry);
                if (newEntry == oldEntry) {
                    // No need to change anything at any level if changes were not made
                    return this;
                }

                final Entry[] newEntries = Arrays.copyOf(node.entries, node.entries.length, Entry[].class);
                newEntries[entryPos] = newEntry;

                newNode = new Node(node.level, node.size, node.nodesBitMap, node.nodes, node.entriesBitMap, newEntries);

            } else if (node.level == MAX_LEVEL) {
                // No new levels can be created, so a collision entry will be created or expanded

                final Entry[] newEntries = Arrays.copyOf(node.entries, node.entries.length, Entry[].class);
                newEntries[entryPos] = oldEntry.add(entry);

                newNode = new Node(node.level, node.size + 1, node.nodesBitMap, node.nodes, node.entriesBitMap, newEntries);

            } else {
                // A new level will be created, a node will replace the existing entry

                final Node deeperNode = createNewLevel(node.level + 1, oldEntry, entry);
                final int deeperNodePos = (pos(mask, node.nodesBitMap) ^ NEG_MASK);

                final long newNodesBitMap = node.nodesBitMap ^ mask;
                final long newEntriesBitMap = node.entriesBitMap ^ mask;

                final Node[] newNodes = new Node[node.nodes.length + 1];
                System.arraycopy(node.nodes, 0, newNodes, 0, deeperNodePos);
                System.arraycopy(node.nodes, deeperNodePos, newNodes, deeperNodePos + 1, node.nodes.length - deeperNodePos);
                newNodes[deeperNodePos] = deeperNode;

                final Entry[] newEntries = new Entry[node.entries.length - 1];
                System.arraycopy(node.entries, 0, newEntries, 0, entryPos);
                System.arraycopy(node.entries, entryPos + 1, newEntries, entryPos, node.entries.length - (entryPos + 1));

                newNode = new Node(node.level, node.size + 1, newNodesBitMap, newNodes, newEntriesBitMap, newEntries);

            }

        }

        Node oldNode;
        for ( ; stackIdx >= 0; stackIdx--) {

            oldNode = node;
            node = nodeStack[stackIdx];

            final Node[] newNodes = Arrays.copyOf(node.nodes, node.nodes.length, Node[].class);
            newNodes[posStack[stackIdx]] = newNode;

            newNode = new Node(node.level, node.size + (newNode.size - oldNode.size), node.nodesBitMap, newNodes, node.entriesBitMap, node.entries);

        }

        return newNode;

    }



    Node remove(final int hash, final Object key) {

        Node[] nodeStack = null;
        int[] posStack = null;
        int stackIdx = -1;

        Node node = this;
        long mask = mask(hash, node.level);

        if ((mask & node.nodesBitMap) != 0L) {
            // There is a node at the selected position: nodes will be stacked until reaching the level
            // at which the data is or should be

            nodeStack = new Node[MAX_LEVEL];
            posStack = new int[MAX_LEVEL];

            int pos;
            do {
                stackIdx++;
                nodeStack[stackIdx] = node;
                posStack[stackIdx] = pos = pos(mask, node.nodesBitMap);
                node = node.nodes[pos];
            } while(((mask = mask(hash, node.level)) & node.nodesBitMap) != 0L);

        }

        Node newNode;
        final int entryPos = pos(mask, node.entriesBitMap);

        if (entryPos < 0) {
            // There is nothing at the position that the removed key should be at: nothing to remove
            return this;
        }

        final Entry oldEntry = node.entries[entryPos];
        final Entry newEntry = oldEntry.remove(hash, key);

        if (newEntry == oldEntry) {
            // No need to change anything at any level if changes were not made (key was not found)
            return this;
        }

        if (newEntry != null) {
            // This was a collision entry from which a collision mapping was removed

            final Entry[] newEntries = Arrays.copyOf(node.entries, node.entries.length, Entry[].class);
            newEntries[entryPos] = newEntry;
            newNode = new Node(node.level, node.size - 1, node.nodesBitMap, node.nodes, node.entriesBitMap, newEntries);

        } else {

            final long newEntriesBitMap = node.entriesBitMap ^ mask;
            final Entry[] newEntries;
            if (newEntriesBitMap == 0L) {
                newEntries = EMPTY_ENTRIES;
                if (node.nodesBitMap == 0L) {
                    // Empty node: this can only happen at the root node (no stack), when the map gets cleared
                    // At any other level, we would reduce nodes upwards when they have only one (non-collision) entry
                    return EMPTY_NODE;
                }
            } else {
                newEntries = new Entry[node.entries.length - 1];
                System.arraycopy(node.entries, 0, newEntries, 0, entryPos);
                System.arraycopy(node.entries, entryPos + 1, newEntries, entryPos, node.entries.length - (entryPos + 1));
            }

            newNode = new Node(node.level, node.size - 1, node.nodesBitMap, node.nodes, newEntriesBitMap, newEntries);

        }

        int nodePos;
        for ( ; stackIdx >= 0; stackIdx--) {

            node = nodeStack[stackIdx];
            nodePos = posStack[stackIdx];

            if (newNode.nodes.length > 0 || newNode.entries.length > 1 || newNode.entries[0].collisions != null) {
                // The new node has at least one node, or two entries, or one collision entry that must live at level 5
                // There is no possibility to "reduce" the node into the upper level. The node will be simply replaced

                final Node[] newNodes = Arrays.copyOf(node.nodes, node.nodes.length, Node[].class);
                newNodes[nodePos] = newNode;

                newNode = new Node(node.level, node.size - 1, node.nodesBitMap, newNodes, node.entriesBitMap, node.entries);

            } else {
                // The new node can be reduced into the upper level as a mere Entry

                final Entry reducedEntry = newNode.entries[0];
                final long reducedEntryMask = mask(reducedEntry.hash, node.level);
                // The pos computed below cannot be positive because there is no entry at that index given there is a node
                final int reducedEntryPos = (pos(reducedEntryMask, node.entriesBitMap) ^ NEG_MASK);

                final long newNodesBitMap = node.nodesBitMap ^ reducedEntryMask;      // Remove mask from node bitmap
                final long newEntriesBitMap = node.entriesBitMap ^ reducedEntryMask;  // Add mask to entry bitmap

                final Entry[] newEntries = new Entry[node.entries.length + 1];
                System.arraycopy(node.entries, 0, newEntries, 0, reducedEntryPos);
                System.arraycopy(node.entries, reducedEntryPos, newEntries, reducedEntryPos + 1, node.entries.length - reducedEntryPos);
                newEntries[reducedEntryPos] = reducedEntry;

                final Node[] newNodes = new Node[node.nodes.length - 1];
                System.arraycopy(node.nodes, 0, newNodes, 0, nodePos);
                System.arraycopy(node.nodes, nodePos + 1, newNodes, nodePos, node.nodes.length - (nodePos + 1));

                newNode = new Node(node.level, node.size - 1, newNodesBitMap, newNodes, newEntriesBitMap, newEntries);

            }

        }

        return newNode;


    }


    Set<Entry> allEntries() {

        final Set<Entry> entrySet = new HashSet<>(this.size + 1, 1.0f);

        Node[] nodeStack = null;
        int[] posStack = null;

        Node node = this;
        int nodeLevel = 0;

        Entry[] collisions;
        do {

            if (node.entriesBitMap != 0L) {
                for (final Entry entry : node.entries) {
                    collisions = entry.collisions;
                    if (collisions == null) {
                        entrySet.add(entry);
                    } else {
                        Collections.addAll(entrySet, collisions);
                    }
                }
            }

            if (node.nodesBitMap != 0L) {
                if (nodeStack == null) {
                    nodeStack = new Node[MAX_LEVEL];
                    posStack = new int[MAX_LEVEL];
                }
                nodeStack[nodeLevel] = node;
                posStack[nodeLevel] = 0;
            } else {
                while (--nodeLevel >= 0 && (++posStack[nodeLevel] >= nodeStack[nodeLevel].nodes.length));
            }

            if (nodeLevel >= 0) {
                node = nodeStack[nodeLevel].nodes[posStack[nodeLevel]];
                nodeLevel++;
            }

        } while (nodeLevel >= 0);

        return entrySet;

    }


    Set<Object> allKeys() {

        final Set<Object> keySet = new HashSet<>(this.size + 1, 1.0f);

        Node[] nodeStack = null;
        int[] posStack = null;

        Node node = this;
        int nodeLevel = 0;

        Entry[] collisions;
        do {

            if (node.entriesBitMap != 0L) {
                for (final Entry entry : node.entries) {
                    collisions = entry.collisions;
                    if (collisions == null) {
                        keySet.add(entry.key);
                    } else {
                        for (final Entry collision : collisions) {
                            keySet.add(collision.key);
                        }
                    }
                }
            }

            if (node.nodesBitMap != 0L) {
                if (nodeStack == null) {
                    nodeStack = new Node[MAX_LEVEL];
                    posStack = new int[MAX_LEVEL];
                }
                nodeStack[nodeLevel] = node;
                posStack[nodeLevel] = 0;
            } else {
                while (--nodeLevel >= 0 && (++posStack[nodeLevel] >= nodeStack[nodeLevel].nodes.length));
            }

            if (nodeLevel >= 0) {
                node = nodeStack[nodeLevel].nodes[posStack[nodeLevel]];
                nodeLevel++;
            }

        } while (nodeLevel >= 0);

        return keySet;

    }


    List<Object> allValues() {

        final List<Object> valueList = new ArrayList<>(this.size);

        Node[] nodeStack = null;
        int[] posStack = null;

        Node node = this;
        int nodeLevel = 0;

        Entry[] collisions;
        do {

            if (node.entriesBitMap != 0L) {
                for (final Entry entry : node.entries) {
                    collisions = entry.collisions;
                    if (collisions == null) {
                        valueList.add(entry.value);
                    } else {
                        for (final Entry collision : collisions) {
                            valueList.add(collision.value);
                        }
                    }
                }
            }

            if (node.nodesBitMap != 0L) {
                if (nodeStack == null) {
                    nodeStack = new Node[MAX_LEVEL];
                    posStack = new int[MAX_LEVEL];
                }
                nodeStack[nodeLevel] = node;
                posStack[nodeLevel] = 0;
            } else {
                while (--nodeLevel >= 0 && (++posStack[nodeLevel] >= nodeStack[nodeLevel].nodes.length));
            }

            if (nodeLevel >= 0) {
                node = nodeStack[nodeLevel].nodes[posStack[nodeLevel]];
                nodeLevel++;
            }

        } while (nodeLevel >= 0);

        return valueList;

    }


}
