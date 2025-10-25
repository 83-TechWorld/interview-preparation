# Distributed Tracing & Logging — Complete GitHub-style Documentation

**Contents:**

1. Overview & core concepts
2. Open-source & commercial tools (quick comparison)
3. How tracing works internally (Jaeger + ELK internals)
4. Recommended architecture patterns (Java + Kubernetes)
5. Step-by-step: instrument Java apps (OpenTelemetry)
6. Step-by-step: deploy Jaeger on Kubernetes (Helm / Operator)
7. Step-by-step: deploy Elastic Stack (ELK / ECK) on Kubernetes
8. Correlating logs & traces (log enrichment, MDC)
9. Sample configs & manifests (copy-paste ready)
10. Troubleshooting, sampling, storage sizing, security & best practices
11. References

> **Note:** This document is written as a single `performance-metrics.md` / `distributed-tracing.md` you can drop into a GitHub repo. The sections below contain commands, manifests and copy-paste configs. I cite primary official docs for the most load-bearing statements (Jaeger k8s, Jaeger Helm, OpenTelemetry Java, Elastic ECK, OpenTelemetry Java instrumentation). ([jaegertracing.io][1])

---

# 1 — Overview & core concepts

**What is distributed tracing?**
Distributed tracing records the path of a request as it flows through services (spans). Each request becomes a *trace* (collection of *spans*). Traces show latency, errors and the causality between services — essential for debugging and SLO/latency root-cause analysis.

**Core terms**

* **Trace** — end-to-end journey of a request (collection of spans).
* **Span** — single operation; has start/end timestamps, attributes (tags), status and parent/child relationships.
* **Trace Context / Context Propagation** — trace id and span id passed between services (W3C Trace Context / `traceparent`).
* **Sampling** — limiting which traces to keep (head-based vs tail-based).
* **Exporter / Collector** — component(s) that receive telemetry and ship to storage/analysis backend (Jaeger collector, OTEL Collector).

# 2 — Open-source & commercial tools (quick comparison)

**Open-source (popular)**

* **OpenTelemetry (OTel)** — collection of SDKs, auto-instrumentation agents and the OTLP protocol. *Recommended as the standard instrumentation layer*. ([OpenTelemetry][2])
* **Jaeger** — tracing backend (agent, collector, query, UI); integrates with OTel/Zipkin formats. Good for open-source tracing storage & UI. ([jaegertracing.io][1])
* **Zipkin** — lightweight tracing collector & UI (classic simple alternative). ([zipkin.io][3])
* **Grafana Tempo / SigNoz / Uptrace** — alternatives focused on cheap long-term storage and query. ([signoz.io][4])
* **Elastic Stack (ElasticSearch + Logstash/Filebeat + Kibana)** — primary for logs, but Elastic Observability supports traces and APM integrations.

**Commercial / hosted**

* **Datadog APM** — unified logs + metrics + traces; easy to get started but paid.
* **New Relic, Dynatrace, Splunk Observability** — full-featured tracing + logs + metrics, APM features, SaaS grade.

**How to choose**

* For OSS + control: **OpenTelemetry + Jaeger/Tempo + Elastic Stack**.
* For integrated experience & faster time-to-value: **Datadog/New Relic/Splunk**.
  (See tool comparisons and market data for 2024–2025.) ([Uptrace][5])

---

# 3 — How tracing works internally (Jaeger & Elastic internals)

## Jaeger internals (high-level)

* **Client instrumentation** (SDK/agent/collector): libraries or agents create spans.
* **Jaeger Agent**: UDP/UDP-like daemon that runs close to application (often as DaemonSet) — receives spans and forwards to collectors.
* **Jaeger Collector(s)**: receive spans (from agent or OTLP), perform validation/enrichment, and write to storage (Elasticsearch, Cassandra, Kafka, or memory).
* **Query Service**: reads from storage and serves backends/UI.
* **UI**: visualizes traces, dependencies, service maps.
* **Storage backend**: Elasticsearch/Cassandra/CosmosDB depending on scale.
  This architecture enables scale by separating ingestion (agents/collectors) from long-term storage and the query layer. ([jaegertracing.io][1])

