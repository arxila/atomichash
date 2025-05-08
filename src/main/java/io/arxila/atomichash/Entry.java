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
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

final class Entry implements Map.Entry<Object,Object>, Serializable {

    private static final long serialVersionUID = -5401891463106165174L;

    static final Object NOT_FOUND = new Object(); // Always to be checked using reference equality

    final int hash;
    final Object key;
    final Object value;
    final Entry[] collisions;



    public Entry(final int hash, final Object key, final Object value, final Entry[] collisions) {
        super();
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.collisions = collisions;
    }



    /*
     * Many of the most used classes for keys have well-implemented hashCode() methods (String, Integer...) but
     * it is important to cover the scenario of classes being used as keys that do not have a good implementation
     * of hashCode() or have no implementation at all -- in which case their identity hashCode (based on memory
     * address) will be used.
     *
     * This mirrors what the standard implementation of hashCode() in java.util.HashMap does to try to improve
     * uniformity of hashes by performing a bitwise XOR of the 16 most significant bits on the 16 less significant,
     * assuming that due to how memory assignment works in the JVM, in cases when the identity hash code is used,
     * the 16 most significant ones will probably show higher entropy.
     */
    static int hash(final Object object) {
        int h;
        return (object == null) ? 0 : (h = object.hashCode()) ^ (h >>> 16);
    }



    static boolean containsKey(final Entry entry, final int hash, final Object key) {
        return (entry.collisions == null) ? containsKeySimple(entry, hash, key) : containsKeyMultiple(entry, hash, key);
    }

    private static boolean containsKeySimple(final Entry entry, final int hash, final Object key) {
        return entry.hash == hash && eq(entry.key, key);
    }

    private static boolean containsKeyMultiple(final Entry entry, final int hash, final Object key) {
        if (entry.hash != hash) {
            return false;
        }
        for (final Entry collision : entry.collisions) {
            if (eq(key, collision.key)) {
                return true;
            }
        }
        return false;
    }


    static boolean containsValue(final Entry entry, final Object value) {
        return (entry.collisions == null) ? containsValueSimple(entry, value) : containsValueMultiple(entry, value);
    }

    private static boolean containsValueSimple(final Entry entry, final Object value) {
        return eq(entry.value, value);
    }

    private static boolean containsValueMultiple(final Entry entry, final Object value) {
        for (final Entry collision : entry.collisions) {
            if (eq(value, collision.value)) {
                return true;
            }
        }
        return false;
    }


    static Object get(final Entry entry, final Object key) {
        return (entry.collisions == null) ? getSimple(entry, key) : getMultiple(entry, key);
    }

    private static Object getSimple(final Entry entry, final Object key) {
        return eq(entry.key, key) ? entry.value : NOT_FOUND;
    }

    private static Object getMultiple(final Entry entry, final Object key) {
        for (final Entry collision : entry.collisions) {
            if (eq(key, collision.key)) {
                return collision.value;
            }
        }
        return NOT_FOUND;
    }


    static Entry set(final Entry entry, final Entry newEntry) {
        // In order to determine whether a mapping already exists, key and value will be applied
        // referential equality and not object equality. This leaves room for the possibility of a mapping
        // (key and/or value) to be replaced by other new objects even if these are "equals" to the old ones.
        return (entry.collisions == null) ? setSimple(entry, newEntry) : setMultiple(entry, newEntry);
    }

    private static Entry setSimple(final Entry entry, final Entry newEntry) {
        return (entry.key == newEntry.key && entry.value == newEntry.value) ? entry : newEntry;
    }

    private static Entry setMultiple(final Entry entry, final Entry newEntry) {
        Entry collision;
        for (int i = 0; i < entry.collisions.length; i++) {
            collision = entry.collisions[i];
            if (eq(newEntry.key, collision.key)) {
                if (collision.key == newEntry.key && collision.value == newEntry.value) {
                    return entry;
                }
                final Entry[] newCollisions = Arrays.copyOf(entry.collisions, entry.collisions.length);
                newCollisions[i] = newEntry;
                return new Entry(entry.hash, null, null, newCollisions);
            }
        }
        throw new IllegalStateException(); // Should never happen
    }


    static Entry add(final Entry entry, final Entry newEntry) {
        return (entry.collisions == null) ? addSimple(entry, newEntry) : addMultiple(entry, newEntry);
    }

    private static Entry addSimple(final Entry entry, final Entry newEntry) {
        return new Entry(entry.hash, null, null, new Entry[]{entry, newEntry});
    }

    private static Entry addMultiple(final Entry entry, final Entry newEntry) {
        final Entry[] newCollisions = new Entry[entry.collisions.length + 1];
        System.arraycopy(entry.collisions, 0, newCollisions, 0, entry.collisions.length);
        newCollisions[entry.collisions.length] = newEntry;
        return new Entry(entry.hash, null, null, newCollisions);
    }


    static Entry remove(final Entry entry, final int hash, final Object key) {
        return (entry.collisions == null) ? removeSimple(entry, hash, key) : removeMultiple(entry, hash, key);
    }

    private static Entry removeSimple(final Entry entry, final int hash, final Object key) {
        return (entry.hash == hash && eq(entry.key, key)) ? null : entry;
    }

    private static Entry removeMultiple(final Entry entry, final int hash, final Object key) {
        if (entry.hash == hash) {
            for (int i = 0; i < entry.collisions.length; i++) {
                if (eq(key, entry.collisions[i].key)) {
                    if (entry.collisions.length == 2) {
                        return entry.collisions[1 - i];
                    }
                    final Entry[] newCollisions = new Entry[entry.collisions.length - 1];
                    System.arraycopy(entry.collisions, 0, newCollisions, 0, i);
                    System.arraycopy(entry.collisions, i + 1, newCollisions, i, entry.collisions.length - (i + 1));
                    return new Entry(entry.hash, null, null, newCollisions);
                }
            }
        }
        return entry;
    }


    /*
     * Equivalent to Objects.equals(), but by being called only from
     * this class we might benefit from runtime profile information on the
     * type of o1. See java.util.AbstractMap#eq().
     *
     * Do not replace with Objects.equals() until JDK-8015417 is resolved.
     */
    private static boolean eq(final Object o1, final Object o2) {
        return (o1 == o2) || (o1 != null && o1.equals(o2));
    }


    @Override
    public Object getKey() {
        return this.key;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public Object setValue(Object value) {
        throw new UnsupportedOperationException("Cannot set value for immutable map entry");
    }


    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Map.Entry<?,?>)) {
            return false;
        }
        if (other instanceof Entry) {
            final Entry otherEntry = (Entry) other;
            if (this.hash != otherEntry.hash) {
                return false;
            }
            if (this.collisions == null) {
                if (otherEntry.collisions != null) {
                    return false;
                }
                return eq(this.key, otherEntry.key) && eq(this.value, otherEntry.value);
            }
            if (otherEntry.collisions == null) {
                return false;
            }
            return Set.of(this.collisions).equals(Set.of(otherEntry.collisions));
        }
        final Map.Entry<?,?> otherEntry = (Map.Entry<?,?>) other;
        return eq(this.key, otherEntry.getKey()) && eq(this.value, otherEntry.getValue());
    }


    @Override
    public int hashCode() {
        if (this.collisions == null) {
            // This follows the definition of java.util.Map.Entry#hashCode()
            return ((this.key == null) ? 0 : this.key.hashCode()) ^
                   ((this.value == null) ? 0 : this.value.hashCode());
        }
        return Set.of(this.collisions).hashCode();
    }

}
