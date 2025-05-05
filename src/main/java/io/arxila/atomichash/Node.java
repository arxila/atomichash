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

    private static final long serialVersionUID = 5892440116260222326L;

    static final int MAX_LEVEL = 5;
    static final int[] HASH_SHIFTS = new int[] { 0, 6, 12, 18, 24, 30 };
    static final int HASH_MASK = 0b111111;
    static final int NEG_MASK = 1 << 31; // will be used for turning 0..63 int positions into negative

    static final Node[] EMPTY_NODES = new Node[0];
    static final Entry[] EMPTY_ENTRIES = new Entry[0];
    static final Node EMPTY_NODE = new Node();

    final int level;
    final int size;
    final long nodesBitMap;
    final Node[] nodes;
    final long entriesBitMap;
    final Entry[] entries;

    private transient Set<Entry> entrySet;
    private transient Set<Object> keySet;
    private transient List<Object> valueList;



    private Node() {
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


    private Node(final int level, final Entry entry0, final Entry entry1) {

        super();

        this.level = level;
        this.size = 2;

        final long mask0 = mask(entry0.hash, this.level);
        final long mask1 = mask(entry1.hash, this.level);

        if (mask0 != mask1) {

            final int index0 = index(entry0.hash, this.level);
            final int index1 = index(entry1.hash, this.level);

            this.nodesBitMap = 0L;
            this.nodes = EMPTY_NODES;

            this.entriesBitMap = (mask0 | mask1);
            this.entries = (index0 < index1) ? new Entry[] {entry0, entry1} : new Entry[] {entry1, entry0};

        } else {
            // We have an index match at this level, so we will need to (try) to create a new level
            if (this.level == MAX_LEVEL) {
                // We have no more levels, so we need an Entry with collisions

                this.nodesBitMap = 0L;
                this.nodes = EMPTY_NODES;

                this.entriesBitMap = mask0;
                this.entries = new Entry[] { entry0.add(entry1) };

            } else {
                // We need an additional level to further differentiate entries

                this.nodesBitMap = mask0;
                this.nodes = new Node[]{ new Node(this.level + 1, entry0, entry1) };

                this.entriesBitMap = 0L;
                this.entries = EMPTY_ENTRIES;

            }
        }
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
        if (this.entriesBitMap != 0L) {
            for (final Entry entry : this.entries) {
                if (entry.containsValue(value)) {
                    return true;
                }
            }
        }
        if (this.nodesBitMap != 0L) {
            for (final Node node : this.nodes) {
                if (node.containsValue(value)) {
                    return true;
                }
            }
        }
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
        final long mask = mask(hash, this.level);

        final int nodePos = pos(mask, this.nodesBitMap);

        if (nodePos >= 0) {
            // There is a node at the selected position: the put operation will be delegated

            final Node oldNode = this.nodes[nodePos];
            final Node newNode = oldNode.put(entry);
            if (oldNode == newNode) {
                return this;
            }

            final Node[] newNodeValues = Arrays.copyOf(this.nodes, this.nodes.length, Node[].class);
            newNodeValues[nodePos] = newNode;

            return new Node(this.level, this.size + (newNode.size - oldNode.size), this.nodesBitMap, newNodeValues, this.entriesBitMap, this.entries);

        }

        final int entryPos = pos(mask, this.entriesBitMap);

        if (entryPos < 0) {
            // There is nothing at the selected position: an entry will be created

            final int newEntryPos = (entryPos ^ NEG_MASK);

            final long newEntriesBitMap = this.entriesBitMap | mask;
            final Entry[] newEntries = new Entry[this.entries.length + 1];
            System.arraycopy(this.entries, 0, newEntries, 0, newEntryPos);
            newEntries[newEntryPos] = entry;
            System.arraycopy(this.entries, newEntryPos, newEntries, newEntryPos + 1, this.entries.length - newEntryPos);

            return new Node(this.level, this.size + 1, this.nodesBitMap, this.nodes, newEntriesBitMap, newEntries);

        }

        // There was an entry at the selected position: either replace (if keys match) or create level / collision
        final Entry oldEntry = this.entries[entryPos];

        if (oldEntry.containsKey(hash, entry.key)) {
            // There is a match (key exists): entry needs to be replaced

            final Entry newEntry = oldEntry.set(entry);
            if (oldEntry == newEntry) {
                return this;
            }

            final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length, Entry[].class);
            newEntries[entryPos] = newEntry;

            return new Node(this.level, this.size, this.nodesBitMap, this.nodes, this.entriesBitMap, newEntries);

        }

        if (this.level == MAX_LEVEL) {
            // No new levels can be created, so a collision entry will be created or expanded

            final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length, Entry[].class);
            newEntries[entryPos] = oldEntry.add(entry);

            return new Node(this.level, this.size + 1, this.nodesBitMap, this.nodes, this.entriesBitMap, newEntries);

        }

        // A new level will be created, a node will replace the existing entry
        final Node newNode = new Node(this.level + 1, (Entry)oldEntry, entry);
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



    Node remove(final int hash, final Object key) {

        final long mask = mask(hash, this.level);

        final int entryPos = pos(mask, this.entriesBitMap);
        if (entryPos >= 0) {

            final Entry oldEntry = this.entries[entryPos];
            final Entry newEntry = oldEntry.remove(hash, key);
            if (newEntry == oldEntry) {
                return this;
            }

            if (newEntry != null) {
                final Entry[] newEntries = Arrays.copyOf(this.entries, this.entries.length, Entry[].class);
                newEntries[entryPos] = newEntry;
                return new Node(this.level, this.size - 1, this.nodesBitMap, this.nodes, this.entriesBitMap, newEntries);
            }

            final long newEntriesBitMap = this.entriesBitMap ^ mask;
            final Entry[] newEntries;
            if (newEntriesBitMap == 0L) {
                if (this.nodesBitMap == 0L) {
                    return EMPTY_NODE;
                }
                newEntries = EMPTY_ENTRIES;
            } else {
                newEntries = new Entry[this.entries.length - 1];
                System.arraycopy(this.entries, 0, newEntries, 0, entryPos);
                System.arraycopy(this.entries, entryPos + 1, newEntries, entryPos, this.entries.length - (entryPos + 1));
            }

            return new Node(this.level, this.size - 1, this.nodesBitMap, this.nodes, newEntriesBitMap, newEntries);

        }

        final int nodePos = pos(mask, this.nodesBitMap);
        if (nodePos < 0) {
            return this;
        }

        final Node oldNode = this.nodes[nodePos];
        final Node newNode = oldNode.remove(hash, key);

        if (oldNode == newNode) {
            return this;
        }

        if (newNode.nodes.length > 0 || newNode.entries.length > 1 || newNode.entries[0].collisions != null) {

            final Node[] newNodes = Arrays.copyOf(this.nodes, this.nodes.length, Node[].class);
            newNodes[nodePos] = newNode;

            return new Node(this.level, this.size - 1, this.nodesBitMap, newNodes, this.entriesBitMap, this.entries);

        }

        // The new Node at level + 1 now has a single non-collision Entry, so we need to link that Entry instead

        final Entry newEntry = newNode.entries[0];
        final int newEntryPos = (entryPos ^ NEG_MASK);

        final long newNodeBitMap = this.nodesBitMap ^ mask;
        final long newEntryBitMap = this.entriesBitMap ^ mask;

        final Entry[] newEntries = new Entry[this.entries.length + 1];
        System.arraycopy(this.entries, 0, newEntries, 0, newEntryPos);
        newEntries[newEntryPos] = newEntry;
        System.arraycopy(this.entries, newEntryPos, newEntries, newEntryPos + 1, this.entries.length - newEntryPos);

        final Node[] newNodes = new Node[this.nodes.length - 1];
        System.arraycopy(this.nodes, 0, newNodes, 0, nodePos);
        System.arraycopy(this.nodes, nodePos + 1, newNodes, nodePos, this.nodes.length - (nodePos + 1));

        return new Node(this.level, this.size - 1, newNodeBitMap, newNodes, newEntryBitMap, newEntries);

    }


    Set<Entry> allEntries() {
        Set<Entry> entrySet;
        if ((entrySet = this.entrySet) != null) {
            return entrySet;
        }
        entrySet = new HashSet<>(this.size + 1, 1.0f);
        addEntries(entrySet);
        return this.entrySet = Collections.unmodifiableSet(entrySet);
    }

    private void addEntries(final Set<Entry> entrySet) {
        if (this.entriesBitMap != 0L) {
            for (final Entry entry : this.entries) {
                if (entry.collisions == null) {
                    entrySet.add(entry);
                } else {
                    Collections.addAll(entrySet, entry.collisions);
                }
            }
        }
        if (this.nodesBitMap != 0L) {
            for (final Node node : this.nodes) {
                node.addEntries(entrySet);
            }
        }
    }



    Set<Object> allKeys() {
        Set<Object> keySet;
        if ((keySet = this.keySet) != null) {
            return keySet;
        }
        keySet = new HashSet<>(this.size + 1, 1.0f);
        addKeys(keySet);
        return this.keySet = Collections.unmodifiableSet(keySet);
    }

    private void addKeys(final Set<Object> keySet) {
        if (this.entriesBitMap != 0L) {
            for (final Entry entry : this.entries) {
                if (entry.collisions == null) {
                    keySet.add(entry.key);
                } else {
                    for (final Entry collision : entry.collisions) {
                        keySet.add(collision.key);
                    }
                }
            }
        }
        if (this.nodesBitMap != 0L) {
            for (final Node node : this.nodes) {
                node.addKeys(keySet);
            }
        }
    }



    List<Object> allValues() {
        List<Object> valueList;
        if ((valueList = this.valueList) != null) {
            return valueList;
        }
        valueList = new ArrayList<>(this.size);
        addValues(valueList);
        return this.valueList = Collections.unmodifiableList(valueList);
    }

    private void addValues(final List<Object> valueList) {
        if (this.entriesBitMap != 0L) {
            for (final Entry entry : this.entries) {
                if (entry.collisions == null) {
                    valueList.add(entry.value);
                } else {
                    for (final Entry collision : entry.collisions) {
                        valueList.add(collision.value);
                    }
                }
            }
        }
        if (this.nodesBitMap != 0L) {
            for (final Node node : this.nodes) {
                node.addValues(valueList);
            }
        }
    }


}
