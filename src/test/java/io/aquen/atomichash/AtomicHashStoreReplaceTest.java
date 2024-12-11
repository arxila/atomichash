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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AtomicHashStoreReplaceTest {

    private AtomicHashStore<String,String> store;


    @Before
    public void initStore() {
        this.store = new AtomicHashStore<>();
    }


    @Test
    public void test00() throws Exception {

        AtomicHashStore<String,String> st = this.store;
        AtomicHashStore<String,String> st2 = st;

        TestUtils.ValueRef<String> ref = new TestUtils.ValueRef<>();
        ref.b = true;

        st2 = st.replace("one", "ONE", "[x]ONE");

        Assert.assertTrue(st2.isEmpty());

        st2 = st.replace("one", "ONE", "[x]ONE", b -> ref.b = b);

        Assert.assertTrue(st2.isEmpty());
        Assert.assertFalse(ref.b);

        st = st2.put("one", "ONE");

        st2 = st.replace("one", "ONE", "[x]ONE");

        Assert.assertEquals(1, st2.size());
        Assert.assertEquals("[x]ONE", st2.get("one"));

        st2 = st.replace("one", "ONE", "[x]ONE", b -> ref.b = b);

        Assert.assertEquals(1, st2.size());
        Assert.assertEquals("[x]ONE", st2.get("one"));
        Assert.assertTrue(ref.b);

        st = st2.replace("one", "ONE", "[y]ONE");

        Assert.assertEquals(1, st.size());
        Assert.assertEquals("[x]ONE", st.get("one"));

        st = st2.replace("one", "ONE", "[y]ONE", b -> ref.b = b);

        Assert.assertEquals(1, st.size());
        Assert.assertEquals("[x]ONE", st.get("one"));
        Assert.assertFalse(ref.b);

    }


    @Test
    public void test01() throws Exception {

        AtomicHashStore<String,String> st = this.store;
        AtomicHashStore<String,String> st2 = st;

        TestUtils.ValueRef<String> ref = new TestUtils.ValueRef<>();
        ref.val = "WHATEVER";

        st2 = st.replace("one", "[x]ONE");

        Assert.assertTrue(st2.isEmpty());

        st2 = st.replace("one", "[x]ONE", v -> ref.val = v);

        Assert.assertTrue(st2.isEmpty());
        Assert.assertNull(ref.val);

        st = st2.put("one", "ONE");

        st2 = st.replace("one", "[x]ONE");

        Assert.assertEquals(1, st2.size());
        Assert.assertEquals("[x]ONE", st2.get("one"));

        st2 = st.replace("one", "[x]ONE", v -> ref.val = v);

        Assert.assertEquals(1, st2.size());
        Assert.assertEquals("[x]ONE", st2.get("one"));
        Assert.assertEquals("ONE", ref.val);

        st = st2.replace("one", "[y]ONE");

        Assert.assertEquals(1, st.size());
        Assert.assertEquals("[y]ONE", st.get("one"));

        st = st2.replace("one", "[y]ONE", v -> ref.val = v);

        Assert.assertEquals(1, st.size());
        Assert.assertEquals("[y]ONE", st.get("one"));
        Assert.assertEquals("[x]ONE", ref.val);

    }

}
