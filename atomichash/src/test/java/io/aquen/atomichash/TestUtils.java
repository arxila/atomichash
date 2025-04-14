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


import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public final class TestUtils {

    private static final String COLLISION_PREFIX = "COLLISION-";
    private static final String COLLISION_SUFFIX = "-COLLISION";
    private static final String[] COLLISIONS;


    static {

        final String[] collisionFragments = new String[] { "aa", "bB", "c#"};

        final List<String> collisions = new ArrayList<>();
        for (int i = 0; i < collisionFragments.length; i++) {
            for (int j = 0; j < collisionFragments.length; j++) {
                for (int k = 0; k < collisionFragments.length; k++) {
                    for (int l = 0; l < collisionFragments.length; l++) {
                        collisions.add(collisionFragments[i] + collisionFragments[j] + collisionFragments[k] + collisionFragments[l]);
                    }
                }
            }
        }

        COLLISIONS = new String[collisions.size()];
        for (int i = 0; i < collisions.size(); i++) {
            COLLISIONS[i] = COLLISION_PREFIX + collisions.get(i) + COLLISION_SUFFIX;
        }

    }



    public static String generateString(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

    public static String generateKey() {
        return generateString(20);
    }

    public static String generateValue() {
        return generateString(150);
    }


    public static KeyValue<String,String>[] generateStringStringKeyValues(
            final int numElements, final int numCollisions, final int numRepeatedKeys) {

        final KeyValue<String,String>[] entries = new KeyValue[numElements];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new KeyValue<>(generateKey(), generateValue());
        }

        if (numElements > 1) {
            if (numCollisions > COLLISIONS.length) {
                throw new IllegalArgumentException("Max collisions is " + COLLISIONS.length);
            }

            int numRepeatedK = numRepeatedKeys;
            int collisionsAndRepeated;
            if (numCollisions > 2 && numRepeatedKeys > 2) {
                collisionsAndRepeated = 2;
                numRepeatedK -= 2;
            } else {
                collisionsAndRepeated = 0;
            }

            for (int i = 0; i < Math.min(numCollisions, numElements); i++) {
                final int pos0 = RandomUtils.nextInt(0, entries.length);
                entries[pos0] = new KeyValue<>(COLLISIONS[i], entries[pos0].getValue());
                if (collisionsAndRepeated > 0) {
                    // This will allow us to combine collisions and repeated keys
                    final int times = RandomUtils.nextInt(1, 12);
                    for (int j = 0; j < times; j++) {
                        final int pos1 = RandomUtils.nextInt(0, entries.length);
                        entries[pos1] = new KeyValue<>(entries[pos0].getKey(), RandomUtils.nextBoolean()? entries[pos1].getValue() : entries[pos0].getValue());
                    }
                    collisionsAndRepeated--;
                }

            }

            for (int i = 0; i < Math.min(numRepeatedK, numElements - 1); i++) {
                final int pos0 = RandomUtils.nextInt(0, entries.length);
                final int times = RandomUtils.nextInt(1, 12);
                for (int j = 0; j < times; j++) {
                    final int pos1 = RandomUtils.nextInt(0, entries.length);
                    entries[pos1] = new KeyValue<>(entries[pos0].getKey(), RandomUtils.nextBoolean()? entries[pos1].getValue() : entries[pos0].getValue());
                }
            }
        }

        return entries;

    }


    public static int[] generateInts(final int numElements, final int minValue, final int maxValue) {
        final int[] accessOrder = new int[numElements];
        for (int i = 0; i < accessOrder.length; i++) {
            accessOrder[i] = RandomUtils.nextInt(minValue, maxValue);
        }
        return accessOrder;
    }


    public static void randomizeArray(final int[] array) {
        for (int i = 0 ; i < array.length; i++) {
            final int i1 = RandomUtils.nextInt(0, array.length);
            final int i2 = RandomUtils.nextInt(0, array.length);
            final int temp = array[i1];
            array[i1] = array[i2];
            array[i2] = temp;
        }
    }


    public static void validateNode(final Node node) {
        validateNode(node, 0);
    }


    private static void validateNode(final Node node, int parentMask) {

        // 1. Ensure no overlap in bitmaps
        if ((node.nodesBitMap & node.entriesBitMap) != 0) {
            throw new IllegalStateException("Invalid node: A position cannot be present in both nodesBitMap and entriesBitMap.");
        }

        // 2. Validate child node and entry counts
        int nodeBitsCount = Long.bitCount(node.nodesBitMap);
        int entryBitsCount = Long.bitCount(node.entriesBitMap);

        if (nodeBitsCount != node.nodes.length) {
            throw new IllegalStateException("Invalid node: Mismatch between nodesBitMap count and Node array length. "
                    + "Expected: " + nodeBitsCount + ", Found: " + node.nodes.length);
        }

        if (entryBitsCount != node.entries.length) {
            throw new IllegalStateException("Invalid node: Mismatch between entriesBitMap count and Entry array length. "
                    + "Expected: " + entryBitsCount + ", Found: " + node.entries.length);
        }

        // 3. Ensure collisions == null for all entries until MAX_LEVEL, and nodes[] is empty at MAX_LEVEL
        if (node.level == Node.MAX_LEVEL) {
            if (node.nodes.length > 0) {
                throw new IllegalStateException("Invalid node: At MAX_LEVEL, nodes array must be empty.");
            }
        } else {
            for (Entry entry : node.entries) {
                if (entry.collisions != null) {
                    throw new IllegalStateException("Invalid node: Collisions must be null for entries until MAX_LEVEL.");
                }
            }
        }

        int levelMask = 0;
        for (int i = 0; i <= node.level; i++) {
            levelMask |= (Node.HASH_MASK << Node.HASH_SHIFTS[i]);
        }

        // 4. Ensure all entry masks match the expected mask at all levels
        int index = 0;
        for (long mask = 1L; mask != 0L; mask <<= 1) {
            int pos = Node.pos(mask, node.entriesBitMap);
            if (pos >= 0) {
                final Entry childEntry = node.entries[pos];
                int maskedChildHash = childEntry.hash & levelMask;
                if (maskedChildHash != (parentMask | (index << Node.HASH_SHIFTS[node.level]))) {
                    throw new IllegalStateException("Invalid entry: Entry hash does not match the mask at level " + node.level);
                }
            }
            index++;
        }


        // 5. Perform recursive validation for child nodes
        index = 0;
        for (long mask = 1L; mask != 0L; mask <<= 1) {
            int pos = Node.pos(mask, node.nodesBitMap);
            if (pos >= 0) {
                final Node childNode = node.nodes[pos];
                if (childNode.level != node.level + 1) {
                    throw new IllegalStateException("Invalid node: Child node level must be parent node level + 1.");
                }
                validateNode(childNode, parentMask | (index << Node.HASH_SHIFTS[node.level]));
            }
            index++;
        }

    }




    public static class ValueRef<V> {
        public V val = null;
        public boolean b = false;
    }



    private TestUtils() {
        super();
    }


}
