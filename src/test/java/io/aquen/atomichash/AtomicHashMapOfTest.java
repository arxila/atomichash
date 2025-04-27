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
import static org.junit.jupiter.api.Assertions.*;

public class AtomicHashMapOfTest {

    @Test
    void testOfEmpty() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    void testOfOneEntry() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of("one", 1);
        assertEquals(1, map.size());
        assertEquals(1, map.get("one"));
    }

    @Test
    void testOfTwoEntries() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of("one", 1, "two", 2);
        assertEquals(2, map.size());
        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
    }

    @Test
    void testOfThreeEntries() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of("one", 1, "two", 2, "three", 3);
        assertEquals(3, map.size());
        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertEquals(3, map.get("three"));
    }

    @Test
    void testOfFourEntries() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of(
            "one", 1, "two", 2, "three", 3, "four", 4
        );
        assertEquals(4, map.size());
        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertEquals(3, map.get("three"));
        assertEquals(4, map.get("four"));
    }

    @Test
    void testOfFiveEntries() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5
        );
        assertEquals(5, map.size());
        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertEquals(3, map.get("three"));
        assertEquals(4, map.get("four"));
        assertEquals(5, map.get("five"));
    }

    @Test
    void testOfSixEntries() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6
        );
        assertEquals(6, map.size());
        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertEquals(3, map.get("three"));
        assertEquals(4, map.get("four"));
        assertEquals(5, map.get("five"));
        assertEquals(6, map.get("six"));
    }

    @Test
    void testOfSevenEntries() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6, "seven", 7
        );
        assertEquals(7, map.size());
        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertEquals(3, map.get("three"));
        assertEquals(4, map.get("four"));
        assertEquals(5, map.get("five"));
        assertEquals(6, map.get("six"));
        assertEquals(7, map.get("seven"));
    }

    @Test
    void testOfEightEntries() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6, "seven", 7, "eight", 8
        );
        assertEquals(8, map.size());
        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertEquals(3, map.get("three"));
        assertEquals(4, map.get("four"));
        assertEquals(5, map.get("five"));
        assertEquals(6, map.get("six"));
        assertEquals(7, map.get("seven"));
        assertEquals(8, map.get("eight"));
    }

    @Test
    void testOfNineEntries() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6, "seven", 7, "eight", 8, "nine", 9
        );
        assertEquals(9, map.size());
        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertEquals(3, map.get("three"));
        assertEquals(4, map.get("four"));
        assertEquals(5, map.get("five"));
        assertEquals(6, map.get("six"));
        assertEquals(7, map.get("seven"));
        assertEquals(8, map.get("eight"));
        assertEquals(9, map.get("nine"));
    }

    @Test
    void testOfTenEntries() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of(
            "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6, "seven", 7, "eight", 8, "nine", 9, "ten", 10
        );
        assertEquals(10, map.size());
        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertEquals(3, map.get("three"));
        assertEquals(4, map.get("four"));
        assertEquals(5, map.get("five"));
        assertEquals(6, map.get("six"));
        assertEquals(7, map.get("seven"));
        assertEquals(8, map.get("eight"));
        assertEquals(9, map.get("nine"));
        assertEquals(10, map.get("ten"));
    }

    @Test
    void testDuplicateKeys() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of(
            "one", 1, "one", 2
        );
        assertEquals(1, map.size());
        assertEquals(2, map.get("one")); // Last value should win
    }

    @Test
    void testNullKey() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of(null, 1);
        assertEquals(1, map.size());
        assertEquals(1, map.get(null));
    }

    @Test
    void testNullValue() {
        AtomicHashMap<String, Integer> map = AtomicHashMap.of("key", null);
        assertEquals(1, map.size());
        assertNull(map.get("key"));
    }
}