## Elastic Stack internals (for logs + APM)

* **Beats (Filebeat)**: lightweight shipper on nodes/pods to forward logs/metrics.
* **Logstash**: optional pipeline processor (parsing, enrichments).
* **Elasticsearch**: indexing, storage, query engine.
* **Kibana**: UI for logs, dashboards and APM views.
  Elastic APM has agents for languages; it can ingest traces and correlate with logs stored in Elasticsearch. ECK (Elastic Cloud on Kubernetes) operator simplifies running Elastic on Kubernetes. ([Elastic][6])

---

# 4 — Recommended architecture patterns (Java + K8s)

Common patterns:

* **OTel Java Auto-instrumentation → OTLP → OTEL Collector (k8s) → Jaeger/Elasticsearch**
* **Instrumented Java apps emit traces + logs. Logs are shipped by Filebeat with trace id injected (MDC) → Elasticsearch / Kibana.**
* **Jaeger for traces, Elastic for logs and metrics (or use an integrated commercial vendor for unified UI).**

Reasons:

* Using OTLP + OTEL Collector gives flexibility to route to multiple backends (Jaeger, Tempo, Elastic, commercial APMs). ([OpenTelemetry][2])

---

# 5 — Instrument Java applications (OpenTelemetry) — **Step-by-step**

### A. Zero-code / Auto-instrumentation (recommended for quick wins)

1. Download the OpenTelemetry Java agent JAR (from opentelemetry-java-instrumentation).
   Example (Linux):

   ```bash
   wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/vLATEST/opentelemetry-javaagent.jar
   ```

2. Run your JVM with the agent and configure exporter (OTLP HTTP/GRPC or Jaeger exporter). Example JVM args (OTLP to Collector):

   ```bash
   java -javaagent:/opt/opentelemetry-javaagent.jar \
        -Dotel.exporter.otlp.endpoint=http://otel-collector:4317 \
        -Dotel.resource.attributes=service.name=my-java-service \
        -jar my-app.jar
   ```

   Or to send directly to Jaeger (HTTP Thrift/Jaeger exporter): set exporter to `jaeger` and configure host/port. ([GitHub][7])

3. For Spring Boot (or any framework) this usually captures HTTP server spans, DB client spans, messaging frameworks and more — no code change.

### B. Manual instrumentation (fine-grained control)

* Use OpenTelemetry Java SDK (Maven/Gradle), create tracer, start spans around critical operations:

```java
Tracer tracer = OpenTelemetry.getGlobalTracer("com.example", "1.0.0");
Span span = tracer.spanBuilder("myOperation").startSpan();
try (Scope s = span.makeCurrent()) {
   // do work
} finally {
   span.end();
}
```

* Add attributes & events to spans for richer context.
* Export via OTLP exporter to Collector/Jaeger.

### C. Logging correlation (very important)

* Add trace ids and span ids to application logs (use MDC for SLF4J/Logback/Log4j):

```xml
<!-- logback.xml -->
<pattern>%d{ISO8601} [%thread] %-5level %logger{36} - trace=%X{trace_id} span=%X{span_id} %msg%n</pattern>
```

* Populate MDC via instrumentation (OTel auto-instrumentation can inject) or add code:

```java
Span current = Span.current();
String traceId = current.getSpanContext().getTraceId();
MDC.put("trace_id", traceId);
```

**Important:** Use the same trace id format exposed by the tracer and ensure logs forwarded by Filebeat / Fluentd pick up these fields.

---

# 6 — Deploy Jaeger on Kubernetes (Helm / Operator) — **Step-by-step**

Two common installs: **Jaeger Helm chart** or **Jaeger Operator**. Helm is simpler; Operator is better for CRD-managed production. Official repo & docs: Jaeger k8s deployment + Helm charts. ([jaegertracing.io][1])

### A. Prerequisites

* Helm 3 installed
* Kubernetes cluster (min 2–3 nodes for production)
* `kubectl` configured

### B. Quick dev install (all-in-one)

