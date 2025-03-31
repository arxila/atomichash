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

    private static final int NEG_MASK = 1 << 31; // will be used for turning 0..63 positions into negative

    final int level;
    final long bitMap;
    final Object[] values;


    Node(final int level, final long bitMap, final Object[] values) {
        super();
        this.level = level;
        this.bitMap = bitMap;
        this.values = values;
    }


    private Node(final int level, final DataEntry dataEntry0, final DataEntry dataEntry1) {
        super();
        this.level = level;
        final int index0 = dataEntry0.hash.indices[this.level];
        final int index1 = dataEntry1.hash.indices[this.level];
        this.bitMap = (1L << index0) | (1L << index1);
        if (index0 != index1) {
            this.values = (index0 < index1) ? new Object[] { dataEntry0, dataEntry1 } : new Object[] { dataEntry1, dataEntry0 };
        } else {
            // We have an index match at this level, so we will need to (try) to create a new level
            if (this.level == Hash.MAX_LEVEL) {
                // We have no more levels, so we need a MultiDataEntry
                this.values = new Object[] { dataEntry0.addKeyValue(dataEntry1.keyValue) };
            } else {
                // We need an additional level to further differentiate entries
                this.values = new Object[]{ new Node(this.level + 1, dataEntry0, dataEntry1) };
            }
        }
    }

    // TODO There is still need for a constructor that takes a KeyValue[] as an argument. This will
    //      be executed outside the critical path so it won't be as important that it's fast, but nevertheless
    //      its performance should be cared for.

    /*
     * Computes the position in the compact array by using the bitmap. This bitmap will contain 1's for
     * every position of the possible 64 (max size of the values array) that can contain an element. The
     * position in the array will correspond to the number of positions occupied before the index, i.e. the
     * number of bits to the right of the bit corresponding to the index for this level.
     *
     * This algorithm benefits from the fact that Long.bitCount is an intrinsic candidate typically implemented
     * as a single "population count" CPU instruction, and thus the computation will be O(1).
     */
    private static <K> int valuePos(final int level, final long bitMap, final Hash hash) {
        final long indexMask = 1L << hash.indices[level];
        final int pos = Long.bitCount(bitMap & (indexMask - 1L));
        return ((bitMap & indexMask) == 0L) ? (pos ^ NEG_MASK) : pos; // negative if absent, positive if present
    }


    boolean contains(final Hash hash) {
        return valuePos(this.level, this.bitMap, hash) >= 0;
    }


    KeyValue get(final Hash hash, final Object key) {
        final int pos = valuePos(this.level, this.bitMap, hash);
        if (pos < 0) {
            return null;
        }
        final Object value = this.values[pos];
        if (value instanceof Node) {
            return ((Node)value).get(hash, key);
        } else if (value instanceof DataEntry) {
            return ((DataEntry)value).get(key);
        } else { // value instanceof MultiDataEntry
            return ((MultiDataEntry)value).get(key);
        }
    }



    public Node put(final DataEntry dataEntry, final boolean replaceIfPresent) {

        final int pos = valuePos(this.level, this.bitMap, dataEntry.hash);

        if (pos < 0) {
            // There was nothing at this position before

            final int newPos = (pos ^ NEG_MASK);

            final long newBitMap = this.bitMap | (1L << newPos);
            final Object[] newValues = new Object[this.values.length + 1];
            System.arraycopy(this.values, 0, newValues, 0, newPos);
            newValues[newPos] = dataEntry;
            System.arraycopy(this.values, newPos, newValues, newPos + 1, this.values.length - newPos);

            return new Node(this.level, newBitMap, newValues);

        }

        // There was a value at this position, we will need to replace or modify

        final Object oldValue = this.values[pos];

        // Will be returned as null if no changes are needed
        final Object newValue = computeNewValueForPut(this.level, oldValue, dataEntry, replaceIfPresent);
        if (newValue == null) {
            // Nothing needs to be changed
            return this;
        }

        final Object[] newValues = Arrays.copyOf(this.values, this.values.length, Object[].class);
        newValues[pos] = newValue;

        return new Node(this.level, this.bitMap, newValues);

    }


    private static Object computeNewValueForPut(
            final int level, final Object oldValue, final DataEntry dataEntry, final boolean replaceIfPresent) {

        if (oldValue instanceof Node) {
            final Node oldNode = (Node) oldValue;

            final Node newNode = oldNode.put(dataEntry, replaceIfPresent);
            return (oldNode != newNode) ? newNode : null;

        }

        if (oldValue instanceof DataEntry) {
            final DataEntry oldEntry = (DataEntry) oldValue;

            if (oldEntry.matches(dataEntry.keyValue.key)) {
                // A match was found and entry should be replaced (if flagged to do so)
                final DataEntry newEntry = oldEntry.replaceKeyValue(dataEntry.keyValue, replaceIfPresent);
                return (oldEntry != newEntry) ? newEntry : null;
            }
            if (level == Hash.MAX_LEVEL) {
                // No match and at the deepest level, so a MultiDataEntry should be created
                return oldEntry.addKeyValue(dataEntry.keyValue);
            }

            // There is no match, a new level can be created
            return new Node(level + 1, oldEntry, dataEntry);

        }

        // oldValue instanceof MultiDataEntry and level is Hash.MAX_LEVEL
        final MultiDataEntry oldEntry = (MultiDataEntry) oldValue;
        final MultiDataEntry newEntry = oldEntry.addOrReplaceKeyValue(dataEntry.keyValue, replaceIfPresent);
        return (oldEntry != newEntry) ? newEntry : null;

    }



    Node putAll(final Node other) {
        // this.level and other.level will always be the same

        // The bitmap for the new compressed array will contain the bits in both nodes
        final long newBitMap = this.bitMap | other.bitMap;
        // We will need as many new values as bits set in the new bitmap
        final Object[] newValues = new Object[Long.bitCount(newBitMap)];

        // Three indices needed to traverse this, the other's and the new compressed array
        int thisIndex = 0, otherIndex = 0, newIndex = 0;

        // We need to iterate all the positions the array can have (64 bits in a long)
        for (long mask = 1L; mask != 0; mask <<= 1) {

            final boolean inThis = (this.bitMap & mask) != 0;
            final boolean inOther = (other.bitMap & mask) != 0;

            if (!inThis && !inOther) {
                // Not found in any of the nodes, we should skip
                continue;
            }

            final Object newValue;
            if (inThis && inOther) {
                // Both nodes contain an entry for this position so we need to merge

                final Object thisValue = this.values[thisIndex++];
                final Object otherValue = other.values[otherIndex++];

                newValue = (this.level < Hash.MAX_LEVEL) ?
                                computeNewValueForPutAllInterLevel(this.level, thisValue, otherValue)
                              : computeNewValueForPutAllMaxLevel(thisValue, otherValue);

            } else if (inThis) {
                newValue = this.values[thisIndex++];
            } else { // inOther
                newValue = other.values[otherIndex++];
            }
            newValues[newIndex++] = newValue;

        }

        return new Node(this.level, newBitMap, newValues);

    }


    private static Object computeNewValueForPutAllInterLevel(
            final int level, final Object thisValue, final Object otherValue) {
        // At an intermediate level, Node values can exist but MultiDataEntry values cannot

        final Node thisNode = (thisValue instanceof Node) ? (Node) thisValue : null;
        final Node otherNode = (otherValue instanceof Node) ? (Node) otherValue : null;

        if (thisNode != null && otherNode != null) {
            return thisNode.putAll(otherNode);
        }

        if (thisNode != null) {
            final DataEntry otherEntry = (DataEntry)otherValue;
            return thisNode.put(otherEntry, true);
        }

        if (otherNode != null) {
            final DataEntry thisEntry = (DataEntry)thisValue;
            return otherNode.put(thisEntry, false);  // !replaceIfPresent because "other" has precedence
        }

        // Both thisValue and otherValue are DataEntry
        final DataEntry thisDataEntry = (DataEntry)thisValue;
        final DataEntry otherDataEntry = (DataEntry)otherValue;

        if (thisDataEntry.matches(otherDataEntry.keyValue)) {
            return thisDataEntry.replaceKeyValue(otherDataEntry.keyValue, true);
        }
        return new Node(level + 1, thisDataEntry, otherDataEntry);

    }




    private static Object computeNewValueForPutAllMaxLevel(final Object thisValue, final Object otherValue) {
        // At Hash.MAX_LEVEL level, MultiDataEntry values can exist but Node values cannot

        final MultiDataEntry thisMultiEntry = (thisValue instanceof MultiDataEntry) ? (MultiDataEntry) thisValue : null;
        final MultiDataEntry otherMultiEntry = (otherValue instanceof MultiDataEntry) ? (MultiDataEntry) otherValue : null;

        if (thisMultiEntry != null && otherMultiEntry != null) {
            return thisMultiEntry.addOrReplaceKeyValues(otherMultiEntry.keyValues);
        }

        if (thisMultiEntry != null) {
            final DataEntry otherDataEntry = (DataEntry)otherValue;
            return thisMultiEntry.addOrReplaceKeyValue(otherDataEntry.keyValue, true);
        }

        if (otherMultiEntry != null) {
            final DataEntry thisDataEntry = (DataEntry)thisValue;
            return otherMultiEntry.addOrReplaceKeyValue(thisDataEntry.keyValue, false);  // !replaceIfPresent because "other" has precedence
        }

        // Both thisValue and otherValue are DataEntry
        final DataEntry thisDataEntry = (DataEntry)thisValue;
        final DataEntry otherDataEntry = (DataEntry)otherValue;

        if (thisDataEntry.matches(otherDataEntry.keyValue)) {
            return thisDataEntry.replaceKeyValue(otherDataEntry.keyValue, true);
        }
        return thisDataEntry.addKeyValue(otherDataEntry.keyValue);

    }


}
