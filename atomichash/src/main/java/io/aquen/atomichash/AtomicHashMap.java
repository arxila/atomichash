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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@SuppressWarnings("unchecked")
public final class AtomicHashMap<K,V> implements Map<K, V>, Serializable {

    private static final long serialVersionUID = 6117491851316897982L;
    private static Object NOT_FOUND = io.aquen.atomichash.Entry.NOT_FOUND;

    private final AtomicReference<Node> root;


    public AtomicHashMap() {
        this.root = new AtomicReference<>();
        this.root.set(Node.EMPTY_NODE);
    }


    @Override
    public int size() {
        return this.root.get().size;
    }


    @Override
    public boolean isEmpty() {
        return this.root.get() == Node.EMPTY_NODE;
    }


    @Override
    public boolean containsKey(final Object key) {
        return this.root.get().containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.root.get().containsValue(value);
    }


    @Override
    public V get(final Object key) {
        final Object value = this.root.get().get(key);
        return (value == NOT_FOUND) ? null : (V) value;
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        final Object value = this.root.get().get(key);
        return (value == NOT_FOUND) ? defaultValue : (V) value;
    }


    @Override
    public V put(final K key, final V value) {
        Node oldNode;
        do {
            oldNode = this.root.get();
        } while (!this.root.compareAndSet(oldNode, oldNode.put(key, value)));
        final Object oldValue = oldNode.get(key);
        return (oldValue == NOT_FOUND) ? null : (V) oldValue;
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        boolean absent;
        Node oldNode;
        do {
            oldNode = this.root.get();
        } while ((absent = !oldNode.containsKey(key)) && !this.root.compareAndSet(oldNode, oldNode.put(key, value)));
        if (!absent) {
            return null;
        }
        return (V) oldNode.get(key);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {

        Objects.requireNonNull(m);

        final Set<? extends Entry<? extends K, ? extends V>> entrySet = m.entrySet();

        Node oldNode, newNode;
        do {
            oldNode = this.root.get();
            newNode = oldNode;
            for (final Entry<? extends K, ? extends V> entry : entrySet) {
                newNode = newNode.put(entry.getKey(), entry.getValue());
            }
        } while (!this.root.compareAndSet(oldNode, newNode));

    }


    @Override
    public V remove(final Object key) {
        Node oldNode;
        do {
            oldNode = this.root.get();
        } while (!this.root.compareAndSet(oldNode, oldNode.remove(key)));
        final Object oldValue = oldNode.get(key);
        return (oldValue == io.aquen.atomichash.Entry.NOT_FOUND) ? null : (V) oldValue;
    }

    @Override
    public void clear() {
        this.root.set(Node.EMPTY_NODE);
    }


    @Override
    public Set<K> keySet() {
        return (Set<K>) this.root.get().allKeys();
    }

    @Override
    public Collection<V> values() {
        return (List<V>) this.root.get().allValues();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return (Set<Entry<K,V>>) (Set<?>) this.root.get().allEntries();
    }


    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        // This implementation applies minor optimizations on the default (e.g. entrySet is immutable)
        Objects.requireNonNull(action);
        final Set<io.aquen.atomichash.Entry> entrySet = (Set<io.aquen.atomichash.Entry>)(Set<?>)entrySet();
        for (io.aquen.atomichash.Entry entry : entrySet) {
            action.accept((K)entry.key, (V)entry.value);
        }
    }


    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        Node oldNode, newNode;
        do {
            oldNode = this.root.get();
            newNode = oldNode;
            for (io.aquen.atomichash.Entry entry : oldNode.allEntries()) {
                newNode = newNode.put(entry.key, function.apply((K)entry.key, (V)entry.value));
            }
        } while (!this.root.compareAndSet(oldNode, newNode));
    }


    @Override
    public boolean equals(final Object other) {
        // Implemented per the definition of Map.equals()
        if (other == this) {
            return true;
        }
        if (!(other instanceof Map)) {
            return false;
        }
        // In order to ensure consistency of the operation, only one call (".entrySet()") will be performed
        // on "this" and on the "other" variable. This avoids possible issues that could arise if first "other.size()"
        // was checked and then "other.entrySet()", but "other" was modified in between.
        final Set<Entry<K,V>> entrySet = entrySet();
        final Map<?,?> otherMap = (Map<?,?>) other;
        return entrySet.equals(otherMap.entrySet());
    }


    @Override
    public int hashCode() {
        // Implemented per the definition of Map.hashCode()
        final Set<Entry<K,V>> entrySet = entrySet();
        int hashCode = 0;
        for (final Entry<K,V> entry : entrySet) {
            hashCode += entry.hashCode();
        }
        return hashCode;
    }


}
