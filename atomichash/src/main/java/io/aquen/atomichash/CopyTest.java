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
                final KeyValue keyValue0 = new KeyValue(keys[j], "Value zero");
                final DataEntry dataEntry0 = new DataEntry(Hash.of(keyValue0.key), keyValue0);
                node = node.put(dataEntry0, true);
            }

            boolean allFound = true;
            for (int j = 0; j < 1000000; j++) {
                final String key = keys[j];
                final Hash hash = Hash.of(key);
                if (!node.contains(hash, key)) {
                    allFound = false;
                }
            }
            System.out.println(allFound);
            System.out.println(node.size);

            long startTime0 = System.nanoTime();

            int counter0 = 0;
            for (int i = 0; i < 100; i++) {

                for (int j = 0; j < 1000000; j++) {
                    final String key = keys[j];
                    final Hash hash = Hash.of(key);
                    if (node.get(hash, key) != null) {
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

            System.out.println(counter0);
            System.out.println(counter1);

        }


}
