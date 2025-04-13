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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public final class AtomicHashMap<K,V> implements Map<K, V>, Serializable {

    private static final long serialVersionUID = 6117491851316897982L;

    private final AtomicReference<Node> root;


    public AtomicHashMap() {
        this.root = new AtomicReference<>();
        this.root.set(Node.EMPTY_NODE);
    }


    public AtomicHashMap(final Map<? extends K, ? extends V> map) {
        this.root = new AtomicReference<>();
        Node node = Node.EMPTY_NODE;
        if (map != null) {
            for (final Entry<? extends K, ? extends V> entry : map.entrySet()) {
                node = node.put(entry.getKey(), entry.getValue());
            }
        }
        this.root.set(node);
    }


    Node innerRoot() {
        return this.root.get();
    }


    // Several methods in the java.util.Map interface consider absent values and those mapped to null to be equivalent
    private static Object normalizeAbsentValue(final Object value) {
        return (value == io.aquen.atomichash.Entry.NOT_FOUND) ? null : value;
    }


    public AtomicHashStore<K,V> store() {
        return new AtomicHashStore<>(this.root.get());
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
        return (V) normalizeAbsentValue(value);
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        final Object value = this.root.get().get(key);
        // The definition of java.util.Map#getOrDefault() returns the default value only if key is not mapped
        return (value == io.aquen.atomichash.Entry.NOT_FOUND) ? defaultValue : (V) value;
    }


    @Override
    public V put(final K key, final V newValue) {
        Node node, newNode;
        do {
            node = this.root.get();
            newNode = node.put(key, newValue);
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        final Object oldValue = node.get(key);
        return (V) normalizeAbsentValue(oldValue);
    }

    @Override
    public V putIfAbsent(final K key, final V newValue) {
        V value;
        Node node, newNode;
        do {
            node = this.root.get();
            value = (V) normalizeAbsentValue(node.get(key));
            newNode = (value == null) ? node.put(key, newValue) : node;
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return value;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> newMappings) {
        Objects.requireNonNull(newMappings);
        final Set<? extends Entry<? extends K, ? extends V>> newEntrySet = newMappings.entrySet();
        Node node, newNode;
        do {
            node = this.root.get();
            newNode = node;
            for (final Entry<? extends K, ? extends V> entry : newEntrySet) {
                newNode = newNode.put(entry.getKey(), entry.getValue());
            }
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
    }


    @Override
    public V remove(final Object key) {
        Node node, newNode;
        do {
            node = this.root.get();
            newNode = node.remove(key);
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        final Object oldValue = node.get(key);
        return (V) normalizeAbsentValue(oldValue);
    }

    @Override
    public boolean remove(final Object key, final Object oldValue) {
        boolean matches;
        Node node, newNode;
        do {
            node = this.root.get();
            matches = Objects.equals(oldValue, node.get(key));
            newNode = (matches) ? node.remove(key) : node;
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return matches;
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
    public boolean replace(final K key, final V oldValue, final V newValue) {
        boolean matches;
        Node node, newNode;
        do {
            node = this.root.get();
            matches = Objects.equals(oldValue, node.get(key));
            newNode = (matches) ? node.put(key, newValue) : node;
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return matches;
    }

    @Override
    public V replace(final K key, final V newValue) {
        boolean mapped;
        Node node, newNode;
        do {
            node = this.root.get();
            mapped = node.containsKey(key);
            newNode = (mapped) ? node.put(key, newValue) : node;
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return (mapped) ? (V) node.get(key) : null;
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        Node node, newNode;
        do {
            node = this.root.get();
            newNode = node;
            for (io.aquen.atomichash.Entry entry : node.allEntries()) {
                newNode = newNode.put(entry.key, function.apply((K)entry.key, (V)entry.value));
            }
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
    }


    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V value, mappedValue;
        Node node, newNode;
        do {
            node = this.root.get();
            value = (V) normalizeAbsentValue(node.get(key));
            mappedValue = (value == null) ? mappingFunction.apply(key) : null;
            newNode = (mappedValue != null) ? node.put(key, mappedValue) : node;
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return value;
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V value, remappedValue;
        Node node, newNode;
        do {
            node = this.root.get();
            value = (V) normalizeAbsentValue(node.get(key));
            remappedValue = (value == null) ? null : remappingFunction.apply(key, value);
            newNode = (value == null) ?
                            node :  // Absent, no changes
                            ((remappedValue == null) ? node.remove(key) : node.put(key, remappedValue));
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return remappedValue;
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V value, remappedValue;
        Node node, newNode;
        do {
            node = this.root.get();
            value = (V) normalizeAbsentValue(node.get(key));
            remappedValue = remappingFunction.apply(key, value);
            newNode = (remappedValue == null) ? node.remove(key) : node.put(key, remappedValue);
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return remappedValue;
    }


    @Override
    public V merge(final K key, final V newValue, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(newValue);
        V value, remappedValue;
        Node node, newNode;
        do {
            node = this.root.get();
            value = (V) normalizeAbsentValue(node.get(key));
            remappedValue = (value == null) ? newValue : remappingFunction.apply(value, newValue);
            newNode =  (remappedValue == null) ? node.remove(key) : node.put(key, remappedValue);
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return remappedValue;
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


    @Override
    public String toString() {
        // Same as java.util.AbstractMap#toString() as it is what most users would expect
        final Iterator<Entry<K,V>> i = entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            final Entry<K,V> e = i.next();
            final K key = e.getKey();
            final V value = e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }

}
