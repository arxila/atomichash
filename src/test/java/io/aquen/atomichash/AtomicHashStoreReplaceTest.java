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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AtomicHashStoreReplaceTest {

    private AtomicHashStore<String, String> store;

    @BeforeEach
    public void initStore() {
        this.store = new AtomicHashStore<>();
    }

    @Test
    public void testReplaceWithOldValueCondition() throws Exception {
        AtomicHashStore<String, String> st = this.store;
        AtomicHashStore<String, String> st2 = st;

        // Replace in an empty store (should return the same instance as no modification happens)
        st2 = st.replace("one", "ONE", "[x]ONE");
        Assertions.assertTrue(st2.isEmpty());
        Assertions.assertSame(st, st2); // Assert they reference the same object

        // Add a value to the store
        st = st.put("one", "ONE");

        // Replace an existing key when old value matches
        st2 = st.replace("one", "ONE", "[x]ONE");
        Assertions.assertEquals(1, st2.size());
        Assertions.assertEquals("[x]ONE", st2.get("one"));
        Assertions.assertNotSame(st, st2); // New instance should be created

        // Replace fails when old value doesn't match (returns the same instance)
        st = st2.replace("one", "INVALID", "[y]ONE");
        Assertions.assertEquals(1, st.size());
        Assertions.assertEquals("[x]ONE", st.get("one"));
        Assertions.assertSame(st, st2); // Assert they reference the same object
    }

    @Test
    public void testReplaceWithoutOldValueCondition() throws Exception {
        AtomicHashStore<String, String> st = this.store;
        AtomicHashStore<String, String> st2 = st;

        // Replace in an empty store (no effect)
        st2 = st.replace("one", "[x]ONE");
        Assertions.assertTrue(st2.isEmpty());
        Assertions.assertSame(st, st2); // Assert same instance is returned due to no changes

        // Add a value to the store
        st = st.put("one", "ONE");

        // Replace an existing key
        st2 = st.replace("one", "[x]ONE");
        Assertions.assertEquals(1, st2.size());
        Assertions.assertEquals("[x]ONE", st2.get("one"));
        Assertions.assertNotSame(st, st2); // New instance should be created

        // Replace again with the same value (should return the same instance)
        st = st2.replace("one", "[x]ONE");
        Assertions.assertEquals(1, st.size());
        Assertions.assertEquals("[x]ONE", st.get("one"));
        Assertions.assertSame(st, st2); // Same instance returned as no changes were made
    }

    @Test
    public void testReplaceWithNullValues() throws Exception {
        AtomicHashStore<String, String> st = this.store;

        // Add a key with a null value
        st = st.put("one", null);

        // Replace key with a null value
        AtomicHashStore<String, String> st2 = st.replace("one", null, "[x]ONE");
        Assertions.assertEquals(1, st2.size());
        Assertions.assertEquals("[x]ONE", st2.get("one"));

        // Attempt replacing with null values
        st2 = st.replace("one", "INVALID", null);
        Assertions.assertEquals(1, st2.size());
        Assertions.assertNull(st2.get("one")); // Value replaced with null

        // Replace in an empty store using null key (should do nothing)
        st2 = st.replace(null, "[x]ONE");
        Assertions.assertSame(st, st2); // Same instance should be returned due to no changes
    }

    @Test
    public void testHashCollisions() throws Exception {
        // Simulate a basic hash collision scenario by overriding hashCode
        class Key {
            private final String value;

            Key(String value) {
                this.value = value;
            }

            @Override
            public int hashCode() {
                return 42; // Force a hash collision
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Key && ((Key) obj).value.equals(this.value);
            }

            @Override
            public String toString() {
                return value;
            }
        }

        AtomicHashStore<Key, String> st = new AtomicHashStore<>();

        // Add two keys with the same hash code
        Key key1 = new Key("one");
        Key key2 = new Key("two");

        st = st.put(key1, "ONE");
        st = st.put(key2, "TWO");

        Assertions.assertEquals(2, st.size());
        Assertions.assertEquals("ONE", st.get(key1));
        Assertions.assertEquals("TWO", st.get(key2));

        // Replace one entry
        AtomicHashStore<Key, String> st2 = st.replace(key1, "ONE", "[x]ONE");
        Assertions.assertEquals(2, st2.size());
        Assertions.assertEquals("[x]ONE", st2.get(key1));
        Assertions.assertEquals("TWO", st2.get(key2));

        // Ensure immutability (original instance is unchanged)
        Assertions.assertEquals("ONE", st.get(key1));
        Assertions.assertEquals("TWO", st.get(key2));
    }

    @Test
    public void testReplaceSameInstanceBehavior() throws Exception {
        AtomicHashStore<String, String> st = this.store;

        // Add an entry to the store
        st = st.put("key", "value");

        // Replace with the same value (should return the same instance)
        AtomicHashStore<String, String> st2 = st.replace("key", "value", "value");
        Assertions.assertSame(st, st2);

        // Replace with a different value (should return a new instance)
        st2 = st.replace("key", "value", "newValue");
        Assertions.assertNotSame(st, st2);
        Assertions.assertEquals("newValue", st2.get("key"));
    }

}