# Java Interview Questions - Memory Management & Performance

## Table of Contents
1. [Java Heap Memory Management](#java-heap-memory-management)
2. [Memory Analysis & Leak Detection](#memory-analysis--leak-detection)
3. [Garbage Collection in Java](#garbage-collection-in-java)
4. [Heap Dump Analysis](#heap-dump-analysis)
5. [Memory Leak Examples & Solutions](#memory-leak-examples--solutions)
6. [Best Practices](#best-practices)

---

## Java Heap Memory Management

### How Java Heap Memory Works

The Java heap is the runtime data area from which the JVM allocates memory for all class instances and arrays. The heap is created when the JVM starts up and may increase or decrease in size while the application runs.

#### Memory Structure

```
┌─────────────────────────────────────────────────────────┐
│                     JVM Memory                          │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐  │
│  │                  Heap Memory                     │  │
│  ├──────────────────────┬──────────────────────────┤  │
│  │     Young Generation  │    Old Generation        │  │
│  ├────────┬──────┬──────┼──────────────────────────┤  │
│  │  Eden  │  S0  │  S1  │      Tenured Space      │  │
│  └────────┴──────┴──────┴──────────────────────────┘  │
│                                                         │
│  ┌─────────────────────────────────────────────────┐  │
│  │               Non-Heap Memory                    │  │
│  ├─────────────────────────────────────────────────┤  │
│  │  Metaspace | Code Cache | Thread Stacks         │  │
│  └─────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Key Components

1. **Young Generation**
   - **Eden Space**: Where new objects are allocated
   - **Survivor Spaces (S0, S1)**: Objects that survive garbage collection

2. **Old Generation (Tenured)**
   - Long-lived objects that survived multiple GC cycles

3. **Metaspace** (Java 8+)
   - Stores class metadata
   - Replaced PermGen from earlier versions

### Memory Parameters

```bash
# Setting heap size
-Xms<size>    # Initial heap size
-Xmx<size>    # Maximum heap size

# Example
java -Xms512m -Xmx2048m MyApplication

# Young generation size
-Xmn<size>    # Size of young generation

# Metaspace size
-XX:MetaspaceSize=<size>
-XX:MaxMetaspaceSize=<size>
```

---

## Memory Analysis & Leak Detection

### Step-by-Step Memory Analysis Process

#### Step 1: Monitor Memory Usage

```bash
# Using jstat to monitor heap usage
jstat -gc <pid> 1000

# Monitor heap memory in real-time
jcmd <pid> GC.heap_info
```

#### Step 2: Identify Memory Issues

**Key Indicators of Memory Problems:**
- Increasing heap usage over time
- Frequent Full GC cycles
- OutOfMemoryError exceptions
- Application slowdown

#### Step 3: Capture Heap Dump

```bash
# Method 1: Using jmap
jmap -dump:format=b,file=heapdump.hprof <pid>

# Method 2: Using jcmd
jcmd <pid> GC.heap_dump heapdump.hprof

# Method 3: Automatic on OutOfMemoryError
java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/path/to/dumps MyApp
```

#### Step 4: Analyze Heap Dump

**Using Different Tools:**

1. **Eclipse Memory Analyzer (MAT)**
   ```bash
   # Download MAT and open heap dump
   ./MemoryAnalyzer -data workspace -vmargs -Xmx4g heapdump.hprof
   ```

2. **jhat (Java Heap Analysis Tool)**
   ```bash
   jhat -port 7000 heapdump.hprof
   # Access at http://localhost:7000
   ```

3. **VisualVM**
   ```bash
   # Start VisualVM and load heap dump
   visualvm --openfile heapdump.hprof
   ```

---

## Garbage Collection in Java

### Types of Garbage Collectors

#### 1. Serial GC
```bash
-XX:+UseSerialGC
```
- **Use Case**: Single-threaded applications, small heaps (<100MB)
- **Characteristics**: Stop-the-world, single-threaded

#### 2. Parallel GC (Throughput Collector)
```bash
-XX:+UseParallelGC
-XX:ParallelGCThreads=4
```
- **Use Case**: Multi-core systems, batch processing
- **Characteristics**: Multi-threaded, high throughput

#### 3. G1GC (Garbage First)
```bash
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
```
- **Use Case**: Large heaps (>4GB), low-latency requirements
- **Characteristics**: Predictable pause times, region-based

#### 4. ZGC (Z Garbage Collector)
```bash
-XX:+UseZGC
-XX:ZCollectionInterval=120
```
- **Use Case**: Very large heaps (TB), ultra-low latency (<10ms)
- **Characteristics**: Concurrent, scalable

#### 5. Shenandoah GC
```bash
-XX:+UseShenandoahGC
```
- **Use Case**: Large heaps, consistent low pause times
- **Characteristics**: Concurrent evacuation, region-based

### GC Selection Guide

| Application Type | Heap Size | Latency Requirement | Recommended GC |
|-----------------|-----------|-------------------|----------------|
| Small, single-core | <100MB | Not critical | Serial GC |
| Batch processing | 1-4GB | Not critical | Parallel GC |
| Web applications | 4-32GB | <1 second | G1GC |
| Real-time systems | >32GB | <10ms | ZGC |
| Large services | >8GB | Consistent low | Shenandoah |

### Common GC Issues and Solutions

#### Issue 1: Frequent Full GC
```bash
# Diagnosis
jstat -gcutil <pid> 1000

# Solution: Increase heap size or tune GC
-Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

#### Issue 2: Long GC Pauses
```bash
# Enable GC logging
-Xlog:gc*:file=gc.log:time,uptime,level,tags

# Analyze pause times
grep "Pause" gc.log | awk '{print $NF}'
```

---

## Heap Dump Analysis

### Comprehensive Analysis Workflow

#### 1. Capture Heap Dump
```java
// Programmatically trigger heap dump
import com.sun.management.HotSpotDiagnosticMXBean;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

public class HeapDumpGenerator {
    public static void dumpHeap(String filePath, boolean live) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(
                server, 
                "com.sun.management:type=HotSpotDiagnostic", 
                HotSpotDiagnosticMXBean.class
            );
            mxBean.dumpHeap(filePath, live);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

#### 2. Analysis Using Eclipse MAT

**Step-by-Step MAT Analysis:**

1. **Open Heap Dump**
   ```
   File → Open Heap Dump → Select .hprof file
   ```

2. **Run Leak Suspects Report**
   - Automatically identifies potential memory leaks
   - Shows biggest objects and their retention paths

3. **Analyze Dominator Tree**
   - Shows objects keeping most memory alive
   - Identify objects with high retained heap

4. **Check Histogram**
   - View all classes and their instance counts
   - Sort by retained heap or shallow heap

5. **Find GC Roots**
   - Trace object references back to GC roots
   - Identify why objects aren't being collected

#### 3. Using JProfiler

```bash
# Attach JProfiler to running application
jprofiler -attach <pid>

# Take heap snapshot
jprofiler -snapshot heap.jps
```

**Analysis Steps:**
1. Open heap walker
2. Check biggest objects
3. Analyze reference graphs
4. Find memory leak candidates

---

## Memory Leak Examples & Solutions

### Example 1: Unclosed Resources

**Problematic Code:**
```java
public class ResourceLeak {
    public void processFile(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            // Processing file...
            // Reader never closed - MEMORY LEAK!
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

**Solution:**
```java
public class ResourceFixed {
    public void processFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            // Processing file...
            // Auto-closed with try-with-resources
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### Example 2: Static Collections

**Problematic Code:**
```java
public class StaticCollectionLeak {
    private static List<Object> cache = new ArrayList<>();
    
    public void addToCache(Object obj) {
        cache.add(obj);  // Objects never removed - MEMORY LEAK!
    }
}
```

**Solution:**
```java
public class StaticCollectionFixed {
    private static Map<String, Object> cache = new WeakHashMap<>();
    // Or implement cache eviction
    private static final int MAX_SIZE = 1000;
    
    public void addToCache(String key, Object obj) {
        if (cache.size() >= MAX_SIZE) {
            // Implement LRU eviction
            evictOldest();
        }
        cache.put(key, obj);
    }
}
```

### Example 3: Thread Local Variables

**Problematic Code:**
```java
public class ThreadLocalLeak {
    private static ThreadLocal<List<Object>> threadLocal = 
        ThreadLocal.withInitial(ArrayList::new);
    
    public void process() {
        List<Object> list = threadLocal.get();
        list.add(new Object());
        // ThreadLocal never cleaned - MEMORY LEAK in thread pools!
    }
}
```

**Solution:**
```java
public class ThreadLocalFixed {
    private static ThreadLocal<List<Object>> threadLocal = 
        ThreadLocal.withInitial(ArrayList::new);
    
    public void process() {
        try {
            List<Object> list = threadLocal.get();
            list.add(new Object());
            // Process...
        } finally {
            threadLocal.remove();  // Clean up ThreadLocal
        }
    }
}
```

---

## Monitoring Tools Comparison

| Tool | Purpose | Command/Usage | Best For |
|------|---------|--------------|----------|
| jstat | GC statistics | `jstat -gc <pid>` | Quick monitoring |
| jmap | Heap dump & histogram | `jmap -histo <pid>` | Memory snapshot |
| jcmd | Multiple diagnostics | `jcmd <pid> GC.heap_info` | Comprehensive info |
| jconsole | GUI monitoring | `jconsole <pid>` | Visual monitoring |
| VisualVM | Profiling & analysis | `visualvm` | Deep analysis |
| MAT | Heap dump analysis | Eclipse plugin | Memory leak detection |
| JProfiler | Commercial profiler | Licensed tool | Professional analysis |

---

## Best Practices

### 1. Proactive Monitoring
```bash
# Enable GC logging in production
-Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=5,filesize=100M

# Enable heap dump on OOM
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/dumps/
```

### 2. Memory Leak Prevention

- **Use try-with-resources** for AutoCloseable objects
- **Clear collections** when no longer needed
- **Remove listeners** and callbacks when done
- **Use weak references** for caches
- **Clean ThreadLocals** in thread pools

### 3. Optimization Strategies

```java
// 1. Object pooling for expensive objects
public class ObjectPool<T> {
    private final Queue<T> pool = new ConcurrentLinkedQueue<>();
    
    public T borrow() {
        T object = pool.poll();
        return object != null ? object : createNew();
    }
    
    public void returnObject(T object) {
        pool.offer(object);
    }
}

// 2. Lazy initialization
public class LazyHolder {
    private volatile ExpensiveObject instance;
    
    public ExpensiveObject getInstance() {
        if (instance == null) {
            synchronized(this) {
                if (instance == null) {
                    instance = new ExpensiveObject();
                }
            }
        }
        return instance;
    }
}
```

### 4. Performance Tuning Checklist

- [ ] Set appropriate heap size (-Xmx, -Xms)
- [ ] Choose right GC algorithm for your use case
- [ ] Enable GC logging for monitoring
- [ ] Configure heap dump on OOM
- [ ] Regular heap dump analysis in staging
- [ ] Monitor GC pause times
- [ ] Track memory metrics in production
- [ ] Implement memory leak tests
- [ ] Use profilers during development
- [ ] Document memory requirements

---

## Quick Reference Commands

```bash
# Find Java process ID
jps -l

# Check heap usage
jmap -heap <pid>

# Force garbage collection
jcmd <pid> GC.run

# Print class histogram
jmap -histo:live <pid>

# Thread dump
jstack <pid> > thread_dump.txt

# Enable flight recorder
java -XX:StartFlightRecording=duration=60s,filename=recording.jfr MyApp

# Analyze GC log
java -jar gcviewer.jar gc.log
```

---

## Additional Resources

- [Official JVM Tuning Guide](https://docs.oracle.com/en/java/javase/17/gctuning/)
- [Baeldung - Java Heap Dump Capture](https://www.baeldung.com/java-heap-dump-capture)
- [Eclipse MAT Documentation](https://www.eclipse.org/mat/documentation/)
- [G1GC Tuning Guide](https://www.oracle.com/technical-resources/articles/java/g1gc.html)

---

## Contributing

Feel free to submit pull requests with additional interview questions, examples, or improvements to this guide.

## License

This repository is licensed under MIT License.