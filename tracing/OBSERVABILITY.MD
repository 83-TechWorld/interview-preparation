Nice — I’ll give you a complete, copy-pasteable **GitHub README** that explains concepts, shows how to wire metrics/logs/traces together, includes configs and examples (Spring Boot + Micrometer), Prometheus + Grafana, ELK + APM Server, index lifecycle management, and a Mermaid architecture diagram for the whole observability stack.

I include authoritative references for the main claims so you can deep-dive. Where it helps I note practical commands/configs to run in Kubernetes or locally.

> **Sources used**: Micrometer docs, Grafana docs (Prometheus datasource + provisioning), Elastic APM & ILM docs, Grafana provisioning docs. ([docs.micrometer.io][1])

---

# Observability for a Distributed System — README

> Contents
>
> 1. Goal & high level overview
> 2. Core concepts (metrics, logs, traces)
> 3. Micrometer in microservices (Spring Boot examples)
> 4. Prometheus + Grafana — how they work, how to set up
> 5. ELK stack (Elasticsearch, Logstash, Kibana) — logs, indices, ILM
> 6. Elastic APM Server and distributed tracing (how it fits with ELK)
> 7. End-to-end monitoring approach (how to correlate metrics, logs, traces)
> 8. Operations & scaling notes, dashboards, alerts, best practices
> 9. Mermaid architecture diagram
> 10. Quick reference commands & sample configs

---

## 1. Goal & high level overview

Provide full observability: **metrics** (Prometheus), **visualization/alerts** (Grafana), **logs** (Filebeat → Logstash → Elasticsearch → Kibana), **traces / APM** (Elastic APM or OpenTelemetry → APM Server → Elasticsearch → Kibana). Combine metrics, logs, and traces to diagnose latency, errors, and resource issues across distributed services.

High-level: metrics for real-time health and alerts; logs for forensic detail; distributed traces for request flows and hotspots. ([Grafana Labs][2])

---

## 2. Core concepts (short)

* **Metrics**: numeric time-series (counters, gauges, histograms). Collected via Micrometer in Spring Boot and scraped by Prometheus. ([docs.micrometer.io][1])
* **Logs**: unstructured or structured event records. Ship with Beats/Logstash into Elasticsearch indices and query in Kibana.
* **Traces (APM)**: spans representing a request path across services. Use Elastic APM or OpenTelemetry agents; APM Server transforms events to ES documents. ([Elastic][3])

---

## 3. Micrometer in microservices (Spring Boot)

**What is Micrometer?**
Micrometer is the metrics façade used by Spring Boot Actuator to expose metrics in a vendor-neutral way. It supports many backends (Prometheus, Graphite, Datadog, etc.). Use Micrometer to instrument JVM metrics, HTTP requests, DB calls, custom timers and counters. ([docs.micrometer.io][1])

### Maven dependencies (pom.xml)

```xml
<!-- Spring Boot + Actuator + Micrometer Prometheus -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### application.yml

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics,trace
  endpoint:
    prometheus:
      enabled: true
  metrics:
    enable:
      all: true
```

### Expose Prometheus endpoint

Spring Boot actuator exposes `/actuator/prometheus` once `micrometer-registry-prometheus` is on the classpath. Prometheus will scrape that endpoint. Example custom metric:

```java
@Autowired
MeterRegistry registry;

public void recordRequest(Duration d) {
  Timer.builder("myapp.request.latency")
      .description("Request latency")
      .register(registry)
      .record(d);
}
```

(Official docs: Micrometer + Spring Boot.) ([docs.micrometer.io][1])

---

## 4. Prometheus + Grafana — how they work & how to set up

### Roles

* **Prometheus**: scrapes HTTP endpoints (pull model), stores time-series, has Alertmanager for alerts.
* **Grafana**: visualization layer; uses Prometheus as a datasource to build dashboards, alerts and annotations. Grafana does **not** collect metrics (it visualizes them). They’re complementary. ([Grafana Labs][2])

### Minimal Prometheus `prometheus.yml`

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'spring-apps'
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['app-1:8080','app-2:8080']
```

For Kubernetes, use ServiceMonitor (Prometheus Operator) or `kubernetes_sd_configs`.

### Example Prometheus alert rule

```yaml
groups:
- name: example-alert
  rules:
  - alert: HighErrorRate
    expr: increase(http_server_requests_seconds_count{status=~"5.."}[5m]) > 10
    for: 2m
    labels:
      severity: page
    annotations:
      summary: "High 5xx rate"
