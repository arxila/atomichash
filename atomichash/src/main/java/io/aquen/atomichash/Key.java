package io.aquen.atomichash;

import java.io.Serializable;

public class Key<K> implements Serializable {

    private static final long serialVersionUID = -652313031611727623L;

    final int hash;
    final int[] indices;
    final K key;


    public Key(final K key) {
        super();
        this.key = key;
        this.hash = hash(key);
        this.indices = new int[] {
            this.hash & 0b111111,
            (this.hash >>> 6) & 0b111111,
            (this.hash >>> 12) & 0b111111,
            (this.hash >>> 18) & 0b111111,
            (this.hash >>> 24) & 0b111111,
            (this.hash >>> 30) & 0b11
        };
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
    static int hash(final Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }


    /*
     * Equivalent to Objects.equals(), but by being called only from
     * this class we might benefit from runtime profile information on the
     * type of o1. See java.util.AbstractMap#eq().
     *
     * Do not replace with Object.equals() until JDK-8015417 is resolved.
     */
    private static boolean eq(final Object o1, final Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Key)) {
            return false;
        }
        final Key<?> e = (Key<?>)o;
        return this.hash == e.hash && eq(this.key, e.key);
    }


    /*
     * Will sort keys in the same way that entries would be returned by an iterator (tree inorder). The aim of this
     * sorting algorithm is to be used when performing multiple insertions, allowing the easy segmenting of
     * the array of new entries so that each segment is easily sent to its corresponding subtree.
     *
     * The use of this comparison algorithm should be restricted to sorting during multi-insertion. Other uses could
     * lead to issues because the algorithm is NOT compatible with the natural order of keys, i.e., even if two Key
     * objects that are .equals() are guaranteed to return .compareTo() == 0, the contrary cannot be guaranteed
     * because the comparison algorithm is only based on the hash value and not the key itself.
     *
     * Note also that the hash being used is the one returned by the hash() method and not the key's .hashCode(),
     * which is specific to this class and thus may not be compatible with other sorting mechanisms present in the
     * key objects themselves (such as e.g. the key objects implementing Comparable).
     */
    static <K> int hashCompare(final Key<K> o1, final Key<K> o2) {

        if (o1.key == o2.key || o1.hash == o2.hash) {
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
