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

public final class Hash implements Serializable {

    private static final long serialVersionUID = 5018497257745964899L;

    public static final int MAX_LEVEL = 5;

    final int hash;
    final int[] indices;


    public static Hash of(final Object object) {
        final int hash = hash(object);
        final int[] indices = new int[] {
                hash & 0b111111,
                (hash >>> 6) & 0b111111,
                (hash >>> 12) & 0b111111,
                (hash >>> 18) & 0b111111,
                (hash >>> 24) & 0b111111,
                (hash >>> 30) & 0b11
        };
        return new Hash(hash, indices);
    }


    private Hash(final int hash, final int[] indices) {
        super();
        this.hash = hash;
        this.indices = indices;
    }


    /*
     * Many of the most used classes for keys have well-implemented hashCode() methods (String, Integer...) but
     * it is important to cover the scenario of classes being used as keys that do not have a good implementation
     * of hashCode() or have no implementation at all -- in which case their identity hashCode (based on memory
     * address) will be used.
     *
     * We will mirror what the standard implementation of "hashCode()" in java.util.HashMap does to try to improve
     * uniformity of hashes by performing a bitwise XOR of the 16 most significant bits on the 16 less significant,
     * assuming that due to how memory assignment works in the JVM, in cases when the identity hash code is used,
     * the 16 most significant ones will probably show a higher entropy.
     */
    static int hash(final Object object) {
        int h;
        return (object == null) ? 0 : (h = object.hashCode()) ^ (h >>> 16);
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Hash)) {
            return false;
        }
        return this.hash == ((Hash)o).hash;
    }


    /*
     * Will sort hashes in the same way that entries would be returned by an iterator (tree inorder). The aim of this
     * sorting algorithm is to be used when performing multiple insertions, allowing the easy segmenting of
     * the array of new entries so that each segment is easily sent to its corresponding subtree.
     *
     * The use of this comparison algorithm should be restricted to sorting during multi-insertion. Other uses could
     * lead to issues because the algorithm is NOT compatible with the natural order of objects, i.e., even if two Hash
     * objects that are .equals() are guaranteed to return .compareTo() == 0, the contrary cannot be guaranteed
     * because the comparison algorithm is only based on the hash value and not the key itself.
     *
     * Note also that the hash being used is the one returned by the hash() method and not the key's .hashCode(),
     * which is specific to this class and thus may not be compatible with other sorting mechanisms present in the
     * key objects themselves (such as e.g. the key objects implementing Comparable).
     */
    static <K> int hashCompare(final Hash o1, final Hash o2) {
        if (o1.hash == o2.hash) {
            return 0;
        }
        if (o1.indices[0] != o2.indices[0]) {
            return Integer.compare(o1.indices[0], o2.indices[0]);
        }
        if (o1.indices[1] != o2.indices[1]) {
            return Integer.compare(o1.indices[1], o2.indices[1]);
        }
        if (o1.indices[2] != o2.indices[2]) {
            return Integer.compare(o1.indices[2], o2.indices[2]);
        }
        if (o1.indices[3] != o2.indices[3]) {
            return Integer.compare(o1.indices[3], o2.indices[3]);
        }
        if (o1.indices[4] != o2.indices[4]) {
            return Integer.compare(o1.indices[4], o2.indices[4]);
        }
        return Integer.compare(o1.indices[5], o2.indices[5]);
    }

}
