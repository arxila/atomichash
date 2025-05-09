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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

final class Root implements Serializable {

    private static final long serialVersionUID = 1589784256747023680L;
    
    static final Root EMPTY_ROOT = new Root(Node.EMPTY_NODE);

    final Node node;

    transient Set<Entry> entrySet;
    transient Set<Object> keySet;
    transient List<Object> valueList;



    static Root of() {
        return Root.EMPTY_ROOT;
    }

    static Root of(final Entry e1) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        return new Root(node);
    }

    static Root of(final Entry e1, final Entry e2) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        return new Root(node);
    }

    static Root of(final Entry e1, final Entry e2, final Entry e3) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        node = node.put(e3);
        return new Root(node);
    }

    static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        node = node.put(e3);
        node = node.put(e4);
        return new Root(node);
    }

    static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        node = node.put(e3);
        node = node.put(e4);
        node = node.put(e5);
        return new Root(node);
    }

    static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5,
                          final Entry e6) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        node = node.put(e3);
        node = node.put(e4);
        node = node.put(e5);
        node = node.put(e6);
        return new Root(node);
    }

    static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5,
                          final Entry e6, final Entry e7) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        node = node.put(e3);
        node = node.put(e4);
        node = node.put(e5);
        node = node.put(e6);
        node = node.put(e7);
        return new Root(node);
    }

    static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5,
                          final Entry e6, final Entry e7, final Entry e8) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        node = node.put(e3);
        node = node.put(e4);
        node = node.put(e5);
        node = node.put(e6);
        node = node.put(e7);
        node = node.put(e8);
        return new Root(node);
    }

    static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5,
                          final Entry e6, final Entry e7, final Entry e8, final Entry e9) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        node = node.put(e3);
        node = node.put(e4);
        node = node.put(e5);
        node = node.put(e6);
        node = node.put(e7);
        node = node.put(e8);
        node = node.put(e9);
        return new Root(node);
    }

    static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5,
                          final Entry e6, final Entry e7, final Entry e8, final Entry e9, final Entry e10) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        node = node.put(e3);
        node = node.put(e4);
        node = node.put(e5);
        node = node.put(e6);
        node = node.put(e7);
        node = node.put(e8);
        node = node.put(e9);
        node = node.put(e10);
        return new Root(node);
    }

    private static Entry entry(final Object key, final Object value) {
        return entry(Entry.hash(key), key, value);
    }

    private static Entry entry(final int hash, final Object key, final Object value) {
        return new Entry(hash, key, value, null);
    }


    private Root(final Node node) {
        this.node = node;
    }



    int size() {
        return this.node.size;
    }
    
    boolean isEmpty() {
        return this.node == Node.EMPTY_NODE;
    }


    boolean containsKey(final Object key) {
        return this.node.containsKey(key);
    }

    boolean containsValue(final Object value) {
        return this.node.containsValue(value);
    }


    Object get(final Object key) {
        final Object value = this.node.get(key);
        return (value == Entry.NOT_FOUND) ? null : this.node.get(key);
    }

    Object getOrDefault(final Object key, final Object defaultValue) {
        final Object value = this.node.get(key);
        // The definition of java.util.Map#getOrDefault() returns the default value only if key is not mapped
        return (value == Entry.NOT_FOUND) ? defaultValue : value;
    }

    Map<Object,Object> getAll(final Object... keys) {
        if (keys == null || keys.length == 0) {
            return Collections.emptyMap();
        }
        final Map<Object,Object> map = new HashMap<>(keys.length + 1, 1.0f);
        Object value;
        for (final Object key : keys) {
            value = this.node.get(key);
            if (value != Entry.NOT_FOUND) {
                map.put(key, value);
            }
        }
        return map;
    }


    Root put(final Entry entry) {
        final Node newNode = this.node.put(entry);
        return (this.node == newNode) ? this : new Root(newNode);

    }

    Root putIfAbsent(final Entry entry) {
        // Map#putIfAbsent() considers null equivalent to absence
        final Object value = this.node.get(entry.key);
        return (value == null || value == Entry.NOT_FOUND) ? new Root(this.node.put(entry)): this;
    }

    Root putAll(final Set<Entry> newEntries) {
        Node newNode = this.node;
        for (final Entry entry : newEntries) {
            newNode = newNode.put(entry);
        }
        return (this.node == newNode) ? this : new Root(newNode);
    }


    Root remove(final int hash, final Object key) {
        final Node newNode = this.node.remove(hash, key);
        return (this.node == newNode) ? this : new Root(newNode);
    }

    Root remove(final int hash, final Object key, final Object oldValue) {
        final Object value = this.node.get(key); // May be NOT_FOUND if not mapped
        return (Objects.equals(value, oldValue)) ? new Root(this.node.remove(hash, key)) : this;
    }


    Root replace(final Entry newEntry) {
        // If key is mapped, the Map interface considers there is a replacement -> new Root object.
        return (this.node.containsKey(newEntry.key)) ? new Root(this.node.put(newEntry)) : this;
    }

    Root replace(final Entry newEntry, final Object oldValue) {
        final Object value = this.node.get(newEntry.key); // May be NOT_FOUND if not mapped
        // If oldValue matches, the Map interface considers there is a replacement -> new Root object.
        return (Objects.equals(value, oldValue)) ? new Root(this.node.put(newEntry)) : this;
    }

    Root replaceAll(final BiFunction<Object, Object, Object> function) {
        Node newNode = this.node;
        for (final Entry entry : entrySet()) {
            newNode = newNode.put(entry(entry.key, function.apply(entry.key, entry.value)));
        }
        return (this.node == newNode) ? this : new Root(newNode);
    }


    Root compute(final int hash, final Object key, final BiFunction<Object,Object,Object> remappingFunction) {
        final Object value = this.node.get(key);
        final Object remappedValue = remappingFunction.apply(key, (value == Entry.NOT_FOUND) ? null : value);
        final Node newNode =
                (remappedValue == null) ? this.node.remove(hash, key) : this.node.put(entry(hash, key, remappedValue));
        return (this.node == newNode) ? this : new Root(newNode);
    }

    Root computeIfAbsent(final int hash, final Object key, final Function<Object,Object> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        final Object value = this.node.get(key);
        final Object mappedValue =
                (value == null || value == Entry.NOT_FOUND) ? mappingFunction.apply(key) : null;
        final Node newNode =
                (mappedValue == null) ? this.node : this.node.put(entry(hash, key, mappedValue));
        return (this.node == newNode) ? this : new Root(newNode);
    }

    Root computeIfPresent(final int hash, final Object key, final BiFunction<Object,Object,Object> remappingFunction) {
        final Object value = this.node.get(key);
        final Object remappedValue =
                (value != null && value != Entry.NOT_FOUND) ? remappingFunction.apply(key, value) : null;
        final Node newNode =
                (value == null || value == Entry.NOT_FOUND) ?
                    this.node :  // Not present -> no changes
                    ((remappedValue == null) ? this.node.remove(hash, key) : this.node.put(entry(hash, key, remappedValue)));
        return (this.node == newNode) ? this : new Root(newNode);
    }


    Root merge(final int hash, final Object key, final Object newValue, final BiFunction<Object,Object,Object> remappingFunction) {
        final Object value = this.node.get(key);
        final Object remappedValue =
                (value == null || value == Entry.NOT_FOUND) ? newValue : remappingFunction.apply(value, newValue);
        final Node newNode =
                (remappedValue == null) ? this.node.remove(hash, key) : this.node.put(entry(hash, key, remappedValue));
        return (this.node == newNode) ? this : new Root(newNode);
    }


    Set<Object> keySet() {
        Set<Object> keySet;
        if ((keySet = this.keySet) != null) {
            return keySet;
        }
        keySet = this.node.allKeys();
        return this.keySet = Collections.unmodifiableSet(keySet);
    }
    
    Collection<Object> values() {
        List<Object> valueList;
        if ((valueList = this.valueList) != null) {
            return valueList;
        }
        valueList = this.node.allValues();
        return this.valueList = Collections.unmodifiableList(valueList);
    }


    Set<Entry> entrySet() {
        Set<Entry> entrySet;
        if ((entrySet = this.entrySet) != null) {
            return entrySet;
        }
        entrySet = this.node.allEntries();
        return this.entrySet = Collections.unmodifiableSet(entrySet);
    }


}
