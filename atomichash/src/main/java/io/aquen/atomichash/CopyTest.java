package io.aquen.atomichash;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CopyTest {


        public static void main(String[] args) {

            final String[] keys = new String[1000000];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = "Key " + i;
            }

            Node node = new Node(0, 0,  0L, new Node[0], 0L, new Entry[0]);


            for (int j = 0; j < 1000000; j++) {
                node = node.put(keys[j], "Value zero");
            }

            node = new Node(0, 0,  0L, new Node[0], 0L, new Entry[0]);


            long startTimeCreat0 = System.nanoTime();

            for (int j = 0; j < 1000000; j++) {
                node = node.put(keys[j], "Value zero");
            }

            long endTimeCreat0 = System.nanoTime();
            System.out.println("Execution time adding 1000000 entries to an empty node with put:                       " + (endTimeCreat0 - startTimeCreat0) + " nanoseconds. Size: " + node.size);


            final KeyValue[] keyValues = new KeyValue[50];
            for (int j = 0; j < 50; j++) {
                keyValues[j] = new KeyValue(keys[j], "Value one");
            }
            long startTimeCreat11 = System.nanoTime();
            Node nodePutAll = Util.createNode(keyValues);
            long endTimeCreat11 = System.nanoTime();

            long startTimeCreat12 = System.nanoTime();
            Node nodePutAll2 = node.putAll(nodePutAll);
            long endTimeCreat12 = System.nanoTime();

            System.out.println("Execution time changing the value of 1000000 entries by createNode and then putAll:    " + ((endTimeCreat11 - startTimeCreat11) + (endTimeCreat12 - startTimeCreat12)) + " nanoseconds. Size: " + nodePutAll2.size + " - Time 1: " + (endTimeCreat11 - startTimeCreat11) + " Time 2: " + (endTimeCreat12 - startTimeCreat12));

            long startTimeCreat2 = System.nanoTime();
            Node nodePutAll3 = node;
            for (int j = 0; j < 50; j++) {
                nodePutAll3 = nodePutAll3.put(keyValues[j].key, keyValues[j].value);
            }

            long endTimeCreat2 = System.nanoTime();
            System.out.println("Execution time changing the value of 1000000 entries by executing put on each one:     " + (endTimeCreat2 - startTimeCreat2) + " nanoseconds. Size: " + nodePutAll3.size);






            Node nodeOne = new Node(0, 0,  0L, new Node[0], 0L, new Entry[0]);
            for (int j = 0; j < 500000; j++) {
                nodeOne = nodeOne.put(keys[j], "Value one");
            }

            int counterValue0 = 0;
            int counterValue1 = 0;
            for (int j = 0; j < 1000000; j++) {
                final Object value = node.get(keys[j]);
                if (value != null && value.equals("Value zero")) {
                    counterValue0++;
                } else if (value != null && value.equals("Value one")) {
                    counterValue1++;
                } else {
                    System.out.println("Error");
                }
            }
            System.out.println("CounterValue0: " + counterValue0 + " CounterValue1: " + counterValue1);

            node = node.putAll(nodeOne);

            counterValue0 = 0;
            counterValue1 = 0;
            for (int j = 0; j < 1000000; j++) {
                final Object value = node.get(keys[j]);
                if (value != null && value.equals("Value zero")) {
                    counterValue0++;
                } else if (value != null && value.equals("Value one")) {
                    counterValue1++;
                } else {
                    System.out.println("Error");
                }
            }
            System.out.println("CounterValue0: " + counterValue0 + " CounterValue1: " + counterValue1);


            boolean allFound = true;
            for (int j = 0; j < 1000000; j++) {
                if (!node.contains(keys[j])) {
                    allFound = false;
                }
            }
            System.out.println("All found: " + allFound);
            System.out.println("Size: " + node.size);

            long startTime0 = System.nanoTime();

            int counter0 = 0;
            for (int i = 0; i < 100; i++) {

                for (int j = 0; j < 1000000; j++) {
                    if (node.get(keys[j]) != null) {
                        counter0++;
                    }
                }

            }

            long endTime0 = System.nanoTime();
            System.out.println("Execution time AtomicHash: " + (endTime0 - startTime0) + " nanoseconds");



            final Map<String, String> map = Collections.synchronizedMap(new HashMap<>());


            for (int j = 0; j < 1000000; j++) {
                map.put(keys[j], "Value zero");
            }

            long startTime1 = System.nanoTime();

            int counter1 = 0;
            for (int i = 0; i < 100; i++) {

                for (int j = 0; j < 1000000; j++) {
                    if (map.get(keys[j]) != null) {
                        counter1++;
                    }
                }

            }

            long endTime1 = System.nanoTime();
            System.out.println("Execution time HashMap:    " + (endTime1 - startTime1) + " nanoseconds");


            long startTime2 = System.nanoTime();

            int counter2 = 0;
            for (int i = 0; i < 100; i++) {

                for (int j = 0; j < 1000000; j++) {
                    if (node.get(keys[j]) != null) {
                        counter2++;
                    }
                }

            }

            long endTime2 = System.nanoTime();
            System.out.println("Execution time AtomicHash: " + (endTime2 - startTime2) + " nanoseconds");


            System.out.println(counter0);
            System.out.println(counter1);
            System.out.println(counter2);

        }


}
