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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public final class AtomicHashStore<K,V> implements Serializable {

    private static final long serialVersionUID = -5478407507091079172L;

    private final Node root;


    public AtomicHashStore() {
        this.root = Node.EMPTY_NODE;
    }

    AtomicHashStore(final Node root) {
        this.root = root;
    }


    Node innerRoot() {
        return this.root;
    }


    public AtomicHashStore<K,V> toStore() {
        return this;
    }


    // Several methods in the java.util.Map interface consider absent values and those mapped to null to be equivalent
    private static Object normalizeAbsentValue(final Object value) {
        return (value == io.aquen.atomichash.Entry.NOT_FOUND) ? null : value;
    }


    public int size() {
        return this.root.size;
    }


    public boolean isEmpty() {
        return this.root == Node.EMPTY_NODE;
    }


    public boolean containsKey(final Object key) {
        return this.root.containsKey(key);
    }

    public boolean containsValue(final Object value) {
        return this.root.containsValue(value);
    }


    public V get(final Object key) {
        final Object value = this.root.get(key);
        return (V) normalizeAbsentValue(value);
    }

    public V getOrDefault(final Object key, final V defaultValue) {
        final Object value = this.root.get(key);
        // The definition of java.util.Map#getOrDefault() returns the default value only if key is not mapped
        return (value == io.aquen.atomichash.Entry.NOT_FOUND) ? defaultValue : (V) value;
    }


    public AtomicHashStore<K,V> put(final K key, final V newValue) {
        final Node newNode = this.root.put(key, newValue);
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }

    public AtomicHashStore<K,V> putIfAbsent(final K key, final V newValue) {
        final Object value = (V) normalizeAbsentValue(this.root.get(key));
        final Node newNode = (value == null) ? this.root.put(key, newValue) : this.root;
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }

    public AtomicHashStore<K,V> putAll(final Map<? extends K, ? extends V> newMappings) {
        Objects.requireNonNull(newMappings);
        Node newNode = this.root;
        for (final Map.Entry<? extends K, ? extends V> entry : newMappings.entrySet()) {
            newNode = newNode.put(entry.getKey(), entry.getValue());
        }
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }


    public AtomicHashStore<K,V> remove(final Object key) {
        final Node newNode = this.root.remove(key);
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }

    public AtomicHashStore<K,V> remove(final Object key, final Object oldValue) {
        final boolean matches = Objects.equals(oldValue, this.root.get(key));
        final Node newNode = (matches) ? this.root.remove(key) : this.root;
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }


    public AtomicHashStore<K,V> clear() {
        return (this.root != Node.EMPTY_NODE) ? new AtomicHashStore<>(Node.EMPTY_NODE) : this;
    }


    public Set<K> keySet() {
        return (Set<K>) this.root.allKeys();
    }

    public Collection<V> values() {
        return (List<V>) this.root.allValues();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return (Set<Map.Entry<K,V>>) (Set<?>) this.root.allEntries();
    }


    public void forEach(final BiConsumer<? super K, ? super V> action) {
        // This implementation applies minor optimizations on the default (e.g. entrySet is immutable)
        Objects.requireNonNull(action);
        final Set<io.aquen.atomichash.Entry> entrySet = (Set<io.aquen.atomichash.Entry>)(Set<?>)entrySet();
        for (io.aquen.atomichash.Entry entry : entrySet) {
            action.accept((K)entry.key, (V)entry.value);
        }
    }


    public AtomicHashStore<K,V> replace(final K key, final V oldValue, final V newValue) {
        final V value = (V) this.root.get(key);
        final boolean matches = Objects.equals(oldValue, value);
        final Node newNode = (matches) ? this.root.put(key, newValue) : this.root;
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }

    public AtomicHashStore<K,V> replace(final K key, final V newValue) {
        final Node newNode = (this.root.containsKey(key)) ? this.root.put(key, newValue) : this.root;
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }

    public AtomicHashStore<K,V> replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        Node newNode = this.root;
        for (io.aquen.atomichash.Entry entry : this.root.allEntries()) {
            newNode = newNode.put(entry.key, function.apply((K)entry.key, (V)entry.value));
        }
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }


    public AtomicHashStore<K,V> computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        final V value = (V) normalizeAbsentValue(this.root.get(key));
        final V mappedValue = (value == null) ? mappingFunction.apply(key) : null;
        final Node newNode = (mappedValue != null) ? this.root.put(key, mappedValue) : this.root;
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }

    public AtomicHashStore<K,V> computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        final V value = (V) normalizeAbsentValue(this.root.get(key));
        final V remappedValue = (value == null) ? null : remappingFunction.apply(key, value);
        final Node newNode =
                (value == null) ?
                        this.root :
                        (remappedValue == null) ? this.root.remove(key) : this.root.put(key, remappedValue);
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }

    public AtomicHashStore<K,V> compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        final V value = (V) normalizeAbsentValue(this.root.get(key));
        final V remappedValue = remappingFunction.apply(key, value);
        final Node newNode = (remappedValue == null) ? this.root.remove(key) : this.root.put(key, remappedValue);
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }


    public AtomicHashStore<K,V> merge(final K key, final V newValue, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(newValue);
        final V value = (V) normalizeAbsentValue(this.root.get(key));
        final V remappedValue = (value == null) ? newValue : remappingFunction.apply(value, newValue);
        final Node newNode = (remappedValue == null) ? this.root.remove(key) : this.root.put(key, remappedValue);
        return (this.root != newNode) ? new AtomicHashStore<>(newNode) : this;
    }


    @Override
    public boolean equals(final Object other) {
        // Implemented per the definition of Map.equals()
        if (other == this) {
            return true;
        }
        if (!(other instanceof AtomicHashStore<?,?>)) {
            return false;
        }
        // In order to ensure consistency of the operation, only one call (".entrySet()") will be performed
        // on "this" and on the "other" variable. This avoids possible issues that could arise if first "other.size()"
        // was checked and then "other.entrySet()", but "other" was modified in between.
        final Set<Map.Entry<K,V>> entrySet = entrySet();
        final AtomicHashStore<?,?> otherStore = (AtomicHashStore<?,?>) other;
        return entrySet.equals(otherStore.entrySet());
    }


    @Override
    public int hashCode() {
        // Implemented per the definition of Map.hashCode()
        final Set<Map.Entry<K,V>> entrySet = entrySet();
        int hashCode = 0;
        for (final Map.Entry<K,V> entry : entrySet) {
            hashCode += entry.hashCode();
        }
        return hashCode;
    }


    @Override
    public String toString() {
        // Same as java.util.AbstractMap#toString() as it is what most users would expect
        final Iterator<Map.Entry<K,V>> i = entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            final Map.Entry<K,V> e = i.next();
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
