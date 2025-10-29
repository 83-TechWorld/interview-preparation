Perfect — this is one of the *core backend performance mastery topics* and something FAANG, Stripe, Netflix, and database-focused companies probe deeply in interviews.

---

# 📘 Database Indexing – Advanced Interview Q&A

> **Masterclass on Indexing, Data Access Patterns, and Query Optimization**
>
> Covers: B-Trees, Hash Indexes, Covering Indexes, Clustered/Non-Clustered/Composite Indexes, Write Amplification, and Query Plans.

---

## 🧠 1. Designing for “Hot” vs “Cold” Data Access Patterns

### ❓Q1. Explain how you would design indexing for “hot” (frequently accessed) vs “cold” (rarely accessed) data in a large table (say `orders` with 1B rows).

**Answer:**

* **Hot data:** Frequently updated or queried data (e.g., last 7 days of orders).

  * ✅ Keep in **main table** (`orders_active`)
  * ✅ Use **in-memory indexes (B-Tree)** on columns like `order_id`, `customer_id`, `status`
  * ✅ Partition by **date** or **region**
  * ✅ Use **autovacuum**/reindex more aggressively (Postgres)
* **Cold data:** Archived orders (older than 1 year)

  * ✅ Move to **archive table** or **cold storage**
  * ✅ Use **compressed storage** and minimal indexes (only `order_id`)
  * ✅ Queries use **UNION ALL** or **partition pruning**

> **Key Concept:**
> Minimize index maintenance on cold data. Index writes scale with updates — so cold partitions should be “read-optimized,” while hot partitions stay “write-optimized.”

---

## 🌳 2. Understanding B-Trees, Hash Indexes, and Covering Indexes

### ❓Q2. Why do most databases default to **B-tree indexes** instead of **Hash indexes**?

**Answer:**

| Feature                                      | B-Tree   | Hash    |
| -------------------------------------------- | -------- | ------- |
| Supports range queries (`>`, `<`, `BETWEEN`) | ✅        | ❌       |
| Equality lookups (`=`)                       | ✅        | ✅       |
| Ordered data storage                         | ✅        | ❌       |
| Space utilization                            | Moderate | Compact |
| Maintenance cost                             | Moderate | Low     |

> **Example:**
>
> ```sql
> CREATE INDEX idx_customer_id ON orders(customer_id); -- B-tree (default)
> ```
>
> B-trees allow both:
>
> ```sql
> SELECT * FROM orders WHERE customer_id = 10;       -- equality
> SELECT * FROM orders WHERE customer_id > 10;       -- range
> ```

Hash indexes only help in equality lookups.
**PostgreSQL** supports hash indexes, but they’re **rarely used** due to lack of ordering and limited optimizer benefit.

---

### ❓Q3. What is a **covering index**, and when should you use one?

**Answer:**
A **covering index** contains **all columns** required to satisfy a query — avoiding table lookups (index-only scans).

> **Example:**
>
> ```sql
> CREATE INDEX idx_orders_covering ON orders(customer_id, order_date, total_amount);
> ```
>
> And:
>
> ```sql
> SELECT customer_id, order_date, total_amount
> FROM orders
> WHERE customer_id = 123;
> ```
>
> The database serves this **entire query from the index**, no table fetch needed → reduces I/O drastically.

**Trade-offs:**

* ✅ Faster read (no table access)
* ❌ More storage
* ❌ Higher write/update overhead

---

## 🧩 3. Clustered, Non-Clustered, and Composite Indexes

### ❓Q4. What’s the difference between **clustered** and **non-clustered** indexes? How does PostgreSQL handle it?

**Answer:**

* **Clustered Index:** Sorts and stores the table rows **physically** according to the index order.

  * Only **one clustered index** per table.
  * In MySQL (InnoDB): the **PRIMARY KEY** is the clustered index.
* **Non-clustered Index:** Stores a separate index structure pointing to row locations.

**PostgreSQL Note:**
Postgres doesn’t maintain physical clustering dynamically — you must manually cluster:

```sql
CLUSTER orders USING idx_order_date;
```

---

### ❓Q5. When should you create a **composite index**, and how does column order matter?

**Answer:**
Composite index helps when queries filter by **multiple columns together**.

> **Example:**
>
> ```sql
> CREATE INDEX idx_orders_cust_date ON orders(customer_id, order_date);
> ```
>
> ✅ Works for:
>
> ```sql
> WHERE customer_id = 10 AND order_date > '2024-01-01';
> ```
>
> ❌ Doesn’t help:
>
> ```sql
> WHERE order_date > '2024-01-01';
> ```

> **Rule:** The **leftmost prefix rule** — indexes help when filters start from the leftmost column sequence.

---

## ⚙️ 4. Write Amplification, Maintenance, and Storage Overhead

### ❓Q6. Why does adding more indexes slow down writes?

