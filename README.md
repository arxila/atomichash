AtomicHash
==========

This Java library provides a thread-safe, hash-based implementation of the `java.util.Map` interface and
a similar immutable implementation of a data store that does not implement such an interface:

  * `io.arxila.atomichash.AtomicHashMap`
  * `io.arxila.atomichash.AtomicHashStore`

All operations in AtomicHash are <ins>**thread-safe**, **atomic** and **non-blocking**</ins>, including both reads
and writes, and including multi-element operations such as `putAll(...)` or `getAll(...)`. This means the map can
never be read in a partially-modified state, and its exact _snapshot_ state for an arbitrary number of mappings can
be obtained at any time.

See the _Features_ section below for more detail on these concurrency capabilities.

AtomicHash is open-source and distributed under the **Apache License 2.0**.


Requirements
------------

AtomicHash requires **Java 11**.


Usage
-----

Library dependency: `io.arxila.atomichash:atomichash:{version}`

From Maven:
```xml
<dependency>
  <groupId>io.arxila.atomichash</groupId>
  <artifactId>atomichash</artifactId>
  <version>{version}</version>
</dependency>
```

`AtomicHashMap` can be used just like any other implementation of `java.util.Map`. Its constructor does not
require the specification of an initial size:

```java
import io.arxila.atomichash.AtomicHashMap;

final Map<String, Object> m = new AtomicHashMap<>();
m.put("one key", someValue);
...
final Object theValue = m.get("one key");
```

Instances can also be created using the convenient `of(...)` factory methods that allow up to 10 mappings to
be directly used to create a map:
```java
final Map<String, Object> m = 
        AtomicHashMap.of("one key", oneValue, "two keys");
```

`AtomicHashStore` is a data store that works exactly like a map (stores key-value mappings) but, contrary to how
the `Map` interface works, `AtomicHashStore` it exposes its internal immutability to the code using it, so every
modification returns a new _store_ object:
```java
import io.arxila.atomichash.AtomicHashMap;

final AtomicHashStore<String, Object> s = new AtomicHashStore<>();
s = s.put("one key", someValue); // A new store is returned
...
final Object theValue = s.get("one key");
```

Or also using `of(...)` factory methods:
```java
final AtomicHashStore<String, Object> s = 
        AtomicHashStore.of("one key", oneValue, "two keys", twoValues);
```



Features of `AtomicHashMap` and `AtomicHashStore`
-------------------------------------------------

* Instances are <ins>**thread-safe**</ins> and can be used concurrently by any number of threads.

* All operations are <ins>**non-blocking**</ins>: no threads will ever be blocked waiting at any locks during any 
read or write operations.

* All operations are <ins>**atomic**, including multi-element operations</ins> such as `putAll(...)` and the
equivalent read operation `getAll(...)` (custom operation not present in the `java.util.Map` interface).

A map can never be read in a partially-modified state as could happen in some scenarios with other concurrent
implementations of `Map` if a `putAll(...)` was being executed by one thread and a second one read the map
while this modification is taking place.

Equivalently, `getAll(...)` allows a number of keys to be atomically read, making sure the obtained mappings do
not belong to a mix of different states through which the map is transitioning while the read operation
takes place. Mappings returned by `getAll(...)` are guaranteed to be the result the `put`/`putAll` operations
that were completed before, and will not include any mappings set during the execution of the `getAll(...)`
operation itself.

Reads and writes can therefore be made in a way that only **consistent** states are ever represented and returned
by `AtomicHashMap` and `AtomicHashStore` objects. In fact, a _snapshot_ of the state of an `AtomicHashMap`
instance can be obtained at any time in the form of an `AtomicHashStore` object by calling its `map.store()`
method.

The above also applies to any other operations offered by `AtomicHashMap` and `AtomicHashStore` objects such as
mapping removal (`remove(...)`. `removeAll(...)`), iteration (`keySet()`, `values()`, `entrySet()`), 
`forEach()`, `compute(...)`, `replace(...)`, etc.

**Note**: insertion order is not internally kept, and therefore elements are not guaranteed to be iterated in the
same order they were added.

Implementation
--------------

AtomicHash internally implements an immutable variation of
a [CTRIE (Concurrent Hash-Trie)](https://en.wikipedia.org/wiki/Ctrie). This structure is composed of a tree of
compact (bitmap-managed) arrays that map keys to positions in each of the tree levels depending on the value of
a range of bits of its (modified) hash code.

Key hash codes (32-bit <kbd>int</kbd>s) are divided into five 6-bit segments plus one final 2-bit segment. Each
of these segments is used, at each level of depth, to compute the position assigned to the key in the compact array
(node) living at that level of depth in the ctrie structure. These are compact arrays with a maximum of 64 positions
(bitmaps are <kbd>long</kbd> values), each of which can contain either a data entry or a link to another node
at level + 1. A maximum of 6 levels can exist (0 to 5), and hash collisions only need to be managed at the deepest
level. All structures are kept immutable, so modifications in an array (node) at a specific level mean the creation
of new nodes from that point up to the root of the tree, and the replacement of the old root with the new one
using an atomic compare-and-swap operation.

Note that, given this implementation is based on immutable tree structures, modifications typically need a higher
use of memory than other common implementations of the {@link java.util.Map} interface.
