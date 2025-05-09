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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A thread-safe implementation of the {@link java.util.Map} interface providing advanced concurrency features.
 * <p>
 * This implementation is <strong>thread-safe</strong>, <strong>atomic</strong> and <strong>non-blocking</strong> for
 * all of its methods, including both reads and writes. This includes multi-element methods such
 * as {@link #putAll(Map)} and {@link #getAll(Object...)} (the latter not a part of
 * the {@link java.util.Map} interface), as well as all other methods for retrieving, adding, modifying or
 * removing mappings, iteration, etc.
 * <p>
 * The map can therefore never be read in a partially modified state, and its exact <em>snapshot</em> state for
 * an arbitrary number of mappings can be obtained at any time.
 * <p>
 * This is achieved by internally implementing an immutable variation of a CTRIE
 * (<a href="https://en.wikipedia.org/wiki/Ctrie">Concurrent Hash-Trie</a>). This structure is composed of a tree of
 * compact (bitmap-managed) arrays that map keys to positions in each of the tree levels depending on the value of
 * a range of bits of its (modified) hash code.
 * <p>
 * Key hash codes (32-bit <kbd>int</kbd>s) are divided into five 6-bit segments plus one final 2-bit segment. Each
 * of these segments is used, at each level of depth, to compute the position assigned to the key in the compact array
 * (node) living at that level of depth in the ctrie structure. These are compact arrays with a maximum of 64 positions
 * (bitmaps are <kbd>long</kbd> values), each of which can contain either a data entry or a link to another node
 * at level + 1. A maximum of 6 levels can exist (0 to 5), and hash collisions only need to be managed at the deepest
 * level. All structures are kept immutable, so modifications in an array (node) at a specific level mean the creation
 * of new nodes from that point up to the root of the tree, and the replacement of the old root with the new one
 * using an atomic compare-and-swap operation.
 * <p>
 * Note that, given this implementation is based on immutable tree structures, modifications typically need a higher
 * use of memory than other common implementations of the {@link java.util.Map} interface.
 * <p>
 * New instances of this class can be created by either calling its constructor {@link #AtomicHashMap()} or by
 * calling any of its static convenience factory <kbd>AtomicHashMap.of(...)</kbd> methods: <kbd>of()</kbd>,
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
public final class AtomicHashMap<K,V> implements Map<K, V>, Serializable {

    private static final long serialVersionUID = 6117491851316897982L;

    private final AtomicReference<Root> root;


    
    private static io.arxila.atomichash.Entry entry(final Object key, final Object value) {
        return entry(io.arxila.atomichash.Entry.hash(key), key, value);
    }

    private static io.arxila.atomichash.Entry entry(final int hash, final Object key, final Object value) {
        return new io.arxila.atomichash.Entry(hash, key, value, null);
    }


    public static <K,V> AtomicHashMap<K,V> of() {
        return new AtomicHashMap<>();
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1) {
        final Root root =
                Root.of(entry(k1, v1));
        return new AtomicHashMap<>(root);
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2));
        return new AtomicHashMap<>(root);
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3));
        return new AtomicHashMap<>(root);
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4));
        return new AtomicHashMap<>(root);
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5));
        return new AtomicHashMap<>(root);
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                              K k6, V v6) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5),
                        entry(k6, v6));
        return new AtomicHashMap<>(root);
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                              K k6, V v6, K k7, V v7) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5),
                        entry(k6, v6), entry(k7, v7));
        return new AtomicHashMap<>(root);
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                              K k6, V v6, K k7, V v7, K k8, V v8) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5),
                        entry(k6, v6), entry(k7, v7), entry(k8, v8));
        return new AtomicHashMap<>(root);
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                              K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5),
                        entry(k6, v6), entry(k7, v7), entry(k8, v8), entry(k9, v9));
        return new AtomicHashMap<>(root);
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                              K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        final Root root =
                Root.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5),
                        entry(k6, v6), entry(k7, v7), entry(k8, v8), entry(k9, v9), entry(k10, v10));
        return new AtomicHashMap<>(root);
    }



    public AtomicHashMap() {
        super();
        this.root = new AtomicReference<>();
        this.root.set(Root.EMPTY_ROOT);
    }


    public AtomicHashMap(final Map<? extends K, ? extends V> map) {
        super();
        final Set<io.arxila.atomichash.Entry> mapEntries = new HashSet<>();
        for (final Entry<? extends K, ? extends V> entry : map.entrySet()) {
            mapEntries.add(entry(entry.getKey(), entry.getValue()));
        }
        this.root = new AtomicReference<>();
        this.root.set(Root.EMPTY_ROOT.putAll(mapEntries));
    }


    private AtomicHashMap(final Root root) {
        super();
        this.root = new AtomicReference<>();
        this.root.set(root);
    }




    Root innerRoot() {
        return this.root.get();
    }


    public AtomicHashStore<K,V> store() {
        return new AtomicHashStore<>(this.root.get());
    }


    @Override
    public int size() {
        return this.root.get().size();
    }


    @Override
    public boolean isEmpty() {
        return this.root.get().isEmpty();
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
        return (V) this.root.get().get(key);
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return (V) this.root.get().getOrDefault(key, defaultValue);
    }


    // Not a part of the java.util.Map interface
    public Map<K,V> getAll(final Object... keys) {
        return (Map<K,V>) this.root.get().getAll(keys);
    }


    @Override
    public V put(final K key, final V newValue) {
        final io.arxila.atomichash.Entry newEntry = entry(key, newValue);
        Root root, newRoot;
        do {
            root = this.root.get();
            newRoot = root.put(newEntry);
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
        return (V) root.get(key);
    }

    @Override
    public V putIfAbsent(final K key, final V newValue) {
        final io.arxila.atomichash.Entry newEntry = entry(key, newValue);
        Root root, newRoot;
        do {
            root = this.root.get();
            newRoot = root.putIfAbsent(newEntry);
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
        return (V) root.get(key);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> newMappings) {
        Objects.requireNonNull(newMappings);
        // Building a set of entries needs an iteration on the new mappings, but it pays off because
        // the Entry objects will already be instanced outside the critical (and repeatable) region.
        final Set<io.arxila.atomichash.Entry> newEntries = new HashSet<>();
        for (final Entry<? extends K, ? extends V> entry : newMappings.entrySet()) {
            newEntries.add(entry(entry.getKey(), entry.getValue()));
        }
        Root root, newRoot;
        do {
            root = this.root.get();
            newRoot = root.putAll(newEntries);
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
    }


    @Override
    public V remove(final Object key) {
        final int hash = io.arxila.atomichash.Entry.hash(key);
        Root root, newRoot;
        do {
            root = this.root.get();
            newRoot = root.remove(hash, key);
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
        return (V) root.get(key);
    }

    @Override
    public boolean remove(final Object key, final Object oldValue) {
        final int hash = io.arxila.atomichash.Entry.hash(key);
        Root root, newRoot;
        do {
            root = this.root.get();
            newRoot = root.remove(hash, key, oldValue);
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
        return (root != newRoot);
    }


    @Override
    public void clear() {
        this.root.set(Root.EMPTY_ROOT);
    }


    @Override
    public Set<K> keySet() {
        return (Set<K>) this.root.get().keySet();
    }

    @Override
    public Collection<V> values() {
        return (List<V>) this.root.get().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return (Set<Entry<K,V>>) (Set<?>) this.root.get().entrySet();
    }


    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        // This implementation applies minor optimizations on the default (e.g. entrySet is immutable)
        Objects.requireNonNull(action);
        // We try to benefit from entrySet being cached to iterate
        for (io.arxila.atomichash.Entry entry : this.root.get().entrySet()) {
            action.accept((K)entry.key, (V)entry.value);
        }
    }


    @Override
    public V replace(final K key, final V newValue) {
        final io.arxila.atomichash.Entry newEntry = entry(key, newValue);
        Root root, newRoot;
        do {
            root = this.root.get();
            newRoot = root.replace(newEntry);
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
        return (root != newRoot) ? (V) root.get(key) : null;
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        final io.arxila.atomichash.Entry newEntry = entry(key, newValue);
        Root root, newRoot;
        do {
            root = this.root.get();
            newRoot = root.replace(newEntry, oldValue);
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
        return (root != newRoot);
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        Root root, newRoot;
        do {
            root = this.root.get();
            newRoot = root.replaceAll((BiFunction<Object,Object,Object>)function);
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
    }


    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        final int hash = io.arxila.atomichash.Entry.hash(key);
        Root root, newRoot;
        do {
            root = this.root.get();
            newRoot = root.compute(hash, key, ((BiFunction<Object,Object,Object>)remappingFunction));
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
        return (V) newRoot.get(key);
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        final int hash = io.arxila.atomichash.Entry.hash(key);
        Root root, newRoot;
        do {
            root = this.root.get();
            newRoot = root.computeIfAbsent(hash, key, ((Function<Object,Object>)mappingFunction));
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
        return (V) newRoot.get(key);
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        final int hash = io.arxila.atomichash.Entry.hash(key);
        Root root, newRoot;
        do {
            root = this.root.get();
            newRoot = root.computeIfPresent(hash, key, ((BiFunction<Object,Object,Object>)remappingFunction));
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
        return (V) newRoot.get(key);
    }


    @Override
    public V merge(final K key, final V newValue, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(newValue);
        final int hash = io.arxila.atomichash.Entry.hash(key);
        V value, remappedValue;
        Root root, newRoot;
        do {
            root = this.root.get();
            value = (V) root.get(key);
            remappedValue = (value == null) ? newValue : remappingFunction.apply(value, newValue);
            newRoot =  (remappedValue == null) ? root.remove(hash, key) : root.put(entry(hash, key, remappedValue));
        } while (root != newRoot && !this.root.compareAndSet(root, newRoot));
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
        // To ensure consistency of the operation, only one call (".entrySet()") will be performed on "this" and
        // on the "other" variable. This avoids possible issues that could arise if first "other.size()"
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
