/*
 * =========================================================================
 *                                                                          
 *   Copyright (c) 2019-2024, Aquen (https://aquen.io)                  
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

import java.util.function.Consumer;

final class NodeBuilder {


    static <K,V> Node<K,V>[] addChild(final Node<K,V>[] children, final boolean childrenMutable,
                                      final int level,
                                      final int newEntryPos, final DataEntry<K,V> newEntry,
                                      final Consumer<V> valueConsumer) {

        // Check the current status of the position to be used
        final Node<K,V> childInPos = children[newEntryPos];

        // There is something in the selected pos, so we need to delegate
        final Node<K,V> newChild;
        if (childInPos != null) {
            newChild = childInPos.put(level + 1, newEntry, valueConsumer);
            if (childInPos == newChild) {
                return children;
            }
        } else {
            newChild = new Node<>(newEntry);
        }

        final Node<K,V>[] newChildren = (childrenMutable? children : children.clone());
        newChildren[newEntryPos] = newChild;

        return newChildren;

    }


    static <K,V> Node<K,V>[] addChildren(final Node<K,V>[] children, final boolean childrenMutable,
                                         final int level,
                                         final int newEntryPos, final DataEntry<K,V>[] newEntries, final int start, final int end) {

        // ASSERTION: We know for sure that (end - start) > 1. We would have been re-routed to a simple "put" if not.

        if (start + 1 == end) {
            return addChild(children, childrenMutable, level, newEntryPos, newEntries[start], null);
        }

        // Check the current status of the position to be used
        final Node<K,V> childInPos = children[newEntryPos];

        // There is something in the selected pos, so we need to delegate
        final Node<K,V> newChild;
        if (childInPos != null) {
            newChild = childInPos.putAll(level + 1, newEntries, start, end);
            if (childInPos == newChild) {
                return children;
            }
        } else {
            // We create a temporary node that only contains data, and execute a putAll on it.
            // Note the NodeData object will be reused (the temporary Node won't, but it's lightweight).
            final Node<K,V> tempChild = new Node<>(newEntries[start]);
            newChild = tempChild.putAll(level + 1, newEntries, start + 1, end);
        }

        final Node<K,V>[] newChildren = (childrenMutable? children : children.clone());
        newChildren[newEntryPos] = newChild;

        return newChildren;

    }



    private NodeBuilder() {
        super();
    }

}
