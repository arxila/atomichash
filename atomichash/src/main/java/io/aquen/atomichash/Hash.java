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

final class Hash {

    public static final int MAX_LEVEL = 5;

    private static final int[] SHIFTS = new int[] { 0, 6, 12, 18, 24, 30 };
    private static final int MASK = 0b111111;


    static int index(final int hash, final int level) {
        return (hash >>> SHIFTS[level]) & MASK;
    }

    static long mask(final int hash, final int level) {
        return 1L << ((hash >>> SHIFTS[level]) & MASK);
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


    private Hash() {
        super();
    }

}
