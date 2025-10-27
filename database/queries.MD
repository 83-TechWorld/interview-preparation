I can certainly provide a comprehensive set of **Medium and Hard PostgreSQL Interview Questions** covering the core topics you mentioned, suitable for cracking interviews at FAANG companies.

Since a full GitHub `README.md` file with all the code blocks and markdown formatting is too extensive to generate directly as a single response, I will provide the content structure and all the necessary descriptions, questions, solutions, and explanations. You can easily copy and paste this into a `README.md` file.

Here is the content, structured logically for a GitHub README.

-----

# 💻 PostgreSQL Interview Questions for FAANG

This repository provides a curated list of **Medium and Hard** PostgreSQL/SQL interview questions, focusing on complex queries and concepts essential for roles at top-tier tech companies.

## 🗃️ Setup: Sample Tables

We'll use three common tables for all examples.

| Table | Description |
| :--- | :--- |
| `employees` | Employee details (ID, Name, Department ID, Salary, Manager ID, Hire Date). |
| `departments` | Department details (ID, Name). |
| `sales` | Transactional data (Transaction ID, Product ID, Employee ID, Sale Amount, Sale Date). |

### 🛠️ Table Creation Queries

```sql
-- 1. Departments Table
CREATE TABLE departments (
    dept_id SERIAL PRIMARY KEY,
    dept_name VARCHAR(100) NOT NULL UNIQUE
);

-- 2. Employees Table
CREATE TABLE employees (
    emp_id SERIAL PRIMARY KEY,
    emp_name VARCHAR(100) NOT NULL,
    dept_id INT REFERENCES departments(dept_id),
    salary NUMERIC(10, 2) NOT NULL,
    manager_id INT REFERENCES employees(emp_id), -- Self-referencing FK
    hire_date DATE NOT NULL
);

-- 3. Sales Table
CREATE TABLE sales (
    txn_id SERIAL PRIMARY KEY,
    emp_id INT REFERENCES employees(emp_id),
    product_id VARCHAR(50) NOT NULL,
    sale_amount NUMERIC(10, 2) NOT NULL,
    sale_date DATE NOT NULL
);
```

\*\*

-----

## ⭐️ Medium Difficulty Queries

These questions test a solid understanding of Joins, Grouping, Filtering, and basic Aggregate/Window Functions.

### 1\. Second Highest Salary (Partition, Rank)

| Topic | Description | Question |
| :--- | :--- | :--- |
| **Window Function** | Using `DENSE_RANK()` for accurate ranking and `PARTITION BY` to rank within groups. | Write a query to find the **second highest salary** for each department. |

#### Solution

```sql
WITH RankedSalaries AS (
    SELECT
        e.emp_name,
        d.dept_name,
        e.salary,
        DENSE_RANK() OVER (PARTITION BY d.dept_id ORDER BY e.salary DESC) as salary_rank
    FROM
        employees e
    JOIN
        departments d ON e.dept_id = d.dept_id
)
SELECT
    dept_name,
    emp_name,
    salary
FROM
    RankedSalaries
WHERE
    salary_rank = 2;
```

#### Explanation

1.  **CTE (`RankedSalaries`):** A Common Table Expression is used to first calculate the rank.
2.  **`DENSE_RANK() OVER (...)`:** This is a window function that assigns a rank to each employee's salary.
      * **`PARTITION BY d.dept_id`:** Resets the ranking for each unique department.
      * **`ORDER BY e.salary DESC`:** Ranks salaries from highest to lowest. `DENSE_RANK()` is used instead of `RANK()` to avoid gaps in the sequence if there are ties for the top salary.
3.  **Final `SELECT`:** Filters the CTE result to only show records where `salary_rank` is **2**.

### 2\. Monthly Sales Cumulative Total (Window Function, Order By)

| Topic | Description | Question |
| :--- | :--- | :--- |
| **Window Function** | Using a Window function with an `ORDER BY` clause for a running total. | Calculate the **running/cumulative total of sales amount** over time for the entire company. |

#### Solution

```sql
SELECT
    sale_date,
    SUM(sale_amount) as daily_sales,
    SUM(SUM(sale_amount)) OVER (ORDER BY sale_date) as cumulative_sales_total
FROM
    sales
GROUP BY
    sale_date
ORDER BY
    sale_date;
```

#### Explanation

1.  **`SUM(sale_amount)` with `GROUP BY sale_date`:** Calculates the total sales for each specific day (`daily_sales`).
2.  **`SUM(SUM(sale_amount)) OVER (ORDER BY sale_date)`:** This is the key.
      * The **inner `SUM(sale_amount)`** is the daily total (from the grouping).
      * The **outer `SUM(...) OVER`** then calculates the cumulative sum of these daily totals.
      * **`ORDER BY sale_date`:** Defines the "window" for the cumulative sum, ensuring the sum increases sequentially by date.

### 3\. Employees with Above-Average Department Salary (Subquery, Joins)

| Topic | Description | Question |
| :--- | :--- | :--- |
| **Correlated Subquery** | Using a subquery that depends on the outer query for filtering. | List employees whose salary is **strictly greater** than the average salary of their own department. |

#### Solution

```sql
SELECT
    e.emp_name,
    e.salary,
    d.dept_name
FROM
    employees e
JOIN
    departments d ON e.dept_id = d.dept_id
WHERE
    e.salary > (
        SELECT
            AVG(salary)
        FROM
            employees
        WHERE
            dept_id = e.dept_id -- Correlated Subquery condition
    );
```

