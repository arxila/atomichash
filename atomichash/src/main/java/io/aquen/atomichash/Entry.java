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
import java.util.Objects;

final class Entry implements Serializable {

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


    public boolean containsKey(final int hash, final Object key) {
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



    public Object get(final Object key) {
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


    public Entry add(final Entry entry) {
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


}
