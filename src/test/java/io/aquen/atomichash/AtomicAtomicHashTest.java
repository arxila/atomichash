/*
 * =========================================================================
 *
 *   Copyright (c) 2019-2024, Aquen OSS (https://aquen.io)
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

public class AtomicAtomicHashTest {



    public static void main(String[] args) {

        AtomicHashStore<String,Object> st = new AtomicHashStore<>();

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));



        st = st.put("Hello", 21);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));


        st = st.put("helloworld", 23);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));


        st = st.put("iFlloworld", 52);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));



        st = st.put("j'lloworld", 31);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.put("lloworld", 99);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.put("IFllo", 423);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.put("n.oworld", 941);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.put("Aloha", 3413);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));


        st = st.put("IFllo", 987);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.put("Hello", 23142);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.put("Ola", 2341233);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.put("Hola", 2341233);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));


        System.out.println();
        System.out.println(st.get("IFllo"));
        System.out.println(st.get("lloworld"));
        System.out.println(st.get("Hello"));
        System.out.println(st.get("Aloha"));

        st = st.remove("Hello");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.remove("IFllo");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.remove("IFllo");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.remove("Aloha");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.remove("j'lloworld");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.remove("iFlloworld");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.remove("Ola");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.remove("helloworld");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.remove("lloworld");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.remove("n.oworld");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.remove("Hola");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.put(null, "Something null!");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        st = st.put(null, "Something other null!");

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

        System.out.println(st.get(null));
        System.out.println(st.get("Hola"));
        System.out.println(st.get("n.oworld"));

        st = st.remove(null);

        System.out.println();
        System.out.println(PrettyPrinter.prettyPrint(st));

    }



}
