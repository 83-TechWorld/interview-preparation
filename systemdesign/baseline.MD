# SRE Monitoring Architecture & Baselines

This document contains:

1. What a **baseline** is and how to compute it
2. Practical baseline examples and alerting rules for **Kafka lag** and **DB connections**
3. A **full SRE monitoring architecture diagram** and explanation (components + data flows)
4. Sample **Prometheus alerting rules**, **SLOs**, and **incident playbooks**
5. Quick remediation runbooks for common alerts

---

## 1) What is a *Baseline*?

A **baseline** is the statistical representation of "normal" behavior for a metric over time. It answers the question: *What do we expect this metric to look like when the system is healthy?*

Baselines are used to:

* Detect anomalies (spikes, drops, trends)
* Set meaningful alert thresholds (avoid noisy/false alerts)
* Drive automated scaling and alert severity

### Key concepts

* **Window**: the period used to compute normal (1h, 24h, 7d). Choose windows that capture seasonality.
* **Percentiles**: use p50/p95/p99 rather than raw mean for skewed metrics.
* **Seasonality**: daily/weekly patterns — compute baselines per time-of-day/day-of-week if necessary.
* **Smoothing**: use moving averages or exponential smoothing to avoid reacting to minute spikes.
* **Confidence band**: baseline +/- x% or baseline percentiles defines normal range.

### Methods to compute baselines (ordered by simplicity → sophistication)

1. **Simple moving average (SMA)**: average over last N minutes. Good for quick baselines.
2. **Percentile baseline**: p95 over last 1h or 24h. Less sensitive to spikes.
3. **Time-of-day windowed baseline**: compute baselines for each hour of day (recommended for human-facing APIs).
4. **Statistical anomaly detection**: Z-score, IQR, or EWMA.
5. **ML-based baseline**: Prophet, ARIMA, or ML anomaly detectors for complex traffic patterns.

---

## 2) Kafka lag baseline — how to compute and monitor

### Why Kafka lag matters

Consumer lag (per consumer group per partition) indicates how far behind consumers are from the latest produced offset. Sustained high lag → data processing delay, backlog, or potential data loss if retention expires.

### Metrics to collect

* `consumer_group_lag` (sum across partitions)
* `consumer_group_lag_per_partition`
* `consumer_group_current_offset`
* `consumer_group_high_watermark`
* `consumer_fetch_rate` (records/sec)
* `producer_write_rate` (records/sec)
* `consumer_processing_rate` (records/sec)
* `partition_leader_election_count`
* `under_replicated_partitions`
* `broker_disk_utilization` and `io_wait`

### Baseline approaches

**a) Relative baseline (recommended):**

* Compute `p95(lag)` for each consumer group over the last 1 hour and 24 hours.
* Alert if current total lag exceeds `max(p95_1h * 2, p95_24h * 1.5, absolute_threshold)` sustained for N minutes.

**b) Absolute baseline (for small predictable workloads):**

* If typical steady-state lag is ~0–50, set warning at 500 and critical at 5000 or retention-based threshold.

**c) Time-of-day baseline**

* Compute baseline per hour (or per 15min bucket) to account for diurnal load.

### Example baseline rule (pseudo PromQL)

* **Warning**: if `sum(kafka_consumer_group_lag{group="orders-service"}) > max( (quantile_over_time(0.95, kafka_consumer_group_lag[1h]) * 2), 500 )` for 5m → WARNING
* **Critical**: if `sum(kafka_consumer_group_lag{group="orders-service"}) > max( (quantile_over_time(0.99, kafka_consumer_group_lag[24h]) * 3), 5000 )` for 2m → CRITICAL

> Tip: use sustained windows (5–15 min) to avoid firing on spikes.

### Alert severity & routing

* **Warning**: Notify the team Slack channel + create a ticket. Auto-scale consumers if possible.
* **Critical**: PagerDuty page to on-call, run consumer health playbook.

### Immediate remediation steps (playbook)