```

### Grafana setup

1. Add Prometheus as a datasource in Grafana (UI or provisioning YAML).
2. Create dashboards (Panel → query with PromQL). Grafana supports dashboard provisioning via YAML (GitOps). ([Grafana Labs][4])

### Grafana provisioning example (datasource)

`provisioning/datasources/prometheus.yml`:

```yaml
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
```

Create dashboards via `provisioning/dashboards` or Grafana HTTP API to automate across environments. ([Grafana Labs][5])

---

## 5. ELK stack — logs, indices, index lifecycle

**ELK** = Elasticsearch (store + index), Logstash (ingest transforms), Kibana (visualize). Filebeat often used on hosts / containers to forward logs to Logstash/ES.

### Filebeat → Logstash → Elasticsearch flow

* Filebeat tails container/host logs and forwards to Logstash or ES.
* Logstash applies parsing (grok), enrichments, outputs to Elasticsearch index templates.
* Kibana points to indices to visualize logs.

### What is an **index** in ELK?

An index is a logical namespace in Elasticsearch (like a SQL table) that stores documents. Indices are created via ingestion (Logstash/Filebeat) or explicitly. Management of indices (rollover, retention) is via **Index Lifecycle Management (ILM)**. ([Elastic][6])

### Example Logstash pipeline (`/etc/logstash/conf.d/logs.conf`)

```conf
input {
  beats {
    port => 5044
  }
}

filter {
  if [fileset][module] == "nginx" {
    grok { match => { "message" => "%{NGINXACCESS}" } }
  }
  date { match => [ "timestamp", "ISO8601" ] }
}

output {
  elasticsearch {
    hosts => ["http://elasticsearch:9200"]
    index => "logs-%{+YYYY.MM.dd}"
    ilm_enabled => true
  }
}
```

### ILM (Index Lifecycle Management) example

Create an ILM policy that rolls indices from hot → warm → cold → delete:

```bash
PUT _ilm/policy/logs_policy
{
  "policy": {
    "phases": {
      "hot": {
        "actions": {
          "rollover": { "max_size": "50gb", "max_age": "7d" }
        }
      },
      "delete": {
        "min_age": "30d",
        "actions": { "delete": {} }
      }
    }
  }
}
```

Apply policy via index template (or data stream). ILM prevents disk exhaustion and controls retention. ([Elastic][6])

---

## 6. Elastic APM Server & distributed tracing

**Elastic APM** collects traces (transactions + spans) via language agents (Java, Node, Python, Go). Agents send data to **APM Server** which validates, transforms, and stores in Elasticsearch; visualize via Kibana APM UI. APM Server can also accept OTLP (OpenTelemetry) data depending on setup. ([Elastic][3])

### Flow

```
Application (APM agent) -> APM Server -> Elasticsearch indices -> Kibana (APM UI)
```

### Example (Java) agent

Add the Elastic APM Java agent to startup:

```bash
java -javaagent:/path/elastic-apm-agent.jar \
 -Delastic.apm.server_urls=http://apm-server:8200 \
 -Delastic.apm.service_name=my-service \
 -jar myapp.jar
