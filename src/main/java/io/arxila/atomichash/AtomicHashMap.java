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
 * The map can therefore never be read in a partially-modified state, and its exact <em>snapshot</em> state for
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

    private final AtomicReference<Node> root;


    public static <K,V> AtomicHashMap<K,V> of() {
        return new AtomicHashMap<>();
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1) {
        final AtomicHashMap<K,V> map = new AtomicHashMap<>();
        Node node = Node.EMPTY_NODE;
        node = Node.put(node, new io.arxila.atomichash.Entry(k1, v1));
        map.root.set(node);
        return map;
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2) {
        final AtomicHashMap<K,V> map = new AtomicHashMap<>();
        Node node = Node.EMPTY_NODE;
        node = Node.put(node, new io.arxila.atomichash.Entry(k1, v1));
        node = Node.put(node, new io.arxila.atomichash.Entry(k2, v2));
        map.root.set(node);
        return map;
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        final AtomicHashMap<K,V> map = new AtomicHashMap<>();
        Node node = Node.EMPTY_NODE;
        node = Node.put(node, new io.arxila.atomichash.Entry(k1, v1));
        node = Node.put(node, new io.arxila.atomichash.Entry(k2, v2));
        node = Node.put(node, new io.arxila.atomichash.Entry(k3, v3));
        map.root.set(node);
        return map;
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        final AtomicHashMap<K,V> map = new AtomicHashMap<>();
        Node node = Node.EMPTY_NODE;
        node = Node.put(node, new io.arxila.atomichash.Entry(k1, v1));
        node = Node.put(node, new io.arxila.atomichash.Entry(k2, v2));
        node = Node.put(node, new io.arxila.atomichash.Entry(k3, v3));
        node = Node.put(node, new io.arxila.atomichash.Entry(k4, v4));
        map.root.set(node);
        return map;
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        final AtomicHashMap<K,V> map = new AtomicHashMap<>();
        Node node = Node.EMPTY_NODE;
        node = Node.put(node, new io.arxila.atomichash.Entry(k1, v1));
        node = Node.put(node, new io.arxila.atomichash.Entry(k2, v2));
        node = Node.put(node, new io.arxila.atomichash.Entry(k3, v3));
        node = Node.put(node, new io.arxila.atomichash.Entry(k4, v4));
        node = Node.put(node, new io.arxila.atomichash.Entry(k5, v5));
        map.root.set(node);
        return map;
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                              K k6, V v6) {
        final AtomicHashMap<K,V> map = new AtomicHashMap<>();
        Node node = Node.EMPTY_NODE;
        node = Node.put(node, new io.arxila.atomichash.Entry(k1, v1));
        node = Node.put(node, new io.arxila.atomichash.Entry(k2, v2));
        node = Node.put(node, new io.arxila.atomichash.Entry(k3, v3));
        node = Node.put(node, new io.arxila.atomichash.Entry(k4, v4));
        node = Node.put(node, new io.arxila.atomichash.Entry(k5, v5));
        node = Node.put(node, new io.arxila.atomichash.Entry(k6, v6));
        map.root.set(node);
        return map;
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                              K k6, V v6, K k7, V v7) {
        final AtomicHashMap<K,V> map = new AtomicHashMap<>();
        Node node = Node.EMPTY_NODE;
        node = Node.put(node, new io.arxila.atomichash.Entry(k1, v1));
        node = Node.put(node, new io.arxila.atomichash.Entry(k2, v2));
        node = Node.put(node, new io.arxila.atomichash.Entry(k3, v3));
        node = Node.put(node, new io.arxila.atomichash.Entry(k4, v4));
        node = Node.put(node, new io.arxila.atomichash.Entry(k5, v5));
        node = Node.put(node, new io.arxila.atomichash.Entry(k6, v6));
        node = Node.put(node, new io.arxila.atomichash.Entry(k7, v7));
        map.root.set(node);
        return map;
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                              K k6, V v6, K k7, V v7, K k8, V v8) {
        final AtomicHashMap<K,V> map = new AtomicHashMap<>();
        Node node = Node.EMPTY_NODE;
        node = Node.put(node, new io.arxila.atomichash.Entry(k1, v1));
        node = Node.put(node, new io.arxila.atomichash.Entry(k2, v2));
        node = Node.put(node, new io.arxila.atomichash.Entry(k3, v3));
        node = Node.put(node, new io.arxila.atomichash.Entry(k4, v4));
        node = Node.put(node, new io.arxila.atomichash.Entry(k5, v5));
        node = Node.put(node, new io.arxila.atomichash.Entry(k6, v6));
        node = Node.put(node, new io.arxila.atomichash.Entry(k7, v7));
        node = Node.put(node, new io.arxila.atomichash.Entry(k8, v8));
        map.root.set(node);
        return map;
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                              K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        final AtomicHashMap<K,V> map = new AtomicHashMap<>();
        Node node = Node.EMPTY_NODE;
        node = Node.put(node, new io.arxila.atomichash.Entry(k1, v1));
        node = Node.put(node, new io.arxila.atomichash.Entry(k2, v2));
        node = Node.put(node, new io.arxila.atomichash.Entry(k3, v3));
        node = Node.put(node, new io.arxila.atomichash.Entry(k4, v4));
        node = Node.put(node, new io.arxila.atomichash.Entry(k5, v5));
        node = Node.put(node, new io.arxila.atomichash.Entry(k6, v6));
        node = Node.put(node, new io.arxila.atomichash.Entry(k7, v7));
        node = Node.put(node, new io.arxila.atomichash.Entry(k8, v8));
        node = Node.put(node, new io.arxila.atomichash.Entry(k9, v9));
        map.root.set(node);
        return map;
    }

    public static <K,V> AtomicHashMap<K,V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                              K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        final AtomicHashMap<K,V> map = new AtomicHashMap<>();
        Node node = Node.EMPTY_NODE;
        node = Node.put(node, new io.arxila.atomichash.Entry(k1, v1));
        node = Node.put(node, new io.arxila.atomichash.Entry(k2, v2));
        node = Node.put(node, new io.arxila.atomichash.Entry(k3, v3));
        node = Node.put(node, new io.arxila.atomichash.Entry(k4, v4));
        node = Node.put(node, new io.arxila.atomichash.Entry(k5, v5));
        node = Node.put(node, new io.arxila.atomichash.Entry(k6, v6));
        node = Node.put(node, new io.arxila.atomichash.Entry(k7, v7));
        node = Node.put(node, new io.arxila.atomichash.Entry(k8, v8));
        node = Node.put(node, new io.arxila.atomichash.Entry(k9, v9));
        node = Node.put(node, new io.arxila.atomichash.Entry(k10, v10));
        map.root.set(node);
        return map;
    }



    public AtomicHashMap() {
        this.root = new AtomicReference<>();
        this.root.set(Node.EMPTY_NODE);
    }


    public AtomicHashMap(final Map<? extends K, ? extends V> map) {
        this.root = new AtomicReference<>();
        Node node = Node.EMPTY_NODE;
        if (map != null) {
            for (final Entry<? extends K, ? extends V> mapEntry : map.entrySet()) {
                node = Node.put(node, new io.arxila.atomichash.Entry(mapEntry.getKey(), mapEntry.getValue()));
            }
        }
        this.root.set(node);
    }


    Node innerRoot() {
        return this.root.get();
    }


    // Several methods in the java.util.Map interface consider absent values and those mapped to null to be equivalent
    private static Object normalizeAbsentValue(final Object value) {
        return (value == io.arxila.atomichash.Entry.NOT_FOUND) ? null : value;
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
        return Node.containsKey(this.root.get(), key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.root.get().containsValue(value);
    }


    @Override
    public V get(final Object key) {
        final Object value = Node.get(this.root.get(), key);
        return (V) normalizeAbsentValue(value);
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        final Object value = Node.get(this.root.get(), key);
        // The definition of java.util.Map#getOrDefault() returns the default value only if key is not mapped
        return (value == io.arxila.atomichash.Entry.NOT_FOUND) ? defaultValue : (V) value;
    }


    // Not a part of the java.util.Map interface
    public Map<K,V> getAll(final Object... keys) {
        if (keys == null || keys.length == 0) {
            return Collections.EMPTY_MAP;
        }
        final Node node = this.root.get();
        final Map<K,V> map = new HashMap<>(keys.length + 1, 1.0f);
        Object value;
        for (final Object key : keys) {
            value = Node.get(node, key);
            if (value != io.arxila.atomichash.Entry.NOT_FOUND) {
                map.put((K)key, (V)value);
            }
        }
        return map;
    }


    @Override
    public V put(final K key, final V newValue) {
        final io.arxila.atomichash.Entry newEntry = new io.arxila.atomichash.Entry(key, newValue);
        Node node, newNode;
        do {
            node = this.root.get();
            newNode = Node.put(node, newEntry);
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        final Object oldValue = Node.get(node, key);
        return (V) normalizeAbsentValue(oldValue);
    }

    @Override
    public V putIfAbsent(final K key, final V newValue) {
        final io.arxila.atomichash.Entry newEntry = new io.arxila.atomichash.Entry(key, newValue);
        V value;
        Node node, newNode;
        do {
            node = this.root.get();
            value = (V) normalizeAbsentValue(Node.get(node, key));
            newNode = (value == null) ? Node.put(node, newEntry) : node;
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return value;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> newMappings) {
        Objects.requireNonNull(newMappings);
        // Building a set of entries needs an iteration on the new mappings, but it pays off because
        // the Entry objects will already be instanced outside the critical (and repeatable) region.
        final Set<io.arxila.atomichash.Entry> newEntrySet = new HashSet<>();
        for (final Entry<? extends K, ? extends V> entry : newMappings.entrySet()) {
            newEntrySet.add(new io.arxila.atomichash.Entry(entry.getKey(), entry.getValue()));
        }
        Node node, newNode;
        do {
            node = this.root.get();
            newNode = node;
            for (final io.arxila.atomichash.Entry entry : newEntrySet) {
                newNode = Node.put(newNode, entry);
            }
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
    }


    @Override
    public V remove(final Object key) {
        final int hash = io.arxila.atomichash.Entry.hash(key);
        Node node, newNode;
        do {
            node = this.root.get();
            newNode = Node.remove(node, hash, key);
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        final Object oldValue = Node.get(node, key);
        return (V) normalizeAbsentValue(oldValue);
    }

    @Override
    public boolean remove(final Object key, final Object oldValue) {
        final int hash = io.arxila.atomichash.Entry.hash(key);
        boolean matches;
        Node node, newNode;
        do {
            node = this.root.get();
            matches = Objects.equals(oldValue, Node.get(node, key)); // No need to worry about NOT_FOUND (requires a mapping)
            newNode = (matches) ? Node.remove(node, hash, key) : node;
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
        final Set<io.arxila.atomichash.Entry> entrySet = (Set<io.arxila.atomichash.Entry>)(Set<?>)entrySet();
        for (io.arxila.atomichash.Entry entry : entrySet) {
            action.accept((K)entry.key, (V)entry.value);
        }
    }


    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        final io.arxila.atomichash.Entry newEntry = new io.arxila.atomichash.Entry(key, newValue);
        boolean matches;
        Node node, newNode;
        do {
            node = this.root.get();
            matches = Objects.equals(oldValue, Node.get(node, key)); // No need to worry about NOT_FOUND (requires a mapping)
            newNode = (matches) ? Node.put(node, newEntry) : node;
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return matches;
    }

    @Override
    public V replace(final K key, final V newValue) {
        final io.arxila.atomichash.Entry newEntry = new io.arxila.atomichash.Entry(key, newValue);
        boolean mapped;
        Node node, newNode;
        do {
            node = this.root.get();
            mapped = Node.containsKey(node, key);
            newNode = (mapped) ? Node.put(node, newEntry) : node;
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return (mapped) ? (V) Node.get(node, key) : null;
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        Node node, newNode;
        do {
            node = this.root.get();
            newNode = node;
            for (io.arxila.atomichash.Entry entry : node.allEntries()) {
                newNode = Node.put(newNode, new io.arxila.atomichash.Entry(entry.key, function.apply((K)entry.key, (V)entry.value)));
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
            value = (V) normalizeAbsentValue(Node.get(node, key));
            mappedValue = (value == null) ? mappingFunction.apply(key) : null;
            newNode = (mappedValue != null) ? Node.put(node, new io.arxila.atomichash.Entry(key, mappedValue)) : node;
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return (mappedValue != null) ? mappedValue : value;
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        final int hash = io.arxila.atomichash.Entry.hash(key);
        V value, remappedValue;
        Node node, newNode;
        do {
            node = this.root.get();
            value = (V) normalizeAbsentValue(Node.get(node, key));
            remappedValue = (value == null) ? null : remappingFunction.apply(key, value);
            newNode = (value == null) ?
                            node :  // Absent, no changes
                            ((remappedValue == null) ? Node.remove(node, hash, key) : Node.put(node, new io.arxila.atomichash.Entry(hash, key, remappedValue)));
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return remappedValue;
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        final int hash = io.arxila.atomichash.Entry.hash(key);
        V value, remappedValue;
        Node node, newNode;
        do {
            node = this.root.get();
            value = (V) normalizeAbsentValue(Node.get(node, key));
            remappedValue = remappingFunction.apply(key, value);
            newNode = (remappedValue == null) ? Node.remove(node, hash, key) : Node.put(node, new io.arxila.atomichash.Entry(hash, key, remappedValue));
        } while (node != newNode && !this.root.compareAndSet(node, newNode));
        return remappedValue;
    }


    @Override
    public V merge(final K key, final V newValue, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(newValue);
        final int hash = io.arxila.atomichash.Entry.hash(key);
        V value, remappedValue;
        Node node, newNode;
        do {
            node = this.root.get();
            value = (V) normalizeAbsentValue(Node.get(node, key));
            remappedValue = (value == null) ? newValue : remappingFunction.apply(value, newValue);
            newNode =  (remappedValue == null) ? Node.remove(node, hash, key) : Node.put(node, new io.arxila.atomichash.Entry(hash, key, remappedValue));
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
