# Java Collections & Streams — Examples, Patterns, Big-O, Concurrency & Interview Qs

*Copy-paste this entire file into `README.md` for a GitHub-ready reference with runnable Java snippets and explanations.*

---

## Table of Contents

1. Overview & step-by-step approach
2. Basic Stream operations: `map`, `flatMap`, `filter`
3. Nested lists & flattening
4. Convert `List` → `Map`, and `Map` values → `List`
5. Grouping, partitioning, ordering, and complex collectors
6. More complex collection patterns (multilevel grouping, top-N per group)
7. Fail-fast vs fail-safe iterators and examples (`ConcurrentModificationException`)
8. Execution / exception handling in stream pipelines (handling checked exceptions)
9. Concurrency: `ConcurrentHashMap`, how it's different, locking & synchronization examples
10. When to use which collection (table) + Big-O for common operations
11. Common interview questions on collections (with short answers)
12. Appendix: imports & helper classes used in examples

---

## 1. Overview — how to read these examples (step-by-step)

1. Read the short concept description.
2. See the Java snippet that demonstrates the concept.
3. Each snippet is small and focused — drop it into a `main` method or unit test.
4. For multi-step examples, the steps are commented inline.

---

## 2. Basic Stream operations: `map`, `flatMap`, `filter`

```java
import java.util.*;
import java.util.stream.*;

public class StreamBasics {
    public static void main(String[] args) {
        // Sample users
        record User(String name, int age, List<String> roles) {}
        List<User> users = List.of(
            new User("Alice", 30, List.of("ADMIN", "USER")),
            new User("Bob", 22, List.of("USER")),
            new User("Charlie", 28, List.of("MANAGER","USER"))
        );

        // map: transform user -> name
        List<String> names = users.stream()
                                  .map(User::name)
                                  .collect(Collectors.toList());
        System.out.println("Names: " + names); // [Alice, Bob, Charlie]

        // filter: only age >= 25
        List<User> adults = users.stream()
                                 .filter(u -> u.age() >= 25)
                                 .collect(Collectors.toList());
        System.out.println("Adults: " + adults);

        // flatMap: flatten roles from multiple users into a single stream of roles
        List<String> allRoles = users.stream()
                                     .flatMap(u -> u.roles().stream())
                                     .distinct()
                                     .collect(Collectors.toList());
        System.out.println("Roles: " + allRoles); // [ADMIN, USER, MANAGER]
    }
}
```

---

## 3. Nested list & flattening (nested `List<List<T>>`)

Example: flatten `List<List<Integer>>` → `List<Integer>`.

```java
import java.util.*;
import java.util.stream.*;

public class FlattenExample {
    public static void main(String[] args) {
        List<List<Integer>> nested = List.of(
            List.of(1, 2, 3),
            List.of(4, 5),
            List.of(6)
        );

        List<Integer> flat = nested.stream()
                                   .flatMap(Collection::stream)
                                   .collect(Collectors.toList());
        System.out.println(flat); // [1,2,3,4,5,6]
    }
}
```

---

## 4. Convert `List` → `Map`, and `Map` values → `List`

### a) Simple `List<T>` → `Map<key, value>` using `Collectors.toMap`

```java
import java.util.*;
import java.util.stream.*;

public class ListToMap {
    public static void main(String[] args) {
        record Person(String id, String name) {}
        List<Person> people = List.of(new Person("p1","Alice"), new Person("p2","Bob"));

        // toMap(keyMapper, valueMapper)
        Map<String, String> idToName = people.stream()
            .collect(Collectors.toMap(Person::id, Person::name));
        System.out.println(idToName); // {p1=Alice, p2=Bob}
    }
}
```

**Note:** If keys may collide, use the merge function overload:

```java
Collectors.toMap(Person::id, Person::name, (existing, replacement) -> existing)
```

### b) Convert `Map<K, V>` values → `List<V>`

```java
Map<String, String> map = Map.of("a","one", "b","two");
List<String> values = new ArrayList<>(map.values()); // simple
// or with streams
List<String> vals = map.entrySet().stream()
                       .map(Map.Entry::getValue)
                       .collect(Collectors.toList());
```

### c) Map values to list of DTOs (example)

```java
Map<String, Integer> counts = Map.of("x", 2, "y", 3);
List<String> expanded = counts.entrySet().stream()
    .flatMap(e -> Collections.nCopies(e.getValue(), e.getKey()).stream())
    .collect(Collectors.toList());
// expanded: ["x", "x", "y", "y", "y"]
```

---

## 5. Grouping, partitioning, ordering, collectors

### a) `groupingBy` (single-level)

Group users by age bracket:

```java
Map<String, List<User>> byBracket = users.stream()
    .collect(Collectors.groupingBy(u -> u.age() >= 25 ? "25+" : "<25"));
```

### b) `groupingBy` with downstream collector (counting or mapping)

```java
Map<String, Long> countByBracket = users.stream()
    .collect(Collectors.groupingBy(
        u -> u.age() >= 25 ? "25+" : "<25",
        Collectors.counting()
    ));
```

### c) `partitioningBy` (boolean split)

```java
Map<Boolean, List<User>> partitioned = users.stream()
    .collect(Collectors.partitioningBy(u -> u.age() >= 25));
```

### d) Ordering (sorted stream)

Sort users by name then collect:

```java
List<User> sortedByName = users.stream()
    .sorted(Comparator.comparing(User::name))
    .collect(Collectors.toList());
```

### e) `toMap` with sorted keys (use `TreeMap`)

```java
Map<String, Integer> map = users.stream()
    .collect(Collectors.toMap(User::name, User::age, (a,b) -> a, TreeMap::new));
```

---

## 6. More complex collection examples

### Example 1: Multi-level grouping (category → priceRange → list)

```java
record Product(String category, String name, double price) {}
List<Product> products = List.of(
    new Product("Books","Refactoring", 40.0),
    new Product("Books","Clean Code", 35.0),
    new Product("Electronics","Headphones", 120.0),
    new Product("Electronics","Mouse", 25.0)
);

Map<String, Map<String, List<Product>>> multi =
    products.stream().collect(Collectors.groupingBy(
        p -> p.category(),
        Collectors.groupingBy(p -> {
            if (p.price() < 50) return "CHEAP";
            else if (p.price() < 100) return "MEDIUM";
            else return "EXPENSIVE";
        })
    ));
```

### Example 2: Top N per group (e.g., top 1 priced product in each category)

```java
Map<String, Optional<Product>> topPerCategory = products.stream().collect(
    Collectors.groupingBy(
        Product::category,
        Collectors.maxBy(Comparator.comparing(Product::price))
    )
);
```

### Example 3: Transform grouping values to lists of names

```java
Map<String, List<String>> namesByCategory = products.stream()
    .collect(Collectors.groupingBy(Product::category,
        Collectors.mapping(Product::name, Collectors.toList())));
```

---

## 7. Fail-fast vs Fail-safe iterators

**Fail-fast**: most `java.util` collections (e.g., `ArrayList`, `HashMap`) use fail-fast iterators that throw `ConcurrentModificationException` if modified structurally while iterating.