1. Check consumer group status: consumer lag per partition (identify hot partitions).
2. Verify consumers are up (`consumer_offset_committed_time`, consumer heartbeat metrics).
3. Check consumer CPU, GC pauses, and thread counts (are consumers stuck?).
4. Check broker health: leader availability, under_replicated_partitions, disk IO.
5. If consumer CPU-bound → scale consumer instances or increase consumer parallelism (more partitions/instances).
6. If broker-side bottleneck (IO): add brokers, increase IOPS, or throttle producers.
7. If one partition has all the load (hot partition): consider partition rebalancing or key hashing change.
8. If retention expiry is near and lag is large → consider emergency scale or increase retention to avoid data loss.

### Example thresholds (starting point)

* **Small systems**: warning 500, critical 5000
* **Medium**: warning 5k, critical 50k
* **Large**: compute relative thresholds vs baseline (percentile based)

---

## 3) DB connections baseline — how to compute and monitor

### Why DB connections matter

Exhausted DB connections lead to application errors, queuing, timeouts, and cascading failures.

### Metrics to collect

* `db_max_connections` (configured max)
* `db_active_connections` (current open)
* `db_connection_wait_count` (how many requests waiting)
* `db_connection_acquire_time` (latency to get a connection)
* `db_connection_errors` (rejections/refused)
* `app_pool_usage` (pool size, active, idle)
* `app_connection_leak_count` or `connection_checkout_time`

### Baseline approaches

**a) Percentile baseline**

* Compute p95 and p99 of `db_active_connections` over 1h and 24h.
* Alert if `active_connections` > 0.8 * `db_max_connections` OR if `active_connections` > p95_1h * 1.25 for more than 5 minutes.

**b) Rate-change baseline**

* Watch `rate(active_connections[5m])` — a fast upward slope suggests a leak or traffic spike.

**c) Pool-level baseline**

* If app-level `pool_usage` regularly exceeds 70% during normal operations, tune pool size or DB connections per instance.

### Example Prometheus-style alerts

* **Warning**: `avg_over_time(db_active_connections[5m]) > 0.7 * db_max_connections` for 5m → warning
* **Critical**: `avg_over_time(db_active_connections[2m]) > 0.9 * db_max_connections` or `increase(db_connection_errors[5m]) > 10` → page

### Remediation steps (playbook)

1. Identify offending app instances: check app metrics for connection leak signs (checkout time > threshold).
2. Check recent deploys or config changes that changed pool size.
3. If sudden spike → enable circuit breaker, rate-limit incoming traffic, or scale out app instances (if DB can handle more connections via pooling).
4. If leak suspected → restart affected app instances one by one, collect heap/thread dumps and find leaked connections (e.g., unclosed JDBC statements).
5. Increase DB max connections as a temporary measure only with capacity planning.
6. For long-term fixes: connection pooling best practices, timeouts, finally blocks, use of libraries that safely close resources.

### Example thresholds (starting point)

* **Warning**: avg connections > 70% of max for 5m
* **Critical**: avg connections > 90% of max or connection errors > 10/min

---

## 4) Full SRE Monitoring Architecture (diagram + components)

> The diagram below shows a typical modern SRE observability stack and data flow. The right-hand side contains users and external systems; the left-hand side is monitoring, detection, and incident response.

### Components (top to bottom)

1. **Instrumentation / Telemetry (Client & Services)**

   * Metrics: Prometheus client libs, OpenTelemetry metrics
   * Logs: structured JSON logs
   * Traces: OpenTelemetry spans
   * Synthetic checks: heartbeat / synthetic transactions

2. **Ingestion / Collectors**

   * Metric scrapers/collectors: Prometheus, Telegraf
   * Log shippers: Filebeat/Fluentd → Log pipeline
   * Tracing collectors: OTLP endpoint → Jaeger/Tempo
   * Synthetic engines: Datadog Synthetics, Pingdom

3. **Storage & Indexing**

   * Metrics: Prometheus TSDB, Cortex, Thanos (long-term)
   * Logs: ElasticSearch, Loki, Datadog logs
   * Traces: Jaeger, Tempo

