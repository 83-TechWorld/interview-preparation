# PostgreSQL Index Complete Guide

## Table of Contents
- [What is an Index?](#what-is-an-index)
- [How Indexes Work Internally](#how-indexes-work-internally)
- [Types of Indexes in PostgreSQL](#types-of-indexes-in-postgresql)
- [Index Strategies and Use Cases](#index-strategies-and-use-cases)
- [Performance Impact](#performance-impact)
- [Tools for Index Analysis](#tools-for-index-analysis)
- [Best Practices](#best-practices)
- [Anti-Patterns to Avoid](#anti-patterns-to-avoid)
- [Real-World Examples](#real-world-examples)

## What is an Index?

An index is a database object that improves the speed of data retrieval operations on a table at the cost of additional writes and storage space. Think of it like a book's index - instead of reading the entire book to find a topic, you look it up in the index and jump directly to the relevant page.

### Key Characteristics
- **Trade-off**: Speeds up reads but slows down writes
- **Storage**: Requires additional disk space and RAM
- **Maintenance**: Must be updated with every INSERT, UPDATE, or DELETE

## How Indexes Work Internally

### B-Tree Structure (Default in PostgreSQL)

PostgreSQL primarily uses B-Tree (Balanced Tree) data structures for indexes. Here's how they work:

```
                    [Root Node: 50]
                   /                \
          [Node: 20, 35]        [Node: 65, 80]
         /      |      \        /      |      \
    [1-15]  [20-30]  [35-45] [50-60] [65-75] [80-95]
```

#### Internal Mechanics:

1. **Page Structure**: 
   - Indexes are stored in 8KB pages (default)
   - Each page contains index entries and pointers

2. **Index Entry Format**:
   ```
   [Key Value | TID (Tuple Identifier)]
   ```
   - Key Value: The indexed column value
   - TID: Physical location of the row (page number + offset)

3. **Search Process**:
   ```sql
   -- When you run:
   SELECT * FROM users WHERE id = 1000;
   
   -- PostgreSQL:
   1. Traverses B-Tree from root
   2. Compares values at each node
   3. Follows appropriate branch
   4. Reaches leaf node with TID
   5. Fetches actual row using TID
   ```

### Index Scan vs Sequential Scan

```sql
-- Sequential Scan: O(n)
-- Reads every row in the table

-- Index Scan: O(log n)
-- Uses index to find specific rows

-- Bitmap Index Scan: 
-- Builds bitmap of matching rows, then fetches all at once
```

## Types of Indexes in PostgreSQL

### 1. B-Tree Index (Default)
```sql
CREATE INDEX idx_users_email ON users(email);
```

**Use Cases:**
- Range queries (`>`, `<`, `BETWEEN`)
- Equality queries (`=`)
- Sorting operations (`ORDER BY`)
- Pattern matching with prefix (`LIKE 'prefix%'`)

**Internal Structure:**
- Balanced tree with sorted keys
- Height typically 3-4 levels for millions of rows
- Each node typically 100-500 keys

### 2. Hash Index
```sql
CREATE INDEX idx_users_id_hash ON users USING HASH (id);
```

**Use Cases:**
- Only equality comparisons (`=`)
- Fixed-size keys
- When you never need range queries

**Limitations:**
- No support for range queries
- Not WAL-logged before PostgreSQL 10
- Generally less popular than B-Tree

### 3. GiST (Generalized Search Tree)
```sql
CREATE INDEX idx_locations_point ON locations USING GIST (coordinates);
```

**Use Cases:**
- Geometric data types
- Full-text search
- Range types
- Custom data types

### 4. SP-GiST (Space-Partitioned GiST)
```sql
CREATE INDEX idx_points_spgist ON points USING SPGIST (location);
```

**Use Cases:**
- Non-balanced data structures
- Quadtrees, k-d trees
- Phone numbers, IP addresses

### 5. GIN (Generalized Inverted Index)
```sql
CREATE INDEX idx_documents_search ON documents USING GIN (to_tsvector('english', content));
```

**Use Cases:**
- Full-text search
- Array elements
- JSON/JSONB fields
- Multiple values per row

**Internal Structure:**
```
Posting Tree:
word1 -> [doc1, doc3, doc7, doc12]
word2 -> [doc2, doc3, doc5]
word3 -> [doc1, doc5, doc7]
```

### 6. BRIN (Block Range Index)
```sql
CREATE INDEX idx_logs_created_at ON logs USING BRIN (created_at);
```

**Use Cases:**
- Very large tables
- Naturally ordered data (timestamps, sequential IDs)
- When approximate results are acceptable

**Storage Efficiency:**
- Stores min/max values per block range
- Extremely small index size (often < 1% of table)

## Index Strategies and Use Cases

### Strategy 1: Composite Indexes

```sql
-- Composite index on multiple columns
CREATE INDEX idx_orders_customer_date ON orders(customer_id, order_date);

-- This index helps with:
SELECT * FROM orders WHERE customer_id = 100;
SELECT * FROM orders WHERE customer_id = 100 AND order_date > '2024-01-01';

-- But NOT with:
SELECT * FROM orders WHERE order_date > '2024-01-01';  -- Doesn't use index efficiently
```

**Leftmost Prefix Rule:**
- Index on (a, b, c) can be used for:
  - WHERE a = ?
  - WHERE a = ? AND b = ?
  - WHERE a = ? AND b = ? AND c = ?
- But not for:
  - WHERE b = ?
  - WHERE c = ?
  - WHERE b = ? AND c = ?

### Strategy 2: Covering Indexes

```sql
-- Include additional columns in index
CREATE INDEX idx_products_category_include 
ON products(category_id) 
INCLUDE (name, price);

-- Index-only scan possible:
SELECT name, price FROM products WHERE category_id = 5;
```

### Strategy 3: Partial Indexes

```sql
-- Index only specific rows
CREATE INDEX idx_orders_pending 
ON orders(created_at) 
WHERE status = 'pending';

-- Smaller index, faster for targeted queries
SELECT * FROM orders 
WHERE status = 'pending' AND created_at > NOW() - INTERVAL '1 day';
```

### Strategy 4: Expression Indexes

```sql
-- Index on computed values
CREATE INDEX idx_users_email_lower ON users(LOWER(email));

-- Helps with case-insensitive searches
SELECT * FROM users WHERE LOWER(email) = 'john@example.com';
```

## Performance Impact

### Read Performance

#### Positive Impact:
```sql
-- Without index: Sequential scan O(n)
EXPLAIN ANALYZE SELECT * FROM users WHERE email = 'test@example.com';
-- Seq Scan: 500ms for 1M rows

-- With index: Index scan O(log n)
CREATE INDEX idx_users_email ON users(email);
EXPLAIN ANALYZE SELECT * FROM users WHERE email = 'test@example.com';
-- Index Scan: 0.5ms for 1M rows
```

#### Performance Gains by Operation:
- **Point lookups**: 100-1000x faster
- **Range queries**: 10-100x faster
- **Joins**: 10-1000x faster
- **Sorting**: Can eliminate sort operation entirely

### Write Performance

#### Negative Impact:
```sql
-- Each index adds overhead to write operations
-- Example with 1M row table:

-- No indexes:
INSERT: ~0.01ms per row
UPDATE: ~0.02ms per row
DELETE: ~0.01ms per row

-- With 5 indexes:
INSERT: ~0.05ms per row (5x slower)
UPDATE: ~0.10ms per row (5x slower)
DELETE: ~0.05ms per row (5x slower)
```

#### Index Maintenance Overhead:
1. **Page Splits**: When B-Tree nodes become full
2. **Rebalancing**: Maintaining tree balance
3. **WAL Writing**: Write-ahead logging for crash recovery
4. **Vacuum**: Cleaning up dead tuples

### Memory Impact

```sql
-- Check index size
SELECT 
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
FROM pg_stat_user_indexes
ORDER BY pg_relation_size(indexrelid) DESC;

-- Check cache hit ratio
SELECT 
    sum(idx_blks_read) as idx_read,
    sum(idx_blks_hit)  as idx_hit,
    sum(idx_blks_hit) / (sum(idx_blks_hit) + sum(idx_blks_read))::float AS cache_hit_ratio
FROM pg_statio_user_indexes;
```

## Tools for Index Analysis

### 1. EXPLAIN and EXPLAIN ANALYZE

```sql
-- Basic query plan
EXPLAIN SELECT * FROM users WHERE email = 'test@example.com';

-- With execution statistics
EXPLAIN (ANALYZE, BUFFERS) SELECT * FROM users WHERE email = 'test@example.com';

-- Verbose output with all details
EXPLAIN (ANALYZE, BUFFERS, VERBOSE, TIMING, FORMAT JSON)
SELECT * FROM orders WHERE customer_id = 100;
```

#### Reading EXPLAIN Output:
```
Index Scan using idx_users_email on users
  Index Cond: (email = 'test@example.com')
  Buffers: shared hit=4
  Planning Time: 0.102 ms
  Execution Time: 0.043 ms
```

### 2. pg_stat_user_indexes

```sql
-- Find unused indexes
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE idx_scan = 0
ORDER BY schemaname, tablename;

-- Find most used indexes
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch,
    pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC
LIMIT 10;
```

### 3. pg_stat_statements Extension

```sql
-- Enable the extension
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Find slow queries that might benefit from indexes
SELECT 
    query,
    calls,
    total_exec_time,
    mean_exec_time,
    rows
FROM pg_stat_statements
WHERE query NOT LIKE '%pg_%'
ORDER BY mean_exec_time DESC
LIMIT 10;
```

### 4. pganalyze or pg_stat_kcache

```sql
-- Analyze index bloat
SELECT
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) AS index_size,
    indexrelid::regclass AS index_name,
    100 * (1 - (index_size::float / table_size::float)) AS bloat_percentage
FROM (
    SELECT
        schemaname,
        tablename,
        indexname,
        indexrelid,
        pg_relation_size(indexrelid) AS index_size,
        pg_relation_size(tablename::regclass) AS table_size
    FROM pg_stat_user_indexes
) AS stats
WHERE index_size > 0
ORDER BY bloat_percentage DESC;
```

### 5. HypoPG Extension

```sql
-- Create hypothetical index without actually building it
CREATE EXTENSION hypopg;

-- Test if an index would be useful
SELECT * FROM hypopg_create_index('CREATE INDEX ON users (email)');

-- Check if query would use the hypothetical index
EXPLAIN SELECT * FROM users WHERE email = 'test@example.com';
```

## Best Practices

### 1. Index Selectivity

```sql
-- Check selectivity before creating index
SELECT 
    column_name,
    n_distinct,
    correlation
FROM pg_stats
WHERE tablename = 'users';

-- Good selectivity (many unique values)
CREATE INDEX ON users(email);  -- Unique for each user

-- Poor selectivity (few unique values)
-- Avoid: CREATE INDEX ON users(is_active);  -- Only true/false
```

### 2. Regular Maintenance

```sql
-- Rebuild bloated indexes
REINDEX INDEX CONCURRENTLY idx_users_email;

-- Update statistics
ANALYZE users;

-- Vacuum to remove dead tuples
VACUUM ANALYZE users;
```

### 3. Monitor Index Usage

```sql
-- Create monitoring query
CREATE OR REPLACE VIEW index_usage AS
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch,
    pg_size_pretty(pg_relation_size(indexrelid)) AS size,
    CASE
        WHEN idx_scan = 0 THEN 'UNUSED'
        WHEN idx_scan < 100 THEN 'RARELY USED'
        WHEN idx_scan < 1000 THEN 'OCCASIONALLY USED'
        ELSE 'FREQUENTLY USED'
    END AS usage_category
FROM pg_stat_user_indexes
ORDER BY idx_scan;
```

### 4. Index Naming Convention

```sql
-- Use consistent naming
-- idx_<table>_<columns>
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_orders_customer_date ON orders(customer_id, order_date);

-- For unique constraints
-- uq_<table>_<columns>
CREATE UNIQUE INDEX uq_users_email ON users(email);

-- For partial indexes
-- idx_<table>_<columns>_where_<condition>
CREATE INDEX idx_orders_pending_where_status ON orders(created_at) WHERE status = 'pending';
```

## Anti-Patterns to Avoid

### 1. Over-Indexing

```sql
-- BAD: Creating indexes on every column
CREATE INDEX ON users(id);
CREATE INDEX ON users(email);
CREATE INDEX ON users(first_name);
CREATE INDEX ON users(last_name);
CREATE INDEX ON users(created_at);
CREATE INDEX ON users(updated_at);
-- Result: Slow writes, excessive storage

-- GOOD: Index only frequently queried columns
CREATE INDEX ON users(email);  -- Used for login
CREATE INDEX ON users(created_at) WHERE status = 'active';  -- For recent active users
```

### 2. Indexing Low-Cardinality Columns

```sql
-- BAD: Index on boolean or status with few values
CREATE INDEX ON users(is_active);  -- Only true/false
CREATE INDEX ON orders(status);  -- Only 'pending', 'completed', 'cancelled'

-- GOOD: Use partial index if needed
CREATE INDEX ON orders(created_at) WHERE status = 'pending';
```

### 3. Wrong Index Type

```sql
-- BAD: Using B-tree for full-text search
CREATE INDEX ON documents(content);  -- Won't help with text search

-- GOOD: Use appropriate index type
CREATE INDEX ON documents USING GIN(to_tsvector('english', content));
```

### 4. Ignoring Composite Index Order

```sql
-- BAD: Wrong column order in composite index
-- If you mostly filter by date first:
CREATE INDEX ON logs(user_id, created_at);

-- GOOD: Most selective column first
CREATE INDEX ON logs(created_at, user_id);
```

### 5. Not Considering Index-Only Scans

```sql
-- BAD: Partial index coverage
CREATE INDEX ON products(category_id);
-- Query still needs table access:
SELECT id, name, price FROM products WHERE category_id = 5;

-- GOOD: Include frequently accessed columns
CREATE INDEX ON products(category_id) INCLUDE (name, price);
```

### 6. Duplicate or Redundant Indexes

```sql
-- BAD: Redundant indexes
CREATE INDEX ON users(email);
CREATE INDEX ON users(email, created_at);  -- First index is redundant

-- The second index can handle queries that the first one can
```

### 7. Not Maintaining Indexes

```sql
-- BAD: Never checking index bloat or usage
-- Indexes can become bloated over time

-- GOOD: Regular maintenance
-- Schedule regular REINDEX operations
-- Monitor with pg_stat_user_indexes
-- Remove unused indexes
```

## Real-World Examples

### Example 1: E-commerce Order System

```sql
-- Table structure
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    order_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10,2),
    shipping_address JSONB
);

-- Analyze query patterns
-- Pattern 1: Customer order history
CREATE INDEX idx_orders_customer_date 
ON orders(customer_id, order_date DESC);

-- Pattern 2: Pending orders for fulfillment
CREATE INDEX idx_orders_pending 
ON orders(order_date) 
WHERE status = 'pending';

-- Pattern 3: Revenue reporting by date
CREATE INDEX idx_orders_date_amount 
ON orders(order_date, total_amount);

-- Pattern 4: Address search
CREATE INDEX idx_orders_shipping_address 
ON orders USING GIN (shipping_address);
```

### Example 2: User Authentication System

```sql
-- Table structure
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255),
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Critical for login performance
CREATE UNIQUE INDEX uq_users_email 
ON users(LOWER(email));

-- For username lookups
CREATE UNIQUE INDEX uq_users_username 
ON users(LOWER(username));

-- For admin queries on recent inactive users
CREATE INDEX idx_users_inactive_recent 
ON users(last_login) 
WHERE is_active = false;

-- Query optimization
EXPLAIN ANALYZE 
SELECT * FROM users 
WHERE LOWER(email) = 'user@example.com';
```

### Example 3: Time-Series Data

```sql
-- Table structure
CREATE TABLE metrics (
    id BIGSERIAL PRIMARY KEY,
    device_id UUID NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    value NUMERIC,
    timestamp TIMESTAMP NOT NULL
);

-- BRIN index for time-series data
CREATE INDEX idx_metrics_timestamp_brin 
ON metrics USING BRIN (timestamp);

-- Composite B-tree for device-specific queries
CREATE INDEX idx_metrics_device_time 
ON metrics(device_id, timestamp DESC);

-- For specific metric analysis
CREATE INDEX idx_metrics_name_time 
ON metrics(metric_name, timestamp DESC)
WHERE value IS NOT NULL;
```

## Advanced Topics

### Index Visibility and Transactions

```sql
-- Indexes are immediately visible after creation
-- But CONCURRENTLY option allows non-blocking creation
CREATE INDEX CONCURRENTLY idx_large_table ON large_table(column);

-- Monitor progress
SELECT * FROM pg_stat_progress_create_index;
```

### HOT Updates (Heap-Only Tuples)

```sql
-- HOT updates avoid index maintenance
-- Only works if indexed columns aren't updated

-- Enable HOT updates by not indexing frequently updated columns
-- Good for columns like 'last_modified', 'view_count'
```

### Index Deduplication (PostgreSQL 13+)

```sql
-- B-tree indexes automatically deduplicate entries
-- Reduces index size for low-cardinality columns

-- Check deduplication stats
SELECT 
    indexrelid::regclass AS index_name,
    avg_leaf_density,
    leaf_pages
FROM pgstatindex('idx_name');
```

## Troubleshooting Guide

### Problem: Index Not Being Used

```sql
-- Check statistics are up to date
ANALYZE table_name;

-- Check random_page_cost setting
SHOW random_page_cost;  -- Lower if using SSD

-- Force index usage for testing
SET enable_seqscan = OFF;
EXPLAIN SELECT...
SET enable_seqscan = ON;  -- Reset
```

### Problem: Slow Index Creation

```sql
-- Increase maintenance_work_mem
SET maintenance_work_mem = '1GB';
CREATE INDEX...

-- Use parallel index creation (PostgreSQL 11+)
SET max_parallel_maintenance_workers = 4;
CREATE INDEX...
```

### Problem: Index Bloat

```sql
-- Check bloat percentage
WITH btree_index_atts AS (
    SELECT 
        nspname, 
        indexname, 
        tablename,
        regexp_split_to_table(indkey::text, ' ')::smallint AS attnum,
        indexrelid
    FROM pg_index
    JOIN pg_class ON pg_class.oid = pg_index.indexrelid
    JOIN pg_namespace ON pg_namespace.oid = pg_class.relnamespace
    JOIN pg_stat_user_indexes ON pg_stat_user_indexes.indexrelid = pg_index.indexrelid
)
SELECT 
    nspname,
    indexname,
    tablename,
    pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
FROM btree_index_atts
GROUP BY nspname, indexname, tablename, indexrelid
ORDER BY pg_relation_size(indexrelid) DESC;

-- Fix with REINDEX
REINDEX INDEX CONCURRENTLY index_name;
```

## Summary and Key Takeaways

1. **Indexes are a trade-off**: Faster reads but slower writes
2. **Choose the right index type**: B-tree for most cases, GIN for arrays/JSON, BRIN for time-series
3. **Follow the leftmost prefix rule** for composite indexes
4. **Monitor selectivity**: Indexes on low-cardinality columns are often useless
5. **Use EXPLAIN ANALYZE** to verify index usage
6. **Maintain regularly**: VACUUM, ANALYZE, and REINDEX when needed
7. **Avoid over-indexing**: Each index has storage and maintenance costs
8. **Consider partial and expression indexes** for specific use cases
9. **Track usage** with pg_stat_user_indexes
10. **Test with hypothetical indexes** before creating real ones

## References and Further Reading

- [PostgreSQL Official Documentation - Indexes](https://www.postgresql.org/docs/current/indexes.html)
- [PostgreSQL Index Types](https://www.postgresql.org/docs/current/indexes-types.html)
- [Query Planning and EXPLAIN](https://www.postgresql.org/docs/current/using-explain.html)
- [Index Maintenance](https://www.postgresql.org/docs/current/routine-reindex.html)
- [pg_stat_user_indexes](https://www.postgresql.org/docs/current/monitoring-stats.html)