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
package io.aquen.atomichash.old;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

abstract class Iterators<K,V> {

    /*
     * Location variables: these locate a child of the node currently at the top of the stack. So
     * only nodes with children should be put on the stack
     */
    private final Node<K,V>[] stack;
    private int stackSize;
    private int[] currentChild;

    private HashEntry<K,V> entry;
    private HashEntry<K,V>[] entries;
    private int entriesPos;


    protected Iterators(final Node<K,V> root) {

        super();
        this.entry = null;

        if (root == null || root.children == null) {

            this.stack = null;
            this.currentChild = null;
            this.stackSize = 0;
            if (root != null) {
                extractData(root);
            }

        } else {

            this.stack = new Node[AtomicHashStore.LEVEL_COUNT]; // max possible node nesting level
            Arrays.fill(this.stack, null);
            this.currentChild = new int[this.stack.length];
            Arrays.fill(this.currentChild, -1);
            this.stack[0] = root;
            this.stackSize = 1;
            computeNext();

        }

    }



    public boolean hasNext() {
        return this.entry != null;
    }


    public HashEntry<K,V> nextEntry() {
        final HashEntry<K,V> n = this.entry;
        computeNext();
        return n;
    }




    private void computeNext() {

        if (this.entries != null) {
            this.entriesPos++;
            if (this.entriesPos < this.entries.length) {
                this.entry = this.entries[this.entriesPos];
                return;
            }
            this.entries = null;
            this.entriesPos = -1;
        }

        this.entry = null;
        if (this.stackSize > 0) {
            selectNextNode();
        }

    }



    private void selectNextNode() {
        if (selectNextSibling()) {
            return;
        }
        this.stackSize--;
        this.stack[this.stackSize] = null;
        if (this.stackSize == 0) {
            return;
        }
        selectNextNode();
    }


    private boolean selectNextSibling() {
        final int stackPos = this.stackSize - 1;
        final Node<K,V> currentNode = this.stack[stackPos];
        int childi = this.currentChild[stackPos] + 1;
        while (childi < currentNode.children.length && currentNode.children[childi] == null) {
            childi++;
        }
        if (childi == currentNode.children.length) {
            this.currentChild[stackPos] = -1;
            return false;
        }
        this.currentChild[stackPos] = childi;
        return selectDeepest();
    }


    private boolean selectDeepest() {
        final int stackPos = this.stackSize - 1;
        final Node<K,V> currentNode = this.stack[stackPos];
        int i = this.currentChild[stackPos];
        if (currentNode.children[i].children == null) {
            extractData(currentNode.children[i]);
            return true;
        }
        this.stack[this.stackSize] = currentNode.children[i];
        this.currentChild[this.stackSize] = -1;
        this.stackSize++;
        return selectNextSibling();
    }


    private void extractData(final Node<K,V> dataNode) {
        if (dataNode.entry != null) {
            this.entry = dataNode.entry;
        } else {
            this.entries = dataNode.entries;
            this.entriesPos = 0;
            this.entry = this.entries[0];
        }
    }




    final static class StoreEntryIterator<K,V>
            extends Iterators<K,V>
            implements Iterator<AtomicHashStore.Entry<K,V>>{

        StoreEntryIterator(final Node<K, V> root) {
            super(root);
        }

        @Override
        public AtomicHashStore.Entry<K, V> next() {
            return super.nextEntry();
        }

    }


    final static class MapEntryIterator<K,V>
            extends Iterators<K,V>
            implements Iterator<Map.Entry<K,V>>{

        MapEntryIterator(final Node<K, V> root) {
            super(root);
        }

        @Override
        public Map.Entry<K, V> next() {
            return super.nextEntry();
        }

    }


    final static class KeyIterator<K,V>
            extends Iterators<K,V>
            implements Iterator<K> {

        KeyIterator(final Node<K, V> root) {
            super(root);
        }

        @Override
        public K next() {
            return super.nextEntry().getKey();
        }
    }


    final static class ValueIterator<K,V>
            extends Iterators<K,V>
            implements Iterator<V> {

        ValueIterator(final Node<K, V> root) {
            super(root);
        }

        @Override
        public V next() {
            return super.nextEntry().getValue();
        }
    }


}
