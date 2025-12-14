```md
# 🚦 Rate Limiting Algorithms  
## Token Bucket & Leaky Bucket — Why, Where, and How They Are Used

Rate limiting algorithms protect systems from **traffic spikes**, **abuse**, and the **thundering herd problem** by controlling how fast requests are allowed to proceed.

---

## ❓ Why Do We Need Rate Limiting?

Without rate limiting:
- One user or service can overload your API
- Sudden traffic spikes can crash databases
- Retry storms amplify failures

Rate limiting ensures:
- **Fair usage**
- **System stability**
- **Predictable performance**

---

# 🪣 Token Bucket Algorithm

## 🧠 Concept (Simple Explanation)

Imagine a bucket:
- Tokens are added at a **fixed rate**
- Each request **consumes one token**
- If no token → request is rejected or delayed

✅ Allows **bursts**  
❌ Still enforces long-term rate

---

## 📊 How Token Bucket Works

```

Tokens added periodically
↓
┌───────────┐
│  TOKEN    │
│  BUCKET   │
│ (capacity)│
└───────────┘
↓
Request consumes 1 token

```

### Key Parameters

| Parameter | Meaning |
|--------|--------|
| Capacity | Max tokens bucket can hold |
| Refill rate | Tokens added per second |
| Cost | Tokens per request |

---

## 🧪 Example

- Capacity: `10`
- Refill rate: `1 token/sec`

Scenario:
- 10 requests instantly → allowed
- 11th request → rejected
- After 1 sec → 1 new request allowed

---

## 📍 Where Token Bucket Is Used

| Use Case | Reason |
|-------|-------|
API Gateways | Allow bursts but limit abuse |
Mobile apps | Handle intermittent network |
Public APIs | Fair client usage |
Microservices | Prevent cascading overload |

---

## 🛠 Tools That Use Token Bucket

| Tool | Where |
|----|----|
NGINX | `limit_req` |
Envoy | Rate limit filter |
AWS API Gateway | Throttling |
Istio | Ingress rate limiting |
Redis | Custom implementations |

---

## 🧱 Data Structures Used

- **Counter + timestamp**
- **Redis INCR + TTL**
- **Atomic variables**
- **Sliding window approximation**

---

# 🚰 Leaky Bucket Algorithm

## 🧠 Concept (Simple Explanation)

Imagine a bucket with a **small hole**:
- Requests enter bucket
- Bucket drains at a **constant rate**
- If bucket overflows → requests dropped

✅ Smooth output  
❌ No bursts

---

## 📊 How Leaky Bucket Works

```

Requests → Bucket → Constant Output Rate → Backend

```

### Key Parameters

| Parameter | Meaning |
|--------|--------|
| Capacity | Max queued requests |
| Leak rate | Requests per second |

---

## 🧪 Example

- Capacity: `5`
- Leak rate: `1 req/sec`

Scenario:
- 10 incoming requests quickly
- First 5 queued
- Remaining 5 dropped
- Requests processed evenly at 1/sec

---

## 📍 Where Leaky Bucket Is Used

| Use Case | Reason |
|-------|-------|
Network shaping | Smooth traffic |
Streaming | Stable throughput |
Ingress proxies | Prevent backend spikes |
IoT systems | Predictable flow |

---

## 🛠 Tools That Use Leaky Bucket

| Tool | Where |
|----|----|
NGINX | `limit_conn` |
Linux TC | Network traffic control |
Cloudflare | Edge protection |
HAProxy | Connection throttling |

---

## 🧱 Data Structures Used

- **Queue**
- **Fixed-size buffer**
- **Circular buffer**
- **Delay queues**

---

# 🆚 Token Bucket vs Leaky Bucket

| Feature | Token Bucket | Leaky Bucket |
|------|-------------|-------------|
Burst allowed | ✅ Yes | ❌ No |
Traffic smoothing | ❌ No | ✅ Yes |
Latency | Low | Higher |
Complexity | Medium | Simple |
Best for | APIs | Networks |

---

## 🎯 Which One Should You Use?

| Scenario | Best Choice |
|--------|-------------|
Public API | Token Bucket |
Payment APIs | Token Bucket |
Network shaping | Leaky Bucket |
Streaming pipelines | Leaky Bucket |
Ingress control | Combination |

---

## 🧩 Real-World Architecture

```

Client
↓
API Gateway (Token Bucket)
↓
Queue (Leaky Bucket)
↓
Worker Pool
↓
Database

```

---

## 🧠 Key Design Rule

> **Token Bucket controls how fast users can talk.  
Leaky Bucket controls how fast systems listen.**

---

## 📦 Libraries & Implementations

### Java
- Resilience4j RateLimiter
- Bucket4j
- Guava RateLimiter

### Node.js
- rate-limiter-flexible
- express-rate-limit

### Go
- golang.org/x/time/rate

### Distributed
- Redis
- Envoy
- Istio

---

## 🚀 Advanced Patterns

- Token Bucket + Redis (global rate limit)
- Hierarchical buckets (org → user → API)
- Adaptive rate limiting (based on CPU / latency)

---
