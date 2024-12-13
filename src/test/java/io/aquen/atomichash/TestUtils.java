/*
 * =========================================================================
 *
 *   Copyright (c) 2019-2024, Aquen (https://aquen.io)
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
import java.util.Comparator;
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


    static final class HashComparator implements Comparator<KeyValue<String,String>> {

        static final HashComparator INSTANCE = new HashComparator();

        @Override
        public int compare(final KeyValue<String, String> o1, final KeyValue<String, String> o2) {
            // This emulates Entry#compareTo

            final int h1 = AtomicHashStore.hash(o1.getKey());
            final int h2 = AtomicHashStore.hash(o2.getKey());

            if (h1 == h2) {
                return Integer.compare(
                            System.identityHashCode(o1.getKey()),
                            System.identityHashCode(o2.getKey()));
            }

            int level = 0;
            while (true) {
                final int s1 = AtomicHashStore.pos(level, h1);
                final int s2 = AtomicHashStore.pos(level, h2);
                final int comp = Integer.compare(s1, s2);
                if (comp != 0) {
                    return comp;
                }
                level++;
            }

        }

    }




    public static <K,V> void validateStoreWellFormed(final AtomicHashStore<K,V> store) {
        if (store.root == null) {
            return;
        }
        validateNodesWellFormed(0, new int[AtomicHashStore.LEVEL_COUNT], 0, store.root);
    }

    private static <K,V> void validateNodesWellFormed(final int level, final int[] poslevels, final int poslevelsi, final Node<K,V> node) {

        if (node.children == null) {

            if (node.children != null) {
                throw new IllegalStateException("Node has both data and children");
            }

            int n = 0;
            int l = 0;
            while (n < poslevelsi) {
                if (poslevels[n] != AtomicHashStore.pos(l, node.hash)) {
                    throw new IllegalStateException("Node data position does not match");
                }
                n++;
                l++;
            }

            if (node.entry != null) {
                if (node.entries != null) {
                    throw new IllegalStateException("Node data has both single and multiple entry data");
                }
                if (node.hash != node.entry.hash) {
                    throw new IllegalStateException("Node data hash does not correspond with hash in its single entry");
                }
            } else if (node.entries != null) {
                for (int i = 0; i < node.entries.length; i++) {
                    if (node.hash != node.entries[i].hash) {
                        throw new IllegalStateException("Node data hash does not correspond with hash in one of the multivalued entries");
                    }
                }
            } else {
                throw new IllegalStateException("Node data has neither single nor multiple entry data");
            }

        } else if (node.children != null) {

            if (node.children.length != (AtomicHashStore.childrenSizeFor(level))) {
                throw new IllegalStateException("Node children array has the incorrect size");
            }
            boolean allNull = true;
            for (int i = 0; i < node.children.length; i++) {
                if (node.children[i] != null) {
                    allNull = false;
                    poslevels[poslevelsi] = i;
                    validateNodesWellFormed(level + 1, poslevels, poslevelsi + 1, node.children[i]);
                }
            }
            if (allNull) {
                throw new IllegalStateException("Node has a children array with all nulls");
            }

        } else {
            throw new IllegalStateException("Node has neither data nor children");
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
