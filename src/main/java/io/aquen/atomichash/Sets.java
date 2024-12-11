/*
 * =========================================================================
 *                                                                          
 *   Copyright (c) 2019-2024, Aquen OSS (https://aquen.io)                  
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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;

final class Sets {

    /*
     * Sets for an AtomicHashStore are not the same as sets for an AtomicHashMap because
     * the java.util.Map specification dictates that a Map's entry/keys set has to reflect
     * modifications in the underlying Map, which does not apply to AtomicHashStore
     * objects due to the fact they are immutable.
     */


    final static class MapEntrySet<K,V> extends AbstractSet<Map.Entry<K,V>> {

        private final AtomicHashStore<K,V> store;

        MapEntrySet(final AtomicHashStore<K,V> store) {
            super();
            this.store = store;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new Iterators.MapEntryIterator<>(this.store.root);
        }

        @Override
        public int size() {
            return this.store.size();
        }

        @Override
        public boolean contains(final Object o) {
            // We can provide a better implementation than that of AbstractSet
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            final Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
            final Object key = entry.getKey();
            final HashEntry<?,?> candidate = this.store.getEntry(AtomicHashStore.hash(key), key);
            return candidate != null && candidate.equals(entry);
        }

    }


    final static class MapKeySet<K,V> extends AbstractSet<K> {

        private final AtomicHashStore<K,V> store;

        MapKeySet(final AtomicHashStore<K,V> store) {
            super();
            this.store = store;
        }

        @Override
        public Iterator<K> iterator() {
            return new Iterators.KeyIterator<>(this.store.root);
        }

        @Override
        public int size() {
            return this.store.size();
        }

        @Override
        public boolean contains(final Object o) {
            // We can provide a better implementation than that of AbstractSet
            return this.store.containsKey(o);
        }

    }





    private Sets() {
        super();
    }

}
