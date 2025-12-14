# We Hit Spring Boot With 1,000,000 Requests/Second... And It Did Something We Didn't Expect 🚀

> **Article by**: Gaddam Naveen  
> **Original Source**: [Medium Article](https://medium.com/@gaddamnaveen192/we-hit-spring-boot-with-1-000-000-requests-second-and-it-did-something-we-didnt-expect-2b7b4db2680c)  
> **Published**: December 2024

## 📋 Table of Contents

1. [Overview](#overview)
2. [The Core Challenge](#the-core-challenge)
3. [Architecture Philosophy](#architecture-philosophy)
4. [Complete Technical Implementation](#complete-technical-implementation)
5. [JVM Optimization](#jvm-optimization)
6. [Linux Kernel Tuning](#linux-kernel-tuning)
7. [Load Balancing Architecture](#load-balancing-architecture)
8. [Kubernetes Deployment](#kubernetes-deployment)
9. [Benchmarking Strategy](#benchmarking-strategy)
10. [Pitfalls & Best Practices](#pitfalls-best-practices)
11. [Complete Request Flow](#complete-request-flow)
12. [Infrastructure Sizing](#infrastructure-sizing)

---

## 🎯 Overview

This document provides a deep dive into achieving **1,000,000 requests per second (1M RPS)** with Spring Boot 4.0 and Java 21. This isn't just about application optimization—it's a comprehensive systems engineering challenge involving:

- **Distributed architectures**
- **Operating system/kernel optimization**
- **Network Interface Card (NIC) tuning**
- **Garbage Collection strategies**
- **Event-driven programming paradigms**

### What is the C10M Problem?

The **C10M problem** (originally proposed by Dan Kegel) refers to safely managing **10 million concurrent connections**. Handling 1M RPS is part of solving this challenge, requiring a complete paradigm shift from traditional blocking architectures.

---

## 🚫 The Core Challenge

### What You CANNOT Use at This Scale

To achieve 1M RPS, you must completely abandon classical blocking paradigms:

❌ **No Servlets** - Thread-per-request model collapses  
❌ **No thread-per-request** - Context switches dominate CPU time  
❌ **No JDBC** - Blocking I/O kills the event loop  
❌ **No synchronized lock contention** - Causes virtual thread pinning  
❌ **No blocking file I/O** - Stalls reactive pipelines  
❌ **No thread pools for reactive pipelines** - Defeats non-blocking purpose  

### What You MUST Use Instead

✅ **Event Loops** - Reuse threads efficiently  
✅ **Zero-Copy** - Avoid heap allocations  
✅ **Async Protocols** - Non-blocking I/O everywhere  
✅ **Edge Load Shedding** - Fail fast under overload  
✅ **Distributed Caching** - Minimize database hits  

---

## 🏗️ Architecture Philosophy

### 1.1 The Core Runtime: Spring WebFlux + Reactor Netty

**Spring WebFlux** is built on **Reactor Netty**, a high-performance network engine using:

#### Key Technologies:
- **Linux Epoll (edge-triggered)** - Efficient event notification
- **DirectByteBuffer (off-heap zero-copy buffers)** - Avoids GC pressure
- **Event loop model** - Few threads handling massive concurrency

#### Why This Matters:

**At 1M RPS:**
- Thread-per-request collapses because context switches dominate CPU time
- Reactive loops reuse threads, minimizing scheduling
- Maximizes L1/L2/L3 cache locality

### 1.1.1 Netty Linux Native Transport Features

**EPOLL**
- Efficient for millions of concurrent file descriptor events

**EPOLL Edge Triggered Mode**
- Event-driven; avoids repeated polling
- More efficient than level-triggered in extreme concurrency

**TCP_FASTOPEN**
- Reduces round trips for new connections
- Avoids waiting for 3-way handshake

**Zero-Copy Buffering**
- Uses DirectByteBuffer
- Avoids moving data into heap
- Drastically reduces GC pressure

**SO_REUSEPORT**
- Allows multiple Netty acceptor threads
- Eliminates accept lock bottleneck

**SO_BACKLOG**
- Queue for pending connections
- Default = 128 → catastrophic at 1M RPS
- Must increase to 65535+

### 1.2 Data Persistence Layer: R2DBC + Reactive Redis

**The Problem with JDBC:**
- JDBC blocks the event loop → kills the entire pipeline
- One blocking call can lose 25k RPS

**The Solution:**

#### R2DBC (Reactive Relational Database Connectivity)
- Non-blocking, async SQL pipeline
- Uses cursor-based streaming with backpressure
- PostgreSQL becomes write-durable datastore

#### Redis Reactive (Lettuce)
- Async I/O
- Pipelining support
- Connection multiplexing
- Cluster-aware
- Redis becomes primary hot-read datastore

### Virtual Threads vs Reactive: The Verdict

| Aspect | Virtual Threads | Reactive |
|--------|----------------|----------|
| **Use Case** | Business applications | Raw scale networking |
| **Complexity** | Lower | Higher |
| **Performance at 1M RPS** | Good | Unmatched |
| **Learning Curve** | Easier | Steeper |

**Conclusion:**
- **Virtual Threads** ≈ excellent for "business apps"
- **Reactive** ≈ unmatched for raw scale networking

---

## 💻 Complete Technical Implementation

### 2.1 build.gradle - Dependency Strategy

```gradle
plugins {
    id 'org.springframework.boot' version '4.0.0'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
}

group = 'com.highperf'
version = '1.0.0'
sourceCompatibility = '21'

repositories {
    mavenCentral()
}

dependencies {
    // ✅ ONLY WebFlux - NO Servlet dependency
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    
    // ✅ Native Netty Epoll for Linux
    implementation 'io.netty:netty-transport-native-epoll:4.1.100.Final:linux-x86_64'
    
    // ✅ R2DBC for reactive PostgreSQL
    implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
    implementation 'org.postgresql:r2dbc-postgresql'
    
    // ✅ Reactive Redis with Lettuce
    implementation 'org.springframework.boot:spring-boot-starter-data-redis-reactive'
    implementation 'io.lettuce:lettuce-core'
    
    // ✅ BlockHound for blocking detection in event loops
    implementation 'io.projectreactor.tools:blockhound:1.0.8.RELEASE'
    
    // ✅ Prometheus metrics for Kubernetes HPA
    implementation 'io.micrometer:micrometer-registry-prometheus'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // Lombok for cleaner code
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'io.projectreactor:reactor-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

**Key Points:**
- **NO servlet dependency** - Only WebFlux
- **Native Netty Epoll** - Linux-specific optimization
- **BlockHound** - Detects blocking calls at runtime
- **Prometheus metrics** - For autoscaling triggers

### 2.2 Application Entry Point

```java
package com.highperf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.blockhound.BlockHound;
import reactor.netty.resources.LoopResources;

@SpringBootApplication
public class HighPerfApplication {

    static {
        // ✅ Install BlockHound EARLY - detects blocking in event loops
        BlockHound.install();
        
        // ✅ Set Reactor Netty tuning BEFORE Spring bootstraps
        System.setProperty("reactor.netty.ioWorkerCount", 
            String.valueOf(Runtime.getRuntime().availableProcessors() * 2));
        
        // ✅ Create custom LoopResources - fix event loop thread count
        LoopResources.create("event-loop", 
            Runtime.getRuntime().availableProcessors() * 2, 
            true);
    }

    public static void main(String[] args) {
        SpringApplication.run(HighPerfApplication.class, args);
    }
}
```

**Critical Optimizations:**
1. **BlockHound Installation** - Catches blocking calls that would kill performance
2. **Event Loop Thread Count** - Set to CPU cores × 2
3. **Early Initialization** - Configure before Spring context loads

### 2.3 Netty Configuration

```java
package com.highperf.config;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.server.HttpServer;

@Configuration
public class NettyConfig implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        factory.addServerCustomizers(httpServer -> 
            httpServer
                .runOn(reactor.netty.resources.LoopResources.create(
                    "netty-http",
                    Runtime.getRuntime().availableProcessors() * 2,
                    true
                ))
                .option(ChannelOption.SO_BACKLOG, 65535)        // ✅ Huge accept queue
                .option(ChannelOption.SO_REUSEADDR, true)       // ✅ Reuse addresses
                .option(EpollChannelOption.SO_REUSEPORT, true)  // ✅ Multi-acceptor threads
                .option(EpollChannelOption.TCP_FASTOPEN, 256)   // ✅ Skip handshake
                .option(ChannelOption.TCP_NODELAY, true)        // ✅ Disable Nagle
                .option(ChannelOption.SO_KEEPALIVE, true)       // ✅ Keep connections alive
                .option(EpollChannelOption.EPOLL_MODE, 
                    io.netty.channel.epoll.EpollMode.EDGE_TRIGGERED) // ✅ Edge-triggered epoll
        );
    }
}
```

**Each TCP Flag Explained:**

| Flag | Purpose | Impact at 1M RPS |
|------|---------|------------------|
| `SO_BACKLOG` | Queue for pending connections | Default 128 = catastrophic |
| `SO_REUSEPORT` | Multiple acceptor threads | Eliminates accept lock |
| `TCP_FASTOPEN` | Skip 3-way handshake | Reduces connection latency |
| `TCP_NODELAY` | Disable Nagle algorithm | Immediate packet send |
| `EPOLL_EDGE_TRIGGERED` | Event notification mode | More efficient at scale |

### 2.4 Reactive Controller

```java
package com.highperf.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class HighPerfController {

    @GetMapping("/health")
    public Mono<String> health() {
        // ✅ Mono<String> ensures event loop flows packets
        // ✅ No thread blocking
        return Mono.just("OK");
    }

    @PostMapping("/data")
    public Mono<String> processData(@RequestBody Mono<String> data) {
        // ✅ @RequestBody Mono<String> streams data (no preload)
        // ✅ Avoids buffering entire request in memory
        return data
            .map(String::toUpperCase)
            .doOnNext(d -> {
                // ✅ No blocking operations here
                // Process asynchronously
            });
    }

    @GetMapping("/stream")
    public Flux<String> streamData() {
        // ✅ Server-Sent Events or streaming response
        return Flux.range(1, 1000)
            .map(i -> "Data: " + i);
    }
}
```

**Key Points:**
- `Mono<String>` ensures event loop flows packets (no blocking)
- No DTO conversions unless necessary (reduces allocations)
- `@RequestBody Mono<String>` streams data without preloading
- All operations remain non-blocking

### 2.5 Redis Configuration

```java
package com.highperf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        
        // ✅ Use simple String serializers - avoid JSON parser overhead
        StringRedisSerializer serializer = new StringRedisSerializer();
        
        RedisSerializationContext<String, String> context = 
            RedisSerializationContext.<String, String>newSerializationContext(serializer)
                .key(serializer)
                .value(serializer)
                .hashKey(serializer)
                .hashValue(serializer)
                .build();
        
        // ✅ Lettuce multiplexing - thousands of concurrent ops on few connections
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
```

**Optimizations:**
- Simple string serializers (no JSON parsing overhead)
- Lettuce multiplexing (thousands of concurrent Redis ops on few connections)
- Non-blocking reactive template

### 2.6 Global Error Handler

```java
package com.highperf.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleException(Exception ex) {
        // ✅ Avoid stack trace creation (super expensive at scale)
        // ✅ JSON responses are lightweight
        // ✅ No blocking logging operations
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getMessage()
        );
        
        return Mono.just(ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error));
    }

    record ErrorResponse(int status, String message) {}
}
```

**Performance Improvements:**
- Avoid stack trace creation (expensive at scale)
- Lightweight JSON responses
- No blocking logging operations

### 2.7 application.yml Configuration

```yaml
spring:
  # ✅ WebFlux configuration
  webflux:
    base-path: /api

  # ✅ Redis configuration
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 200ms              # ✅ Fail fast
      lettuce:
        pool:
          max-active: 50
          max-idle: 20
          min-idle: 10
        cluster:
          refresh:
            adaptive: true

  # ✅ R2DBC configuration
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/highperf
    username: postgres
    password: postgres
    pool:
      initial-size: 10
      max-size: 30                # ✅ Small - connections are async & multiplexed
      max-idle-time: 30m

# ✅ Actuator configuration for metrics
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true  # ✅ P99 latency for HPA triggers

# ✅ Logging configuration
logging:
  level:
    root: WARN
    com.highperf: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

**Key Configurations:**
- Redis timeout 200ms (fail fast)
- R2DBC pool sizing small (connections are async & multiplexed)
- Actuator exposes P99 latency (for HPA scaling triggers)
- Minimal logging (reduces overhead)

---

## ⚙️ JVM Optimization for 1M RPS

### Why ZGC Generational?

**Traditional ZGC Issues:**
- Non-generational ZGC scans whole heap
- Wasteful for short-lived objects

**ZGC Generational Benefits (Java 21):**
- Optimizes short-lived objects
- Massive performance gain
- Ultra-low latency GC pauses

### Complete JVM Flags

```bash
java \
  # ✅ Use ZGC Generational (Java 21+)
  -XX:+UseZGC \
  -XX:+ZGenerational \
  
  # ✅ Heap sizing (adjust based on workload)
  -Xms8G -Xmx8G \
  
  # ✅ Off-heap memory for Netty DirectByteBuffers
  -XX:MaxDirectMemorySize=4G \
  
  # ✅ Pre-touch memory pages at startup
  -XX:+AlwaysPreTouch \
  
  # ✅ String deduplication
  -XX:+UseStringDeduplication \
  
  # ✅ Disable explicit GC calls
  -XX:+DisableExplicitGC \
  
  # ✅ Performance tuning
  -XX:+TieredCompilation \
  -XX:TieredStopAtLevel=1 \
  
  # ✅ GC logging for analysis
  -Xlog:gc*:file=/var/log/gc.log:time,level,tags \
  
  # ✅ Enable JFR for profiling
  -XX:StartFlightRecording=disk=true,maxsize=1G,dumponexit=true \
  
  -jar high-perf-app.jar
```

### JVM Tuning Impact

| Parameter | Purpose | Impact |
|-----------|---------|--------|
| `ZGenerational` | Young/old generation split | 30-50% GC improvement |
| `AlwaysPreTouch` | Pre-allocate memory | No page faults during runtime |
| `MaxDirectMemorySize` | Off-heap buffers | Reduces GC pressure from Netty |
| `TieredCompilation` | JIT optimization | Faster startup, better peak perf |

---

## 🐧 Linux Kernel Tuning

### Why OS Tuning Matters

**At 1M RPS, the JVM is NOT the bottleneck.**

The Linux kernel network stack collapses before the application is even reached.

### 4.1 sysctl.conf Critical Parameters

```bash
# ✅ File descriptor limits
fs.file-max = 2000000                    # 2 million FDs (Netty connections = FDs)
fs.nr_open = 2000000

# ✅ TCP connection tuning
net.core.somaxconn = 65535               # Allow many pending accepts
net.ipv4.tcp_max_syn_backlog = 65535     # SYN queue size
net.core.netdev_max_backlog = 65535      # Device queue size

# ✅ TCP connection reuse (critical for outbound connections)
net.ipv4.tcp_tw_reuse = 1                # Reuse TIME_WAIT sockets
net.ipv4.tcp_fin_timeout = 15            # Reduce FIN_WAIT timeout

# ✅ TCP Fast Open
net.ipv4.tcp_fastopen = 3                # Enable for both client and server

# ✅ Buffer sizes for high throughput
net.core.rmem_max = 134217728            # 128MB receive buffer
net.core.wmem_max = 134217728            # 128MB send buffer
net.core.rmem_default = 16777216         # 16MB default receive
net.core.wmem_default = 16777216         # 16MB default send

net.ipv4.tcp_rmem = 4096 87380 134217728 # TCP receive buffer (min, default, max)
net.ipv4.tcp_wmem = 4096 65536 134217728 # TCP send buffer (min, default, max)

# ✅ Increase local port range
net.ipv4.ip_local_port_range = 1024 65535

# ✅ TCP keepalive settings
net.ipv4.tcp_keepalive_time = 600
net.ipv4.tcp_keepalive_intvl = 60
net.ipv4.tcp_keepalive_probes = 3

# ✅ Disable slow start after idle
net.ipv4.tcp_slow_start_after_idle = 0

# ✅ Enable TCP window scaling
net.ipv4.tcp_window_scaling = 1

# ✅ Increase maximum orphaned sockets
net.ipv4.tcp_max_orphans = 262144

# Apply settings
sudo sysctl -p
```

**Critical Parameters Explained:**

| Parameter | Value | Why It Matters |
|-----------|-------|----------------|
| `fs.file-max` | 2,000,000 | Each connection = file descriptor |
| `somaxconn` | 65,535 | Pending accept queue size |
| `tcp_tw_reuse` | 1 | Reuse TIME_WAIT sockets for outbound |
| `rmem/wmem` | 128MB | High-throughput packet buffers |
| `tcp_fastopen` | 3 | Skip TCP handshake overhead |

### 4.2 NIC Tuning

**The NIC is often the ultimate bottleneck.**

```bash
# ✅ Enable RSS (Receive-Side Scaling)
# Distributes NIC interrupts across CPU cores
ethtool -L eth0 combined 16

# ✅ Increase ring buffer sizes
ethtool -G eth0 rx 4096 tx 4096

# ✅ Enable RPS (Receive Packet Steering)
# Software fallback if hardware queues are limited
echo "ff" > /sys/class/net/eth0/queues/rx-0/rps_cpus

# ✅ IRQ Pinning - bind NIC interrupts to specific CPU cores
# Avoids cache thrashing
echo "1" > /proc/irq/45/smp_affinity  # Pin to CPU 0
echo "2" > /proc/irq/46/smp_affinity  # Pin to CPU 1

# ✅ Disable interrupt coalescing for low latency
ethtool -C eth0 rx-usecs 0 tx-usecs 0

# ✅ Enable hardware offloading
ethtool -K eth0 tso on gso on gro on
```

**NIC Optimization Impact:**
- **RSS**: Distributes load across CPU cores (4-8x improvement)
- **IRQ Pinning**: Prevents cache thrashing (reduces jitter)
- **Ring buffers**: Prevents packet drops under burst traffic

---

## ⚖️ Load Balancing Architecture

### The Problem

**A single load balancer cannot handle 1M RPS end-to-end.**

### The Solution: L4 → L7 → App Model

```
Internet → AWS NLB (L4) → NGINX/Envoy (L7) → Spring Boot Pods
         [TCP/UDP]        [HTTP routing]      [Application]
```

### 5.1 AWS Network Load Balancer (NLB)

**Capabilities:**
- Handles millions of concurrent TCP connections
- Does not inspect HTTP (operates at Layer 4)
- Lowest possible latency (~microseconds)
- Auto-scales automatically
- Preserves source IP

**Configuration:**
```yaml
# NLB Target Group
Health Check:
  Protocol: TCP
  Port: 8080
  Interval: 10s
  Healthy Threshold: 2
  Unhealthy Threshold: 2

Connection Settings:
  Deregistration Delay: 30s
  Connection Idle Timeout: 350s
```

### 5.2 NGINX / Envoy (L7)

**Purpose:**
- HTTP routing logic
- Header filtering
- Rate limiting
- Circuit breaking

**NGINX Configuration:**

```nginx
http {
    upstream backend {
        least_conn;  # Connection-based load balancing
        
        # ✅ Keepalive pool - avoid TCP handshakes
        keepalive 1024;
        keepalive_requests 10000;
        keepalive_timeout 75s;
        
        server pod-1:8080 max_fails=3 fail_timeout=30s;
        server pod-2:8080 max_fails=3 fail_timeout=30s;
        # ... more pods
    }
    
    server {
        listen 80;
        
        # ✅ Multi-accept + epoll handles huge concurrency
        multi_accept on;
        accept_mutex off;
        
        location / {
            proxy_pass http://backend;
            
            # ✅ Connection reuse
            proxy_http_version 1.1;
            proxy_set_header Connection "";
            
            # ✅ Timeouts
            proxy_connect_timeout 2s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
            
            # ✅ Buffering
            proxy_buffering off;
            proxy_request_buffering off;
        }
    }
}

events {
    worker_connections 10000;
    use epoll;
    multi_accept on;
}
```

**Envoy Alternative:**
- Service mesh integration
- Advanced traffic management
- Better observability
- gRPC support

---

## ☸️ Kubernetes Deployment Strategy

### Resource Calculation

**Each Pod handles ~10k RPS**
- Need **100 pods** for 1M RPS
- With 30% buffer: **130 pods**

### Deployment Configuration

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: high-perf-app
  namespace: production
spec:
  replicas: 100  # Start with 100, HPA will adjust
  
  selector:
    matchLabels:
      app: high-perf-app
  
  template:
    metadata:
      labels:
        app: high-perf-app
        version: v1.0.0
    
    spec:
      # ✅ Anti-affinity - spread pods across nodes
      affinity:
        podAntiAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
          - labelSelector:
              matchExpressions:
              - key: app
                operator: In
                values:
                - high-perf-app
            topologyKey: kubernetes.io/hostname
      
      containers:
      - name: app
        image: high-perf-app:1.0.0
        
        # ✅ Guaranteed QoS - prevents CPU throttling
        resources:
          requests:
            memory: "8Gi"
            cpu: "4"
          limits:
            memory: "8Gi"
            cpu: "4"
        
        ports:
        - containerPort: 8080
          protocol: TCP
        
        # ✅ Liveness and readiness probes
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 2
        
        env:
        - name: JAVA_OPTS
          value: >-
            -XX:+UseZGC
            -XX:+ZGenerational
            -Xms8G -Xmx8G
            -XX:MaxDirectMemorySize=4G
            -XX:+AlwaysPreTouch
        
        # ✅ Volume mounts for logs
        volumeMounts:
        - name: logs
          mountPath: /var/log
      
      volumes:
      - name: logs
        emptyDir: {}
---
# ✅ Horizontal Pod Autoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: high-perf-app-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: high-perf-app
  
  minReplicas: 100
  maxReplicas: 200
  
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  
  # ✅ Custom metric - P99 latency
  - type: Pods
    pods:
      metric:
        name: http_server_requests_seconds_p99
      target:
        type: AverageValue
        averageValue: "100m"  # 100ms P99 threshold
  
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
---
# ✅ Service
apiVersion: v1
kind: Service
metadata:
  name: high-perf-app-service
  namespace: production
spec:
  type: LoadBalancer
  selector:
    app: high-perf-app
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  sessionAffinity: None
```

**Key Configuration Points:**

| Setting | Value | Purpose |
|---------|-------|---------|
| **Anti-affinity** | Required | Spread pods across nodes (avoid NIC bottleneck) |
| **QoS** | Guarantee