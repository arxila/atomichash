package io.aquen.atomichash;

import java.io.Serializable;

public class KeyValue<K,V> implements Serializable {

    private static final long serialVersionUID = -4410811831138689524L;
    static final KeyValueHashComparator<Object,Object> COMPARATOR = new KeyValueHashComparator<>();

    final Key<K> key;
    final V value;


    public KeyValue(final K key, final V value) {
        super();
        this.key = new Key<>(key);
        this.value = value;
    }


    /*
     * Will sort KeyValue objects entirely based on the hash-based ordering of their keys. Values are explicitly
     * ignored because this will only be used for segmenting entries during multi-insertion.
     *
     * Implementing a comparator here is preferable to making the KeyValue class directly implement the Comparable
     * interface because we are sorting only on the hash of the Key object.
     */
    public static class KeyValueHashComparator<K,V> implements java.util.Comparator<KeyValue<K,V>>, Serializable {

        private static final long serialVersionUID = 999374493097670529L;

        @Override
        public int compare(final KeyValue<K,V> o1, final KeyValue<K,V> o2) {
            return Key.hashCompare(o1.key, o2.key);
        }

    }


    // TODO Next steps: Node, with bitmap and values as an Object[] (performing instanceof is faster). No AtomicReferences because we need putAll to be atomic.
}