```

APM Server configuration (apm-server.yml) points to Elasticsearch and may be run as a service or in Kubernetes.

### How APM integrates with logs & metrics

* APM traces include service/transaction IDs which you can include in logs (via MDC) to correlate logs to traces.
* APM + ELK gives traces and the underlying logs in Kibana for the same transaction. Use trace id in logs for correlation.

---

## 7. End-to-end monitoring & correlation (practical)

To monitor full E2E service communication and correlate across metrics/logs/traces:

1. **Instrument services**:

   * Metrics: Micrometer -> /actuator/prometheus (Prometheus scrape).
   * Traces: Elastic APM / OpenTelemetry -> APM Server.
   * Logs: structured JSON logs with fields like `service.name`, `trace.id`, `span.id`, `env`, and `request.id`.

2. **Include trace IDs in logs**: configure log formatter (Logback/Log4j) to include `%X{trace.id}` via MDC. This allows Kibana/Logstash to filter logs by trace id.

3. **Use a single service naming & tag convention**: `service.name`, `environment`, `instance`, `pod`.

4. **Dashboards**:

   * Grafana: metrics-based dashboards (latency, error rate, CPU, memory).
   * Kibana APM & Logs: traces and logs side-by-side.
   * Link from Grafana panels to trace view or Kibana via template variables and links.

5. **Alerts**:

   * Prometheus Alertmanager for metrics alerts (pager duty).
   * Kibana watcher or Elasticsearch alerts for log-based alerts or anomalies.
   * Grafana Alerting can integrate with Prometheus and other sources.

---

## 8. How Prometheus differs from Grafana (quick)

* **Prometheus**: metric acquisition, storage, query (PromQL), and alerting (via Alertmanager).
* **Grafana**: visualization & dashboarding layer; queries Prometheus (or many other datasources) and renders dashboards and dashboards-based alerts. They are almost always used together. ([Grafana Labs][2])

---

## 9. How to create new Grafana dashboards (programmatic / GitOps)

* **Manual**: UI → New Dashboard → Add Panels → Save.
* **Programmatic**:

  * Use Grafana HTTP API (create/update JSON).
  * Use provisioning (YAML files under `provisioning/dashboards` and `provisioning/datasources`) to ship dashboards with code (recommended for CI/CD). ([Grafana Labs][7])

Example provisioning for dashboards:

```yaml
apiVersion: 1
providers:
- name: 'default'
  orgId: 1
  folder: ''
  type: file
  options:
    path: /var/lib/grafana/dashboards
```

Place JSON files there and Grafana will auto-import.

---

## 10. Monitoring Spring Boot traces/memory/CPU

* **Traces**: Elastic APM Java agent auto-instruments HTTP requests, JDBC, messaging. Add agent to JVM. Use APM UI in Kibana. ([Elastic][3])
* **JVM metrics**: Micrometer exposes `jvm.memory.used`, `jvm.gc.pause`, `process.cpu.*` → scrape via Prometheus.
* **OS / process metrics**: node_exporter (for host-level metrics) or cAdvisor (container metrics) → Prometheus.
* **Heap dumps & profiling**: use async-profiler, JFR (Java Flight Recorder) when needed; export metrics to monitor memory pressure.

---

## 11. APM Server & how it should work (operational)

* APM agents send traces to **APM Server** (HTTP). APM Server validates and transforms to Elasticsearch documents. You can use Fleet-managed APM Server (Elastic Cloud) or self-host. APM Server also supports ingest pipelines and agent configuration. ([Elastic][8])

Key configuration items:

* `apm-server.yml`: Elasticsearch output, secret token, heap size, pipelines.
* Security: enable TLS between agents and APM Server; secure ES access.
* Retention: APM indices also use ILM for rollover and retention.

---

## 12. Best practices & scaling

* **Metric cardinality**: low-cardinality labels only. High-cardinality labels explode Prometheus TSDB.
* **Index lifecycle**: use ILM and data streams to control retention and costs. Use warm/cold nodes for older data. ([Elastic][6])
* **Alerting strategy**: limit noisy alerts; use aggregation windows and `for` clauses.
* **Correlation**: enforce trace id in logs; put linkbacks from dashboards to Kibana trace.
* **Kubernetes**: use Prometheus Operator and Elastic operator / ECK for simplified deployments.
* **Security**: secure endpoints, use RBAC (Kibana/Grafana), enable TLS, rotate credentials.

---

## 13. Mermaid architecture (paste into README; GitHub renders it)

```mermaid
flowchart LR
  subgraph Metrics
    A[Spring Boot + Micrometer] -->|exposes /actuator/prometheus| P[Prometheus Scrape]
    HostExporter[node_exporter / cAdvisor] --> P
  end

  subgraph Traces
    Svc1[Service A (APM agent)] -->|span| APM[APM Server]
    Svc2[Service B (APM agent)] -->|span| APM
  end

  subgraph Logs
    AppLogs[App logs (JSON)] --> FB[Filebeat / Fluentd]
    FB --> LS[Logstash]
    LS --> ES[Elasticsearch]
  end

  P --> Grafana[Grafana Dashboards]
  ES --> Kibana[Kibana / APM UI]
  APM --> ES
  Grafana -->|link| Kibana
  Grafana -->|datasource| Prometheus
  Kibana -->|search| ES
