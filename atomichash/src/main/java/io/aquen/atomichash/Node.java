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

final class Node<K,V> implements Serializable {

    private static final long serialVersionUID = 5892440116260222326L;

    final int level;
    final long bitMap;
    final Object[] values;


    Node(final int level, final KeyValue<K,V> keyValue) {
        super();
        this.level = level;
        this.bitMap = bitMap;
        this.values = values;
    }

    Node(final int level, final int bitMap, final Object[] values) {
        super();
        this.level = level;
        this.bitMap = bitMap;
        this.values = values;
    }

    /*
     * Computes the position in the compact array by using the bitmap. This bitmap will contain 1's for
     * every position of the possible 64 (max size of the values array) that can contain an element. The
     * position in the array will correspond to the number of positions occupied before the index, i.e. the
     * number of bits to the right of the bit corresponding to the index for this level.
     *
     * This algorithm benefits from the fact that Long.bitCount is an intrinsic candidate typically implemented
     * as a single "population count" CPU instruction, and thus the computation will be O(1).
     */
    private int valuePos(final Key<K> key) {
        final long indexMask = 1L << key.indices[this.level];
        final int pos = Long.bitCount(this.bitMap & (indexMask - 1L));
        return ((this.bitMap & indexMask) == 0L) ? -pos : pos; // negative if absent, positive if present
    }

    @SuppressWarnings("unchecked")
    public KeyValue<K,V> get(final Key<K> key) {
        final int pos = valuePos(key);
        if (pos < 0) {
            return null;
        }
        final Object value = this.values[pos];
        if (value instanceof KeyValue) {
            return (KeyValue<K,V>) value;
        }
        return ((Node<K,V>)value).get(key);
    }




}