```bash
helm repo add jaegertracing https://jaegertracing.github.io/helm-charts
helm repo update

# Install simple all-in-one (not for production):
helm install jaeger jaegertracing/jaeger \
  --set provisionDataStore.cassandra=false \
  --set provisioning.enabled=false
```

This creates collector, query, UI and agent in-cluster. For quick tests you can port-forward the UI:

```bash
kubectl port-forward svc/jaeger-query 16686:16686
# Visit http://localhost:16686
```

### C. Production pattern (recommended)

1. **Storage**: choose Elasticsearch (scale), Cassandra, or Kafka-based pipeline. Many production setups use Elasticsearch as Jaeger storage backend.
2. **Install Elastic (if using ES for Jaeger)** — see section 7 (ECK) for Elastic installation and sizing. ([Elastic][8])
3. **Install Jaeger with Helm & Elasticsearch config**:

```bash
helm install jaeger jaegertracing/jaeger \
  --set cassandra.enabled=false \
  --set storage.type=elasticsearch \
  --set storage.elasticsearch.host=elasticsearch-master \
  --set storage.elasticsearch.port=9200
```

4. **Run Jaeger Agent as DaemonSet** (recommended) so that app pods send UDP spans locally:

```yaml
# example: jaeger-agent DaemonSet is enabled by helm chart via values.yaml
agent:
  strategy: DaemonSet
```

5. **Scale collectors** via `replicaCount` and use a load balancer or internal service.

### D. Using OTEL Collector in front of Jaeger

* Many production setups run an **OpenTelemetry Collector** (receiver → processors → exporters) and export to Jaeger (or multiple backends simultaneously). This allows batching, sampling, pipelining, and security (TLS/Auth).

**Collector example Helm / Deployment**: configure `receivers.otlp`, `exporters.jaeger`, then point Java agent to Collector OTLP endpoint. This decouples apps from backend details. ([jaegertracing.io][1])

---

# 7 — Deploy Elastic Stack (ELK / ECK) on Kubernetes — **Step-by-step**

Elastic Stack is commonly used for logs and APM; ECK (Elastic Cloud on Kubernetes) operator is recommended to run Elasticsearch on k8s. Official ECK docs show Helm install. ([Elastic][8])

### A. Install ECK (operator) via Helm

```bash
helm repo add elastic https://helm.elastic.co
helm repo update
helm install elastic-operator elastic/eck-operator --namespace elastic-system --create-namespace
```

### B. Create Elasticsearch cluster (example `elasticsearch.yaml`)

```yaml
apiVersion: elasticsearch.k8s.elastic.co/v1
kind: Elasticsearch
metadata:
  name: quickstart
spec:
  version: 8.9.0
  nodeSets:
  - name: default
    count: 3
    config:
      node.store.allow_mmap: false
    volumeClaimTemplates:
    - metadata:
        name: elasticsearch-data
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 30Gi
```

Apply:

```bash
kubectl apply -f elasticsearch.yaml
```

### C. Install Kibana

```yaml
apiVersion: kibana.k8s.elastic.co/v1
kind: Kibana
metadata:
  name: quickstart
spec:
  version: 8.9.0
  count: 1
  elasticsearchRef:
    name: quickstart
```

`kubectl apply -f kibana.yaml`

### D. Shipping logs from Kubernetes

* Use **Filebeat** as a DaemonSet to read container logs, add fields (e.g., kubernetes metadata, trace id) and send to Elasticsearch or Logstash. Example Filebeat Helm or manifest available in Elastic docs.

### E. Optional Logstash pipeline

If you need complex parsing:

```bash
# Example Logstash pipeline config (logstash.conf)
input { beats { port => 5044 } }
filter {
  json { source => "message" } # if logs are JSON
  mutate { add_field => { "trace.id" => "%{[json][trace_id]}" } }
}
output { elasticsearch { hosts => ["http://elasticsearch:9200"] } }
```

---

# 8 — Correlating logs & traces (best practice)

* **Inject trace id into logs (MDC)** so logs and traces can be joined in Kibana/Jaeger UI. OTEL auto-instrumentation can help, or you can set MDC manually as shown earlier.
* **Log format**: prefer structured JSON logs so Filebeat/Logstash can parse fields directly. Example JSON log line:

