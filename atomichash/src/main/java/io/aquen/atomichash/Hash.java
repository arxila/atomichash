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

final class Hash implements Serializable {

    private static final long serialVersionUID = 5018497257745964899L;

    public static final int MAX_LEVEL = 5;

    private static final int[] SHIFTS = new int[] { 0, 6, 12, 18, 24, 30 };
    private static final int MASK = 0b111111;
    static final int NEG_MASK = 1 << 31; // will be used for turning 0..63 int positions into negative

    final int hash;


    public static Hash of(final Object object) {
        return new Hash(hash(object));
    }


    private Hash(final int hash) {
        super();
        this.hash = hash;
    }


    int index(final int level) {
        return (this.hash >>> SHIFTS[level]) & MASK;
    }

    long mask(final int level) {
        return 1L << ((this.hash >>> SHIFTS[level]) & MASK);
    }

    /*
     * Computes the position in a compact array by using a bitmap. This bitmap will contain 1's for
     * every position of the possible 64 (max size of the values array) that can contain an element. The
     * position in the array will correspond to the number of positions occupied before the index, i.e. the
     * number of bits to the right of the bit corresponding to the index for this level.
     *
     * This algorithm benefits from the fact that Long.bitCount is an intrinsic candidate typically implemented
     * as a single "population count" CPU instruction, and thus the computation will be O(1).
     */
    int pos(final int level, final long bitMap) {
        final long indexMask = mask(level);
        final int pos = Long.bitCount(bitMap & (indexMask - 1L));
        return ((bitMap & indexMask) != 0L) ? pos : (pos ^ NEG_MASK); // positive if present, negative if absent
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
    static int hashCompare(final Hash o1, final Hash o2) {
        if (o1.hash == o2.hash) {
            return 0;
        }
        final int index0 = Integer.compare(o1.index(0), o2.index(0));
        if (index0 != 0) {
            return index0;
        }
        final int index1 = Integer.compare(o1.index(1), o2.index(1));
        if (index1 != 0) {
            return index1;
        }
        final int index2 = Integer.compare(o1.index(2), o2.index(2));
        if (index2 != 0) {
            return index2;
        }
        final int index3 = Integer.compare(o1.index(3), o2.index(3));
        if (index3 != 0) {
            return index3;
        }
        final int index4 = Integer.compare(o1.index(4), o2.index(4));
        if (index4 != 0) {
            return index4;
        }
        return Integer.compare(o1.index(5), o2.index(5));
    }

}