#### Explanation

1.  **Outer Query:** Selects the employee details and joins with `departments`.
2.  **`WHERE e.salary > (...)`:** Filters the outer query rows.
3.  **Correlated Subquery:** The inner `SELECT AVG(salary)` calculates the average salary. Crucially, the `WHERE dept_id = e.dept_id` ties the inner query to the current row (`e`) of the outer query, ensuring the average is calculated **only for that employee's department**.

-----

## 🚀 Hard Difficulty Queries (FAANG Level)

These questions require combining multiple advanced techniques like CTEs, Window Functions, and advanced joins/aggregations, often relating to hierarchical data or complex metrics.

### 4\. Manager Hierarchy (Recursive CTE)

| Topic | Description | Question |
| :--- | :--- | :--- |
| **Recursive CTE** | Using a CTE that references itself to traverse hierarchical data (like a tree structure). | For every employee, list their full **chain of command** (i.e., their direct manager, the manager's manager, and so on, up to the top-most manager/CEO). |

#### Solution

```sql
WITH RECURSIVE ManagerHierarchy AS (
    -- Anchor Member (The starting point: employees with direct managers)
    SELECT
        e.emp_id,
        e.emp_name,
        e.manager_id,
        1 as level,
        CAST(e.emp_name AS TEXT) as hierarchy_path
    FROM
        employees e
    WHERE
        e.manager_id IS NOT NULL

    UNION ALL

    -- Recursive Member (The step: link to the next manager up the chain)
    SELECT
        h.emp_id,
        h.emp_name,
        m.manager_id,
        h.level + 1,
        h.hierarchy_path || ' -> ' || m.emp_name
    FROM
        ManagerHierarchy h
    JOIN
        employees m ON h.manager_id = m.emp_id
)
SELECT
    emp_name,
    hierarchy_path
FROM
    ManagerHierarchy
ORDER BY
    emp_id, level DESC;
```

#### Explanation

1.  **`WITH RECURSIVE`:** Declares a recursive CTE, which can call itself.
2.  **Anchor Member:** The first `SELECT` defines the starting set (all employees who report to someone). It initializes the `level` and `hierarchy_path`.
3.  **`UNION ALL`:** Combines the anchor result with the recursive result.
4.  **Recursive Member:** Joins the CTE (`ManagerHierarchy h`) back to the `employees` table (`m`) on `h.manager_id = m.emp_id`.
      * It finds the manager (`m`) of the current manager in the hierarchy (`h`).
      * It increments the `level` and concatenates the new manager's name to the `hierarchy_path`.
      * This process continues until the `manager_id` is `NULL` (the top of the hierarchy).

### 5\. First Purchase Date Per Customer (Lateral Join)

| Topic | Description | Question |
| :--- | :--- | :--- |
| **Lateral Join** | Combining an outer query with a new table generated for *each* row of the outer query. | For each employee who made a sale, find the **first product they ever sold** and the date it happened. |

#### Data Modeling Note:

To simplify this for demonstration, we will assume `emp_id` in the `sales` table acts as a pseudo-"customer/seller" identifier.

#### Solution

```sql
SELECT
    e.emp_name,
    d.dept_name,
    first_sale.product_id,
    first_sale.sale_date,
    first_sale.sale_amount
FROM
    employees e
JOIN
    departments d ON e.dept_id = d.dept_id
JOIN LATERAL (
    SELECT
        s.product_id,
        s.sale_date,
        s.sale_amount
    FROM
        sales s
    WHERE
        s.emp_id = e.emp_id -- Correlates with the outer table 'e'
    ORDER BY
        s.sale_date ASC, s.txn_id ASC
    LIMIT 1
) AS first_sale ON TRUE; -- The 'ON TRUE' is common for lateral joins
```

#### Explanation

1.  **`JOIN LATERAL`:** This is the critical part. The subquery aliased as `first_sale` runs **once for every row** returned by the outer `employees` query (`e`).
2.  **Correlated Subquery in `LATERAL`:** The `WHERE s.emp_id = e.emp_id` ties the `sales` data to the current employee in the outer query.
3.  **`ORDER BY / LIMIT 1`:** Inside the `LATERAL` subquery, we efficiently find the single row representing the earliest sale (by date and transaction ID as a tie-breaker).
4.  **Result:** The final `SELECT` combines the employee details with the single-row result from the `first_sale` lateral join.

### 6\. Departmental Sales Analysis (CTE, Aggregate, Window)

| Topic | Description | Question |
| :--- | :--- | :--- |
| **Complex Aggregation** | Mixing standard aggregates, grouping, and advanced window functions to find a comparative metric. | Find the **top 3 departments** based on total sales amount, and for each of these top departments, calculate their sales amount as a **percentage of the total company sales**. |

#### Solution

```sql
WITH DepartmentSales AS (
    -- 1. Calculate total sales per department
    SELECT
        d.dept_name,
        SUM(s.sale_amount) AS total_dept_sales
    FROM
        sales s
    JOIN
        employees e ON s.emp_id = e.emp_id
    JOIN
        departments d ON e.dept_id = d.dept_id
    GROUP BY
        d.dept_name
),
RankedDepartmentSales AS (
    -- 2. Rank departments and calculate total company sales
    SELECT
        ds.dept_name,
        ds.total_dept_sales,
        -- Calculate total sales across ALL departments (The Grand Total)
        SUM(ds.total_dept_sales) OVER () AS grand_total_sales,
        -- Rank departments by their total sales
        RANK() OVER (ORDER BY ds.total_dept_sales DESC) AS dept_rank
    FROM
        DepartmentSales ds
)
SELECT
    dept_name,
    total_dept_sales,
    -- Calculate percentage
    ROUND((total_dept_sales / grand_total_sales) * 100, 2) AS percent_of_company_sales
FROM
    RankedDepartmentSales
WHERE
    dept_rank <= 3 -- 3. Filter for the top 3
ORDER BY
    total_dept_sales DESC;
```

#### Explanation

1.  **`DepartmentSales` CTE:** A standard aggregate query to get the total sales for each department.
2.  **`RankedDepartmentSales` CTE:** This is where the Window Function is used.
      * **`SUM(ds.total_dept_sales) OVER ()`:** This calculates the **grand total sales** across all departments. The empty `OVER()` clause means the window is the *entire result set*, calculating an un-partitioned aggregate.
      * **`RANK() OVER (ORDER BY ...)`:** Ranks the departments based on their sales.
3.  **Final `SELECT`:**
      * Filters the result to keep only the top 3 departments (`WHERE dept_rank <= 3`).
      * Calculates the percentage by dividing `total_dept_sales` by `grand_total_sales` (both are now available on the same row), and rounds the result.

-----

## 💡 Key PostgreSQL Concepts & Performance

### 1\. `WHERE` vs `HAVING`

| Clause | Purpose | Applied To | Execution Order |
| :--- | :--- | :--- | :--- |
| **`WHERE`** | Filters **individual rows** before they are grouped. | Columns in the table (before aggregation). | Before `GROUP BY` and aggregation. |
| **`HAVING`** | Filters **groups of rows** (aggregated results). | Aggregate function results (e.g., `COUNT(*)`, `SUM(salary)`). | After `GROUP BY` and aggregation. |

**Example:**

  * `WHERE e.salary > 50000` (Filters employees)
  * `HAVING AVG(e.salary) > 60000` (Filters departments whose average salary is \> 60k)

### 2\. Difference Between `RANK()`, `DENSE_RANK()`, and `ROW_NUMBER()`

| Function | Ties Get Same Rank? | Rank Sequence Continues After Tie? | Purpose |
| :--- | :--- | :--- | :--- |
| **`ROW_NUMBER()`** | No (Each row gets a unique number) | Yes (1, 2, 3, 4...) | Pagination, unique ID generation within a partition. |
| **`RANK()`** | Yes | No (Gaps are left) | Classic ranking (1, 2, 2, 4, 5...) |
| **`DENSE_RANK()`** | Yes | Yes (No gaps) | Continuous ranking (1, 2, 2, 3, 4...) |

### 3\. Understanding `EXPLAIN ANALYZE`

**Question:** How do you debug a slow query in PostgreSQL?

**Answer:** The primary tool is `EXPLAIN ANALYZE`.

  * **`EXPLAIN`:** Shows the **planned execution path** the PostgreSQL query planner *intends* to use, along with estimated costs (cost, rows, width).
  * **`EXPLAIN ANALYZE`:** **Executes the query** and shows the **actual execution time** and other runtime statistics for each node in the plan. This is crucial for identifying bottlenecks:
      * **High Time vs High Cost:** Shows if the planner's cost estimates were inaccurate.
      * **Inefficient Operations:** Pinpoints slow operations like **Sequential Scans** (Full Table Scans) or costly **Nested Loop Joins** where a more efficient index-based access or hash join should have been used.
      * **Missing/Ineffective Indexes:** Often reveals the need for an index.
      * **Postgres-Specific Stats:** Shows `Buffers` usage (read from disk vs. shared memory).

<!-- end list -->

```sql
EXPLAIN ANALYZE
SELECT emp_name FROM employees WHERE salary > 100000;
```

# PostgreSQL Interview Questions — Medium & Hard (FAANG-level)

> A comprehensive README with questions, sample schemas/data, solutions, and detailed explanations for medium and hard PostgreSQL interview problems. Use this as practice material and a reference when preparing for FAANG or other high-bar technical interviews.

---

## How to use this file

1. Read each **Question** and try to solve it on your own using a local PostgreSQL instance or an online SQL playground that supports PostgreSQL.
2. Each problem includes: **Schema & sample data**, **SQL solution**, **Expected result**, and a **Step-by-step explanation**.
3. Focus on understanding why each solution works and the alternative approaches (when provided).

---

## Table of Contents

1. [Sample Schema & Data (common tables)](#sample-schema--data)
2. [Joins — Medium](#joins---medium)
3. [CTE & Recursive CTE — Medium/Hard](#cte--recursive-cte---mediumhard)
4. [Window Functions (partition, rank, dense_rank, row_number) — Medium/Hard](#window-functions---mediumhard)
5. [Aggregates, GROUP BY, HAVING, mixed aggregates — Medium](#aggregates-group-by-having-mixed-aggregates---medium)
6. [Subqueries & Correlated Subqueries — Medium/Hard](#subqueries--correlated-subqueries---mediumhard)
7. [Advanced: LATERAL, JSONB, array ops, unnest — Hard]
8. [Performance / EXPLAIN, Indexes, Query tuning — Hard]
9. [Transactions, locking, concurrency patterns — Hard]
10. [Partitioning, Materialized Views, Upsert (ON CONFLICT) — Hard]
11. [Full Text Search & trigram similarity — Medium/Hard]
12. [Appendix: Cheatsheet & common patterns]

---

## Sample Schema & Data

We'll use a small set of related tables that re-appear across examples: `users`, `orders`, `order_items`, `products`, `departments`, `employees`.

```sql
-- Users
CREATE TABLE users (
  id serial PRIMARY KEY,
  name text NOT NULL,
  email text UNIQUE NOT NULL,
  created_at timestamptz DEFAULT now()
);

-- Products
CREATE TABLE products (
  id serial PRIMARY KEY,
  name text NOT NULL,
  category text,
  price numeric(10,2) NOT NULL
);

-- Orders
CREATE TABLE orders (
  id serial PRIMARY KEY,
  user_id int REFERENCES users(id),
  created_at timestamptz DEFAULT now(),
  status text -- 'placed','shipped','cancelled'
);

-- Order items
CREATE TABLE order_items (
  id serial PRIMARY KEY,
  order_id int REFERENCES orders(id),
  product_id int REFERENCES products(id),
  qty int NOT NULL,
  price numeric(10,2) NOT NULL -- snapshot of price
);

-- Departments & Employees (for hierarchical / recursive examples)
CREATE TABLE departments (
  id serial PRIMARY KEY,
  name text NOT NULL,
  parent_id int REFERENCES departments(id)
);

CREATE TABLE employees (
  id serial PRIMARY KEY,
  name text NOT NULL,
  dept_id int REFERENCES departments(id),
  salary numeric(12,2),
  hired_at date
);
```

Insert minimal sample data (abbreviated) to test queries during practice.

```sql
INSERT INTO users (name,email) VALUES
('Alice','a@example.com'),('Bob','b@example.com'),('Carol','c@example.com');

INSERT INTO products (name,category,price) VALUES
('Phone','electronics',699.99),('Headphones','electronics',199.99),('Desk','furniture',249.00);

INSERT INTO orders (user_id,created_at,status) VALUES
(1,'2025-01-10','placed'),(1,'2025-02-15','shipped'),(2,'2025-01-20','cancelled');

INSERT INTO order_items (order_id,product_id,qty,price) VALUES
(1,1,1,699.99),(1,2,2,199.99),(2,3,1,249.00);

INSERT INTO departments (id,name,parent_id) VALUES
(1,'Engineering',NULL),(2,'Platform',1),(3,'HR',NULL);

INSERT INTO employees (name,dept_id,salary,hired_at) VALUES
('Sam',1,120000,'2020-03-01'),('Jane',2,115000,'2021-06-12'),('Tim',2,90000,'2022-01-10');
```

---

# 1. Joins — Medium

### Q1 — Find each order with user name and total order value

**Goal:** For each order, show `order_id`, `user_name`, `total_value` (sum of `qty * price`) — include orders with no items (total 0).

**SQL (using LEFT JOIN + group):**

```sql
SELECT
  o.id AS order_id,
  u.name AS user_name,
  COALESCE(SUM(oi.qty * oi.price), 0) AS total_value
FROM orders o
LEFT JOIN users u ON u.id = o.user_id
LEFT JOIN order_items oi ON oi.order_id = o.id
GROUP BY o.id, u.name
ORDER BY o.id;
```

**Explanation:**

* `LEFT JOIN order_items` ensures orders with no items are present.
* `SUM(oi.qty * oi.price)` computes total; `COALESCE` turns `NULL` into `0` for orders with no items.
* `GROUP BY o.id, u.name` because `u.name` is selected alongside aggregated column.

**Expected (based on sample data):**

* Order 1: total = 1*699.99 + 2*199.99 = 1099.97
* Order 2: total = 1*249.00 = 249.00

### Q2 — Inner join vs LEFT join caveat

**Question:** When you move a `WHERE` condition from the `JOIN` clause to the `WHERE` clause, you may unintentionally convert a `LEFT JOIN` into an effective `INNER JOIN`.

**Example:**

```sql
-- Intention: find orders and any items with product category electronics
SELECT o.id, p.category
FROM orders o
LEFT JOIN order_items oi ON oi.order_id = o.id
LEFT JOIN products p ON p.id = oi.product_id AND p.category = 'electronics';

-- But if you do
SELECT o.id, p.category
FROM orders o
LEFT JOIN order_items oi ON oi.order_id = o.id
LEFT JOIN products p ON p.id = oi.product_id
WHERE p.category = 'electronics'; -- This filters out rows where p is NULL
```

**Takeaway:** Keep filter conditions that are meant to filter joined table rows inside the `ON` clause when you want to preserve the outer join semantics.

---

# 2. CTE & Recursive CTE — Medium/Hard

### Q3 — Top-N per group using CTE (employees top earner per department)

**Question:** For each department, return the employee(s) with the maximum salary.

**SQL (CTE + window function):**

```sql
WITH ranked AS (
  SELECT e.*, d.name AS dept_name,
         RANK() OVER (PARTITION BY e.dept_id ORDER BY e.salary DESC) AS rnk
  FROM employees e
  JOIN departments d ON d.id = e.dept_id
)
SELECT id, name, dept_name, salary
FROM ranked
WHERE rnk = 1;
```

**Explanation:**

* The CTE `ranked` computes a rank per department; then outer query filters `rnk = 1` to get top earners.
* `RANK()` allows multiple employees to tie.

### Q4 — Recursive CTE: department hierarchy

**Question:** Given a `departments` table with `parent_id`, produce a list of departments with their ancestor path and depth.

**SQL (recursive CTE):**

```sql
WITH RECURSIVE dept_tree AS (
  SELECT id, name, parent_id, name::text AS path, 1 AS depth
  FROM departments
  WHERE parent_id IS NULL

  UNION ALL

  SELECT d.id, d.name, d.parent_id, (dt.path || ' > ' || d.name)::text, dt.depth + 1
  FROM departments d
  JOIN dept_tree dt ON d.parent_id = dt.id
)
SELECT * FROM dept_tree ORDER BY path;
```

**Explanation:**

* Anchor rows are top-level departments (no parent).
* Recursive part appends child departments and increments `depth`.
* Useful for trees/graphs without cycles; in presence of cycles you should guard against infinite loops (e.g., track visited nodes).

---

# 3. Window Functions — Medium/Hard

### Q5 — Running total and moving average

**Question:** For orders ordered by `created_at`, compute a running total of order value and 3-order moving average.

**SQL:**

```sql
WITH order_values AS (
  SELECT o.id, o.created_at, COALESCE(SUM(oi.qty * oi.price),0) AS value
  FROM orders o
  LEFT JOIN order_items oi ON oi.order_id = o.id
  GROUP BY o.id, o.created_at
)
SELECT
  id,
  created_at,
  value,
  SUM(value) OVER (ORDER BY created_at ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_total,
  AVG(value) OVER (ORDER BY created_at ROWS BETWEEN 2 PRECEDING AND CURRENT ROW) AS moving_avg_3
FROM order_values
ORDER BY created_at;
```

**Explanation:**

* `SUM(...) OVER (ORDER BY created_at ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)` creates a running total.
* The `ROWS BETWEEN 2 PRECEDING AND CURRENT ROW` window creates a 3-row moving average.
* If `created_at` ties exist, specify tiebreaker (e.g., `ORDER BY created_at, id`) for deterministic behavior.

### Q6 — Rank, dense_rank, row_number differences

**Quick examples:**

* `ROW_NUMBER()` gives a unique serial number per partition — breaks ties arbitrarily (or by second ORDER BY column).
* `RANK()` gives same rank to ties, but gaps in ranking exist after ties.
* `DENSE_RANK()` gives same rank to ties, without gaps.

**SQL snippet to demo:**

```sql
SELECT name, dept_id, salary,
  ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rn,
  RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rnk,
  DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS drnk
FROM employees;
```

**Interpretation:** Use `ROW_NUMBER()` when you need exactly one row per partition (e.g., top 1). Use `RANK()` or `DENSE_RANK()` when ties should produce multiple "top" rows.

---

# 4. Aggregates, GROUP BY, HAVING, mixed aggregates — Medium

### Q7 — Orders per user, and only users with > 2 orders and average order value > 100

**SQL:**

```sql
SELECT u.id AS user_id, u.name,
       COUNT(DISTINCT o.id) AS orders_count,
       AVG(order_total) AS avg_order_value
FROM users u
JOIN (
  SELECT o.id, o.user_id, COALESCE(SUM(oi.qty * oi.price),0) AS order_total
  FROM orders o
  LEFT JOIN order_items oi ON oi.order_id = o.id
  GROUP BY o.id, o.user_id
) AS oo ON oo.user_id = u.id
GROUP BY u.id, u.name
HAVING COUNT(DISTINCT oo.id) > 2 AND AVG(oo.order_total) > 100
ORDER BY avg_order_value DESC;
```

**Explanation:**

* Inner aggregated subquery `oo` computes each order's total.
* Outer query groups by user to compute per-user metrics.
* `HAVING` filters after aggregation.

---

# 5. Subqueries & Correlated Subqueries — Medium/Hard

### Q8 — Correlated subquery: find users whose order total for some order exceeds their average order value

**Question:** Return user_id and order_id where that specific order's total > user's average order total.

**SQL (correlated):**

```sql
SELECT o.id AS order_id, o.user_id,
  (SELECT COALESCE(SUM(oi2.qty * oi2.price),0) FROM order_items oi2 WHERE oi2.order_id = o.id) AS this_order_total,
  (SELECT AVG(sub.total) FROM (
     SELECT COALESCE(SUM(oi3.qty * oi3.price),0) AS total
     FROM orders o3
     LEFT JOIN order_items oi3 ON oi3.order_id = o3.id
     WHERE o3.user_id = o.user_id
     GROUP BY o3.id
  ) AS sub) AS user_avg_order
FROM orders o
WHERE (SELECT COALESCE(SUM(oi2.qty * oi2.price),0) FROM order_items oi2 WHERE oi2.order_id = o.id)
      >
      (SELECT AVG(sub.total) FROM (
         SELECT COALESCE(SUM(oi3.qty * oi3.price),0) AS total
         FROM orders o3
         LEFT JOIN order_items oi3 ON oi3.order_id = o3.id
         WHERE o3.user_id = o.user_id
         GROUP BY o3.id
      ) AS sub);
```

**Explanation:**

* The correlated subquery references `o.user_id` and compares the value of a specific order with the user's average.
* This is expressive but can be slow for large datasets; rewriting with window functions (partition by user) is typically faster.

**Alternative (window function):**

```sql
WITH order_values AS (
  SELECT o.id, o.user_id, COALESCE(SUM(oi.qty * oi.price),0) AS total
  FROM orders o
  LEFT JOIN order_items oi ON oi.order_id = o.id
  GROUP BY o.id, o.user_id
)
SELECT id AS order_id, user_id, total
FROM (
  SELECT ov.*, AVG(total) OVER (PARTITION BY user_id) AS user_avg
  FROM order_values ov
) t
WHERE total > user_avg;
```

**Explanation:** Window version avoids repeated aggregates per user and usually performs better.

---

# 6. Advanced: LATERAL, JSONB, arrays, unnest — Hard

### Q9 — Use LATERAL to pick the top priced product per category

**Question:** For each category, return the single product row with the highest price. Use `LATERAL` so you can return the whole product row.

**SQL:**

```sql
SELECT c.category, p.*
FROM (
  SELECT DISTINCT category FROM products
) c
CROSS JOIN LATERAL (
  SELECT * FROM products p2
  WHERE p2.category = c.category
  ORDER BY p2.price DESC
  LIMIT 1
) p;
```

**Explanation:** `LATERAL` allows the subquery to reference the current row from the left side (`c.category`). It's a readable pattern for "top 1 per group" when you want the full row instead of aggregates.

### Q10 — JSONB: query and update a nested object

Suppose `users` has a `preferences jsonb` column:

```sql
ALTER TABLE users ADD COLUMN preferences jsonb DEFAULT '{}';
UPDATE users SET preferences = '{"newsletter": true, "theme": "dark"}' WHERE id = 1;
```

**Question:** Toggle `newsletter` setting for a specific user and return the new value.

**SQL (using jsonb_set):**

```sql
UPDATE users
SET preferences = jsonb_set(preferences, '{newsletter}', to_jsonb(NOT (preferences->>'newsletter')::boolean), true)
WHERE id = 1
RETURNING preferences->>'newsletter' AS newsletter;
```

**Explanation:**

* `preferences->>'newsletter'` extracts the boolean as text; cast to boolean then invert.
* `jsonb_set` updates the JSONB value and returns the updated JSON.

### Q11 — Arrays + unnest: find customers who bought both product A and B

**Strategy:** Aggregate product IDs per order (or per user) into an array, then check containment.

**SQL (per user):**

```sql
WITH user_products AS (
  SELECT o.user_id,
         ARRAY_AGG(DISTINCT oi.product_id) AS products
  FROM orders o
  JOIN order_items oi ON oi.order_id = o.id
  GROUP BY o.user_id
)
SELECT u.*, up.products
FROM user_products up
JOIN users u ON u.id = up.user_id
WHERE up.products @> ARRAY[1,2]; -- contains product ids 1 and 2
```

**Explanation:** `@>` operator checks if the array contains the listed elements (order-independent). Good for "has all" checks.

---

# 7. Performance / EXPLAIN, Indexes, Query tuning — Hard

### Q12 — Using `EXPLAIN (ANALYZE, BUFFERS)` to identify slow query

**Question:** What to look for in the plan and how to fix a sequential scan that should use an index?

**Checklist:**

* Look at `cost`, actual `time`, `rows`, `width` fields in `EXPLAIN ANALYZE`.
* If a sequential scan reads many rows but you expect an index seek, confirm:

  * There exists a suitable index (e.g., `CREATE INDEX ON orders(user_id, created_at)` for queries filtering by `user_id` and sorting `created_at`).
  * Statistics are up-to-date: run `ANALYZE table;`.
  * Selectivity: if the predicate matches a large fraction, seq scan might be optimal.
  * Function calls / casts on columns can prevent index usage (e.g., `WHERE lower(email) = 'x'` needs an expression index).

**Example fix:** create a partial index for common query patterns:

```sql
CREATE INDEX idx_orders_user_status_createdat ON orders (user_id, created_at) WHERE status = 'shipped';
```

**Explanation:** Partial index helps if most queries target `status = 'shipped'`. Also consider BRIN indexes for very large append-only timestamped tables.

---

# 8. Transactions, locking, concurrency patterns — Hard

### Q13 — Prevent lost updates: implement optimistic locking using a `version` column

**Schema:**

```sql
ALTER TABLE products ADD COLUMN version int DEFAULT 1;
```

**Update pattern:**

```sql
-- Client reads product with version = 3
-- Later tries to update
UPDATE products
SET price = 649.99, version = version + 1
WHERE id = 1 AND version = 3;

-- If rows_affected = 0, the update lost race; client should re-read and retry or abort.
```

**Explanation:** Optimistic locking avoids heavy locking by detecting concurrent modifications.

### Q14 — Advisory locks for application-level mutual exclusion

```sql
-- Acquire (session) lock
SELECT pg_advisory_lock(12345);
-- Do critical work
-- Release
SELECT pg_advisory_unlock(12345);
```

**Explanation:** Advisory locks are cooperative application-level locks — useful for background jobs where you want only one worker to process a resource.

---

# 9. Partitioning, Materialized Views, Upsert (ON CONFLICT) — Hard

### Q15 — Range partition orders by month

**Create partitioned table:**

```sql
CREATE TABLE orders_by_month (
  id serial PRIMARY KEY,
  user_id int,
  created_at date NOT NULL,
  status text
) PARTITION BY RANGE (created_at);

CREATE TABLE orders_2025_01 PARTITION OF orders_by_month
  FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE TABLE orders_2025_02 PARTITION OF orders_by_month
  FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
```

**Benefits:** Partition pruning helps queries that filter by `created_at` to only touch relevant partitions.

### Q16 — Upsert with `ON CONFLICT` (insert-or-update)

**Question:** Insert a product but update price if product name exists.

```sql
INSERT INTO products (name, category, price)
VALUES ('Phone','electronics',679.99)
ON CONFLICT (name)
DO UPDATE SET price = EXCLUDED.price
RETURNING *;
```

**Explanation:** `EXCLUDED` is the pseudo-table holding the proposed row. You can set more complex `WHERE` conditions to avoid always updating.

---

# 10. Full Text Search & trigram similarity — Medium/Hard

### Q17 — Basic full text search: find products matching a search phrase

```sql
ALTER TABLE products ADD COLUMN tsv tsvector;
UPDATE products SET tsv = to_tsvector('english', coalesce(name,'') || ' ' || coalesce(category,''));
CREATE INDEX idx_products_tsv ON products USING GIN (tsv);

SELECT id, name, ts_rank(tsv, plainto_tsquery('english','wireless headphones')) AS rank
FROM products
WHERE tsv @@ plainto_tsquery('english','wireless headphones')
ORDER BY rank DESC
LIMIT 10;
```

**Explanation:**

* `to_tsvector` converts text into searchable tokens.
* GIN index speeds search. `ts_rank` ranks relevance.

### Q18 — Fuzzy search with pg_trgm

```sql
-- extension
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_products_name_trgm ON products USING GIN (name gin_trgm_ops);

SELECT * FROM products WHERE name % 'fone' ORDER BY similarity(name,'fone') DESC LIMIT 5;
```

**Explanation:** `%` operator finds similar strings using trigram similarity. Good for typo-tolerant search.

---

# 11. Hard interview-style problems

### Q19 — Find top K frequent item pairs (market-basket analysis)

**Question:** Given `order_items`, find the top 10 pairs of products that appear together in the same order (unordered pairs), ordered by co-occurrence count.

**SQL:**

```sql
SELECT p1.product_id AS product_a, p2.product_id AS product_b, COUNT(*) AS cnt
FROM (
  SELECT order_id, product_id
  FROM order_items
  GROUP BY order_id, product_id
) p1
JOIN (
  SELECT order_id, product_id
  FROM order_items
  GROUP BY order_id, product_id
) p2
  ON p1.order_id = p2.order_id AND p1.product_id < p2.product_id
GROUP BY p1.product_id, p2.product_id
ORDER BY cnt DESC
LIMIT 10;
```

**Explanation:**

* Self-join `order_items` per `order_id` pairing product rows with greater `product_id` to avoid duplicate reversed pairs.
* Aggregate by pair and order by frequency.

### Q20 — Event-sessionization: group events into sessions per user (hard)

Assume an `events(user_id, event_time, payload)` table. Define sessions where a gap > 30 minutes starts a new session.

**Solution (window functions):**

```sql
WITH evt AS (
  SELECT *, LAG(event_time) OVER (PARTITION BY user_id ORDER BY event_time) AS prev_time
  FROM events
), flagged AS (
  SELECT e.*,
         CASE WHEN prev_time IS NULL OR event_time - prev_time > INTERVAL '30 minutes' THEN 1 ELSE 0 END AS is_new_session
  FROM evt e
), sess AS (
  SELECT *, SUM(is_new_session) OVER (PARTITION BY user_id ORDER BY event_time) AS session_no
  FROM flagged
)
SELECT user_id, session_no, MIN(event_time) AS session_start, MAX(event_time) AS session_end, COUNT(*) AS events_count
FROM sess
GROUP BY user_id, session_no
ORDER BY user_id, session_start;
```

**Explanation:**

* `LAG` gets previous event timestamp.
* Flag new session starts where gap > 30 minutes.
* Cumulative `SUM` over the flags yields session numbers.
* Grouping by the session number aggregates session-level metrics.

---

# 12. Appendix: Cheatsheet & Common Patterns

* Use `EXPLAIN (ANALYZE, BUFFERS)` to inspect query performance.
* Prefer `JOIN` with explicit `ON` and keep filters intended for the joined table in the `ON` clause for outer joins.
* For "top N per group": window functions (`ROW_NUMBER()`, `RANK()`) or `LATERAL` + `LIMIT 1` are idiomatic.
* Replace correlated subqueries with window functions when possible for performance.
* Use `jsonb` for schemaless columns; index with `GIN` (`CREATE INDEX ON table USING gin (jsonb_col jsonb_path_ops)` or default GIN).
* For bulk-updates/inserts use `COPY` or `pg_bulkload` (outside core SQL) for performance.

---

## Closing notes

This README is intentionally example-rich. Practice by running these queries on a local dataset and increasing data volume to observe differences in performance. For FAANG interviews, be ready to:

* Explain complexity trade-offs and index choices.
* Rewrite slow queries into faster forms (window-based, CTEs, lateral).
* Discuss concurrency, isolation levels, and strategies for distributed systems (e.g., optimistic locking, advisory locks).

Good luck — run the queries, time them, and iterate!

---

*If you want, I can also:*

* Provide additional **very hard** questions (e.g., graph reachability with recursive CTE, probabilistic data sketches, approximate distinct counts using HyperLogLog patterns),
* Export this README as a downloadable `.md` file,
* Or generate smaller printable cheat-sheets for quick revision.


Here are medium and hard PostgreSQL interview questions targeting FAANG-level standards, including query samples and explanations across all critical topics such as joins, CTEs, subqueries, window functions, partitions, aggregation, and more. The following content is organized in sections that could be used directly in a GitHub README.md file.[1][2]

***

# PostgreSQL FAANG Interview Questions

This document covers advanced and complex PostgreSQL queries, with each question, example, and its explanation designed to help you tackle FAANG-level interviews.

***

## Joins

**Question**: Retrieve all employees and their respective department names, including those without a department.

**Query**
```sql
SELECT e.employee_id, e.name, d.department_name
FROM employees e
LEFT JOIN departments d ON e.department_id = d.department_id;
```
**Explanation**: Uses a `LEFT JOIN` to include employees without a department, showing all records from `employees` and matching from `departments`.[2][1]

***

## Subqueries & Nested Queries

**Question**: Find all employees whose salary exceeds the average salary.

**Query**
```sql
SELECT name, salary
FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees);
```
**Explanation**: The subquery calculates the average salary, then the outer query returns employees whose salary is greater than this value.[2]

***

## CTEs (Common Table Expressions)

**Question**: List departments with more than 5 employees.

**Query**
```sql
WITH dept_count AS (
  SELECT department_id, COUNT(*) AS num_employees
  FROM employees
  GROUP BY department_id
)
SELECT d.department_name, dc.num_employees
FROM departments d
JOIN dept_count dc ON d.department_id = dc.department_id
WHERE dc.num_employees > 5;
```
**Explanation**: The CTE `dept_count` computes employee count per department, which the main query filters.[1][2]

***

## Window Functions (Partition, Rank, etc.)

**Question**: Return the top three highest-paid employees in each department.

**Query**
```sql
SELECT department_id, name, salary
FROM (
  SELECT department_id, name, salary,
         RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) as rnk
  FROM employees
) ranked
WHERE ranked.rnk <= 3;
```
**Explanation**: The `RANK()` window function assigns ranks within partitions (departments), then the outer query filters for top three.[3][1]

***

## Group By & Aggregate Functions

**Question**: For each department, show the total salary costs.

**Query**
```sql
SELECT department_id, SUM(salary) AS total_salary
FROM employees
GROUP BY department_id;
```
**Explanation**: Aggregates salary per department using `SUM()` and `GROUP BY`.[1][2]

***

## Mix of Aggregate and Group By with HAVING

**Question**: Show departments with more than ten employees and their average salary.

**Query**
```sql
SELECT department_id, AVG(salary) AS avg_salary, COUNT(*) AS num_employees
FROM employees
GROUP BY department_id
HAVING COUNT(*) > 10;
```
**Explanation**: Combines aggregation, grouping, and filters groups using `HAVING`.[2][1]

***

## Advanced Order By

**Question**: List the top five employees by hire date.

**Query**
```sql
SELECT name, hire_date
FROM employees
ORDER BY hire_date DESC
LIMIT 5;
```
**Explanation**: Orders rows descending by hire date and limits results to 5.[2]

***

## Complex: Gap and Islands Problem

**Question**: Identify continuous periods of employee activity (gaps and islands).

**Query**
```sql
SELECT employee_id,
       activity_date,
       activity_type,
       activity_date - ROW_NUMBER() OVER (PARTITION BY employee_id ORDER BY activity_date) AS grp
FROM activities
ORDER BY employee_id, activity_date;
```
**Explanation**: Uses `ROW_NUMBER()` and arithmetic on the date to identify sequence breaks per employee.[3]

***

## Partition By with Aggregate

**Question**: Calculate department-wise cumulative salary for each employee.

**Query**
```sql
SELECT employee_id, department_id, salary,
       SUM(salary) OVER (PARTITION BY department_id ORDER BY salary) AS cumulative_salary
FROM employees;
```
**Explanation**: The window aggregate accumulates salary totals per department.[3][1]

***

## README Structure for GitHub

To use in a GitHub `README.md` file, replicate the markdown structure above, categorizing each question under its topic and including the question, solution, and explanation as provided.

***

**Citations:**  
All content is compiled and adapted based on reliable, up-to-date sources for FAANG and advanced SQL/PostgreSQL interviews.[3][1][2]

[1](https://www.geeksforgeeks.org/postgresql/postgresql-interview-questions/)
[2](https://github.com/interviewplus-ai/postgres-interview-questions-and-answers)
[3](https://www.reddit.com/r/dataengineering/comments/n2jm1s/what_are_the_most_commond_advanced_sql_interview/)
[4](https://github.com/naiborhujosua/faang_sql_questions_postgresql)
[5](https://www.stratascratch.com/blog/sql-interview-questions-you-must-prepare-the-ultimate-guide/)
[6](https://www.datacamp.com/blog/top-postgresql-interview-questions-for-all-levels)
[7](https://martendb.io/documents/querying/advanced-sql)
[8](https://www.vervecopilot.com/interview-questions/top-30-most-common-postgresql-interview-questions-you-should-prepare-for)
[9](https://igotanoffer.com/blogs/tech/system-design-interviews)
[10](https://thoughtbot.com/blog/reading-an-explain-analyze-query-plan)
[11](https://interviewkickstart.com/blogs/interview-questions/complex-sql-interview-questions)
[12](https://bytescout.com/blog/postgresql-advanced-queries.html)
[13](https://github.com/mrbardia72/db-Interview-Questions-and-Answers)
[14](https://datalemur.com/blog/google-sql-interview-questions)
[15](https://www.enterprisedb.com/postgres-tutorials/postgresql-query-introduction-explanation-and-50-examples)
[16](https://www.turing.com/interview-questions/postgresql)
[17](https://www.postgresql.org/docs/7.1/advanced.html)
[18](https://labex.io/tutorials/postgresql-postgresql-interview-questions-and-answers-593697)
[19](https://www.nucamp.co/blog/coding-bootcamp-back-end-with-python-and-sql-advanced-sql-techniques-in-postgresql)
[20](https://github.com/Devinterview-io/databases-interview-questions)