**Example (fail-fast):**

```java
List<String> list = new ArrayList<>(List.of("a","b","c"));
for (String s : list) {
    if ("b".equals(s)) {
        list.remove(s); // throws ConcurrentModificationException
    }
}
```

**How to avoid:**

1. Use `Iterator.remove()` while iterating:

```java
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("b")) it.remove(); // safe
}
```

2. Use `CopyOnWriteArrayList` (fail-safe snapshot iterator, safe for concurrent reads/writes but expensive on writes):

```java
CopyOnWriteArrayList<String> cow = new CopyOnWriteArrayList<>(List.of("a","b","c"));
for (String s : cow) {
    if (s.equals("b")) cow.remove(s); // safe, no CME
}
```

3. Use concurrent collections like `ConcurrentHashMap` (iterators weakly consistent — reflect some state but not guaranteed to include concurrent updates).

---

## 8. Execution / exception handling in stream pipelines

Streams don't have built-in checked-exception support. Wrap checked exceptions or recover gracefully.

### Pattern: wrapper function to catch exceptions and wrap results in `Optional` or custom `Result`

```java
import java.util.function.Function;

public static <T,R> Function<T,R> wrapUnchecked(CheckedFunction<T,R> f) {
    return t -> {
        try { return f.apply(t); }
        catch(Exception e) { throw new RuntimeException(e); }
    };
}

@FunctionalInterface
public interface CheckedFunction<T,R> {
    R apply(T t) throws Exception;
}
```

### Example usage

```java
List<String> inputs = List.of("1","x","3");
List<Integer> ints = inputs.stream()
    .map(s -> {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1; // fallback or filter these later
        }
    })
    .filter(i -> i >= 0)
    .collect(Collectors.toList());
```

### Alternative: collect exceptions

You can map each element to `Either<Exception, Result>` or `Optional<Result>` to collect failures separately.

---

## 9. Concurrency: `ConcurrentHashMap`, differences & thread safety

### What is `ConcurrentHashMap`?

* A thread-safe Map implementation optimized for concurrent access.
* In Java 8+, internal implementation uses CAS and `synchronized` locks on individual bins, not a single global lock (no classic segment array as in older JDKs).
* Supports high concurrency for reads without locks and effectively fine-grained locking for writes.

### Differences vs `Collections.synchronizedMap(...)` and `Hashtable`

* `synchronizedMap` locks the entire map for each operation — coarse-grained lock (lower concurrency).
* `Hashtable` is legacy and also synchronizes on the whole table.
* `ConcurrentHashMap` provides better concurrency and non-blocking reads; iterators are weakly consistent (no CME, but might not reflect latest writes immediately).

### Example usage

```java
import java.util.concurrent.*;

ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<>();
chm.put("a", 1);

// atomic put-if-absent
chm.putIfAbsent("b", 2);

// compute atomically
chm.compute("a", (k, v) -> v == null ? 1 : v + 1);

// merge values
chm.merge("c", 1, Integer::sum);
```

### When to choose `ConcurrentHashMap`

* Many concurrent reads and writes.
* Want thread-safe atomic operations (`putIfAbsent`, `compute`, `merge`).
* Iteration while mutation — no `ConcurrentModificationException`.

### Locking / synchronizing collection objects

1. **Whole-collection lock (coarse-grained)**

```java
List<String> synced = Collections.synchronizedList(new ArrayList<>());
synchronized (synced) {
    Iterator<String> it = synced.iterator(); // must synchronize during iteration
    while (it.hasNext()) {
        System.out.println(it.next());
    }
}
```

2. **Synchronized methods (object-level lock)**

```java
public class SafeListWrapper {
    private final List<String> list = new ArrayList<>();

    public synchronized void add(String s) { list.add(s); }
    public synchronized String get(int i) { return list.get(i); }
}
```

3. **`ReentrantLock` (explicit lock for fine control)**

```java
import java.util.concurrent.locks.*;

ReentrantLock lock = new ReentrantLock();
try {
    lock.lock();
    // safe access
} finally {
    lock.unlock();
}
```

4. **Use concurrent data structures to avoid external locks** (e.g., `ConcurrentHashMap`, `ConcurrentLinkedQueue`, `CopyOnWriteArrayList`) — recommended.

---

## 10. When to use which collection + Big-O (average / expected)

> **Notation**: n = number of elements. These are common average/expected time complexities (Java typical implementations).

|             Collection |           get / contains          |           add           |          remove          |     iteration     |      memory      | Use case                                                 |
| ---------------------: | :-------------------------------: | :---------------------: | :----------------------: | :---------------: | :--------------: | :------------------------------------------------------- |
|            `ArrayList` | O(1) (by index) / O(n) (contains) |      amortized O(1)     |           O(n)           |        O(n)       |      compact     | Random access, append-heavy                              |
|           `LinkedList` |                O(n)               | O(1) (addFirst/addLast) |           O(n)           |        O(n)       |  higher (nodes)  | Frequent insert/remove at ends                           |
|  `HashSet` / `HashMap` |           O(1) expected           |      O(1) expected      |       O(1) expected      |        O(n)       |     moderate     | Unordered unique elements, key→value                     |
|  `TreeSet` / `TreeMap` |              O(log n)             |         O(log n)        |         O(log n)         |   O(n) (sorted)   |     moderate     | Sorted keys, range queries                               |
|    `ConcurrentHashMap` |           O(1) expected           |      O(1) expected      |       O(1) expected      | weakly consistent |     moderate     | High concurrency map                                     |
| `CopyOnWriteArrayList` |      O(1) get, O(n) contains      |   O(n) (copy on write)  |           O(n)           |        O(n)       | high when writes | Many reads, few writes; snapshot iteration               |
| `Vector` / `Hashtable` |                O(1)               |   O(1) (synchronized)   |           O(1)           |        O(n)       |      legacy      | Thread-safe but coarse-grained lock (rarely recommended) |
|        `PriorityQueue` |           O(n) contains           |         O(log n)        | O(log n) for remove head |        O(n)       |       heap       | Priority processing (min/max)                            |

**Notes**

* HashMap average case O(1) but worst-case O(n) if many collisions (rare with good hashing).
* `ArrayList` add at end amortized O(1) due to occasional resize.
* `ConcurrentHashMap` in JDK8 is highly optimized for concurrent read/write (no global lock).

---

## 11. Common interview questions on collections (with model answers / hints)

1. **Q:** Difference between `ArrayList` and `LinkedList`?
   **A:** `ArrayList` backed by array → O(1) random access, amortized append; `LinkedList` nodes → O(1) insert/delete at ends, O(n) access by index.

2. **Q:** How does `HashMap` work (basics)?
   **A:** Hash key → bucket index → linked list / tree (if many collisions) of entries; equals/hashCode determine key equality.

3. **Q:** Why override `equals()` and `hashCode()`?
   **A:** `HashMap`/`HashSet` rely on `hashCode()` for bucket selection and `equals()` for equality checks. They must be consistent.