```

---

## 14. Quick reference: commands & example files

### Prometheus (docker-compose snippet)

```yaml
prometheus:
  image: prom/prometheus
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml
  ports:
    - "9090:9090"
```

### Grafana (docker-compose + provisioning)

```yaml
grafana:
  image: grafana/grafana
  volumes:
    - ./provisioning:/etc/grafana/provisioning
  ports:
    - "3000:3000"
```

### Run APM Server (docker)

```bash
docker run -d --name apm-server \
  -e ELASTICSEARCH_HOST=elasticsearch:9200 \
  -e APM_SECRET_TOKEN=changeme \
  -p 8200:8200 \
  docker.elastic.co/apm/apm-server:8.0.0
```

### Create ILM policy (curl)

```bash
curl -X PUT "http://elasticsearch:9200/_ilm/policy/logs_policy" -H 'Content-Type: application/json' -d'
{ "policy": { "phases": { "hot": {"actions": {"rollover": {"max_age": "7d", "max_size": "50gb"}} }, "delete": {"min_age": "30d","actions": {"delete": {}}} } } }'
```

---

## 15. Checklist to make it work (practical rollout plan)

1. Instrument all services: Micrometer + APM agent + structured logging (JSON) with trace ID.
2. Deploy Prometheus (or Prometheus Operator) and configure scrape targets (service discovery in k8s). ([Grafana Labs][2])
3. Deploy Grafana; provision Prometheus datasource + dashboards via YAML. ([Grafana Labs][5])
4. Deploy Filebeat / Fluentd and Logstash; set index templates and ILM policy in Elasticsearch. ([Elastic][6])
5. Deploy APM Server and configure agents to point to it. Test that traces arrive in Kibana APM. ([Elastic][8])
6. Create Prometheus alerting rules + connect to Alertmanager; configure receiver (PagerDuty/Slack).
7. Add Grafana dashboards that combine metrics and link to Kibana traces for context.
8. Tune retention and index lifecycle. Audit cardinality and reduce label explosion. ([Elastic][6])

---

## 16. Further reading (official docs)

* Micrometer reference (Prometheus implementation). ([docs.micrometer.io][1])
* Grafana + Prometheus getting started. ([Grafana Labs][2])
* Elastic APM & APM Server docs. ([Elastic][3])
* Elasticsearch Index Lifecycle Management (ILM). ([Elastic][6])
* Grafana provisioning & dashboard API. ([Grafana Labs][5])

---

If you want, I can do any (pick one) of the following *right now* and produce runnable artifacts you can drop into your repo:

* A ready `observability/` folder with `prometheus.yml`, Grafana provisioning files, and `docker-compose.yml`.
* A Spring Boot sample app `application.yml` + `Dockerfile` instrumented with Micrometer + Elastic APM agent.
* A Logstash pipeline + Filebeat config + ILM policy curl file.
* A Grafana dashboard JSON modeled for JVM + HTTP latency.

Which one should I generate first? (I’ll produce the full files you can copy into a repo.)

[1]: https://docs.micrometer.io/micrometer/reference/implementations/prometheus.html?utm_source=chatgpt.com "Micrometer Prometheus"
[2]: https://grafana.com/docs/grafana/latest/getting-started/get-started-grafana-prometheus/?utm_source=chatgpt.com "Get started with Grafana and Prometheus"
[3]: https://www.elastic.co/docs/solutions/observability/apm?utm_source=chatgpt.com "Application performance monitoring (APM) | Elastic Docs"
[4]: https://grafana.com/docs/grafana/latest/datasources/prometheus/configure/?utm_source=chatgpt.com "Configure the Prometheus data source"
[5]: https://grafana.com/docs/grafana/latest/administration/provisioning/?utm_source=chatgpt.com "Provision Grafana | Grafana documentation"
[6]: https://www.elastic.co/docs/manage-data/lifecycle/index-lifecycle-management?utm_source=chatgpt.com "Index lifecycle management | Elastic Docs"
[7]: https://grafana.com/docs/grafana/latest/dashboards/build-dashboards/create-dashboard/?utm_source=chatgpt.com "Create a dashboard | Grafana documentation"
[8]: https://www.elastic.co/docs/solutions/observability/apm/apm-server/setup?utm_source=chatgpt.com "Set up APM Server | Elastic Docs"
