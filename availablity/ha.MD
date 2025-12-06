There are several **High Availability (HA) architectures**, each used by enterprises like Amazon, Netflix, banks, telecom, and cloud-native systems. These architectures help ensure an application stays online even when failures happen.

Below is a clean and comprehensive list of all major HA architectures.

---

# 🌐 **High Availability Architectures (Full List)**

![Image](https://docs.rapidminer.com/9.3/server/high-availability/img/ha-arch-overview.png?utm_source=chatgpt.com)

![Image](https://media.geeksforgeeks.org/wp-content/uploads/20240423165007/Active-Active-vs-Active-Passive-Architecture.webp?utm_source=chatgpt.com)

![Image](https://cdn-media-1.freecodecamp.org/images/1%2ABAZtz_iDmGIj2hy-MvuUPg.png?utm_source=chatgpt.com)

---

# 1️⃣ **Active–Passive (Warm Standby)**

* One system is **active**, one is **standby**.
* Standby becomes active only when failure happens.

**Used in:**
Banks, payment systems, core enterprise workloads.

**Pros:** Simple & cheaper
**Cons:** Small downtime when failover happens

---

# 2️⃣ **Active–Active (Multi-Active)**

* Multiple regions/nodes serve traffic simultaneously.
* Zero-downtime failover.

**Used by:** Netflix, Amazon, Google, Uber

**Pros:** Highest availability
**Cons:** Highest cost, complex

---

# 3️⃣ **Clustered Architecture (HA Cluster)**

Multiple servers form a cluster with shared data or replicated data.

Types:

* **Failover clusters**
* **Load-balanced clusters**
* **Database clusters (Galera, MySQL group replication)**
* **Kubernetes clusters**

**Pros:** Automatic failover, self-healing
**Cons:** Needs shared storage or replication logic

---

# 4️⃣ **Multi-AZ Architecture (Cloud HA Standard)**

Deploying an application across **multiple availability zones** (same region but different data centers).

**Used widely in AWS, Azure, GCP.**

Example:

* EC2 + RDS in Multi-AZ
* ALB distributes across AZs
* EKS nodes spread across AZs

**Pros:** Survives datacenter failure
**Cons:** Region-level outage still impacts you

---

# 5️⃣ **Multi-Region Architecture**

Your application runs in **two or more regions**.

Patterns:

* **Active-Active multi-region**
* **Active-Passive multi-region**
* **Disaster Recovery (DR) region**

**Pros:** Survives even regional disasters
**Cons:** Database sync becomes complex

---

# 6️⃣ **Blue–Green Deployment Architecture (HA for Deployments)**

Two environments exist:

* **Blue** (current live)
* **Green** (new version)

You switch traffic between them.

**Pros:** Zero downtime deployments
**Cons:** Needs duplicate infrastructure

---

# 7️⃣ **Canary Release Architecture**

New version is deployed to a small % of traffic first.

**Used by:** Google, Netflix, Facebook

**Pros:** Prevents global failure from a faulty version
**Cons:** Needs observability & routing rules

---

# 8️⃣ **CQRS + Event Sourcing Architecture**

Command and Query are separated.
Event sourcing stores **state changes** instead of raw state.

**HA benefits:**

* Write model and read model can scale independently
* Easy recovery by replaying events
* Supports distributed systems

Used by **banking systems, e-commerce order flows**.

---

# 9️⃣ **Serverless High Availability Architecture**

Services like:

* AWS Lambda
* DynamoDB
* S3
* API Gateway

are **multi-AZ and auto-scaled by default**.

**Pros:** Very high HA, low operational work
**Cons:** Cold starts, vendor lock-in

---

# 🔟 **High Availability with Message-Driven Architecture**

Using messaging systems:

* Kafka (multi-node, partitioned)
* AWS SQS
* RabbitMQ clusters

**Why it’s HA:**
If downstream service fails, the queue buffers requests.

**Used in:** Streaming platforms, banking, ecommerce

---

# 1️⃣1️⃣ **Distributed Cache Replicated Architecture**

Using:

* Redis Cluster
* Redis Sentinel
* Memcached pools

Provides:

* Failover
* Replication
* Automatic leader election

Used heavily in microservices architectures.

---

# 1️⃣2️⃣ **Kubernetes High Availability Architecture**

K8s provides:

* Self-healing pods
* Auto-scaling
* Multi-node redundancy
* Multi-master clusters

**Pros:** Strong built-in HA
**Cons:** Complex cluster management

---

# 1️⃣3️⃣ **Edge + CDN HA Architecture**

Platforms like:

* AWS CloudFront
* Akamai
* Cloudflare

Cache static assets globally.

Even if the backend is slow:
➡️ Edge locations keep serving cached content
➡️ Application appears highly available

---

# 1️⃣4️⃣ **Graceful Degradation Architecture**

When systems fail, the app reduces functionality instead of going down.

Example:

* Netflix disables recommendations but streaming works
* Amazon disables “reviews” but buying still works

**Huge HA benefit:** partial functionality is still better than downtime.

---

# 1️⃣5️⃣ **Circuit Breaker & Bulkhead Architecture**

Used with microservices.

* Circuit Breaker stops calling a failing service
* Bulkhead isolates failures so other services are unaffected

Used by: Netflix Hystrix, Resilience4j

---

# 🌟 Summary Table

| HA Architecture      | Usage                       | Availability Level | Complexity |
| -------------------- | --------------------------- | ------------------ | ---------- |
| Active-Passive       | Basic failover              | Medium             | Low        |
| Active-Active        | Global traffic distribution | Very High          | High       |
| Multi-AZ             | Cloud standard              | High               | Low        |
| Multi-Region         | Disaster protection         | Very High          | High       |
| HA Clusters          | Databases, apps             | High               | Medium     |
| Blue-Green           | Deployments                 | High               | Medium     |
| Canary               | Safe rollout                | High               | High       |
| Serverless           | Managed services            | Very High          | Low        |
| Messaging-based      | Microservices               | High               | Medium     |
| K8s HA               | Microservices               | High               | Medium     |
| CDN/Edge             | Global apps                 | High               | Low        |
| Graceful Degradation | UX-first apps               | Medium             | Medium     |
| Circuit Breaker      | Microservices               | Medium             | Medium     |

---
