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
import java.util.Arrays;
import java.util.List;
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


    Entry(final int hash, final Object key, final Object value) {
        super();
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.collisions = null;
    }


    private Entry(final int hash, final Entry[] collisions) {
        super();
        this.hash = hash;
        this.key = null;
        this.value = null;
        this.collisions = collisions;
    }


    boolean containsKey(final int hash, final Object key) {
        if (this.collisions == null) {
            return this.hash == hash && eq(this.key, key);
        }
        if (this.hash != hash) {
            return false;
        }
        for (final Entry collision : this.collisions) {
            if (Objects.equals(collision.key, key)) {
                return true;
            }
        }
        return false;
    }


    boolean containsValue(final Object value) {
        if (this.collisions == null) {
            return eq(this.value, value);
        }
        for (final Entry collision : this.collisions) {
            if (Objects.equals(collision.value, value)) {
                return true;
            }
        }
        return false;
    }



    Object get(final Object key) {
        if (this.collisions == null) {
            return eq(this.key, key) ? this.value : NOT_FOUND;
        }
        for (final Entry collision : this.collisions) {
            if (Objects.equals(collision.key, key)) {
                return collision.value;
            }
        }
        return NOT_FOUND;

    }


    Entry set(final Entry entry) {
        // In order to determine whether a mapping already exists, key and value will be applied
        // referential equality and not object equality. This leaves room for the possibility of a mapping
        // (key and/or value) to be replaced by other objects even if these are "equals".
        if (this.collisions == null) {
            if (this.key == entry.key && this.value == entry.value) {
                return this;
            }
            return entry;
        }
        Entry collision;
        for (int i = 0; i < this.collisions.length; i++) {
            collision = this.collisions[i];
            if (Objects.equals(collision.key, entry.key)) {
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
        if (this.collisions == null) {
            return new Entry(this.hash, new Entry[]{this, entry});
        }
        final Entry[] newCollisions = new Entry[this.collisions.length + 1];
        System.arraycopy(this.collisions, 0, newCollisions, 0, this.collisions.length);
        newCollisions[this.collisions.length] = entry;
        return new Entry(this.hash, newCollisions);
    }


    Entry remove(final int hash, final Object key) {
        if (this.collisions == null) {
            return (this.hash == hash && eq(this.key, key)) ? null : this;
        }
        for (int i = 0; i < this.collisions.length; i++) {
            if (this.hash == hash && Objects.equals(this.collisions[i].key, key)) {
                if (this.collisions.length == 2) {
                    return this.collisions[1 - i];
                }
                final Entry[] newCollisions = new Entry[this.collisions.length - 1];
                System.arraycopy(this.collisions, 0, newCollisions, 0, i);
                System.arraycopy(this.collisions, i + 1, newCollisions, i, this.collisions.length - (i + 1));
                return new Entry(this.hash, newCollisions);
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
        return o1 == null ? o2 == null : o1.equals(o2);
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
            return Objects.equals(Set.of(this.collisions), Set.of(otherEntry.collisions));
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
