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
import java.util.Objects;
import java.util.Set;

final class Entry implements Map.Entry<Object,Object>, Serializable {

    private static final long serialVersionUID = -5401891463106165174L;

    static final Object NOT_FOUND = new Object(); // Always to be checked using reference equality

    final int hash;
    final Object key;
    final Object value;
    final Entry[] collisions;


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



    Entry(final int hash, final Object key, final Object value) {
        super();
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.collisions = null;
    }

    Entry(final Object key, final Object value) {
        super();
        this.hash = hash(key);
        this.key = key;
        this.value = value;
        this.collisions = null;
    }


    private Entry(final int hash, final Entry[] collisions) {
        super();
        // collisions will never be null, and this is meant to make clear that every object created though this
        // constructor will use the "*Multiple" methods.
        Objects.requireNonNull(collisions);
        this.hash = hash;
        this.key = null;
        this.value = null;
        this.collisions = collisions;
    }


    boolean containsKey(final int hash, final Object key) {
        return (this.collisions == null) ? containsKeySimple(hash, key) : containsKeyMultiple(hash, key);
    }

    private boolean containsKeySimple(final int hash, final Object key) {
        return this.hash == hash && eq(this.key, key);
    }

    private boolean containsKeyMultiple(final int hash, final Object key) {
        if (this.hash != hash) {
            return false;
        }
        for (final Entry collision : this.collisions) {
            if (eq(key, collision.key)) {
                return true;
            }
        }
        return false;
    }


    boolean containsValue(final Object value) {
        return (this.collisions == null) ? containsValueSimple(value) : containsValueMultiple(value);
    }

    private boolean containsValueSimple(final Object value) {
        return eq(this.value, value);
    }

    private boolean containsValueMultiple(final Object value) {
        for (final Entry collision : this.collisions) {
            if (eq(value, collision.value)) {
                return true;
            }
        }
        return false;
    }


    Object get(final Object key) {
        return (this.collisions == null) ? getSimple(key) : getMultiple(key);
    }

    private Object getSimple(final Object key) {
        return eq(this.key, key) ? this.value : NOT_FOUND;
    }

    private Object getMultiple(final Object key) {
        for (final Entry collision : this.collisions) {
            if (eq(key, collision.key)) {
                return collision.value;
            }
        }
        return NOT_FOUND;
    }


    Entry set(final Entry entry) {
        // In order to determine whether a mapping already exists, key and value will be applied
        // referential equality and not object equality. This leaves room for the possibility of a mapping
        // (key and/or value) to be replaced by other new objects even if these are "equals" to the old ones.
        return (this.collisions == null) ? setSimple(entry) : setMultiple(entry);
    }

    private Entry setSimple(final Entry entry) {
        return (this.key == entry.key && this.value == entry.value) ? this : entry;
    }

    private Entry setMultiple(final Entry entry) {
        Entry collision;
        for (int i = 0; i < this.collisions.length; i++) {
            collision = this.collisions[i];
            if (eq(entry.key, collision.key)) {
                if (collision.key == entry.key && collision.value == entry.value) {
                    return this;
                }
                final Entry[] newCollisions = Arrays.copyOf(this.collisions, this.collisions.length);
                newCollisions[i] = entry;
                return new Entry(this.hash, newCollisions);
            }
        }
        throw new IllegalStateException(); // Should never happen
    }


    Entry add(final Entry entry) {
        return (this.collisions == null) ? addSimple(entry) : addMultiple(entry);
    }

    private Entry addSimple(final Entry entry) {
        return new Entry(this.hash, new Entry[]{this, entry});
    }

    private Entry addMultiple(final Entry entry) {
        final Entry[] newCollisions = new Entry[this.collisions.length + 1];
        System.arraycopy(this.collisions, 0, newCollisions, 0, this.collisions.length);
        newCollisions[this.collisions.length] = entry;
        return new Entry(this.hash, newCollisions);
    }


    Entry remove(final int hash, final Object key) {
        return (this.collisions == null) ? removeSimple(hash, key) : removeMultiple(hash, key);
    }

    private Entry removeSimple(final int hash, final Object key) {
        return (this.hash == hash && eq(this.key, key)) ? null : this;
    }

    private Entry removeMultiple(final int hash, final Object key) {
        if (this.hash == hash) {
            for (int i = 0; i < this.collisions.length; i++) {
                if (eq(key, this.collisions[i].key)) {
                    if (this.collisions.length == 2) {
                        return this.collisions[1 - i];
                    }
                    final Entry[] newCollisions = new Entry[this.collisions.length - 1];
                    System.arraycopy(this.collisions, 0, newCollisions, 0, i);
                    System.arraycopy(this.collisions, i + 1, newCollisions, i, this.collisions.length - (i + 1));
                    return new Entry(this.hash, newCollisions);
                }
            }
        }
        return this;
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