4. **Q:** What's the difference between `ConcurrentHashMap` and `Collections.synchronizedMap()`?
   **A:** `synchronizedMap` uses one lock for whole map; `ConcurrentHashMap` uses finer-grained mechanisms and non-blocking reads for better concurrency.

5. **Q:** What is fail-fast? How to avoid `ConcurrentModificationException`?
   **A:** Many iterators detect structural modification and throw `CME`. Avoid by using iterator.remove(), using concurrent collections, or working on a snapshot.

6. **Q:** When to use `CopyOnWriteArrayList`?
   **A:** When reads vastly outnumber writes and you need snapshot-style iteration without locking.

7. **Q:** How to make a `HashMap` thread-safe?
   **A:** Use `ConcurrentHashMap`, or wrap with `Collections.synchronizedMap()` and externally synchronize during iterations.

8. **Q:** Explain `WeakHashMap`, `IdentityHashMap`, `LinkedHashMap`.
   **A:** `WeakHashMap` entries may be GC'ed; `IdentityHashMap` uses `==` for keys; `LinkedHashMap` maintains insertion/access order.

9. **Q:** How to avoid memory leaks with listeners and collections?
   **A:** Use weak references, clean-up on remove, or use `WeakReference`/`WeakHashMap` when lifecycle must not be prolonged.

10. **Q:** What problems can arise from using mutable objects as `HashMap` keys?
    **A:** If key's state (and thus hashCode/equals) changes while in map, the entry may become unreachable and effectively lost.

---

## 12. Appendix — imports & helper code (single place to copy)

Put these imports at the top of examples where needed:

```java
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.function.*;
import java.util.stream.*;
```

### Helper: sample `User` record used in examples

```java
public record User(String name, int age, List<String> roles) {}
```

---

## Extra: Example — Putting it all together (complex pipeline)

```java
// 1. Have a list of employees (name, salary, dept)
record Employee(String name, String dept, double salary) {}
List<Employee> employees = List.of(
    new Employee("Alice","Engineering",120000),
    new Employee("Bob","Engineering",90000),
    new Employee("Carol","HR",70000),
    new Employee("Dan","HR",80000),
    new Employee("Eve","Engineering",125000)
);

// 2. Find top-2 earners by department, returning Map<dept, List<Employee>>
Map<String, List<Employee>> top2ByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::dept,
        Collectors.collectingAndThen(
            Collectors.toList(),
            list -> list.stream()
                        .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                        .limit(2)
                        .collect(Collectors.toList())
        )
    ));
```

---

## Final tips & best practices

* Prefer **streams** for readable pipelines on collections but avoid overcomplicating when a simple `for` loop is clearer.
* Use **concurrent collections** instead of manual synchronization when possible.
* Always consider **mutability** of objects used as keys in maps or elements in sets.
* For high-throughput concurrent systems, measure and load-test (real workloads change tradeoffs).

### calaude###

# Java Collections Comprehensive Guide