```json
{"timestamp":"2025-10-25T10:00:00Z","level":"INFO","message":"user created","trace_id":"..." }
```

* **Use a common ID field name** (e.g., `trace_id`) across your logs and traces.
* **Use OTEL Collector processors** to add attributes, enforce sampling, and redact PII prior to export.

---

# 9 — Sample configs & manifests (copy-paste)

> The following are *minimal* examples. Adjust versions, resource requests, storage sizes for production.

### A. Minimal Jaeger Helm install (values override)

```yaml
# jaeger-values.yaml
agent:
  strategy: DaemonSet

collector:
  sp:
    replicaCount: 2

query:
  replicaCount: 1

storage:
  type: memory  # use elastic or cassandra for prod
```

Install:

```bash
helm repo add jaegertracing https://jaegertracing.github.io/helm-charts
helm repo update
helm install -f jaeger-values.yaml jaeger jaegertracing/jaeger
```

### B. OTEL Collector example YAML (receives OTLP, exports to Jaeger)

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: otel-collector-config
data:
  otel-collector-config.yaml: |
    receivers:
      otlp:
        protocols:
          grpc:
          http:
    processors:
      batch:
    exporters:
      jaeger:
        endpoint: "jaeger-collector.observability.svc.cluster.local:14250"
        tls:
          insecure: true
    service:
      pipelines:
        traces:
          receivers: [otlp]
          processors: [batch]
          exporters: [jaeger]
---
# deployment for collector (omitted here — use official helm or k8s manifest)
```

### C. Java command line (docker / k8s):

* If your Java app runs in a container, add the agent and environment variables in the container image or the pod spec:

```yaml
env:
- name: OTEL_EXPORTER_OTLP_ENDPOINT
  value: "http://otel-collector:4317"
- name: OTEL_RESOURCE_ATTRIBUTES
  value: "service.name=my-service,service.version=1.0.0"
command: ["java", "-javaagent:/opt/opentelemetry-javaagent.jar", "-jar", "/app/app.jar"]
volumeMounts:
- name: otel-agent
  mountPath: /opt/opentelemetry-javaagent.jar
  subPath: opentelemetry-javaagent.jar
