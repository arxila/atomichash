package io.aquen.atomichash;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MiscTest {


        public static void main(String[] args) {

            final AtomicHashMap<String,String> map00 = new AtomicHashMap<>();
            System.out.println(PrettyPrinter.printNode(map00.innerRoot()));

            map00.put("Key 0", "Value 0");
            System.out.println(PrettyPrinter.printNode(map00.innerRoot()));

            map00.put("Key 1", "Value 1");
            System.out.println(PrettyPrinter.printNode(map00.innerRoot()));

            map00.put("Key 2", "Value 2");
            System.out.println(PrettyPrinter.printNode(map00.innerRoot()));

            for (int i = 2; i < 20; i++) {
                map00.put("Key " + i, "Value* " + i);
            }
            System.out.println(PrettyPrinter.printNode(map00.innerRoot()));

            for (int i = 10; i < 40; i++) {
                map00.put("Key " + i, "Value* " + i);
            }
            System.out.println(PrettyPrinter.printNode(map00.innerRoot()));

        }


}
