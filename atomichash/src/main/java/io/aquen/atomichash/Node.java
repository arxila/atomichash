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
import java.util.Arrays;
import java.util.function.Consumer;

final class Node<K,V> implements Serializable {

    private static final long serialVersionUID = 6914544628900109073L;


    final Node<K,V>[] children; // can contain many nulls

    final int hash;
    final HashEntry<K,V> entry;
    final HashEntry<K,V>[] entries;





    Node(final Node<K,V>[] children) {
        super();
        this.children = children;
        this.hash = -1; // This is actually a valid hash value, but it will not be used when node has children
        this.entry = null;
        this.entries = null;
    }


    Node(final DataEntry<K,V> dataEntry) {
        super();
        this.children = null;
        this.hash = dataEntry.hash;
        this.entry = new HashEntry<>(dataEntry);
        this.entries = null;
    }


    private Node(final HashEntry<K,V> entry) {
        super();
        this.children = null;
        this.hash = entry.hash;
        this.entry = entry;
        this.entries = null;
    }


    private Node(final HashEntry<K,V>[] entries) {
        super();
        this.children = null;
        this.hash = entries[0].hash;
        this.entry = null;
        this.entries = entries;
    }




    int size() {

        if (this.children == null) {
            return (this.entry == null) ? this.entries.length : 1;
        }

        Node<K,V>[] children = this.children;
        Node<K,V> child;
        int size = 0;
        for (int i = 0; i < children.length; i++) {
            child = children[i];
            if (child != null) {
                size += child.size();
            }
        }
        return size;

    }




    Node<K,V> put(final int level, final DataEntry<K, V> entry, final Consumer<V> oldValueConsumer) {

        // Check if we simply need to add an additional entry to the ones already present
        if (this.children == null && this.hash == entry.hash) {
            return putData(entry, oldValueConsumer);
        }

        Node<K,V>[] newChildren = this.children;
        boolean newChildrenMutable = false;
        if (newChildren == null) {
            newChildren = new Node[AtomicHashStore.childrenSizeFor(level)];
            newChildren[AtomicHashStore.pos(level, this.hash)] = this;
            newChildrenMutable = true;
        }

        final int newEntryPos = AtomicHashStore.pos(level, entry.hash);
        newChildren = NodeBuilder.addChild(newChildren, newChildrenMutable, level, newEntryPos, entry, oldValueConsumer);
        if (newChildren == this.children){
            return this;
        }

        return new Node<>(newChildren);

    }


    private Node<K,V> putData(final DataEntry<K,V> newEntry, final Consumer<V> oldValueConsumer) {

        if (this.entry != null) {
            // This is single-valued

            if (this.entry.key == newEntry.key && this.entry.value == newEntry.value) {
                // No need to perform any modifications, we might avoid a rewrite of a tree path!
                if (oldValueConsumer != null) {
                    oldValueConsumer.accept(this.entry.value);
                }
                return this;
            }

            if (eq(this.entry.key, newEntry.key)) {
                // We are replacing the previous value for a new one
                if (oldValueConsumer != null) {
                    oldValueConsumer.accept(this.entry.value);
                }
                return new Node<>(newEntry);
            }

            if (oldValueConsumer != null) {
                oldValueConsumer.accept(null);
            }

            // There is an hash collision, but this is a different slot, so we need to go multi value
            final HashEntry<K,V>[] newEntries = new HashEntry[] { this.entry, new HashEntry<>(newEntry)};

            // We will keep this array sorted in order to ease searches in large multi-valued nodes
            Arrays.sort(newEntries);

            return new Node<>(newEntries);

        }

        // TODO We should improve this to avoid linear performance depending on the amount of collisions. This was also fixed in HashMap in Java 8 to avoid DoS

        int pos = -1;
        for (int i = 0; i < this.entries.length; i++) {
            if (eq(this.entries[i].key, newEntry.key)) {
                pos = i;
                break;
            }
        }
        if (pos >= 0) {

            if (oldValueConsumer != null) {
                oldValueConsumer.accept(this.entries[pos].value);
            }

            if (this.entries[pos].key == newEntry.key && this.entries[pos].value == newEntry.value) {
                // No need to perform any modifications, we might avoid a rewrite of a tree path!
                // Note this will only happen if key and value are actually the same object, not by object equality
                return this;
            }

            final HashEntry<K,V>[] newEntries = this.entries.clone();
            newEntries[pos] = new HashEntry<>(newEntry);

            // We will keep this array sorted in order to ease searches in large multi-valued nodes
            Arrays.sort(newEntries);

            return new Node<>(newEntries);

        }

        if (oldValueConsumer != null) {
            oldValueConsumer.accept(null);
        }

        final HashEntry<K,V>[] newEntries = Arrays.copyOf(this.entries, this.entries.length + 1);
        newEntries[this.entries.length] = new HashEntry<>(newEntry);

        // We will keep this array sorted in order to ease searches in large multi-valued nodes
        Arrays.sort(newEntries);

        return new Node<>(newEntries);

    }