```

### D. Filebeat example (values for Helm) to enrich logs with kubernetes metadata

Use official filebeat Helm chart and set `filebeat.autodiscover` for containers; include processors to parse JSON and add `trace_id` field.

---

# 10 — Troubleshooting, sampling, sizing, security & best practices

**Sampling**

* Start with **head-based** sampling (front-end / gateway) to limit ingestion — then consider **tail-based** sampling (OTEL Collector) to keep interesting traces. Tail-based allows you to keep traces with errors or high latency. Use Collector sampler processors.

**Storage & retention**

* Traces are high cardinality — plan retention (days) and storage backend (Elasticsearch is common but needs shards / sizing). Use indices rollover and ILM in Elasticsearch.

**Security**

* Use TLS between agents, collectors and storage. Authenticate the OLTP/OTLP endpoints (mTLS or token auth). Secure Kibana with basic auth or SSO.

**Performance**

* Use batching in Collector/exporter, back-pressure mechanisms, and proper resource requests/limits for collectors. Avoid instrumenting aggressively (attributes with huge blobs).

**Troubleshooting tips**

* If no spans appear in Jaeger: check agent/collector logs, ensure app can reach agent (UDP) or collector (OTLP), check sampling config.
* If logs missing trace ids: confirm MDC population and that Filebeat reads current log format (JSON vs text).

---

# 11 — References & further reading

* Jaeger — Kubernetes deployment & architecture. ([jaegertracing.io][1])
* Jaeger Helm charts (official). ([GitHub][9])
* OpenTelemetry docs — Java getting started & auto-instrumentation. ([OpenTelemetry][2])
* Elastic ECK docs — Helm install & deploying Elastic Stack on k8s. ([Elastic][8])
* Zipkin (overview). ([zipkin.io][3])

---

# Appendix — Quick checklist to implement tracing in your environment

1. **Decide OTEL vs Native agent**: Use OpenTelemetry auto-instrumentation agent for Java. ([GitHub][7])
2. **Run OTEL Collector in Kubernetes** (receive OTLP → processors → exporter to Jaeger and/or ES).
3. **Deploy Jaeger (Helm/Operator)** and point Collector exporter to Jaeger collector. ([GitHub][9])
4. **Deploy Elastic (ECK)** for logs and APM storage (optional) and Filebeat to ship logs. ([Elastic][8])
5. **Add MDC trace_id to application logs** and prefer JSON logs for easy parsing.
6. **Set sampling** (start with low rate for prod; raise for dev).
7. **Secure & monitor** collectors/storage.

---

If you want, I can:

* Convert this into a ready-to-commit `distributed-tracing.md` file (GitHub-flavored markdown) and include the example manifests as separate files in a small repo tree.
* Produce a second file with production-grade Helm `values.yaml` for Jaeger (Elasticsearch storage) + ECK resource manifests sized for your node sizes.
* Generate step-by-step commands tailored to your Kubernetes distribution (EKS / GKE / AKS / on-prem).

Which of those would you like me to produce next?

[1]: https://www.jaegertracing.io/docs/2.0/deployment/kubernetes/?utm_source=chatgpt.com "Deploying on Kubernetes"
[2]: https://opentelemetry.io/docs/languages/java/getting-started/?utm_source=chatgpt.com "Getting Started by Example"
[3]: https://zipkin.io/?utm_source=chatgpt.com "OpenZipkin · A distributed tracing system"
[4]: https://signoz.io/blog/distributed-tracing-tools/?utm_source=chatgpt.com "Top 15 Distributed Tracing Tools for Microservices in 2025"
[5]: https://uptrace.dev/tools/top-observability-tools?utm_source=chatgpt.com "Top 10 Observability Tools in 2025"
[6]: https://www.elastic.co/docs/deploy-manage/deploy/cloud-on-k8s/managing-deployments-using-helm-chart?utm_source=chatgpt.com "Elastic Stack Helm chart | Elastic Docs"
[7]: https://github.com/open-telemetry/opentelemetry-java-instrumentation?utm_source=chatgpt.com "open-telemetry/opentelemetry-java-instrumentation"
[8]: https://www.elastic.co/docs/deploy-manage/deploy/cloud-on-k8s/install-using-helm-chart?utm_source=chatgpt.com "Install using a Helm chart"
[9]: https://github.com/jaegertracing/helm-charts?utm_source=chatgpt.com "Helm Charts for Jaeger backend"


Here’s a clear breakdown in **GitHub Markdown style**, with diagrams and ELK stack indexing explanation.

---

# Trace and Span ID — How it Works

## 1. Conceptual Diagram

```
Client Request
       │
       ▼
┌─────────────┐
│ Service A   │
│ Span A (trace_id=1234, span_id=1) │
└─────────────┘
       │
       ├───────────────┐
       ▼               ▼
┌─────────────┐   ┌─────────────┐
│ Service B   │   │ Service C   │
│ Span B      │   │ Span C      │
│ trace_id=1234│   │ trace_id=1234│
│ parent_span=1│   │ parent_span=1│
└─────────────┘   └─────────────┘
       │
       ▼