4. **Processing, Correlation & Enrichment**

   * Enrich logs with trace IDs, request IDs, customer IDs
   * Metric rollups, downsampling (thanos/loki)
   * Alert rule evaluation (Prometheus Alertmanager)
   * ML Anomaly detection module (optional)

5. **Visualization & Dashboards**

   * Grafana dashboards for metrics + traces + logs links
   * Kibana / Log UI for logs
   * Service maps and topology view

6. **Alerting & Incident Management**

   * Alertmanager -> routes to Slack, PagerDuty, OpsGenie
   * Incident ticket creation (Jira, ServiceNow)
   * Runbook links + playbooks in alerts

7. **Automation & Remediation**

   * Auto-scaling (K8s HPA / Cluster autoscaler)
   * Auto-restart failed pods (Kubernetes)
   * SRE runbooks for manual steps
   * Remediation bots (auto-rollbacks, restarts, scale-up)

8. **SLO, Error Budget & Release Controls**

   * SLO engine evaluating SLI metrics (availability, latency)
   * Error budget alerting → freeze deploys if budget exhausted

9. **Post-incident Analysis**

   * Blameless postmortem repository
   * RCA dashboards
   * Action items tracked in Agile board

### Data flow (brief)

1. App services emit metrics, logs, and traces (with request-id).
2. Collectors ingest telemetry and forward to their respective stores.
3. Alert rules and ML detectors evaluate metrics and trigger alerts.
4. Alerts route to on-call via Alertmanager → PagerDuty.
5. On-call uses dashboards/traces/logs to diagnose and execute runbooks.
6. If safe, automation remediates; otherwise the on-call follows manual steps.
7. Post-incident, SLO engine and postmortem process update runbooks and SLOs.

---

## 5) Sample Prometheus alert rules (copyable)

```yaml
groups:
- name: kafka.rules
  rules:
  - alert: KafkaConsumerLagWarning
    expr: |
      sum by (consumer_group) (kafka_consumer_group_lag) > (
        max(quantile_over_time(0.95, kafka_consumer_group_lag[1h]) * 2, 500)
      )
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High Kafka consumer lag for {{ $labels.consumer_group }}"
      description: "Consumer group {{ $labels.consumer_group }} has total lag > baseline. Check consumer health."

- name: db.rules
  rules:
  - alert: DBConnectionsHigh
    expr: |
      avg_over_time(db_active_connections[5m]) > (0.7 * db_max_connections)
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "DB connections high"
      description: "Average DB connections over 5m > 70% of max. Check connection pool usage and app instances."
```

---

## 6) Incident playbook templates (quick)

### Kafka lag (Critical)

1. Acknowledge page. Open consumer group detail in UI.
2. Identify partitions with highest lag. Note the producer rate.
3. Check consumer pod logs for exceptions/timeouts.
4. If consumer pods are OOM/GC stuck — restart pods one-by-one and inspect heap.
5. If broker IO is the bottleneck — check disk utilization and broker request handler metrics; add broker or increase disk IOPS.
6. If hot partition — consider re-partitioning or key re-hash as planned maintenance.
7. After mitigations, monitor lag decline. Close incident.

### DB connections (Critical)

1. Acknowledge page. Open DB metrics and app pool metrics.
2. Identify app instances with highest pool usage; check recent deploys.
3. If leak suspected → restart suspect instances, collect thread/heap dumps.
4. If traffic spike → enable rate-limiting or circuit-breaker and scale read replicas.
5. If DB CPU/disk IO saturated → scale DB (read replicas) or increase resources.
6. After mitigations, verify connections stable and errors gone.

---

## 7) KPIs & SLIs to track (recommended)

* **Availability SLI**: 5xx count / total requests (per service)
* **Latency SLI**: p95 request latency
* **Error budget**: % of time SLO violated in rolling 30d
* **Kafka SLI**: consumer lag percentiles, message processing latency
* **DB SLI**: connection saturation, slow query percentiles, DB error rate

---

---

*End of document.*