    Node<K,V> putAll(final int level, final DataEntry<K, V>[] entries, final int start, final int end) {

        if (start == end) {
            return this;
        }

        if (start + 1 == end) {
            // Re-route to a normal "put" operation
            return put(level, entries[start], null);
        }

        Node<K,V>[] newChildren = this.children;
        boolean newChildrenMutable = false;

        if (newChildren == null) {
            // This is a data node, and we know there are at least two different keys that need to be inserted here.
            // Unless all the entries we are adding have the same hash as the existing data entry, we will need to
            // turn that data entry into a Node.

            if (allHashesMatch(this.hash, entries, start, end)) {
                // All hashes match! so we need to delegate entirely to the NodeData
                Node<K,V> newNode = this;
                for (int i = start; i < end; i++) {
                    newNode = newNode.putData(entries[i], null);
                }
                if (newNode == this) {
                    // Nothing was added because all entries already existed -- this should actually never happen
                    return this;
                }
                return newNode;
            }

            // Not all hashes matched (usual case), so given we have > 1 keys to be inserted in this node, we
            // are sure we will need to turn this Node's data into a nested node
            newChildren = new Node[AtomicHashStore.childrenSizeFor(level)];
            newChildren[AtomicHashStore.pos(level, this.hash)] = this;
            newChildrenMutable = true;

        }

        // We will need to segment all the selected entries, determining the position to be assigned to each segment

        int i = start;
        int x;

        int ipos = AtomicHashStore.pos(level, entries[i].hash);
        int currentPos;

        while (i < end) {

            x = i;
            currentPos = ipos;
            while (ipos == currentPos && ++i < end) {
                ipos = AtomicHashStore.pos(level, entries[i].hash);
            }

            // We determined that entries[x..i) corresponds to children[currentPos]

            newChildren = NodeBuilder.addChildren(newChildren, newChildrenMutable, level, currentPos, entries, x, i);
            if (newChildren != this.children){
                newChildrenMutable = true;
            }

        }

        if (newChildren == this.children) {
            return this;
        }

        return new Node(newChildren);

    }



    private static <K,V> boolean allHashesMatch(final int hash, final DataEntry<K,V>[] entries, final int start, final int end) {
        for (int i = start; i < end; i++) {
            if (entries[i].hash != hash) {
                return false;
            }
        }
        return true;
    }



    Node<K,V> remove(final int level, final int hash, final Object key, final Consumer<V> oldValueConsumer) {

        if (this.children != null) {

            final int pos = AtomicHashStore.pos(level, hash);
            final Node<K,V> child = this.children[pos];

            if (child == null) {
                // Not found
                return this;
            }

            final Node<K,V> newChild = child.remove(level + 1, hash, key, oldValueConsumer);
            if (newChild == child) {
                return this;
            }

            if (newChild == null && onlyOneChild(this.children)) {
                // This branch has become empty
                return null;
            }

            // Note newChild can still be null here
            final Node<K,V>[] newChildren = this.children.clone();
            newChildren[pos] = newChild;

            return new Node<>(newChildren);

        }

        // Not a branch -- this is a Node with data

        if (this.hash != hash) {
            return this;
        }

        return this.removeData(key, oldValueConsumer);

    }


    private Node<K,V> removeData(final Object key, final Consumer<V> oldValueConsumer) {

        if (this.entry != null) {
            // This is single-valued

            if (eq(this.entry.key,key)) {
                if (oldValueConsumer != null) {
                    oldValueConsumer.accept(this.entry.value);
                }
                return null;
            }

            if (oldValueConsumer != null) {
                oldValueConsumer.accept(null);
            }
            return this;

        }

        int pos = -1;
        for (int i = 0; i < this.entries.length; i++) {
            if (eq(this.entries[i].key, key)) {
                pos = i;
                break;
            }
        }

        if (pos >= 0) {

            if (oldValueConsumer != null) {
                oldValueConsumer.accept(this.entries[pos].value);
            }

            if (this.entries.length == 2) {
                // There are only two items in the multi value, and we are removing one, so now its single value
                final HashEntry<K,V> remainingEntry = this.entries[pos == 0? 1 : 0];
                return new Node<>(remainingEntry);
            }

            final HashEntry<K,V>[] newEntries = new HashEntry[this.entries.length - 1];
            System.arraycopy(this.entries, 0, newEntries, 0, pos);
            System.arraycopy(this.entries, pos + 1, newEntries, pos, (this.entries.length - (pos + 1)));

            return new Node<>(newEntries);

        }

        if (oldValueConsumer != null) {
            oldValueConsumer.accept(null);
        }

        return this;

    }




    private static <K,V> boolean onlyOneChild(final Node<K,V>[] children) {
        boolean found = false;
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                if (found) {
                    return false;
                }
                found = true;
            }
        }
        return true;
    }


    /**
     * Equivalent to Objects.equals(), but by being called only from
     * HashEntry we might benefit from runtime profile information on the
     * type of o1. See java.util.AbstractMap#eq().
     *
     * Do not replace with Object.equals until JDK-8015417 is resolved.
     */
    private static boolean eq(final Object o1, final Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }


}