┌─────────────┐
│ Service D   │
│ Span D      │
│ trace_id=1234│
│ parent_span=B│
└─────────────┘
```

### Explanation:

1. **Trace ID** — unique identifier for a single request across multiple services.
2. **Span ID** — unique identifier for a specific operation within a trace.
3. **Parent Span ID** — links a span to its parent span (causal relationship).
4. Together, trace_id + span_id + parent_span_id form a **tree of spans**, which can be visualized in Jaeger/Zipkin.

---

## 2. How it Works in Code (Java Example)

```java
Span span = tracer.spanBuilder("processOrder").startSpan();
try (Scope scope = span.makeCurrent()) {
    // downstream call
    Span child = tracer.spanBuilder("callInventory").startSpan();
    child.end();
} finally {
    span.end();
}
```

* `span` gets `trace_id = 1234`
* `child` gets `trace_id = 1234` and `parent_span_id = span_id of parent`

OTEL auto-instrumentation does this automatically for HTTP, gRPC, DB calls.

---

# 3 — ELK Stack Index & Index Pattern

### What is an Index?

* An **Elasticsearch index** is like a database table.
* It stores documents (logs, traces, metrics).
* Each document is a JSON object.

Example:

```json
{
  "timestamp": "2025-10-25T10:00:00Z",
  "service": "order-service",
  "level": "INFO",
  "message": "order created",
  "trace_id": "1234",
  "span_id": "1"
}
```

### What is an Index Pattern?

* An **index pattern** is a way for Kibana to match one or more indices for visualization.
* E.g., `logs-*` matches all indices starting with `logs-`.
* Used to query data in dashboards, discover view, and visualizations.

---

## 4. How to Create an Index in Elasticsearch

### Via API:

```bash
PUT /logs-2025.10.25
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
      "timestamp": { "type": "date" },
      "service": { "type": "keyword" },
      "level": { "type": "keyword" },
      "message": { "type": "text" },
      "trace_id": { "type": "keyword" },
      "span_id": { "type": "keyword" }
    }
  }
}
```

### Via Filebeat/Logstash:

* Set `index` in the output section:

```yaml
output.elasticsearch:
  hosts: ["http://elasticsearch:9200"]
  index: "logs-%{+yyyy.MM.dd}"
```

---

## 5. How Trace IDs Help in ELK

1. Logs from multiple services include the **same trace_id**.
2. Kibana can search logs by trace_id to reconstruct the flow.
3. You can build dashboards showing **latency per trace**, errors per trace, and service call hierarchy.

---

If you want, I can **draw a full GitHub Markdown diagram combining Trace + Span + logs in ELK**, showing **flow from Java app → OTEL → Jaeger → ELK → Kibana**, which will visualize exactly how trace_id flows across services and logs.

1 — Indices in ELK Stack
What is an Index?
An Elasticsearch index is like a database table.
It stores documents, which are JSON objects representing logs, metrics, or traces.
Example of a log document:
{
  "timestamp": "2025-10-25T11:00:00Z",
  "service": "order-service",
  "level": "INFO",
  "message": "Order created successfully",
  "trace_id": "4bf92f3577b34da6a3ce929d0e0e4736",
  "span_id": "00f067aa0ba902b7"
}
Indices can be time-based (logs-2025.10.25) or service-based (orders-*).
Elasticsearch shards and replicas are set per index to scale and provide redundancy.
Index Pattern in Kibana
Kibana uses an index pattern to query one or more indices.
Example: logs-* matches all daily logs like logs-2025.10.24, logs-2025.10.25.
This is used in the Discover view, dashboards, and visualizations.
2 — Trace ID
What is a Trace ID?
A trace_id is a unique identifier for a single request as it travels across services.
It connects multiple spans (individual operations) into one trace.
What is a Span ID?
Each operation (span) in a trace has a span_id.
Each span may have a parent_span_id to indicate its parent.
Trace ID in Logs
When you instrument your Java app (OTel or Jaeger), each log can include the trace_id and span_id.
Example:
{
  "timestamp": "2025-10-25T11:00:00Z",
  "service": "payment-service",
  "message": "Payment processed",
  "trace_id": "4bf92f3577b34da6a3ce929d0e0e4736",
  "span_id": "00f067aa0ba902b7"
}

Diagram Explanation:
Client Request creates a new trace_id (1234) and initial span (span_id=1) in Service A.
Parent-Child Spans:
Service A calls Service B → span_id=2, parent_span=1
Service A calls Service C → span_id=3, parent_span=1
Service B calls Service D → span_id=4, parent_span=2
ELK logs:
Each service includes trace_id and span_id in logs.
Logs can be queried in Kibana by trace_id to see full request context.
Jaeger UI:
Shows a tree of spans: A → B → D, and A → C.
Each span has duration, status, and can link to logs in ELK via trace_id.