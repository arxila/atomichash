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
import java.util.List;
import java.util.Set;

final class Root implements Serializable {

    private static final long serialVersionUID = 1589784256747023680L;
    
    static final Root EMPTY_ROOT = new Root(Node.EMPTY_NODE);

    final Node node;

    transient Set<Entry> entrySet;
    transient Set<Object> keySet;
    transient List<Object> valueList;



    public static Root of() {
        return Root.EMPTY_ROOT;
    }

    public static Root of(final Entry e1) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        return new Root(node);
    }

    public static Root of(final Entry e1, final Entry e2) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        return new Root(node);
    }

    public static Root of(final Entry e1, final Entry e2, final Entry e3) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        node = node.put(e3);
        return new Root(node);
    }

    public static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        node = node.put(e3);
        node = node.put(e4);
        return new Root(node);
    }

    public static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5) {
        Node node = Node.EMPTY_NODE;
        node = node.put(e1);
        node = node.put(e2);
        node = node.put(e3);
        node = node.put(e4);
        node = node.put(e5);
        return new Root(node);
    }

    public static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5,
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

    public static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5,
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

    public static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5,
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

    public static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5,
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

    public static Root of(final Entry e1, final Entry e2, final Entry e3, final Entry e4, final Entry e5,
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


    // May return Entry.NOT_FOUND if not found (so that it can be differentiated from a null value)
    Object get(final Object key) {
        return this.node.get(key);
    }



    Root put(final Entry entry) {
        final Node newNode = this.node.put(entry);
        return (this.node == newNode) ? this : new Root(newNode);

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

    
    public Set<Object> keySet() {
        Set<Object> keySet;
        if ((keySet = this.keySet) != null) {
            return keySet;
        }
        keySet = this.node.allKeys();
        return this.keySet = Collections.unmodifiableSet(keySet);
    }
    

    public Collection<Object> values() {
        List<Object> valueList;
        if ((valueList = this.valueList) != null) {
            return valueList;
        }
        valueList = this.node.allValues();
        return this.valueList = Collections.unmodifiableList(valueList);
    }
    

    public Set<Entry> entrySet() {
        Set<Entry> entrySet;
        if ((entrySet = this.entrySet) != null) {
            return entrySet;
        }
        entrySet = this.node.allEntries();
        return this.entrySet = Collections.unmodifiableSet(entrySet);
    }



}
