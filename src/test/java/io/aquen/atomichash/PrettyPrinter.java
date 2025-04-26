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

final class PrettyPrinter {


    static <K,V> String print(final AtomicHashMap<K,V> map) {
        return printNode(map.innerRoot());
    }


    static <K,V> String print(final AtomicHashStore<K,V> store) {
        return printNode(store.innerRoot());
    }


    static <K,V> String printNode(final Node node) {
        final StringBuilder stringBuilder = new StringBuilder();
        printNode(0, 0, 0, stringBuilder, node);
        return stringBuilder.toString();
    }


    private static <K,V> void printNode(
            final int indentLevel, final int level, final int indexInLevel, final StringBuilder stringBuilder,
            final Node node) {

        stringBuilder.append(indentForLevel(indentLevel));
        stringBuilder.append(String.format("[%1d|%02x] {\n", level, indexInLevel));
        for (final Entry childEntry : node.entries) {
            printEntry(indentLevel + 1, stringBuilder, childEntry);
        }
        for (int i = 0; i < node.nodes.length; i++) {
            final Node childNode = node.nodes[i];
            printNode(indentLevel + 1, level + 1, i, stringBuilder, childNode);
        }
        stringBuilder.append(indentForLevel(indentLevel));
        stringBuilder.append('}');
        stringBuilder.append('\n');

    }


    private static <K,V> void printEntry(
            final int indentLevel, final StringBuilder stringBuilder, final Entry entry) {

        stringBuilder.append(indentForLevel(indentLevel));
        stringBuilder.append(String.format("[%17s] (", formatHash(entry.hash)));
        if (entry.collisions == null) {
            stringBuilder.append(String.format(" <%s> <%s> )", entry.key, entry.value));
        } else {
            stringBuilder.append('\n');
            for (int i = 0; i < entry.collisions.length; i++) {
                stringBuilder.append(indentForLevel(indentLevel + 1));
                stringBuilder.append(String.format("( <%s> <%s> )", entry.key, entry.value));
                stringBuilder.append('\n');
            }
            stringBuilder.append(indentForLevel(indentLevel));
            stringBuilder.append(")");
        }
        stringBuilder.append('\n');

    }


    private static String formatHash(int hash) {
        int firstFragment = (hash >>> Node.HASH_SHIFTS[5]) & Node.HASH_MASK;
        int secondFragment = (hash >>> Node.HASH_SHIFTS[4]) & Node.HASH_MASK;
        int thirdFragment = (hash >>> Node.HASH_SHIFTS[3]) & Node.HASH_MASK;
        int fourthFragment = (hash >>> Node.HASH_SHIFTS[2]) & Node.HASH_MASK;
        int fifthFragment = (hash >>> Node.HASH_SHIFTS[1]) & Node.HASH_MASK;
        int sixthFragment = (hash >>> Node.HASH_SHIFTS[0]) & Node.HASH_MASK;

        return String.format("%02x|%02x|%02x|%02x|%02x|%02x",
                firstFragment, secondFragment, thirdFragment,
                fourthFragment, fifthFragment, sixthFragment);
    }



    private static String indentForLevel(final int indentLevel) {
        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < (indentLevel * 2); i++) {
            strBuilder.append(' ');
        }
        return strBuilder.toString();
    }




    private PrettyPrinter() {
        super();
    }
    
}
