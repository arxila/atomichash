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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AtomicHashStoreOfTest {

    @Test
    void testOfEmpty() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of();
        assertTrue(store.isEmpty());
        assertEquals(0, store.size());
    }

    @Test
    void testOfOneEntry() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of("one", 1);
        assertEquals(1, store.size());
        assertEquals(1, store.get("one"));
    }

    @Test
    void testOfTwoEntries() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of("one", 1, "two", 2);
        assertEquals(2, store.size());
        assertEquals(1, store.get("one"));
        assertEquals(2, store.get("two"));
    }

    @Test
    void testOfThreeEntries() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of("one", 1, "two", 2, "three", 3);
        assertEquals(3, store.size());
        assertEquals(1, store.get("one"));
        assertEquals(2, store.get("two"));
        assertEquals(3, store.get("three"));
    }

    @Test
    void testOfFourEntries() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of(
            "one", 1, "two", 2, "three", 3, "four", 4
        );
        assertEquals(4, store.size());
        assertEquals(1, store.get("one"));
        assertEquals(2, store.get("two"));
        assertEquals(3, store.get("three"));
        assertEquals(4, store.get("four"));
    }

    @Test
    void testOfFiveEntries() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5
        );
        assertEquals(5, store.size());
        assertEquals(1, store.get("one"));
        assertEquals(2, store.get("two"));
        assertEquals(3, store.get("three"));
        assertEquals(4, store.get("four"));
        assertEquals(5, store.get("five"));
    }

    @Test
    void testOfSixEntries() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6
        );
        assertEquals(6, store.size());
        assertEquals(1, store.get("one"));
        assertEquals(2, store.get("two"));
        assertEquals(3, store.get("three"));
        assertEquals(4, store.get("four"));
        assertEquals(5, store.get("five"));
        assertEquals(6, store.get("six"));
    }

    @Test
    void testOfSevenEntries() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6, "seven", 7
        );
        assertEquals(7, store.size());
        assertEquals(1, store.get("one"));
        assertEquals(2, store.get("two"));
        assertEquals(3, store.get("three"));
        assertEquals(4, store.get("four"));
        assertEquals(5, store.get("five"));
        assertEquals(6, store.get("six"));
        assertEquals(7, store.get("seven"));
    }

    @Test
    void testOfEightEntries() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6, "seven", 7, "eight", 8
        );
        assertEquals(8, store.size());
        assertEquals(1, store.get("one"));
        assertEquals(2, store.get("two"));
        assertEquals(3, store.get("three"));
        assertEquals(4, store.get("four"));
        assertEquals(5, store.get("five"));
        assertEquals(6, store.get("six"));
        assertEquals(7, store.get("seven"));
        assertEquals(8, store.get("eight"));
    }

    @Test
    void testOfNineEntries() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6, "seven", 7, "eight", 8, "nine", 9
        );
        assertEquals(9, store.size());
        assertEquals(1, store.get("one"));
        assertEquals(2, store.get("two"));
        assertEquals(3, store.get("three"));
        assertEquals(4, store.get("four"));
        assertEquals(5, store.get("five"));
        assertEquals(6, store.get("six"));
        assertEquals(7, store.get("seven"));
        assertEquals(8, store.get("eight"));
        assertEquals(9, store.get("nine"));
    }

    @Test
    void testOfTenEntries() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6, "seven", 7, "eight", 8, "nine", 9, "ten", 10
        );
        assertEquals(10, store.size());
        assertEquals(1, store.get("one"));
        assertEquals(2, store.get("two"));
        assertEquals(3, store.get("three"));
        assertEquals(4, store.get("four"));
        assertEquals(5, store.get("five"));
        assertEquals(6, store.get("six"));
        assertEquals(7, store.get("seven"));
        assertEquals(8, store.get("eight"));
        assertEquals(9, store.get("nine"));
        assertEquals(10, store.get("ten"));
    }

    @Test
    void testDuplicateKeys() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of(
            "one", 1, "one", 2
        );
        assertEquals(1, store.size());
        assertEquals(2, store.get("one")); // Last value should win
    }

    @Test
    void testNullKey() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of(null, 1);
        assertEquals(1, store.size());
        assertEquals(1, store.get(null));
    }

    @Test
    void testNullValue() {
        AtomicHashStore<String, Integer> store = AtomicHashStore.of("key", null);
        assertEquals(1, store.size());
        assertNull(store.get("key"));
    }
}