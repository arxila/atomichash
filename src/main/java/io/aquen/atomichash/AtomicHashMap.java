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
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class AtomicHashMap<K,V> implements Map<K,V>, Serializable {

    private static final long serialVersionUID = 2626373528770987645L;

    private final AtomicReference<AtomicHashStore<K,V>> innerMap;



    public AtomicHashMap() {
        super();
        this.innerMap = new AtomicReference<>();
        this.innerMap.set(AtomicHashStore.of());
    }


    public AtomicHashMap(final Map<? extends K, ? extends V> m) {

        super();

        Objects.requireNonNull(m);

        this.innerMap = new AtomicReference<>();

        if (m instanceof AtomicHashMap) {
            final AtomicHashMap<? extends K, ? extends V> ahm = (AtomicHashMap<? extends K, ? extends V>)m;
            this.innerMap.set((AtomicHashStore<K, V>) ahm.store());
        } else {
            final AtomicHashStore<K, V> store = AtomicHashStore.of();
            this.innerMap.set(store.putAll(m));
        }

    }


    private AtomicHashMap(final AtomicHashStore<K,V> store) {
        super();
        this.innerMap = new AtomicReference<>();
        this.innerMap.set(store);
    }




    AtomicHashStore<K,V> store() {
        return this.innerMap.get();
    }




    @Override
    public int size() {
        return store().size();
    }




    @Override
    public boolean isEmpty() {
        return store().isEmpty();
    }




    @Override
    public boolean containsKey(final Object key) {
        return store().containsKey(key);
    }




    @Override
    public boolean containsValue(final Object value) {
        return store().containsValue(value);
    }




    @Override
    public V get(final Object key) {
        return store().get(key);
    }


    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return store().getOrDefault(key, defaultValue);
    }




    @Override
    public V put(final K key, final V value) {
        final ValueConsumer<V> vc = new ValueConsumer<>();
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.put(key, value, vc)));
        return vc.val;
    }




    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.putAll(m)));
    }




    @Override
    public V putIfAbsent(final K key, final V value) {
        final ValueConsumer<V> vc = new ValueConsumer<>();
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.putIfAbsent(key, value, vc)));
        return vc.val;
    }




    @Override
    public V remove(final Object key) {
        final ValueConsumer<V> vc = new ValueConsumer<>();
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.remove(key, vc)));
        return vc.val;
    }


    @Override
    public boolean remove(final Object key, final Object value) {
        final BooleanConsumer bc = new BooleanConsumer();
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.remove(key, value, bc)));
        return bc.val;
    }




    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        store().forEach(action);
    }




    @Override
    public V replace(final K key, final V value) {
        final ValueConsumer<V> vc = new ValueConsumer<>();
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.replace(key, value, vc)));
        return vc.val;
    }


    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        final BooleanConsumer bc = new BooleanConsumer();
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.replace(key, oldValue, newValue, bc)));
        return bc.val;
    }




    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.replaceAll(function)));
    }




    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        final ValueConsumer<V> vc = new ValueConsumer<>();
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.computeIfAbsent(key, mappingFunction, vc)));
        return vc.val;
    }




    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        final ValueConsumer<V> vc = new ValueConsumer<>();
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.computeIfPresent(key, remappingFunction, vc)));
        return vc.val;
    }




    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        final ValueConsumer<V> vc = new ValueConsumer<>();
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.compute(key, remappingFunction, vc)));
        return vc.val;
    }




    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        final ValueConsumer<V> vc = new ValueConsumer<>();
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.merge(key, value, remappingFunction, vc)));
        return vc.val;
    }




    @Override
    public void clear() {
        AtomicHashStore<K,V> store;
        do {
            store = store();
        } while(!this.innerMap.compareAndSet(store, store.clear()));
    }



    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        return new Sets.MapEntrySet<>(this.innerMap.get());
    }


    @Override
    public Set<K> keySet() {
        return new Sets.MapKeySet<>(this.innerMap.get());
    }


    @Override
    public Collection<V> values() {
        return new Collections.MapValueCollection<>(this.innerMap.get());
    }




    public static <K, V> AtomicHashMap<K, V> copyOf(final AtomicHashMap<? extends K, ? extends V> map) {
        return new AtomicHashMap<>((AtomicHashStore<K, V>) map.store());
    }




    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }

        if (o instanceof AtomicHashMap) {

            final AtomicHashMap<?,?> other = (AtomicHashMap<?,?>) o;

            final AtomicHashStore<K,V> st = store();
            final AtomicHashStore<?,?> ost = other.store();

            return st.equals(ost);

        }

        if (o instanceof Map) { // Map#equals() requires being able to compare with any other Map implementation

            final Map<?,?> m = (Map<?,?>)o;
            final AtomicHashStore<K,V> st = store();

            if (st.size() != m.size()) {
                return false;
            }

            HashEntry<K,V> entry;
            for (final AtomicHashStore.Entry<K, V> e : st) {
                entry = (HashEntry<K,V>)e;
                if (entry.value == null) {
                    if (!(m.get(entry.key) == null && m.containsKey(entry.key))) {
                        return false;
                    }
                } else {
                    if (!entry.value.equals(m.get(entry.key))) {
                        return false;
                    }
                }
            }

            return true;

        }

        return false;

    }


    @Override
    public int hashCode() {
        return store().hashCode();
    }




    private static class ValueConsumer<V> implements Consumer<V> {

        private V val = null;

        @Override
        public void accept(final V v) {
            this.val = v;
        }

    }

    private static class BooleanConsumer implements Consumer<Boolean> {

        private boolean val = false;

        @Override
        public void accept(final Boolean v) {
            this.val = (v != null && v.booleanValue());
        }

    }

}
