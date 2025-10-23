# Java Interview Questions - Memory Management & Performance

## Table of Contents
1. [Java Heap Memory Management](#java-heap-memory-management)
2. [Memory Analysis & Leak Detection](#memory-analysis--leak-detection)
3. [Garbage Collection in Java](#garbage-collection-in-java)
4. [Heap Dump Analysis](#heap-dump-analysis)
5. [Memory Leak Examples & Solutions](#memory-leak-examples--solutions)
6. [Best Practices](#best-practices)
7. [Memory Analysis in Kubernetes & Containers](#memory-analysis-in-kubernetes--containers)
8. [Tomcat Server Memory Analysis](#tomcat-server-memory-analysis)
9. [IDE Plugins for Memory Analysis](#ide-plugins-for-memory-analysis)
10. [Remote Memory Analysis Setup](#remote-memory-analysis-setup)
11. [Container-Specific Memory Troubleshooting](#container-specific-memory-troubleshooting)
12. [Production Monitoring Checklist](#production-monitoring-checklist)

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

## Memory Analysis in Kubernetes & Containers

### Kubernetes Pod Memory Analysis

#### 1. Accessing Pods for Memory Analysis

```bash
# Get pod information
kubectl get pods -n <namespace>
kubectl top pods -n <namespace>

# Check pod memory usage
kubectl top pod <pod-name> -n <namespace> --containers

# Execute commands in pod
kubectl exec -it <pod-name> -n <namespace> -- /bin/bash

# Get JVM process ID in container
kubectl exec <pod-name> -n <namespace> -- jps -l

# Take heap dump from pod
kubectl exec <pod-name> -n <namespace> -- jmap -dump:format=b,file=/tmp/heapdump.hprof <pid>

# Copy heap dump to local machine
kubectl cp <namespace>/<pod-name>:/tmp/heapdump.hprof ./heapdump.hprof
```

#### 2. Container Memory Limits & JVM Configuration

```yaml
# Kubernetes deployment with memory settings
apiVersion: apps/v1
kind: Deployment
metadata:
  name: java-app
spec:
  template:
    spec:
      containers:
      - name: java-app
        image: myapp:latest
        resources:
          requests:
            memory: "1Gi"
          limits:
            memory: "2Gi"
        env:
        - name: JAVA_OPTS
          value: "-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/heap-dumps"
        volumeMounts:
        - name: heap-dumps
          mountPath: /heap-dumps
      volumes:
      - name: heap-dumps
        emptyDir: {}
```

#### 3. Monitoring Memory in Kubernetes

```bash
# Install metrics-server if not present
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Create monitoring script
cat << 'EOF' > monitor-pod-memory.sh
#!/bin/bash
POD=$1
NAMESPACE=$2

while true; do
  kubectl exec $POD -n $NAMESPACE -- sh -c 'cat /proc/1/status | grep -E "VmRSS|VmSize"'
  kubectl exec $POD -n $NAMESPACE -- jstat -gcutil 1 1000 1
  sleep 5
done
EOF

chmod +x monitor-pod-memory.sh
./monitor-pod-memory.sh <pod-name> <namespace>
```

### Amazon EKS Memory Analysis

#### 1. EKS-Specific Configuration

```bash
# Connect to EKS cluster
aws eks update-kubeconfig --name <cluster-name> --region <region>

# Enable Container Insights for monitoring
aws eks create-addon \
  --cluster-name <cluster-name> \
  --addon-name adot \
  --addon-version latest

# Deploy CloudWatch agent for detailed metrics
kubectl apply -f https://raw.githubusercontent.com/aws-samples/amazon-cloudwatch-container-insights/latest/k8s-deployment-manifest-templates/deployment-mode/daemonset/container-insights-monitoring/cloudwatch-namespace.yaml
```

#### 2. EKS Pod Memory Profiling

```yaml
# EKS pod with JMX enabled for remote profiling
apiVersion: v1
kind: Service
metadata:
  name: jmx-service
spec:
  type: LoadBalancer
  ports:
  - port: 9090
    targetPort: 9090
    name: jmx
  selector:
    app: java-app
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: java-app
spec:
  template:
    spec:
      containers:
      - name: java-app
        image: myapp:latest
        ports:
        - containerPort: 9090
        env:
        - name: JAVA_OPTS
          value: >-
            -Dcom.sun.management.jmxremote
            -Dcom.sun.management.jmxremote.port=9090
            -Dcom.sun.management.jmxremote.rmi.port=9090
            -Dcom.sun.management.jmxremote.authenticate=false
            -Dcom.sun.management.jmxremote.ssl=false
            -Djava.rmi.server.hostname=localhost
```

#### 3. Using AWS Systems Manager for Heap Dumps

```bash
# Create SSM document for heap dump
aws ssm create-document --name "JavaHeapDump" --document-type "Command" --content '{
  "schemaVersion": "2.2",
  "description": "Take Java Heap Dump",
  "mainSteps": [
    {
      "action": "aws:runShellScript",
      "name": "takeHeapDump",
      "inputs": {
        "runCommand": [
          "#!/bin/bash",
          "PID=$(pgrep java)",
          "jmap -dump:format=b,file=/tmp/heapdump-$(date +%Y%m%d-%H%M%S).hprof $PID",
          "aws s3 cp /tmp/heapdump*.hprof s3://my-bucket/heap-dumps/"
        ]
      }
    }
  ]
}'
```

### Docker Container Memory Analysis

#### 1. Direct Docker Commands

```bash
# Check container memory usage
docker stats <container-id>

# Execute heap dump in container
docker exec <container-id> jmap -dump:format=b,file=/tmp/heapdump.hprof 1

# Copy heap dump from container
docker cp <container-id>:/tmp/heapdump.hprof ./heapdump.hprof

# Attach to container for live analysis
docker exec -it <container-id> /bin/bash

# Check Java process memory
docker exec <container-id> jcmd 1 VM.native_memory summary
```

#### 2. Docker Compose with Memory Limits

```yaml
version: '3.8'
services:
  java-app:
    image: myapp:latest
    deploy:
      resources:
        limits:
          memory: 2G
        reservations:
          memory: 1G
    environment:
      JAVA_OPTS: >-
        -XX:MaxRAMPercentage=75.0
        -XX:+UseContainerSupport
        -XX:+HeapDumpOnOutOfMemoryError
        -XX:HeapDumpPath=/dumps
    volumes:
      - ./heap-dumps:/dumps
    ports:
      - "9090:9090"  # JMX port
```

---

## Tomcat Server Memory Analysis

### 1. Tomcat JVM Configuration

```bash
# catalina.sh or setenv.sh configuration
export CATALINA_OPTS="$CATALINA_OPTS -Xms1024m"
export CATALINA_OPTS="$CATALINA_OPTS -Xmx4096m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseG1GC"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+HeapDumpOnOutOfMemoryError"
export CATALINA_OPTS="$CATALINA_OPTS -XX:HeapDumpPath=/opt/tomcat/dumps/"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDetails"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDateStamps"
export CATALINA_OPTS="$CATALINA_OPTS -Xloggc:/opt/tomcat/logs/gc.log"

# Enable JMX for remote monitoring
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.port=9090"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
```

### 2. Tomcat Memory Leak Detection

```xml
<!-- server.xml - Enable memory leak detection -->
<Server>
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />
</Server>
```

### 3. Analyzing Tomcat Memory Issues

```bash
# Find Tomcat process
ps aux | grep tomcat

# Take thread dump
kill -3 <tomcat-pid>
# Or
jstack <tomcat-pid> > thread_dump.txt

# Analyze Tomcat heap
jmap -heap <tomcat-pid>

# Check loaded classes (useful for PermGen/Metaspace issues)
jmap -clstats <tomcat-pid>

# Monitor Tomcat with JConsole
jconsole <tomcat-host>:9090
```

### 4. Tomcat Manager App for Monitoring

```xml
<!-- tomcat-users.xml -->
<tomcat-users>
  <role rolename="manager-gui"/>
  <role rolename="manager-status"/>
  <user username="admin" password="password" roles="manager-gui,manager-status"/>
</tomcat-users>
```

Access: `http://localhost:8080/manager/status`

---

## IDE Plugins for Memory Analysis

### IntelliJ IDEA Plugins

#### 1. **JProfiler Integration**
```
Installation: File → Settings → Plugins → Search "JProfiler"
Usage:
- Run → Profile 'Application'
- Analyze CPU, Memory, Threads
- Take heap snapshots
- Find memory leaks
```

#### 2. **VisualVM Launcher**
```
Installation: File → Settings → Plugins → Search "VisualVM Launcher"
Configuration:
- Set VisualVM executable path
- Right-click on main class → Run with VisualVM
- Automatic profiling integration
```

#### 3. **JVM Debugger Memory View**
```
Built-in Feature:
- Debug your application
- View → Tool Windows → Memory View
- Track object allocations
- Analyze memory consumption during debugging
```

#### 4. **Eclipse Memory Analyzer (MAT) Integration**
```
Installation: Install MAT standalone
IntelliJ Integration:
- Tools → External Tools → Add
- Program: /path/to/mat/MemoryAnalyzer
- Arguments: $FilePath$
- Generate heap dump → Analyze with MAT
```

#### 5. **Heap Dump Analysis (Built-in)**
```
Usage:
- Help → Diagnostic Tools → Analyze Heap Dump
- Open .hprof files directly
- View dominator tree
- Find memory leaks
```

#### IntelliJ Profiler Configuration

```xml
<!-- Run configuration VM options -->
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=./dumps
-Xlog:gc*:file=gc.log
-XX:+FlightRecorder
-XX:StartFlightRecording=duration=60s,filename=recording.jfr
```

### Visual Studio Code Extensions

#### 1. **Java Extension Pack (Microsoft)**
```json
// .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Launch with Profiling",
      "request": "launch",
      "mainClass": "com.example.Main",
      "vmArgs": "-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./dumps -Xmx1g",
      "env": {
        "JAVA_OPTS": "-XX:+PrintGCDetails"
      }
    }
  ]
}
```

#### 2. **Java Debugger**
Features:
- Memory view during debugging
- Variable inspection
- Heap analysis

#### 3. **Monitor Java Application**
```
Installation: Extensions → Search "Monitor Java"
Features:
- JMX connection
- Memory monitoring
- Thread analysis
- GC monitoring
```

#### 4. **Tomcat for Java Extension**
```json
// settings.json
{
  "tomcat.workspace": "/opt/tomcat",
  "java.jdt.ls.vmargs": "-XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"
}
```

#### 5. **Remote Development Extension**
```bash
# Connect to remote Kubernetes pod
code --install-extension ms-vscode-remote.remote-ssh
code --install-extension ms-vscode-remote.remote-containers
```

### VS Code Tasks for Memory Analysis

```json
// .vscode/tasks.json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Take Heap Dump",
      "type": "shell",
      "command": "jmap -dump:format=b,file=heapdump-${workspaceFolderBasename}.hprof ${input:pid}",
      "problemMatcher": []
    },
    {
      "label": "Analyze GC Log",
      "type": "shell",
      "command": "java -jar gcviewer.jar gc.log",
      "problemMatcher": []
    },
    {
      "label": "Start VisualVM",
      "type": "shell",
      "command": "visualvm --openid ${input:pid}",
      "problemMatcher": []
    }
  ],
  "inputs": [
    {
      "id": "pid",
      "type": "promptString",
      "description": "Enter Java Process ID"
    }
  ]
}
```

---

## Remote Memory Analysis Setup

### 1. JMX Remote Connection

```java
// JMX Connection Code
import javax.management.*;
import javax.management.remote.*;
import java.lang.management.*;

public class RemoteMemoryMonitor {
    public static void connectAndAnalyze(String host, int port) throws Exception {
        String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
        JMXServiceURL serviceUrl = new JMXServiceURL(url);
        JMXConnector connector = JMXConnectorFactory.connect(serviceUrl);
        MBeanServerConnection connection = connector.getMBeanServerConnection();
        
        // Get Memory MBean
        MemoryMXBean memoryMBean = ManagementFactory.newPlatformMXBeanProxy(
            connection,
            ManagementFactory.MEMORY_MXBEAN_NAME,
            MemoryMXBean.class
        );
        
        // Analyze memory
        MemoryUsage heapUsage = memoryMBean.getHeapMemoryUsage();
        System.out.println("Heap Used: " + heapUsage.getUsed() / 1024 / 1024 + " MB");
        System.out.println("Heap Max: " + heapUsage.getMax() / 1024 / 1024 + " MB");
        
        // Trigger GC if needed
        memoryMBean.gc();
        
        connector.close();
    }
}
```

### 2. SSH Tunnel for Remote Analysis

```bash
# Create SSH tunnel to remote server
ssh -L 9090:localhost:9090 user@remote-server

# Create SSH tunnel to Kubernetes pod
kubectl port-forward pod/<pod-name> 9090:9090 -n <namespace>

# Connect VisualVM or JConsole
jconsole localhost:9090
visualvm --openjmx localhost:9090
```

### 3. Automated Memory Monitoring Script

```bash
#!/bin/bash
# monitor-remote-memory.sh

REMOTE_HOST=$1
REMOTE_USER=$2
APP_NAME=$3

# Function to take remote heap dump
take_heap_dump() {
    TIMESTAMP=$(date +%Y%m%d-%H%M%S)
    ssh $REMOTE_USER@$REMOTE_HOST "
        PID=\$(pgrep -f $APP_NAME)
        jmap -dump:format=b,file=/tmp/heapdump-$TIMESTAMP.hprof \$PID
    "
    scp $REMOTE_USER@$REMOTE_HOST:/tmp/heapdump-$TIMESTAMP.hprof ./
    echo "Heap dump saved: heapdump-$TIMESTAMP.hprof"
}

# Function to monitor memory
monitor_memory() {
    ssh $REMOTE_USER@$REMOTE_HOST "
        PID=\$(pgrep -f $APP_NAME)
        while true; do
            echo '===== Memory Stats ====='
            jstat -gcutil \$PID
            echo '===== Heap Info ====='
            jcmd \$PID GC.heap_info
            sleep 5
        done
    "
}

# Main menu
echo "1. Take Heap Dump"
echo "2. Monitor Memory"
read -p "Select option: " choice

case $choice in
    1) take_heap_dump ;;
    2) monitor_memory ;;
    *) echo "Invalid option" ;;
esac
```

---

## Container-Specific Memory Troubleshooting

### Common Issues and Solutions

#### 1. Container OOMKilled

```yaml
# Check why pod was killed
kubectl describe pod <pod-name> -n <namespace>

# Solution: Increase memory limits
resources:
  limits:
    memory: "4Gi"  # Increased from 2Gi
  requests:
    memory: "2Gi"
```

#### 2. JVM Not Respecting Container Limits

```dockerfile
# Dockerfile with proper JVM settings
FROM openjdk:17-slim

# Enable container support
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0"

ENTRYPOINT ["java", "$JAVA_OPTS", "-jar", "app.jar"]
```

#### 3. Native Memory Issues

```bash
# Enable Native Memory Tracking
-XX:NativeMemoryTracking=summary

# Check native memory in container
kubectl exec <pod-name> -- jcmd 1 VM.native_memory summary
```

---

## Production Monitoring Checklist

### Kubernetes/Container Environment

- [ ] Set appropriate resource limits and requests
- [ ] Enable container-aware JVM flags
- [ ] Configure heap dumps to persistent volumes
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Enable JMX with proper security
- [ ] Configure log aggregation (ELK/CloudWatch)
- [ ] Set up alerts for memory thresholds
- [ ] Test horizontal pod autoscaling
- [ ] Implement graceful shutdown handlers
- [ ] Regular heap dump analysis in staging

### Tomcat Specific

- [ ] Configure appropriate heap size in setenv.sh
- [ ] Enable access logs with memory tracking
- [ ] Set up manager app for monitoring
- [ ] Configure connection pool sizes
- [ ] Enable memory leak detection listeners
- [ ] Monitor PermGen/Metaspace usage
- [ ] Set session timeout appropriately
- [ ] Configure thread pool sizes
- [ ] Enable GC logging
- [ ] Regular application redeployment testing

---

## Additional Resources

- [Official JVM Tuning Guide](https://docs.oracle.com/en/java/javase/17/gctuning/)
- [Baeldung - Java Heap Dump Capture](https://www.baeldung.com/java-heap-dump-capture)
- [Eclipse MAT Documentation](https://www.eclipse.org/mat/documentation/)
- [G1GC Tuning Guide](https://www.oracle.com/technical-resources/articles/java/g1gc.html)
- [Kubernetes Java Apps Best Practices](https://kubernetes.io/docs/tutorials/configuration/configure-java-microservice/)
- [AWS EKS Best Practices](https://aws.github.io/aws-eks-best-practices/)
- [Docker Java Application Guide](https://docs.docker.com/language/java/)
- [Tomcat Memory Configuration](https://tomcat.apache.org/tomcat-9.0-doc/config/)

---

## Contributing

Feel free to submit pull requests with additional interview questions, examples, or improvements to this guide.

## License

This repository is licensed under MIT License.