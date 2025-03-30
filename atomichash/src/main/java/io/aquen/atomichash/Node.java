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

final class Node<K,V> implements Serializable {

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


    Node(final int level, final KeyValue<K,V> keyValue0, final KeyValue<K,V> keyValue1) {
        super();
        this.level = level;
        final int index0 = keyValue0.key.indices[this.level];
        final int index1 = keyValue1.key.indices[this.level];
        this.bitMap = (1L << index0) | (1L << index1);
        if (index0 != index1) {
            this.values = (index0 < index1) ? new Object[] { keyValue0, keyValue1 } : new Object[] { keyValue1, keyValue0 };
        } else {
            // TODO When we are at the last level we cannot just level + 1
            this.values = new Object[] { new Node<>(this.level + 1, keyValue0, keyValue1) };
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
    private static <K> int valuePos(final int level, final long bitMap, final Key<K> key) {
        final long indexMask = 1L << key.indices[level];
        final int pos = Long.bitCount(bitMap & (indexMask - 1L));
        return ((bitMap & indexMask) == 0L) ? (pos ^ NEG_MASK) : pos; // negative if absent, positive if present
    }


    public boolean contains(final Key<K> key) {
        return valuePos(this.level, this.bitMap, key) >= 0;
    }


    @SuppressWarnings("unchecked")
    public KeyValue<K,V> get(final Key<K> key) {
        final int pos = valuePos(this.level, this.bitMap, key);
        if (pos < 0) {
            return null;
        }
        final Object value = this.values[pos];
        if (value instanceof KeyValue) {
            return (KeyValue<K,V>) value;
        }
        return ((Node<K,V>)value).get(key);
    }



    public Node<K,V> put(final KeyValue<K,V> newKV) {

        final int pos = valuePos(this.level, this.bitMap, newKV.key);

        if (pos < 0) {
            // There was nothing at this position before

            final int newPos = (pos ^ NEG_MASK);

            final long newBitMap = this.bitMap | (1L << newPos);
            final Object[] newValues = new Object[this.values.length + 1];
            System.arraycopy(this.values, 0, newValues, 0, newPos);
            newValues[newPos] = newKV;
            System.arraycopy(this.values, newPos, newValues, newPos + 1, this.values.length - newPos);

            return new Node<>(this.level, newBitMap, newValues);

        }

        // There was a value at this position, we will need to replace or modify

        final Object existingValue = this.values[pos];
        final Object[] newValues = Arrays.copyOf(this.values, this.values.length, Object[].class);

        final Object newValue;
        if (existingValue instanceof KeyValue<?,?>) {
            final KeyValue<K,V> existingKV = (KeyValue<K, V>) this.values[pos];
            // TODO When we are at the last level we cannot just level + 1
            newValue = (existingKV.key.equals(newKV.key)) ? newKV : new Node<>(this.level + 1, existingKV, newKV);
        } else {
            newValue = ((Node<K,V>) existingValue).put(newKV);
        }
        newValues[pos] = newValue;

        return new Node<>(this.level, this.bitMap, newValues);

    }



    Node<K,V> putAll(final Node<K,V> other) {
        // this.level and other.level will always be the same

        // The bitmap for the new compressed array will contain the bits in both nodes
        final long newBitMap = this.bitMap | other.bitMap;
        // We will need as many new values as bits set in the new bitmap
        final Object[] newValues = new Object[Long.bitCount(newBitMap)];

        // Three indices needed to traverse the existing, the other's and the new compressed array
        int thisIndex = 0, otherIndex = 0, newIndex = 0;

        // We need to iterate all the positions the array can have (64 bits in a long)
        for (long mask = 1L; mask != 0; mask <<= 1) {

            final boolean inThis = (this.bitMap & mask) != 0;
            final boolean inOther = (other.bitMap & mask) != 0;

            final Object newValue;
            if (inThis && inOther) {
                // Both nodes contain an entry for this position so we need to merge

                final Object thisValue = this.values[thisIndex++];
                final Object otherValue = other.values[otherIndex++];

                final KeyValue<K,V> thisKV = (thisValue instanceof KeyValue<?,?>) ? (KeyValue<K,V>) thisValue : null;
                final KeyValue<K,V> otherKV = (otherValue instanceof KeyValue<?,?>) ? (KeyValue<K,V>) otherValue : null;

                if (thisKV != null && otherKV != null) {
                    // TODO When we are at the last level we cannot just level + 1
                    newValue = new Node<>(this.level + 1, thisKV, otherKV);
                } else if (thisKV != null) {
                    newValue = ((Node<K,V>)otherValue).put(thisKV);
                } else if (otherKV != null) {
                    newValue = ((Node<K,V>)thisValue).put(otherKV);
                } else {
                    // thisKV == null && otherKV == null, both are nodes and a putAll is needed
                    newValue = ((Node<K,V>)thisValue).putAll((Node<K,V>)otherValue);
                }

            } else if (inThis) {
                newValue = this.values[thisIndex++];
            } else if (inOther) {
                newValue = other.values[otherIndex++];
            } else {
                // Not found in any of the nodes, we should skip
                continue;
            }
            newValues[newIndex++] = newValue;

        }

        return new Node<>(this.level, newBitMap, newValues);

    }



}
