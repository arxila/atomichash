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
