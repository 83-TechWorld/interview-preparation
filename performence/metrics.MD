# 🚀 Application Performance Metrics

Monitoring **application performance** is crucial for ensuring scalability, reliability, and user satisfaction.  
This document outlines the **key performance metrics**, **tools**, **frameworks**, and **steps** to measure them effectively.

---

| **Category**                      | **Metric**                                      | **Description / Purpose**                          | **Tools**                         |
| --------------------------------- | ----------------------------------------------- | -------------------------------------------------- | --------------------------------- |
| **Latency & Response**            | `p50`, `p95`, `p99`                             | How long each request takes (median, tail latency) | Prometheus, k6, JMeter            |
| **Throughput**                    | `requests_per_second`                           | How many requests the app can handle per second    | Prometheus, Grafana               |
| **Error Rate**                    | `% of 4xx / 5xx responses`                      | Detects broken APIs or downstream failures         | Grafana, SigNoz                   |
| **Availability (Uptime)**         | % uptime per period                             | Reliability of the service                         | Uptime Kuma, Grafana              |
| **Resource Utilization**          | CPU, Memory, Disk I/O, Network                  | Detects overloaded containers/nodes                | Node Exporter, cAdvisor           |
| **Database Metrics**              | Query latency, Connection pool usage, Deadlocks | Bottlenecks in DB layer                            | pgBadger, Prometheus SQL Exporter |
| **Cache Metrics**                 | Hit ratio, Evictions                            | Efficiency of caching                              | Redis Exporter                    |
| **Queue Metrics**                 | Queue length, Consumer lag                      | Message broker health (Kafka, RabbitMQ)            | Kafka Exporter                    |
| **Thread Pool / Connection Pool** | Active vs. idle threads/connections             | Identifies exhaustion or blocking                  | Micrometer, Spring Actuator       |
| **GC Metrics**                    | Pause time, Frequency                           | Measures JVM memory pressure                       | GCViewer, Prometheus JMX          |
| **Service-Level Metrics**         | SLA, SLO, SLI                                   | Business-level KPIs derived from raw metrics       | Grafana, New Relic, SigNoz        |


## 📊 1. Core Application Performance Metrics

| **Metric** | **Description** | **Example Tools** | **Testing Type** |
|-------------|----------------|-------------------|------------------|
| **Response Time / Latency** | Time taken to complete a request (measured in ms or s). | JMeter, k6, Apache Benchmark | Load Testing |
| **Throughput (Requests per Second)** | Number of requests your system handles per second. | Gatling, Locust | Stress Testing |
| **Error Rate** | Percentage of failed requests or errors (HTTP 4xx, 5xx). | Prometheus + Grafana | Load / Soak Testing |
| **Apdex Score (Application Performance Index)** | User satisfaction metric (0.0 to 1.0). | New Relic, Datadog, SigNoz | Load Testing |
| **CPU & Memory Usage** | Measures compute and memory utilization. | cAdvisor, Grafana, Prometheus | Resource Monitoring |
| **Disk I/O & Network I/O** | Tracks read/write and data transfer rates. | Node Exporter, Telegraf | Stress / Endurance Testing |
| **Garbage Collection Time** | Time spent in memory cleanup (Java/.NET apps). | JVisualVM, Prometheus JMX Exporter | Performance Profiling |
| **Database Query Time** | Average query execution time, slow queries. | pgBadger, MySQLTuner, Jaeger | Database Load Test |
| **Concurrency / Active Users** | Number of simultaneous users supported. | JMeter, k6 | Scalability Test |
| **Uptime / Availability** | Application reliability (e.g., 99.9% uptime). | Uptime Kuma, Grafana OnCall | Synthetic Monitoring |

---

## 🧩 2. Real-World Metrics (Used in Industry)

| **Metric** | **Target Value (Typical)** | **Example** |
|-------------|---------------------------|--------------|
| Response Time | < 200ms (API) | Banking/Fintech Apps |
| Error Rate | < 1% | E-commerce Websites |
| Throughput | 1000+ req/sec | Social Media / Streaming Platforms |
| Uptime | 99.99% | SaaS / Cloud Applications |
| CPU Utilization | < 70% | Production Microservices |
| Memory Usage | < 80% | ML/AI Workloads |
| Apdex Score | > 0.85 | B2C Web Portals |

