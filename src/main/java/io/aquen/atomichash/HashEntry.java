/*
 * =========================================================================
 *                                                                          
 *   Copyright (c) 2019-2024 Aquen (https://aquen.io)                  
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
import java.util.Map;

final class HashEntry<K,V> implements AtomicHashStore.Entry<K,V>, Serializable, Comparable<HashEntry<K,V>> {

    private static final long serialVersionUID = -4165737057742605795L;

    final int hash;
    final K key;
    final V value;




    HashEntry(final DataEntry<K,V> dataEntry) {
        super();
        this.hash = dataEntry.hash;
        this.key = dataEntry.key;
        this.value = dataEntry.value;
    }


    @Override
    public K getKey() {
        return this.key;
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public Object setValue(final Object value) {
        // (Not) implemented, allowed at the java.util.Map.Entry specification
        throw new UnsupportedOperationException("Setting values is forbidden in this implementation");
    }




    /**
     * Equivalent to Objects.equals(), but by being called only from
     * HashEntry we might benefit from runtime profile information on the
     * type of o1. See java.util.AbstractMap#eq().
     *
     * Do not replace with Object.equals until JDK-8015417 is resolved.
     */
    private static boolean eq(final Object o1, final Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }


    @Override
    public boolean equals(final Object o) {
        // Implemented according to the java.util.Map.Entry specification
        if (this == o) {
            return true;
        }
        if (!(o instanceof Map.Entry)) {
            return false;
        }
        final Map.Entry<?,?> e = (Map.Entry<?,?>)o;
        return eq(this.key, e.getKey()) && eq(this.value, e.getValue());
    }

    @Override
    public int hashCode() {
        // Implemented according to the java.util.Map.Entry specification
        return (this.key == null ? 0 : this.key.hashCode()) ^
                (this.value == null ? 0 : this.value.hashCode());
    }

    @Override
    public int compareTo(final HashEntry<K, V> o) {

        // We will need to order in the same way that entries would be returned by an iterator (tree inorder)
        // NOTE this class therefore has a natural ordering that is inconsistent with equals()

        final int h1 = this.hash;
        final int h2 = o.hash;

        if (h1 == h2) {
            // Hash collisions are solved by comparing the key's identity hash code.
            // NOTE it's important that we don't involve values here so that we can perform replaceAll
            // operations without needing to reorder the entries after value changes.
            return Integer.compare(
                        System.identityHashCode(this.key),
                        System.identityHashCode(o.key));
        }

        int comp;
        int level = 0;
        do {
            comp = Integer.compare(AtomicHashStore.pos(level, h1), AtomicHashStore.pos(level, h2));
            if (comp != 0) {
                return comp;
            }
            level++;
        } while (true);

    }

}
