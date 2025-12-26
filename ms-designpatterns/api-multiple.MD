```markdown
# 🛡️ Defending APIs from Abuse: Rate Limiting Done Right

## Problem Statement

> **If a hacker spams your API, how do you stop them—without crashing yourself?**

A naive rate-limiting approach looks like this:

1. Request arrives  
2. Query database → “How many requests has this user made?”  
3. Decide allow / deny  

❌ **This is dangerous**

- Every request now triggers **DB reads**
- An attacker can intentionally flood requests
- The **rate-limit check itself becomes the Denial-of-Service attack**
- Your database dies before your business logic is even touched

This is a classic **self-inflicted DoS**

---

## Core Requirement for a Real Solution

A production-grade rate-limiter **must**:

- ✅ Run in **O(1) constant time**
- ✅ Use **no disk I/O**
- ✅ Work entirely **in memory**
- ✅ Be **predictable under abuse**
- ✅ Scale across multiple servers / regions

That’s exactly why modern systems use **Token Bucket**

---

## ✅ The Solution: Token Bucket Algorithm


::contentReference[oaicite:0]{index=0}


### Mental Model 🪣

- Imagine a bucket with **capacity = 10 tokens**
- Tokens are added at a fixed rate (e.g. **2 tokens/sec**)
- **Each request consumes 1 token**

### Decision Path (Critical Simplicity)

```

Request arrives
↓
Is token available?
├─ Yes → consume token → allow request
└─ No  → reject immediately (HTTP 429)

```

No queues  
No waiting  
No retries  
No cascading failures  

---

## Where Does the State Live?

**Important detail most explanations skip**

Token Bucket is enforced **per key**, not globally.

### Rate-Limit Key Design

```

rate_limit:{scope}:{identifier}:{resource}

```

Example:
```

rate_limit:user:42:POST:/v1/orders

````

This gives you:
- Per-user limits
- Per-API limits
- Per-method limits
- Per-IP limits (for anonymous traffic)

Each key owns **its own bucket**

---

## What Is Stored (Only 2 Values!)

For each key, store **only**:

```text
current_tokens
last_updated_timestamp
````

Usually stored in **Redis**

No counters
No logs
No history tables

---

## Lazy Refill (No Background Jobs!)

There is **no token refiller thread**

Instead, tokens are calculated **only when a request arrives**

```text
elapsed_time = now - last_updated_timestamp
new_tokens   = elapsed_time * refill_rate
current_tokens = min(capacity, current_tokens + new_tokens)
```

Then:

* If `current_tokens >= 1` → allow & decrement
* Else → reject immediately

---

## Atomicity (Why Lua Scripts Are Used)

All of this logic must be **atomic**

Otherwise:

* Two concurrent requests might both pass
* Limits become meaningless under load

### Standard Approach

* Use **Redis Lua scripts**
* Entire calculation + update runs as **one atomic operation**

This is why Redis is the industry default for rate limiting

---

## Why This Scales So Well

| Property           | Result                  |
| ------------------ | ----------------------- |
| In-memory only     | No DB bottlenecks       |
| Constant time      | Predictable latency     |
| Stateless services | Easy horizontal scaling |
| Deterministic      | Safe under attack       |
| Edge-friendly      | Works at gateways/CDNs  |

---

## How Big Companies Use This

* **Stripe**
  Per-API-key token buckets to protect payment endpoints

* **Shopify**
  Per-store and per-app rate buckets

* **Cloudflare**
  Token buckets enforced at the **edge**, before traffic hits origin

If your system survives traffic spikes gracefully, a token bucket is almost certainly involved.

---

## Other Rate-Limiting Algorithms (When to Use What)

### 1️⃣ Fixed Window Counter ❌

**How it works**

* Count requests per minute

**Problems**

* Burst at window boundary
* Easy to exploit

**Use only for**

* Simple dashboards
* Non-critical internal tools

---

### 2️⃣ Sliding Window Log ❌

**How it works**

* Store timestamps of all requests

**Problems**

* High memory usage
* Cleanup complexity

---

### 3️⃣ Sliding Window Counter ⚠️

**How it works**

* Approximation using weighted windows

**Better than fixed**

* Still less precise than Token Bucket

---

### 4️⃣ Leaky Bucket ⚠️

**How it works**

* Requests are queued and drained at fixed rate

**Problems**

* Queues build up
* Increases latency under load

---

### ✅ 5️⃣ Token Bucket (Best Default)

**Why it wins**

* Allows bursts
* No queues
* Simple math
* Industry standard

---

## Libraries & Tools You Can Use

### JVM / Spring Boot

* **Bucket4j**
* **Resilience4j RateLimiter**
* **Spring Cloud Gateway (RedisRateLimiter)**

### Node.js

* `rate-limiter-flexible`
* `express-rate-limit` (Redis-backed)

### Go

* `golang.org/x/time/rate`

### Infrastructure / Edge

* **NGINX** (limit_req_zone)
* **Envoy**
* **Kong**
* **AWS API Gateway throttling**

---

## Advanced Real-World Patterns

### 🔹 Multi-Level Rate Limiting

Apply limits at:

* IP level
* User level
* API key level
* Endpoint level

### 🔹 Fail-Closed Strategy

If Redis is down:

* Default to **reject**
* Protect system integrity

### 🔹 Separate Buckets for Reads vs Writes

Writes are expensive → tighter limits

---

## Final Takeaway

> **Rate limiting is not about fairness — it’s about survival**

* Databases should never be in the rate-limit path
* Counters are dangerous under attack
* Token Bucket is fast, deterministic, and battle-tested

If your system stays up during traffic storms,
a **tiny in-memory bucket** is probably doing the hardest job silently 🪣🚀

---

```
```