## Table of Contents
1. [Stream Operations Examples](#stream-operations-examples)
2. [Collection Conversion Examples](#collection-conversion-examples)
3. [Complex Collection Operations](#complex-collection-operations)
4. [Fail-Fast Behavior](#fail-fast-behavior)
5. [Exception Handling in Collections](#exception-handling-in-collections)
6. [Thread Safety and Synchronization](#thread-safety-and-synchronization)
7. [Collection Selection Guide](#collection-selection-guide)
8. [Big O Notation Metrics](#big-o-notation-metrics)
9. [Interview Questions](#interview-questions)

---

## Stream Operations Examples

### 1. Map Operation
Transforms each element in the collection.

```java
import java.util.*;
import java.util.stream.*;

public class MapExample {
    public static void main(String[] args) {
        // Example 1: Convert strings to uppercase
        List<String> names = Arrays.asList("john", "jane", "bob", "alice");
        List<String> upperCaseNames = names.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
        System.out.println(upperCaseNames); // [JOHN, JANE, BOB, ALICE]
        
        // Example 2: Extract specific field from objects
        List<Person> people = Arrays.asList(
            new Person("John", 25),
            new Person("Jane", 30),
            new Person("Bob", 35)
        );
        List<String> personNames = people.stream()
            .map(Person::getName)
            .collect(Collectors.toList());
        System.out.println(personNames); // [John, Jane, Bob]
        
        // Example 3: Mathematical transformation
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> squares = numbers.stream()
            .map(n -> n * n)
            .collect(Collectors.toList());
        System.out.println(squares); // [1, 4, 9, 16, 25]
    }
}

class Person {
    private String name;
    private int age;
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() { return name; }
    public int getAge() { return age; }
}
```

### 2. FlatMap Operation
Flattens nested structures into a single stream.

```java
import java.util.*;
import java.util.stream.*;

public class FlatMapExample {
    public static void main(String[] args) {
        // Example 1: Flatten list of lists
        List<List<Integer>> nestedNumbers = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5),
            Arrays.asList(6, 7, 8, 9)
        );
        List<Integer> flatList = nestedNumbers.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        System.out.println(flatList); // [1, 2, 3, 4, 5, 6, 7, 8, 9]
        
        // Example 2: Split sentences into words
        List<String> sentences = Arrays.asList(
            "Hello World",
            "Java Collections",
            "Stream API"
        );
        List<String> words = sentences.stream()
            .flatMap(sentence -> Arrays.stream(sentence.split(" ")))
            .collect(Collectors.toList());
        System.out.println(words); // [Hello, World, Java, Collections, Stream, API]
        
        // Example 3: Complex object flattening
        List<Department> departments = Arrays.asList(
            new Department("IT", Arrays.asList(
                new Employee("John", 50000),
                new Employee("Jane", 60000)
            )),
            new Department("HR", Arrays.asList(
                new Employee("Bob", 45000),
                new Employee("Alice", 55000)
            ))
        );
        
        List<Employee> allEmployees = departments.stream()
            .flatMap(dept -> dept.getEmployees().stream())
            .collect(Collectors.toList());
        
        double avgSalary = departments.stream()
            .flatMap(dept -> dept.getEmployees().stream())
            .mapToDouble(Employee::getSalary)
            .average()
            .orElse(0.0);
        System.out.println("Average Salary: " + avgSalary);
    }
}

class Department {
    private String name;
    private List<Employee> employees;
    
    public Department(String name, List<Employee> employees) {
        this.name = name;
        this.employees = employees;
    }
    
    public List<Employee> getEmployees() { return employees; }
}

class Employee {
    private String name;
    private double salary;
    
    public Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }
    
    public double getSalary() { return salary; }
}
```

### 3. Filter Operation
Filters elements based on a condition.

```java
import java.util.*;
import java.util.stream.*;

public class FilterExample {
    public static void main(String[] args) {
        // Example 1: Filter numbers
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> evenNumbers = numbers.stream()
            .filter(n -> n % 2 == 0)
            .collect(Collectors.toList());
        System.out.println(evenNumbers); // [2, 4, 6, 8, 10]
        
        // Example 2: Filter with multiple conditions
        List<Product> products = Arrays.asList(
            new Product("Laptop", 1000, "Electronics"),
            new Product("Book", 20, "Education"),
            new Product("Phone", 800, "Electronics"),
            new Product("Pen", 5, "Stationery")
        );
        
        List<Product> expensiveElectronics = products.stream()
            .filter(p -> p.getPrice() > 500)
            .filter(p -> "Electronics".equals(p.getCategory()))
            .collect(Collectors.toList());
        
        // Example 3: Chaining operations
        List<String> result = products.stream()
            .filter(p -> p.getPrice() < 100)
            .map(Product::getName)
            .filter(name -> name.length() > 3)
            .collect(Collectors.toList());
        System.out.println(result); // [Book]
    }
}

class Product {
    private String name;
    private double price;
    private String category;
    
    public Product(String name, double price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
}
```

### 4. Nested List Operations
Working with nested data structures.

```java
import java.util.*;
import java.util.stream.*;

public class NestedListExample {
    public static void main(String[] args) {
        // Example 1: 2D List operations
        List<List<Integer>> matrix = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5, 6),
            Arrays.asList(7, 8, 9)
        );
        
        // Transpose matrix
        List<List<Integer>> transposed = IntStream.range(0, 3)
            .mapToObj(i -> matrix.stream()
                .map(row -> row.get(i))
                .collect(Collectors.toList()))
            .collect(Collectors.toList());
        
        // Example 2: Deep copy of nested list
        List<List<String>> original = Arrays.asList(
            new ArrayList<>(Arrays.asList("a", "b")),
            new ArrayList<>(Arrays.asList("c", "d"))
        );
        
        List<List<String>> deepCopy = original.stream()
            .map(ArrayList::new)
            .collect(Collectors.toList());
        
        // Example 3: Find element in nested structure
        List<List<Integer>> nested = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5, 6),
            Arrays.asList(7, 8, 9)
        );
        
        boolean contains5 = nested.stream()
            .anyMatch(list -> list.contains(5));
        System.out.println("Contains 5: " + contains5); // true
        
        // Sum all elements in nested list
        int sum = nested.stream()
            .flatMap(Collection::stream)
            .mapToInt(Integer::intValue)
            .sum();
        System.out.println("Sum: " + sum); // 45
    }
}
```

---

## Collection Conversion Examples

### 5. Convert List to Map

```java
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class ListToMapExample {
    public static void main(String[] args) {
        List<Student> students = Arrays.asList(
            new Student(1, "John", 85),
            new Student(2, "Jane", 92),
            new Student(3, "Bob", 78),
            new Student(4, "Alice", 88)
        );
        
        // Example 1: Simple key-value mapping
        Map<Integer, String> idToName = students.stream()
            .collect(Collectors.toMap(
                Student::getId,
                Student::getName
            ));
        System.out.println(idToName); // {1=John, 2=Jane, 3=Bob, 4=Alice}
        
        // Example 2: Object as value
        Map<Integer, Student> idToStudent = students.stream()
            .collect(Collectors.toMap(
                Student::getId,
                Function.identity()
            ));
        
        // Example 3: Handle duplicates
        List<Student> studentsWithDuplicates = Arrays.asList(
            new Student(1, "John", 85),
            new Student(2, "Jane", 92),
            new Student(1, "Johnny", 88) // Duplicate ID
        );
        
        Map<Integer, String> handleDuplicates = studentsWithDuplicates.stream()
            .collect(Collectors.toMap(
                Student::getId,
                Student::getName,
                (existing, replacement) -> existing + ", " + replacement
            ));
        System.out.println(handleDuplicates); // {1=John, Johnny, 2=Jane}
        
        // Example 4: Custom map implementation
        LinkedHashMap<String, Integer> nameToScore = students.stream()
            .collect(Collectors.toMap(
                Student::getName,
                Student::getScore,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
}

class Student {
    private int id;
    private String name;
    private int score;
    
    public Student(int id, String name, int score) {
        this.id = id;
        this.name = name;
        this.score = score;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public int getScore() { return score; }
}
```

### 6. Map Values to List

```java
import java.util.*;
import java.util.stream.*;

public class MapToListExample {
    public static void main(String[] args) {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("John", 85);
        scores.put("Jane", 92);
        scores.put("Bob", 78);
        scores.put("Alice", 88);
        
        // Example 1: Extract all values
        List<Integer> allScores = new ArrayList<>(scores.values());
        System.out.println(allScores); // [85, 92, 78, 88]
        
        // Example 2: Extract all keys
        List<String> names = new ArrayList<>(scores.keySet());
        System.out.println(names); // [John, Jane, Bob, Alice]
        
        // Example 3: Convert entries to custom objects
        List<StudentScore> studentScores = scores.entrySet().stream()
            .map(entry -> new StudentScore(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
        
        // Example 4: Filter and convert
        List<String> highScorers = scores.entrySet().stream()
            .filter(entry -> entry.getValue() > 85)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        System.out.println(highScorers); // [Jane, Alice]
        
        // Example 5: Sort by value and get keys
        List<String> sortedByScore = scores.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        System.out.println(sortedByScore); // [Bob, John, Alice, Jane]
    }
}

class StudentScore {
    private String name;
    private int score;
    
    public StudentScore(String name, int score) {
        this.name = name;
        this.score = score;
    }
}
```

### 7. Grouping By

```java
import java.util.*;
import java.util.stream.*;

public class GroupingByExample {
    public static void main(String[] args) {
        List<Transaction> transactions = Arrays.asList(
            new Transaction("Food", 50, "2024-01-15"),
            new Transaction("Transport", 30, "2024-01-16"),
            new Transaction("Food", 80, "2024-01-17"),
            new Transaction("Entertainment", 100, "2024-01-18"),
            new Transaction("Transport", 25, "2024-01-19"),
            new Transaction("Food", 60, "2024-01-20")
        );
        
        // Example 1: Simple grouping
        Map<String, List<Transaction>> byCategory = transactions.stream()
            .collect(Collectors.groupingBy(Transaction::getCategory));
        
        // Example 2: Group and count
        Map<String, Long> categoryCount = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.counting()
            ));
        System.out.println(categoryCount); // {Food=3, Transport=2, Entertainment=1}
        
        // Example 3: Group and sum
        Map<String, Double> categoryTotal = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.summingDouble(Transaction::getAmount)
            ));
        System.out.println(categoryTotal); // {Food=190.0, Transport=55.0, Entertainment=100.0}
        
        // Example 4: Multi-level grouping
        Map<String, Map<String, List<Transaction>>> multiLevel = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.groupingBy(Transaction::getDate)
            ));
        
        // Example 5: Group with custom collector
        Map<String, String> categorySummary = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.mapping(
                    t -> String.valueOf(t.getAmount()),
                    Collectors.joining(", ")
                )
            ));
        System.out.println(categorySummary); 
        // {Food=50.0, 80.0, 60.0, Transport=30.0, 25.0, Entertainment=100.0}
    }
}

class Transaction {
    private String category;
    private double amount;
    private String date;
    
    public Transaction(String category, double amount, String date) {
        this.category = category;
        this.amount = amount;
        this.date = date;
    }
    
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
}
```

### 8. Ordering/Sorting

```java
import java.util.*;
import java.util.stream.*;

public class OrderingExample {
    public static void main(String[] args) {
        List<Person> people = Arrays.asList(
            new Person("John", 25, 50000),
            new Person("Jane", 30, 60000),
            new Person("Bob", 25, 55000),
            new Person("Alice", 28, 52000)
        );
        
        // Example 1: Sort by single field
        List<Person> sortedByAge = people.stream()
            .sorted(Comparator.comparing(Person::getAge))
            .collect(Collectors.toList());
        
        // Example 2: Sort by multiple fields
        List<Person> sortedByAgeAndSalary = people.stream()
            .sorted(Comparator.comparing(Person::getAge)
                .thenComparing(Person::getSalary))
            .collect(Collectors.toList());
        
        // Example 3: Reverse order
        List<Person> sortedBySalaryDesc = people.stream()
            .sorted(Comparator.comparing(Person::getSalary).reversed())
            .collect(Collectors.toList());
        
        // Example 4: Custom comparator
        Comparator<Person> customComparator = (p1, p2) -> {
            int ageCompare = Integer.compare(p1.getAge(), p2.getAge());
            if (ageCompare != 0) return ageCompare;
            return Double.compare(p2.getSalary(), p1.getSalary()); // Reverse for salary
        };
        
        List<Person> customSorted = people.stream()
            .sorted(customComparator)
            .collect(Collectors.toList());
        
        // Example 5: Null-safe sorting
        List<String> namesWithNulls = Arrays.asList("John", null, "Alice", "Bob", null);
        List<String> sortedNames = namesWithNulls.stream()
            .sorted(Comparator.nullsLast(Comparator.naturalOrder()))
            .collect(Collectors.toList());
        System.out.println(sortedNames); // [Alice, Bob, John, null, null]
    }
    
    static class Person {
        private String name;
        private int age;
        private double salary;
        
        public Person(String name, int age, double salary) {
            this.name = name;
            this.age = age;
            this.salary = salary;
        }
        
        public String getName() { return name; }
        public int getAge() { return age; }
        public double getSalary() { return salary; }
    }
}
```

### 9. Partitioning

```java
import java.util.*;
import java.util.stream.*;

public class PartitioningExample {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // Example 1: Simple partitioning
        Map<Boolean, List<Integer>> evenOddPartition = numbers.stream()
            .collect(Collectors.partitioningBy(n -> n % 2 == 0));
        System.out.println("Even: " + evenOddPartition.get(true));  // [2, 4, 6, 8, 10]
        System.out.println("Odd: " + evenOddPartition.get(false));  // [1, 3, 5, 7, 9]
        
        // Example 2: Partition with downstream collector
        List<Student> students = Arrays.asList(
            new Student(1, "John", 85),
            new Student(2, "Jane", 92),
            new Student(3, "Bob", 78),
            new Student(4, "Alice", 88),
            new Student(5, "Charlie", 95)
        );
        
        Map<Boolean, Double> avgScoreByPass = students.stream()
            .collect(Collectors.partitioningBy(
                s -> s.getScore() >= 85,
                Collectors.averagingDouble(Student::getScore)
            ));
        System.out.println("Avg score (passed): " + avgScoreByPass.get(true));   // 90.0
        System.out.println("Avg score (failed): " + avgScoreByPass.get(false));  // 78.0
        
        // Example 3: Partition and count
        Map<Boolean, Long> countByPass = students.stream()
            .collect(Collectors.partitioningBy(
                s -> s.getScore() >= 85,
                Collectors.counting()
            ));
        System.out.println("Passed: " + countByPass.get(true));   // 4
        System.out.println("Failed: " + countByPass.get(false));  // 1
    }
}
```

---

## Complex Collection Operations

### 10. Advanced Collection Examples

```java
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;

public class ComplexCollectionExample {
    public static void main(String[] args) {
        // Example 1: Sliding window
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        List<List<Integer>> windows = slidingWindow(numbers, 3);
        System.out.println(windows); // [[1,2,3], [2,3,4], [3,4,5], ...]
        
        // Example 2: Frequency map
        List<String> words = Arrays.asList("apple", "banana", "apple", "orange", "banana", "apple");
        Map<String, Long> frequency = words.stream()
            .collect(Collectors.groupingBy(
                Function.identity(),
                Collectors.counting()
            ));
        System.out.println(frequency); // {apple=3, banana=2, orange=1}
        
        // Example 3: Top K elements
        List<Integer> nums = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        List<Integer> top3 = nums.stream()
            .sorted(Comparator.reverseOrder())
            .distinct()
            .limit(3)
            .collect(Collectors.toList());
        System.out.println("Top 3: " + top3); // [9, 6, 5]
        
        // Example 4: Intersection of multiple sets
        Set<Integer> set1 = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        Set<Integer> set2 = new HashSet<>(Arrays.asList(3, 4, 5, 6, 7));
        Set<Integer> set3 = new HashSet<>(Arrays.asList(4, 5, 6, 7, 8));
        
        Set<Integer> intersection = set1.stream()
            .filter(set2::contains)
            .filter(set3::contains)
            .collect(Collectors.toSet());
        System.out.println("Intersection: " + intersection); // [4, 5]
        
        // Example 5: Merge maps with conflict resolution
        Map<String, Integer> map1 = new HashMap<>();
        map1.put("a", 1);
        map1.put("b", 2);
        
        Map<String, Integer> map2 = new HashMap<>();
        map2.put("b", 3);
        map2.put("c", 4);
        
        Map<String, Integer> merged = Stream.of(map1, map2)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                Integer::sum
            ));
        System.out.println("Merged: " + merged); // {a=1, b=5, c=4}
    }
    
    private static <T> List<List<T>> slidingWindow(List<T> list, int windowSize) {
        return IntStream.range(0, list.size() - windowSize + 1)
            .mapToObj(i -> list.subList(i, i + windowSize))
            .collect(Collectors.toList());
    }
}
```

---

## Fail-Fast Behavior

### 11. Fail-Fast Iterator Examples

```java
import java.util.*;

public class FailFastExample {
    public static void main(String[] args) {
        // Example 1: ConcurrentModificationException
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        try {
            for (String item : list) {
                if (item.equals("B")) {
                    list.remove(item); // This will throw ConcurrentModificationException
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("Fail-fast: Cannot modify list during iteration");
        }
        
        // Example 2: Correct way using Iterator
        List<String> list2 = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        Iterator<String> iterator = list2.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            if (item.equals("B")) {
                iterator.remove(); // Safe removal
            }
        }
        System.out.println(list2); // [A, C, D]
        
        // Example 3: Using removeIf (Java 8+)
        List<String> list3 = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        list3.removeIf(item -> item.equals("B"));
        System.out.println(list3); // [A, C, D]
        
        // Example 4: Fail-safe with CopyOnWriteArrayList
        List<String> failSafeList = new CopyOnWriteArrayList<>(Arrays.asList("A", "B", "C", "D"));
        for (String item : failSafeList) {
            if (item.equals("B")) {
                failSafeList.remove(item); // No exception thrown
            }
        }
        System.out.println(failSafeList); // [A, C, D]
        
        // Example 5: Detection of structural modification
        Map<String, Integer> map = new HashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        
        int modCount = 0;
        Iterator<Map.Entry<String, Integer>> mapIterator = map.entrySet().iterator();
        
        // Simulate external modification
        new Thread(() -> {
            try {
                Thread.sleep(100);
                map.put("D", 4); // Structural modification
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        
        try {
            Thread.sleep(200);
            while (mapIterator.hasNext()) {
                mapIterator.next(); // May throw ConcurrentModificationException
            }
        } catch (Exception e) {
            System.out.println("Detected concurrent modification");
        }
    }
}
```

---

## Exception Handling in Collections

### 12. Exception Handling Chain Examples

```java
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class ExceptionHandlingExample {
    public static void main(String[] args) {
        // Example 1: Try-catch in stream operations
        List<String> numbers = Arrays.asList("1", "2", "three", "4", "five");
        
        List<Integer> parsed = numbers.stream()
            .map(s -> {
                try {
                    return Optional.of(Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    System.out.println("Failed to parse: " + s);
                    return Optional.<Integer>empty();
                }
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
        System.out.println(parsed); // [1, 2, 4]
        
        // Example 2: Custom wrapper for exception handling
        List<Integer> result = numbers.stream()
            .map(wrapFunction(Integer::parseInt))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        // Example 3: Collect errors separately
        List<String> errors = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        
        numbers.forEach(s -> {
            try {
                values.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                errors.add(s + ": " + e.getMessage());
            }
        });
        
        System.out.println("Values: " + values);
        System.out.println("Errors: " + errors);
        
        // Example 4: Using Either pattern
        List<Either<String, Integer>> results = numbers.stream()
            .map(s -> {
                try {
                    return Either.<String, Integer>right(Integer.parseInt(s));
                } catch (NumberFormatException e) {
                    return Either.<String, Integer>left("Failed: " + s);
                }
            })
            .collect(Collectors.toList());
        
        results.forEach(either -> 
            either.fold(
                error -> System.out.println("Error: " + error),
                value -> System.out.println("Success: " + value)
            )
        );
        
        // Example 5: Validation chain
        List<User> users = Arrays.asList(
            new User("John", "john@email.com", 25),
            new User("", "invalid-email", 15),
            new User("Jane", "jane@email.com", 30)
        );
        
        List<ValidationResult> validationResults = users.stream()
            .map(ExceptionHandlingExample::validateUser)
            .collect(Collectors.toList());
        
        validationResults.forEach(System.out::println);
    }
    
    private static <T, R> Function<T, R> wrapFunction(Function<T, R> function) {
        return input -> {
            try {
                return function.apply(input);
            } catch (Exception e) {
                return null;
            }
        };
    }
    
    private static ValidationResult validateUser(User user) {
        List<String> errors = new ArrayList<>();
        
        if (user.getName() == null || user.getName().isEmpty()) {
            errors.add("Name is required");
        }
        if (!user.getEmail().contains("@")) {
            errors.add("Invalid email format");
        }
        if (user.getAge() < 18) {
            errors.add("User must be 18 or older");
        }
        
        return new ValidationResult(user, errors);
    }
    
    static class Either<L, R> {
        private final L left;
        private final R right;
        
        private Either(L left, R right) {
            this.left = left;
            this.right = right;
        }
        
        public static <L, R> Either<L, R> left(L value) {
            return new Either<>(value, null);
        }
        
        public static <L, R> Either<L, R> right(R value) {
            return new Either<>(null, value);
        }
        
        public <T> T fold(Function<L, T> leftFunc, Function<R, T> rightFunc) {
            return left != null ? leftFunc.apply(left) : rightFunc.apply(right);
        }
    }
    
    static class User {
        private String name;
        private String email;
        private int age;
        
        public User(String name, String email, int age) {
            this.name = name;
            this.email = email;
            this.age = age;
        }
        
        public String getName() { return name; }
        public String getEmail() { return email; }
        public int getAge() { return age; }
    }
    
    static class ValidationResult {
        private User user;
        private List<String> errors;
        
        public ValidationResult(User user, List<String> errors) {
            this.user = user;
            this.errors = errors;
        }
        
        @Override
        public String toString() {
            return "User: " + user.getName() + ", Valid: " + errors.isEmpty() + 
                   (errors.isEmpty() ? "" : ", Errors: " + errors);
        }
    }
}
```

---

## Thread Safety and Synchronization

### 13. ConcurrentHashMap Examples

```java
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ConcurrentHashMapExample {
    public static void main(String[] args) throws InterruptedException {
        // Example 1: Basic ConcurrentHashMap operations
        ConcurrentHashMap<String, Integer> concurrentMap = new ConcurrentHashMap<>();
        concurrentMap.put("A", 1);
        concurrentMap.put("B", 2);
        
        // Atomic operations
        concurrentMap.putIfAbsent("C", 3);
        concurrentMap.computeIfAbsent("D", k -> 4);
        concurrentMap.compute("A", (k, v) -> v == null ? 1 : v + 1);
        concurrentMap.merge("B", 10, Integer::sum);
        
        System.out.println(concurrentMap); // {A=2, B=12, C=3, D=4}
        
        // Example 2: Thread-safe word counter
        ConcurrentHashMap<String, AtomicInteger> wordCount = new ConcurrentHashMap<>();
        List<String> words = Arrays.asList("apple", "banana", "apple", "orange", "banana", "apple");
        
        // Simulate concurrent processing
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(words.size());
        
        for (String word : words) {
            executor.submit(() -> {
                wordCount.computeIfAbsent(word, k -> new AtomicInteger(0))
                        .incrementAndGet();
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        
        wordCount.forEach((word, count) -> 
            System.out.println(word + ": " + count.get())
        );
        
        // Example 3: Parallel stream operations
        ConcurrentHashMap<String, List<Integer>> groupedData = new ConcurrentHashMap<>();
        
        IntStream.range(0, 1000).parallel().forEach(i -> {
            String key = "Group" + (i % 5);
            groupedData.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>())
                      .add(i);
        });
        
        // Example 4: Search operations
        ConcurrentHashMap<String, Integer> searchMap = new ConcurrentHashMap<>();
        searchMap.put("Alice", 25);
        searchMap.put("Bob", 30);
        searchMap.put("Charlie", 35);
        searchMap.put("David", 28);
        
        // Parallel search
        String found = searchMap.search(2, (k, v) -> v > 30 ? k : null);
        System.out.println("Found: " + found); // Charlie
        
        // Parallel reduce
        Integer sum = searchMap.reduce(2,
            (k, v) -> v,
            Integer::sum
        );
        System.out.println("Sum of ages: " + sum); // 118
        
        // Example 5: Bulk operations with forEach
        searchMap.forEach(2, (k, v) -> System.out.println(k + ": " + v));
    }
}
```

### 14. Thread Safety Examples

```java
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class ThreadSafetyExample {
    // Example 1: Synchronizing collection access
    private static final List<String> synchronizedList = 
        Collections.synchronizedList(new ArrayList<>());
    
    // Example 2: Using explicit locks
    private static final List<String> manualLockList = new ArrayList<>();
    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    
    // Example 3: Copy-on-write collections
    private static final CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();
    
    // Example 4: Concurrent collections
    private static final BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();
    private static final ConcurrentSkipListSet<Integer> skipListSet = new ConcurrentSkipListSet<>();
    
    public static void main(String[] args) throws InterruptedException {
        // Test synchronized collection
        testSynchronizedCollection();
        
        // Test manual locking
        testManualLocking();
        
        // Test copy-on-write
        testCopyOnWrite();
        
        // Test blocking queue
        testBlockingQueue();
        
        // Test concurrent skip list
        testConcurrentSkipList();
    }
    
    private static void testSynchronizedCollection() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(100);
        
        for (int i = 0; i < 100; i++) {
            final int num = i;
            executor.submit(() -> {
                synchronizedList.add("Item-" + num);
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        System.out.println("Synchronized list size: " + synchronizedList.size());
        
        // Manual synchronization for compound operations
        synchronized (synchronizedList) {
            if (!synchronizedList.isEmpty()) {
                String first = synchronizedList.get(0);
                synchronizedList.remove(0);
                System.out.println("Removed: " + first);
            }
        }
    }
    
    private static void testManualLocking() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(20);
        
        // Writers
        for (int i = 0; i < 10; i++) {
            final int num = i;
            executor.submit(() -> {
                rwLock.writeLock().lock();
                try {
                    manualLockList.add("Write-" + num);
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    rwLock.writeLock().unlock();
                    latch.countDown();
                }
            });
        }
        
        // Readers
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                rwLock.readLock().lock();
                try {
                    System.out.println("Read size: " + manualLockList.size());
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    rwLock.readLock().unlock();
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        System.out.println("Manual lock list size: " + manualLockList.size());
    }
    
    private static void testCopyOnWrite() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Add elements
        for (int i = 0; i < 5; i++) {
            cowList.add("Element-" + i);
        }
        
        // Reader thread - iterates without locking
        executor.submit(() -> {
            for (String item : cowList) {
                System.out.println("Reading: " + item);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        // Writer thread - modifies during iteration
        executor.submit(() -> {
            try {
                Thread.sleep(50);
                cowList.add("New-Element");
                System.out.println("Added new element");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        Thread.sleep(1000);
        executor.shutdown();
        System.out.println("COW list final size: " + cowList.size());
    }
    
    private static void testBlockingQueue() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Producer
        executor.submit(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    blockingQueue.put("Message-" + i);
                    System.out.println("Produced: Message-" + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Consumer
        executor.submit(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    String message = blockingQueue.take();
                    System.out.println("Consumed: " + message);
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        Thread.sleep(2000);
        executor.shutdown();
    }
    
    private static void testConcurrentSkipList() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(50);
        
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            executor.submit(() -> {
                skipListSet.add(random.nextInt(100));
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        
        System.out.println("Skip list size: " + skipListSet.size());
        System.out.println("First: " + skipListSet.first());
        System.out.println("Last: " + skipListSet.last());
    }
}
```

### 15. Custom Thread-Safe Collection

```java
import java.util.*;
import java.util.concurrent.locks.*;

public class ThreadSafeCache<K, V> {
    private final Map<K, V> cache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final int maxSize;
    
    public ThreadSafeCache(int maxSize) {
        this.maxSize = maxSize;
    }
    
    public V get(K key) {
        lock.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            if (cache.size() >= maxSize && !cache.containsKey(key)) {
                // Simple eviction - remove first entry
                Iterator<K> it = cache.keySet().iterator();
                if (it.hasNext()) {
                    it.next();
                    it.remove();
                }
            }
            cache.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public V computeIfAbsent(K key, java.util.function.Function<K, V> mappingFunction) {
        V value = get(key);
        if (value != null) {
            return value;
        }
        
        lock.writeLock().lock();
        try {
            // Double-check after acquiring write lock
            value = cache.get(key);
            if (value == null) {
                value = mappingFunction.apply(key);
                if (value != null) {
                    put(key, value);
                }
            }
            return value;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    // Example usage
    public static void main(String[] args) throws InterruptedException {
        ThreadSafeCache<String, String> cache = new ThreadSafeCache<>(100);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1000);
        
        for (int i = 0; i < 1000; i++) {
            final int num = i;
            executor.submit(() -> {
                cache.computeIfAbsent("key-" + (num % 50), 
                    k -> "value-" + num);
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        System.out.println("Cache size: " + cache.size());
    }
}
```

---

## Collection Selection Guide

### When to Use Which Collection

| Collection Type | When to Use | Key Characteristics |
|-----------------|-------------|---------------------|
| **ArrayList** | - Random access needed<br>- Mostly read operations<br>- Known size or small lists | - Fast random access<br>- Slow insertion/deletion in middle<br>- Dynamic array |
| **LinkedList** | - Frequent insertion/deletion<br>- Queue/Deque operations<br>- No random access needed | - Fast insertion/deletion<br>- Slow random access<br>- Double-linked nodes |
| **HashSet** | - Unique elements only<br>- Fast contains() checks<br>- No ordering needed | - No duplicates<br>- Unordered<br>- O(1) basic operations |
| **TreeSet** | - Sorted unique elements<br>- Range operations<br>- Natural ordering | - Sorted order<br>- O(log n) operations<br>- NavigableSet operations |
| **LinkedHashSet** | - Unique elements<br>- Preserve insertion order<br>- Predictable iteration | - Insertion order maintained<br>- Slightly slower than HashSet |
| **HashMap** | - Key-value pairs<br>- Fast lookups<br>- No ordering needed | - O(1) get/put<br>- Allows null key/values<br>- Unordered |
| **TreeMap** | - Sorted key-value pairs<br>- Range queries<br>- Natural key ordering | - Sorted by keys<br>- O(log n) operations<br>- NavigableMap operations |
| **LinkedHashMap** | - Key-value pairs<br>- Preserve insertion order<br>- LRU cache implementation | - Insertion order maintained<br>- Access-order option |
| **PriorityQueue** | - Priority-based processing<br>- Heap operations<br>- Top K elements | - Min/Max heap<br>- O(log n) offer/poll<br>- Not thread-safe |
| **ArrayDeque** | - Stack/Queue operations<br>- Better than LinkedList for deque<br>- No capacity restrictions | - Resizable array<br>- O(1) operations at both ends<br>- No null elements |
| **ConcurrentHashMap** | - Thread-safe map<br>- High concurrency<br>- Parallel operations | - Thread-safe<br>- Segment-based locking<br>- No null keys/values |
| **CopyOnWriteArrayList** | - Thread-safe list<br>- Many readers, few writers<br>- Small lists | - Thread-safe<br>- Snapshot iteration<br>- Expensive writes |
| **BlockingQueue** | - Producer-consumer pattern<br>- Thread coordination<br>- Bounded buffers | - Thread-safe<br>- Blocking operations<br>- Various implementations |

---

## Big O Notation Metrics

### Time Complexity for Common Operations

| Data Structure | Access | Search | Insertion | Deletion | Space |
|----------------|--------|---------|-----------|----------|-------|
| **ArrayList** | O(1) | O(n) | O(n)* | O(n) | O(n) |
| **LinkedList** | O(n) | O(n) | O(1) | O(1) | O(n) |
| **HashMap** | N/A | O(1)** | O(1)** | O(1)** | O(n) |
| **TreeMap** | N/A | O(log n) | O(log n) | O(log n) | O(n) |
| **HashSet** | N/A | O(1)** | O(1)** | O(1)** | O(n) |
| **TreeSet** | N/A | O(log n) | O(log n) | O(log n) | O(n) |
| **LinkedHashMap** | N/A | O(1)** | O(1)** | O(1)** | O(n) |
| **LinkedHashSet** | N/A | O(1)** | O(1)** | O(1)** | O(n) |
| **PriorityQueue** | N/A | O(n) | O(log n) | O(log n) | O(n) |
| **ArrayDeque** | O(1) | O(n) | O(1)*** | O(1)*** | O(n) |
| **Stack** | O(n) | O(n) | O(1) | O(1) | O(n) |
| **ConcurrentHashMap** | N/A | O(1)** | O(1)** | O(1)** | O(n) |
| **CopyOnWriteArrayList** | O(1) | O(n) | O(n) | O(n) | O(n) |

\* Amortized O(1) at the end, O(n) in the middle  
\** Average case, O(n) worst case due to hash collisions  
\*** At both ends

### Detailed Performance Characteristics

| Operation | ArrayList | LinkedList | HashMap | TreeMap | HashSet | TreeSet |
|-----------|-----------|------------|---------|---------|---------|---------|
| **get(index)** | O(1) | O(n) | - | - | - | - |
| **add(element)** | O(1)* | O(1) | - | - | O(1)** | O(log n) |
| **add(index, element)** | O(n) | O(n) | - | - | - | - |
| **remove(index)** | O(n) | O(n) | - | - | - | - |
| **remove(element)** | O(n) | O(n) | - | - | O(1)** | O(log n) |
| **contains()** | O(n) | O(n) | O(1)** | O(log n) | O(1)** | O(log n) |
| **size()** | O(1) | O(1) | O(1) | O(1) | O(1) | O(1) |
| **put(k,v)** | - | - | O(1)** | O(log n) | - | - |
| **get(key)** | - | - | O(1)** | O(log n) | - | - |
| **remove(key)** | - | - | O(1)** | O(log n) | - | - |

\* Amortized time, worst case O(n) when resize needed  
\** Average case, O(n) worst case with hash collisions

---

## Interview Questions

### Common Collection Interview Questions

#### 1. Basic Questions
- What is the difference between ArrayList and LinkedList?
- When would you use HashMap vs TreeMap?
- Explain the difference between HashSet, LinkedHashSet, and TreeSet
- What is the difference between HashMap and ConcurrentHashMap?
- How does HashMap work internally?
- What is the contract between equals() and hashCode()?
- What happens when two objects have the same hashCode?

#### 2. Intermediate Questions
- How to make a collection thread-safe?
- Explain fail-fast and fail-safe iterators
- What is the load factor in HashMap?
- How to sort a HashMap by values?
- Difference between Comparable and Comparator?
- What is CopyOnWriteArrayList and when to use it?
- How to find duplicate elements in a list?

#### 3. Advanced Questions with Solutions

```java
// Question: Implement an LRU Cache
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    
    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // true for access-order
        this.capacity = capacity;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

// Question: Find first non-repeating character
public static Character firstNonRepeating(String str) {
    Map<Character, Integer> counts = new LinkedHashMap<>();
    for (char c : str.toCharArray()) {
        counts.merge(c, 1, Integer::sum);
    }
    return counts.entrySet().stream()
        .filter(e -> e.getValue() == 1)
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);
}

// Question: Group anagrams together
public static List<List<String>> groupAnagrams(List<String> words) {
    return new ArrayList<>(words.stream()
        .collect(Collectors.groupingBy(
            word -> {
                char[] chars = word.toCharArray();
                Arrays.sort(chars);
                return new String(chars);
            }
        )).values());
}

// Question: Find intersection of multiple lists
public static <T> Set<T> intersection(List<List<T>> lists) {
    if (lists.isEmpty()) return new HashSet<>();
    
    Set<T> result = new HashSet<>(lists.get(0));
    for (int i = 1; i < lists.size(); i++) {
        result.retainAll(lists.get(i));
    }
    return result;
}

// Question: Implement a frequency counter
public static <T> Map<T, Long> frequencyMap(Collection<T> collection) {
    return collection.stream()
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.counting()
        ));
}

// Question: Top K frequent elements
public static List<Integer> topKFrequent(int[] nums, int k) {
    Map<Integer, Long> frequency = Arrays.stream(nums)
        .boxed()
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.counting()
        ));
    
    return frequency.entrySet().stream()
        .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
        .limit(k)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
}
```

### Performance Optimization Tips

1. **Choose the right collection**: Use ArrayList for random access, LinkedList for frequent insertions/deletions
2. **Initial capacity**: Set initial capacity for ArrayList and HashMap to avoid resizing
3. **Load factor**: Adjust HashMap load factor based on memory vs performance trade-off
4. **Immutable collections**: Use Collections.unmodifiableXXX() or Guava's ImmutableXXX
5. **Primitive collections**: Use libraries like Trove or Eclipse Collections for primitive types
6. **Lazy evaluation**: Use Stream API for lazy evaluation and better performance
7. **Parallel processing**: Use parallelStream() for large datasets with independent operations
8. **Caching**: Implement proper caching strategies using WeakHashMap or LRU cache

---

## Best Practices

### Do's
1. ✅ Use generics to ensure type safety
2. ✅ Choose appropriate collection based on use case
3. ✅ Use Iterator for safe removal during iteration
4. ✅ Override equals() and hashCode() together
5. ✅ Use concurrent collections for thread-safe operations
6. ✅ Set initial capacity when size is known
7. ✅ Use immutable collections when possible
8. ✅ Prefer isEmpty() over size() == 0

### Don'ts
1. ❌ Don't modify collection while iterating (without Iterator)
2. ❌ Don't use raw types
3. ❌ Don't ignore null handling
4. ❌ Don't assume ordering in HashMap/HashSet
5. ❌ Don't use == for comparing collection contents
6. ❌ Don't synchronize on collection instances
7. ❌ Don't ignore thread-safety requirements
8. ❌ Don't store mutable objects as keys

---

## Conclusion

This guide covers the essential aspects of Java Collections Framework including:
- Stream operations (map, flatMap, filter)
- Collection conversions and transformations
- Thread safety and synchronization
- Performance characteristics and Big O notation
- Best practices and common pitfalls
- Interview preparation materials
