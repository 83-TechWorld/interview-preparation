![Image](https://miro.medium.com/v2/resize%3Afit%3A1400/1%2Ab45bS64M7snuUJF1b9cVzA.png?utm_source=chatgpt.com)

![Image](https://www.mysql.com/common/images/products/MySQL_Cluster_Scalability_v1.png?utm_source=chatgpt.com)

![Image](https://assets.digitalocean.com/articles/understanding_sharding/DB_image_3_cropped.png?utm_source=chatgpt.com)

![Image](https://media.geeksforgeeks.org/wp-content/uploads/20231228162624/Sharding.jpg?utm_source=chatgpt.com)

Below is a **deep, layered explanation** of the **problem, solution, terminology, and frameworks** behind **Pinterest’s scaling decision**, written in a **systems-design + real-world engineering** mindset.

---

## 📌 Case Study: How Pinterest Survived 100M Users When MySQL Almost Collapsed

---

## 1️⃣ The REAL Problem (Not “Big Data”)

Most people think Pinterest’s issue was **data volume**.
That’s wrong.

### ❌ The actual enemy: **Relational Complexity at Scale**

Pinterest’s domain model looked innocent:

```
User → Boards → Pins → Likes → Repins → Comments
```

But relational databases suffer when **three things happen together**:

| Factor                     | Why it hurts                        |
| -------------------------- | ----------------------------------- |
| Billions of rows           | Indexes grow, cache misses increase |
| Many-to-many relationships | JOIN explosion                      |
| High read concurrency      | Locks + CPU saturation              |

### 🔥 One Page Load = JOIN Storm

To render **one board page**, MySQL had to:

* Join `boards`
* Join `board_pins`
* Join `pins`
* Join `likes`
* Join `users`
* Apply ordering + pagination

👉 Even with indexes, **JOIN cost is multiplicative**, not linear.

---

## 2️⃣ Why MySQL Failed (JOINs + Sharding = 💣)

### ❗ The Sharding Wall

MySQL **cannot JOIN across shards**.

Once you shard:

* `JOIN board_pins ON pins.id = board_pins.pin_id`
* ❌ becomes **impossible** if `pins` live on shard A and `board_pins` on shard B

### Result:

* Cross-shard JOINs
* Distributed locks
* Query planners losing optimization
* Latency spikes → cascading failures

---

## 3️⃣ The “Unthinkable” Decision 😱

### ❌ They removed:

* Foreign Keys
* Cascading deletes
* Referential integrity
* SQL JOINs

### ✅ They accepted:

* Application-managed relationships
* Eventual consistency
* Developer responsibility

> **MySQL stopped being a “relational” database.
> It became a fast key-value store.**

---

## 4️⃣ The Core Idea: **Application-Level Sharding**

This architecture later became known internally as **“Spotlight”**.

### New Principle:

> **The database stores data.
> The application understands relationships.**

---

## 5️⃣ Universal ID (The Masterstroke)

### 🎯 Why IDs Matter in Distributed Systems

Without JOINs, you must answer instantly:

* Where does this record live?
* What type is it?
* Which shard owns it?

### 🧠 Pinterest’s Custom 64-bit ID

```
| Shard ID | Type ID | Local ID |
|  16 bits | 10 bits | 38 bits  |
```

#### Breakdown:

| Section  | Purpose              |
| -------- | -------------------- |
| Shard ID | Which MySQL server   |
| Type ID  | Pin / Board / User   |
| Local ID | Auto-increment value |

### ⚡ Why this is powerful

No metadata service
No lookup table
No network hop

Just bit parsing.

```text
ID → parse bits → shard #40 → connect → SELECT
```

---

## 6️⃣ Mapping Tables (Adjacency Lists)

Since JOINs are gone, relationships move into **explicit tables**.

### Example: `board_has_pins`

| Column      | Meaning |
| ----------- | ------- |
| board_id    | Parent  |
| pin_id      | Child   |
| sequence_id | Order   |

This is effectively a **graph edge list**.

---

## 7️⃣ How a Board Page Is Rendered (Step-by-Step)

### Old Way (JOIN-Based)

```
1 SQL query
→ Massive JOIN
→ Slow, blocking
```

### New Way (Distributed Fan-Out)

```
1️⃣ Query board shard → get pin_ids
2️⃣ Parse pin_ids → compute shard locations
3️⃣ Fire parallel queries to shards
4️⃣ Merge results in memory
```

### 🧵 This is called:

* Scatter–Gather
* Fan-out / Fan-in
* Parallel fetch pattern

---

## 8️⃣ New Engineering Challenges They Accepted

| Challenge       | How they handled it   |
| --------------- | --------------------- |
| No foreign keys | App-level validation  |
| Orphaned data   | Async cleanup jobs    |
| Ordering        | Explicit sequence_id  |
| Latency         | Parallelism + caching |
| Consistency     | Eventual consistency  |

---

## 9️⃣ Important Terminologies Explained (Plain English)

### 🔹 Manual Sharding

You decide **where data lives**, not the DB.

### 🔹 Application-Level Join

The app fetches data separately and merges it.

### 🔹 Adjacency List

A table representing relationships as rows.

### 🔹 Scatter-Gather

Send queries in parallel → combine results.

### 🔹 Dumb Storage Engine

DB only stores bytes; logic lives elsewhere.

---

## 1️⃣0️⃣ Frameworks & Tools Pinterest Used (2013 Era)

| Area        | Tools                           |
| ----------- | ------------------------------- |
| DB          | MySQL                           |
| Language    | Python                          |
| Async Fetch | Custom thread pools             |
| Caching     | Memcached                       |
| IDs         | Custom Snowflake-like generator |
| Sharding    | Hand-written logic              |

---

## 1️⃣1️⃣ Why This Worked (Key Insight)

> **At scale, relational databases are not relationship engines.
> They are persistence engines.**

Pinterest optimized for:

* Read scalability
* Predictable latency
* Horizontal growth

---

## 1️⃣2️⃣ If You Did This TODAY (Modern Stack)

You would NOT rebuild Spotlight manually.

### ✅ Modern Alternatives

| Problem             | Modern Solution   |
| ------------------- | ----------------- |
| ID generation       | Snowflake / KSUID |
| Graph relationships | Neo4j             |
| Wide relationships  | DynamoDB          |
| Global scale        | Spanner           |
| Fan-out reads       | Redis + CQRS      |
| Joins at scale      | Elasticsearch     |

---