**Answer:**
Each **INSERT/UPDATE/DELETE** must also:

* Update **every index** where indexed columns change.
* Rebalance B-Tree nodes.
* Write index changes to **WAL (Write-Ahead Log)** for durability.

> **Example:**
>
> * Table with 5 indexes → 1 row update = 6 writes (1 table + 5 indexes)
> * Higher **write amplification**

> **Optimization:**
> Use indexes only on **frequently queried** columns, not on columns that **change frequently**.

---

## 🧭 5. Query Plan Analysis and Index Efficiency

### ❓Q7. How would you identify if an index is helping or hurting a query?

**Answer:**
Use **EXPLAIN ANALYZE**.

> ```sql
> EXPLAIN ANALYZE
> SELECT * FROM orders WHERE customer_id = 123;
> ```
>
> Output:
>
> ```
> Index Scan using idx_orders_customer_id on orders ...
> (cost=0.43..25.00 rows=10 width=64)
> ```
>
> ✅ Index Scan = good
> ❌ Seq Scan = full table scan (index not used)

> **Signs an index hurts performance:**
>
> * Too many **random I/O reads** (esp. small selective indexes)
> * Index maintenance slowing **insert/update/delete**
> * **Query planner misestimation** causing wrong index usage

---

### ❓Q8. What’s the difference between **index scan**, **bitmap index scan**, and **sequential scan**?

| Scan Type             | Description                     | Use Case                       |
| --------------------- | ------------------------------- | ------------------------------ |
| **Index Scan**        | Direct lookup via index         | Highly selective filters       |
| **Bitmap Index Scan** | Combines multiple index results | Multiple `OR` conditions       |
| **Sequential Scan**   | Reads full table                | Low selectivity or small table |

---

## 🧮 6. Advanced Scenarios

### ❓Q9. You have a `users` table with 100M rows and frequent lookups by both `email` and `last_login`.

Would you use two separate indexes or a composite one?

**Answer:**
Use **two separate indexes**:

```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_last_login ON users(last_login);
```

Because:

* Queries are independent (`WHERE email = ?` vs `WHERE last_login > ?`)
* Composite `(email, last_login)` only helps if both columns are filtered together

---

### ❓Q10. When is **no index** actually better?

**Answer:**

* On **write-heavy** tables where reads are rare
* For **staging tables**, **batch inserts**, or **ETL pipelines**
* For **temporary tables** in analytics

Adding indexes can degrade write throughput by 50–200% due to maintenance overhead.

---

## 🧰 7. Tools and Monitoring

| Tool                                  | Purpose                         |
| ------------------------------------- | ------------------------------- |
| **pg_stat_user_indexes (PostgreSQL)** | Monitor unused indexes          |
| **pg_repack**                         | Rebuild bloated indexes         |
| **pg_stat_statements**                | Track slow queries              |
| **pt-index-usage (Percona)**          | Index usage in MySQL            |
| **Elasticsearch Index Stats API**     | Monitor shard-level performance |

---

## ☁️ 8. NoSQL Indexing Analogies

| System            | Index Type                                                | Similar Concept                |
| ----------------- | --------------------------------------------------------- | ------------------------------ |
| **Cassandra**     | Secondary Index, Materialized View                        | Similar to non-clustered index |
| **DynamoDB**      | Global Secondary Index (GSI), Local Secondary Index (LSI) | GSI = cross-partition index    |
| **Elasticsearch** | Inverted Index                                            | Optimized for text search      |

---

## ⚖️ 9. Real Interview Challenge

> **Scenario:**
> You manage a `transactions` table (2 billion rows).
> 80% queries filter by `account_id` and date range (`created_at`),
> 20% queries filter by `status` and `amount`.

### 💡 How to index?

**Step 1:** Composite index for common query pattern:

```sql
CREATE INDEX idx_txn_account_date ON transactions(account_id, created_at);
```

**Step 2:** Partial index for rare queries:

```sql
CREATE INDEX idx_txn_status_partial ON transactions(status)
WHERE status IN ('FAILED', 'PENDING');
```

**Step 3:** Table partitioning by month for fast pruning.

---

## 🧩 10. Summary Table

| Concept         | Use Case              | Trade-off                  |
| --------------- | --------------------- | -------------------------- |
| B-Tree          | General queries       | Balanced read/write        |
| Hash            | Equality only         | Fast lookup, no range      |
| Covering Index  | Index-only scans      | High storage               |
| Clustered Index | Ordered access        | Expensive reordering       |
| Composite Index | Multi-column filters  | Leftmost-prefix constraint |
| Partial Index   | Filtered subset       | Less maintenance           |
| No Index        | Write-heavy workloads | Full scans on reads        |

---

> **Remember:**
> Indexing is not just about *speeding up queries*.
> It’s about *balancing read vs write cost* and *storage vs latency trade-offs*.

---