---

## 🧠 3. Recommended Open Source Tools

| **Category** | **Tool** | **Purpose** |
|---------------|-----------|--------------|
| Performance Testing | [Apache JMeter](https://jmeter.apache.org/) | Load & stress testing |
| Performance Scripting | [k6](https://k6.io/) | Modern load testing with JavaScript |
| Monitoring | [Prometheus](https://prometheus.io/) + [Grafana](https://grafana.com/) | Metrics storage + visualization |
| Tracing | [Jaeger](https://www.jaegertracing.io/) | Distributed tracing |
| APM Alternative | [SigNoz](https://signoz.io/) | Open-source New Relic/Datadog alternative |
| Infrastructure Metrics | [cAdvisor](https://github.com/google/cadvisor) | Docker container resource metrics |
| Uptime Monitoring | [Uptime Kuma](https://github.com/louislam/uptime-kuma) | Self-hosted status page |

---

## ⚙️ 4. How to Measure Metrics — Step-by-Step

### Step 1: Load & Stress Testing
- Use **JMeter** or **k6** to simulate concurrent users.
- Example (k6):
  ```bash
  k6 run loadtest.js


# ⚙️ Advanced Performance Metrics

Understanding advanced performance metrics helps engineering teams pinpoint **bottlenecks, latency spikes**, and **user dissatisfaction** before they escalate into production issues.  
This section explains **Percentile Response Times**, **Apdex Score**, and **Garbage Collection Pause Time** — how to measure, analyze, and optimize them.

---

## 🔹 1. Percentile Response Time (p95, p99)

### 💡 What It Means
Percentiles show how fast your application responds for most users.

- **p95 (95th Percentile):** 95% of requests are faster than this time, 5% are slower.  
- **p99 (99th Percentile):** 99% of requests are faster, only 1% are slower.

These help detect **tail latency** — slow outliers that degrade user experience.

### 🧮 Example

| Percentile | Response Time | Interpretation |
|-------------|----------------|----------------|
| p50 | 150 ms | 50% of requests finish under 150ms |
| p95 | 400 ms | 95% finish under 400ms |
| p99 | 700 ms | 99% finish under 700ms |

If p99 >> p95, there’s likely a **performance outlier** due to:
- Garbage collection
- Slow queries
- Thread or I/O blocking

### 🔍 How to Measure

| Tool | How to Find Percentile |
|------|-------------------------|
| **JMeter** | View “95%” and “99%” columns in the Summary Report |
| **k6** | Auto output: `http_req_duration{p(95)}` |
| **Prometheus + Grafana** | Query:<br>`histogram_quantile(0.95, sum(rate(http_request_duration_seconds_bucket[5m])) by (le))` |
| **SigNoz / Datadog / New Relic** | Built-in percentile charts |

### 📈 Industry Benchmarks

| Application Type | p95 Target | p99 Target |
|------------------|------------|------------|
| API Gateway | < 300 ms | < 500 ms |
| Web App | < 500 ms | < 800 ms |
| Financial / Trading | < 150 ms | < 300 ms |

---

## 🔹 2. Apdex Score (Application Performance Index)

### 💡 What It Means
Apdex converts raw response times into a single **user satisfaction score** (0.0 to 1.0).

### 🧮 Formula


You define a threshold **T** (acceptable response time).

### ⚙️ Example

If T = 1s and out of 100 requests:
- 80 ≤ 1s (Satisfied)
- 10 between 1–4s (Tolerating)
- 10 > 4s (Frustrated)

Then:
Apdex = (80 + (10/2)) / 100 = 0.85


### 🧠 Interpretation

| Apdex Score | User Experience |
|--------------|----------------|
| 0.94 – 1.00 | Excellent |
| 0.85 – 0.94 | Good |
| 0.70 – 0.85 | Fair |
| < 0.70 | Poor |

### 🧰 How to Measure

| Tool | Method |
|------|--------|
| **New Relic / Datadog / SigNoz** | Automatically computed |
| **Grafana + Prometheus** | Define custom thresholds using PromQL |
| **k6** | Define test thresholds: `thresholds: { http_req_duration: ['p(95)<500'] }` |

---

## 🔹 3. Garbage Collection (GC) Pause Time

### 💡 What It Means
In languages like **Java**, **.NET**, and **Python**, Garbage Collection reclaims unused memory.  
However, it can **pause the application**, temporarily freezing threads and increasing latency.

**GC Pause Time** measures how long the app pauses during these events.

### 🧩 Key Metrics

| Metric | Description |
|---------|-------------|
| `gc_pause_time_seconds` | Duration of each GC pause |
| `gc_pause_count` | Number of GC events |
| `heap_usage_before_gc` / `after_gc` | Memory reclaimed |

### 🧰 How to Measure

| Environment | Method |
|--------------|--------|
| **Java (JVM)** | Enable GC logs:<br>`-Xlog:gc*:file=gc.log:time,uptime,level,tags`<br>Analyze using [GCEasy.io](https://gceasy.io/) or [GCViewer](https://github.com/chewiebug/GCViewer)` |
| **Micrometer + Prometheus** | Use `/actuator/prometheus` endpoint to expose `jvm_gc_pause_seconds` metrics |
| **JVisualVM / JConsole** | View live GC events visually |
| **Python** | Use built-in module:<br>`import gc; print(gc.get_stats())` |
| **Node.js** | Run app with `--trace_gc` or monitor via [Clinic.js](https://clinicjs.org/) |

### 📊 Grafana Query Example (JVM)
```promql
sum(rate(jvm_gc_pause_seconds_sum[5m])) / sum(rate(jvm_gc_pause_seconds_count[5m]))

| Metric                    | Purpose                     | Affects                       |
| ------------------------- | --------------------------- | ----------------------------- |
| **p95/p99 Response Time** | Shows tail latency          | Influenced by GC, DB, network |
| **Apdex Score**           | User satisfaction index     | Drops when p95 increases      |
| **GC Pause Time**         | JVM memory management delay | Causes p99 spikes             |


┌─────────────────────────────────────────────┐
│                Microservices                │
│ (API, Auth, DB, Cache, Payment, Notification)│
└─────────────┬────────────┬────────────┬──────┘
              │            │            │
        ┌─────▼────┐  ┌────▼────┐  ┌────▼────┐
        │ Micrometer│  │ NodeExporter││ DB Exporter│
        └─────┬────┘  └────┬────┘  └────┬────┘
              │            │            │
              ▼            ▼            ▼
          ┌──────────────────────────────┐
          │         Prometheus           │
          │  (Scrapes /metrics endpoint) │
          └────────────┬─────────────────┘
                       ▼
                ┌─────────────┐
                │   Grafana   │
                │(Dashboards) │
                └─────────────┘


| Feature                   | **Jaeger**                              | **Zipkin**                         |
| ------------------------- | --------------------------------------- | ---------------------------------- |
| **Origin**                | Uber                                    | Twitter                            |
| **CNCF Project**          | ✅ Yes                                   | ❌ No                              |
| **OpenTelemetry Support** | ✅ Native                                | ⚠️ Partial                         |
| **Scalability**           | 🚀 Excellent (Production-grade)         | ⚙️ Moderate                         |
| **Architecture**          | Agent + Collector + Query               | Single server or Collector + Query |
| **Storage**               | Elasticsearch, Cassandra, Kafka, Badger | MySQL, Cassandra, Elasticsearch    |
| **UI**                    | Modern, detailed trace analysis         | Simpler, lightweight               |
| **Deployment**            | Multiple components (K8s ready)         | Single JAR or Docker image         |
| **Performance**           | High throughput                         | Lightweight but limited            |
| **Integration**           | Prometheus, Grafana, OpenTelemetry      | Spring Cloud Sleuth, Brave         |
| **Use Case**              | Large-scale, distributed microservices  | Small-to-medium microservices      |