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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A thread-safe, immutable key-value store.
 * <p>
 * This class corresponds to the internal data store used by {@link AtomicHashMap}. All of its operations
 * are <strong>thread-safe</strong>, <strong>atomic</strong> and <strong>non-blocking</strong>, including both reads
 * and writes. All modifications return a new instance of {@link AtomicHashStore} containing the modified data.
 * <p>
 * This class internall implements an immutable variation of a CTRIE
 * (<a href="https://en.wikipedia.org/wiki/Ctrie">Concurrent Hash-Trie</a>). This structure is composed of a tree of
 * compact (bitmap-managed) arrays that map keys to positions in each of the tree levels depending on the value of
 * a range of bits of its (modified) hash code.
 * <p>
 * Key hash codes (32-bit <kbd>int</kbd>s) are divided into five 6-bit segments plus one final 2-bit segment. Each
 * of these segments is used, at each level of depth, to compute the position assigned to the key in the compact array
 * (node) living at that level of depth in the ctrie structure. These are compact arrays with a maximum of 64 positions
 * (bitmaps are <kbd>long</kbd> values), each of which can contain either a data entry or a link to another node
 * at level + 1. A maximum of 6 levels can exist (0 to 5), and hash collisions only need to be managed at the deepest
 * level.
 * <p>
 * New instances of this class can be created by either calling its constructor {@link #AtomicHashStore()} or by
 * calling any of its static convenience factory <kbd>AtomicHashStore.of(...)</kbd> methods: <kbd>of()</kbd>,
 * <kbd>of(k1, v1)</kbd>, <kbd>of(k1, v1, k2, v2)</kbd>, <kbd>of(k1, v1, k2, v2, k3, v3)</kbd>, etc.
 * <p>
 * Note that this implementation does not keep the insertion order. Iteration order is not guaranteed to be
 * consistent.
 *
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@SuppressWarnings("unchecked")
public final class AtomicHashStore<K,V> implements Serializable {

    private static final long serialVersionUID = -5478407507091079172L;

    private final Root root;



    private static Entry entry(final Object key, final Object value) {
        return entry(Entry.hash(key), key, value);
    }

    private static Entry entry(final int hash, final Object key, final Object value) {
        return new Entry(hash, key, value, null);
    }


    public static <K,V> AtomicHashStore<K,V> of() {
        return new AtomicHashStore<>();
    }

    public static <K,V> AtomicHashStore<K,V> of(K k1, V v1) {
        final Root root =
                Root.of(entry(k1, v1));
        return new AtomicHashStore<>(root);
    }

    public static <K,V> AtomicHashStore<K,V> of(K k1, V v1, K k2, V v2) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2));
        return new AtomicHashStore<>(root);
    }

    public static <K,V> AtomicHashStore<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3));
        return new AtomicHashStore<>(root);
    }

    public static <K,V> AtomicHashStore<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4));
        return new AtomicHashStore<>(root);
    }

    public static <K,V> AtomicHashStore<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5));
        return new AtomicHashStore<>(root);
    }

    public static <K,V> AtomicHashStore<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                                K k6, V v6) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5),
                        entry(k6, v6));
        return new AtomicHashStore<>(root);
    }

    public static <K,V> AtomicHashStore<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                                K k6, V v6, K k7, V v7) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5),
                        entry(k6, v6), entry(k7, v7));
        return new AtomicHashStore<>(root);
    }

    public static <K,V> AtomicHashStore<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                                K k6, V v6, K k7, V v7, K k8, V v8) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5),
                        entry(k6, v6), entry(k7, v7), entry(k8, v8));
        return new AtomicHashStore<>(root);
    }

    public static <K,V> AtomicHashStore<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                                K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5),
                        entry(k6, v6), entry(k7, v7), entry(k8, v8), entry(k9, v9));
        return new AtomicHashStore<>(root);
    }

    public static <K,V> AtomicHashStore<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                                K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5),
                        entry(k6, v6), entry(k7, v7), entry(k8, v8), entry(k9, v9), entry(k10, v10));
        return new AtomicHashStore<>(root);
    }



    public AtomicHashStore() {
        this.root = Root.EMPTY_ROOT;
    }

    AtomicHashStore(final Root root) {
        this.root = root;
    }


    Root innerRoot() {
        return this.root;
    }


    public AtomicHashStore<K,V> toStore() {
        return this;
    }


    public int size() {
        return this.root.size();
    }


    public boolean isEmpty() {
        return this.root.isEmpty();
    }


    public boolean containsKey(final Object key) {
        return this.root.containsKey(key);
    }

    public boolean containsValue(final Object value) {
        return this.root.containsValue(value);
    }


    public V get(final Object key) {
        return (V) this.root.get(key);
    }

    public V getOrDefault(final Object key, final V defaultValue) {
        return (V) this.root.getOrDefault(key, defaultValue);
    }


    public Map<K,V> getAll(final Object... keys) {
        return (Map<K,V>) this.root.getAll(keys);
    }


    public AtomicHashStore<K,V> put(final K key, final V newValue) {
        final Root newRoot = this.root.put(entry(key, newValue));
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
    }

    public AtomicHashStore<K,V> putIfAbsent(final K key, final V newValue) {
        // Map#putIfAbsent() considers null equivalent to absence
        final Object value = this.root.get(key);
        final Root newRoot = (value == null) ? this.root.put(entry(key, newValue)) : this.root;
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
    }

    public AtomicHashStore<K,V> putAll(final Map<? extends K, ? extends V> newMappings) {
        Objects.requireNonNull(newMappings);
        Root newRoot = this.root;
        for (final Map.Entry<? extends K, ? extends V> entry : newMappings.entrySet()) {
            newRoot = newRoot.put(entry(entry.getKey(), entry.getValue()));
        }
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
    }


    public AtomicHashStore<K,V> remove(final Object key) {
        final Root newRoot = this.root.remove(Entry.hash(key), key);
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
    }

    public AtomicHashStore<K,V> remove(final Object key, final Object oldValue) {
        final boolean matches = Objects.equals(oldValue, this.root.get(key));
        final Root newRoot = (matches) ? this.root.remove(Entry.hash(key), key) : this.root;
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
    }


    public AtomicHashStore<K,V> clear() {
        return (this.root.isEmpty()) ? this : new AtomicHashStore<>();
    }


    public Set<K> keySet() {
        return (Set<K>) this.root.keySet();
    }

    public Collection<V> values() {
        return (Collection<V>) this.root.values();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return (Set<Map.Entry<K,V>>) (Set<?>) this.root.entrySet();
    }


    public void forEach(final BiConsumer<? super K, ? super V> action) {
        // This implementation applies minor optimizations on the default (e.g. entrySet is immutable)
        Objects.requireNonNull(action);
        // We try to benefit from entrySet being cached to iterate
        for (Entry entry : ((Set<Entry>)(Set<?>) entrySet())) {
            action.accept((K)entry.key, (V)entry.value);
        }
    }


    public AtomicHashStore<K,V> replace(final K key, final V oldValue, final V newValue) {
        final V value = (V) this.root.get(key);
        final boolean matches = Objects.equals(oldValue, value);
        final Root newRoot = (matches) ? this.root.put(entry(key, newValue)) : this.root;
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
    }

    public AtomicHashStore<K,V> replace(final K key, final V newValue) {
        final Root newRoot = (this.root.containsKey(key)) ? this.root.put(entry(key, newValue)) : this.root;
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
    }

    public AtomicHashStore<K,V> replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        Root newRoot = this.root;
        // We try to benefit from entrySet being cached to iterate
        for (Entry entry : ((Set<Entry>)(Set<?>) entrySet())) {
            newRoot = newRoot.put(entry(entry.key, function.apply((K)entry.key, (V)entry.value)));
        }
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
    }


    public AtomicHashStore<K,V> computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        final V value = (V) this.root.get(key);
        final V mappedValue = (value == null) ? mappingFunction.apply(key) : null;
        final Root newRoot = (mappedValue != null) ? this.root.put(entry(key, mappedValue)) : this.root;
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
    }

    public AtomicHashStore<K,V> computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        final V value = (V) this.root.get(key);
        final V remappedValue = (value == null) ? null : remappingFunction.apply(key, value);
        final int hash = Entry.hash(key);
        final Root newRoot =
                (value == null) ?
                        this.root :
                        (remappedValue == null) ? this.root.remove(hash, key) : this.root.put(entry(hash, key, remappedValue));
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
    }

    public AtomicHashStore<K,V> compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        final V value = (V) this.root.get(key);
        final V remappedValue = remappingFunction.apply(key, value);
        final int hash = Entry.hash(key);
        final Root newRoot = (remappedValue == null) ? this.root.remove(hash, key) : this.root.put(entry(hash, key, remappedValue));
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
    }


    public AtomicHashStore<K,V> merge(final K key, final V newValue, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(newValue);
        final V value = (V) this.root.get(key);
        final V remappedValue = (value == null) ? newValue : remappingFunction.apply(value, newValue);
        final int hash = Entry.hash(key);
        final Root newRoot = (remappedValue == null) ? this.root.remove(hash, key) : this.root.put(entry(hash, key, remappedValue));
        return (this.root != newRoot) ? new AtomicHashStore<>(newRoot) : this;
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
