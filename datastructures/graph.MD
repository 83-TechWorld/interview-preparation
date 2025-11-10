I apologize for that\! It seems you weren't able to copy the content I generated about Graphs and the **Number of Islands** problem.

Here is the **complete content** again, formatted in GitHub Markdown, ready for you to copy and use.

-----

## 💻 The Graph Data Structure

A **Graph** is a non-linear data structure that consists of a finite set of **nodes** (or **vertices**) and a set of **edges** connecting these nodes. It's used to represent pairwise relationships between objects.

### 1\. Structure and Components 🗺️

| Component | Description | Diagram Tag |
| :--- | :--- | :--- |
| **Node (Vertex)** | The fundamental entity. It represents an object or an item (e.g., a city, a person, a webpage). |  |
| **Edge** | A link or connection between two nodes. It represents the relationship between them. | |
| **Adjacency** | Two nodes are **adjacent** if they are connected by an edge. | |

-----

### 2\. Normal Operations

The primary operations performed on a graph involve adding, removing, and traversing its components.

| Operation | Description |
| :--- | :--- |
| **Add/Remove Vertex** | Adds a new node to the graph or removes an existing one. |
| **Add/Remove Edge** | Creates a connection between two nodes or breaks an existing one. |
| **Traversal** | Systematically visiting every node in the graph. The two most common methods are **Breadth-First Search (BFS)** and **Depth-First Search (DFS)**. |
| **Search** | Finding a specific node or edge that satisfies a condition. |

-----

### 3\. Wide Usage of Graphs 🌐

Graphs are widely used because almost everything in the real world can be modeled as a relationship network.

  * **Social Networks (e.g., Facebook, LinkedIn):** Nodes are **people**, and edges represent **friendship** or **connection**.
  * **Mapping/GPS (e.g., Google Maps, Waze):** Nodes are **locations** (intersections/cities), and edges are **roads**. Algorithms find the shortest path.
  * **The Internet/Web Pages:** Nodes are **web pages**, and edges are **hyperlinks**. Graph traversal algorithms (like PageRank) are used for indexing and search.
  * **Airline Systems:** Nodes are **airports**, and edges are **flights**.

-----

Graph primer — what, how, and basic ops
Graph = a set of vertices (nodes) and edges (connections).
Types:
Directed / Undirected edges
Weighted / Unweighted edges
Simple / Multi-graph, etc.
Representations
Adjacency List (most common & memory-efficient)
List<List<Integer>> adj = new ArrayList<>(); // for 0..n-1
// or Map<Integer, List<Integer>> for sparse/labelled graphs
Adjacency Matrix (dense graphs)
int[][] mat = new int[n][n]; // mat[u][v] == 1 (or weight)
Edge list (list of (u,v,weight))
When to use which
Adj list → most graph algorithms, sparse graphs.
Matrix → small n or dense graphs (fast edge lookup O(1)).
Edge list → when doing sorting by weight (Kruskal).
Basic operations (concepts)
BFS (Breadth-First Search) — level order, shortest path in unweighted graphs, queue-based.
DFS (Depth-First Search) — backtracking, topological sort, connectivity, recursion/stack-based.
Dijkstra — shortest path in weighted graphs with non-negative weights (PriorityQueue).
Bellman-Ford — weighted graphs with negative edges (not covered here).
Topological sort — ordering in DAGs (Kahn’s BFS or DFS postorder).
Union-Find (Disjoint Set) — connectivity, cycle detection (useful for Kruskal, islands sometimes).
Debugging tips:
Print adjacency list to verify graph construction.
For BFS/DFS, log node entry/exit times and queue contents.
Check visited[] initialization and resetting between runs.
Small test cases (3–6 nodes) drawn on paper.

-----

## 💡 Problem: Number of Islands

The "Number of Islands" problem is a classic example of **Graph Traversal (Depth-First Search - DFS or Breadth-First Search - BFS)** on a 2D grid.

### Problem Statement

You are given an $m \times n$ 2D binary grid **grid** which represents a map of **'1's** (land) and **'0's** (water). An **island** is surrounded by water and is formed by connecting adjacent lands horizontally or vertically. Find the **total number of islands**.

### Real-World Use Case 🏞️

This problem models identifying **connected components** in various systems:

1.  **Image Processing:** Identifying distinct **objects** or **regions** in a binary image.
2.  **Network Connectivity:** Determining the number of **separate networks** or clusters in a larger system.

-----

### Solution Strategy (The 'How')

The grid is treated as a graph.

1.  **Initialize Count:** Start with `islands_count = 0`.
2.  **Iterate:** Go through every cell in the grid.
3.  **Find Land ('1'):** If a '1' is found, increment `islands_count`.
4.  **Explore and Mark (DFS/BFS):** Start a **Depth-First Search (DFS)** from that cell to visit **all connected '1's** belonging to this island. As you visit a land cell, **mark it as '0' (visited/water)**. This ensures the island is counted only once.
5.  **Repeat:** Continue iteration.

-----

### Step-by-Step Example (DFS)

Consider the grid:

```
['1', '1', '0', '0', '0']
['1', '1', '0', '0', '0']
['0', '0', '1', '0', '0']
['0', '0', '0', '1', '1']
```

| Step | Action | Islands Count | Notes |
| :--- | :--- | :--- | :--- |
| 1 | Found **Grid[0][0]** ('1'). | **1** | Start DFS. Marks (0,0), (0,1), (1,0), (1,1) as '0'. |
| 2 | Continue iteration. Found **Grid[2][2]** ('1'). | **2** | Start DFS. Marks (2,2) as '0'. |
| 3 | Continue iteration. Found **Grid[3][3]** ('1'). | **3** | Start DFS. Marks (3,3) and (3,4) as '0'. |
| 4 | Grid fully traversed. | **3** | **Result: 3 Islands** |

-----

### Java Solution (GitHub Markdown Format)

```java
/**
 * Number of Islands Problem
 * * This solution uses the Depth-First Search (DFS) traversal algorithm.
 * The core idea is: 
 * 1. Traverse the grid and when a piece of land ('1') is found, 
 * 2. Increment the island counter.
 * 3. Use DFS to "sink" (mark as '0') all connected land parts 
 * of that island so it's not counted again.
 */
class Solution {
    /**
     * Main method to count the total number of islands.
     * @param grid The 2D character array representing the map ('1' for land, '0' for water).
     * @return The total number of islands.
     */
    public int numIslands(char[][] grid) {
        // Handle edge case where the grid might be null or empty
        if (grid == null || grid.length == 0) {
            return 0;
        }

        int rows = grid.length;
        int cols = grid[0].length;
        int islandCount = 0; // Initialize the counter for the islands

        // 1. Iterate through every cell in the grid
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                
                // 2. If we find a piece of land ('1'), we've found a new, uncounted island
                if (grid[r][c] == '1') {
                    islandCount++; // Increment the island count
                    
                    // 3. Start the DFS from this cell to explore and mark the entire island
                    // This prevents us from counting the same island multiple times.
                    sinkIsland(grid, r, c); 
                }
            }
        }

        return islandCount;
    }

    /**
     * Recursive helper function (DFS) to "sink" an entire island.
     * It marks all connected land cells ('1') as water ('0').
     * @param grid The grid map.
     * @param r The current row index.
     * @param c The current column index.
     */
    private void sinkIsland(char[][] grid, int r, int c) {
        int rows = grid.length;
        int cols = grid[0].length;

        // Base Case 1: Check boundary conditions (If we step outside the grid)
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            return;
        }

        // Base Case 2: Check for water or already visited land (If we hit '0')
        // We only proceed if the current cell is land ('1').
        if (grid[r][c] == '0') {
            return;
        }

        // Action: Mark the current cell as visited/water ('0')
        // This is the key step to prevent cycles and recounting!
        grid[r][c] = '0';

        // Recursive Steps: Explore all four adjacent neighbors (horizontal and vertical)

        // Go UP
        sinkIsland(grid, r - 1, c);
        // Go DOWN
        sinkIsland(grid, r + 1, c);
        // Go LEFT
        sinkIsland(grid, r, c - 1);
        // Go RIGHT
        sinkIsland(grid, r, c + 1);
    }
}
```

-----


awesome — let’s dive into **Graphs**! 🌐
i’ll start with a clear primer on graph data structures and operations, then solve **6 classic graph problems** step-by-step with Java code, debugging tips, complexity, and real-world use cases.

---

# Graph primer — what, how, and basic ops

**Graph** = a set of **vertices (nodes)** and **edges** (connections).
Types:

* **Directed / Undirected** edges
* **Weighted / Unweighted** edges
* **Simple / Multi-graph**, etc.

### Representations

1. **Adjacency List** (most common & memory-efficient)

```java
List<List<Integer>> adj = new ArrayList<>(); // for 0..n-1
// or Map<Integer, List<Integer>> for sparse/labelled graphs
```

2. **Adjacency Matrix** (dense graphs)

```java
int[][] mat = new int[n][n]; // mat[u][v] == 1 (or weight)
```

3. **Edge list** (list of (u,v,weight))

### When to use which

* Adj list → most graph algorithms, sparse graphs.
* Matrix → small n or dense graphs (fast edge lookup `O(1)`).
* Edge list → when doing sorting by weight (Kruskal).

### Basic operations (concepts)

* **BFS (Breadth-First Search)** — level order, shortest path in unweighted graphs, queue-based.
* **DFS (Depth-First Search)** — backtracking, topological sort, connectivity, recursion/stack-based.
* **Dijkstra** — shortest path in weighted graphs with non-negative weights (PriorityQueue).
* **Bellman-Ford** — weighted graphs with negative edges (not covered here).
* **Topological sort** — ordering in DAGs (Kahn’s BFS or DFS postorder).
* **Union-Find (Disjoint Set)** — connectivity, cycle detection (useful for Kruskal, islands sometimes).

Debugging tips:

* Print adjacency list to verify graph construction.
* For BFS/DFS, log node entry/exit times and queue contents.
* Check visited[] initialization and resetting between runs.
* Small test cases (3–6 nodes) drawn on paper.

---

# Problem 1 — Number of Islands (LeetCode style)

### Problem

Given a 2D grid of `'1'` (land) and `'0'` (water), count islands (connected components of 1s — 4-directional).

### Data structure & approach

Grid is a 2D graph: each cell connects to up/down/left/right neighbors. Use **DFS** or **BFS** to mark visited land. Each time you find an unvisited `'1'`, start DFS/BFS — that’s one island.

### Step-by-step (example)

```
11000
11000
00100
00011
```

* Visit (0,0) → flood-fill all connected → island 1
* Next unvisited '1' at (2,2) → flood-fill → island 2
* Next at (3,3) → island 3

### Java (BFS version)

```java
class NumberOfIslands {
    public int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0) return 0;
        int rows = grid.length, cols = grid[0].length;
        boolean[][] vis = new boolean[rows][cols];
        int islands = 0;

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!vis[r][c] && grid[r][c] == '1') {
                    islands++;
                    // BFS queue
                    Queue<int[]> q = new LinkedList<>();
                    q.offer(new int[]{r,c});
                    vis[r][c] = true;
                    while (!q.isEmpty()) {
                        int[] cur = q.poll();
                        for (int[] d : dirs) {
                            int nr = cur[0] + d[0], nc = cur[1] + d[1];
                            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                                && !vis[nr][nc] && grid[nr][nc] == '1') {
                                vis[nr][nc] = true;
                                q.offer(new int[]{nr,nc});
                            }
                        }
                    }
                }
            }
        }
        return islands;
    }
}
```

**Complexity:** O(R·C) time, O(R·C) space (for visited or recursion).
**Debug tip:** Visualize flood-fill for one starting cell and ensure all connected lands marked.

---

# Problem 2 — Clone Graph

### Problem

Given a reference to a node in a connected undirected graph, return a deep copy (clone) of the graph.

### Data structure & approach

Graph with adjacency list implicit in Node objects. Use **BFS or DFS** with a `Map<Original, Clone>` to avoid re-cloning nodes (avoid infinite loops).

### Step-by-step

* Start at source node, create clone, put in map.
* Explore neighbors; for each neighbor, if not cloned, clone and enqueue/DFS; add clone neighbor references.

### Java (BFS)

```java
Yes, let's dive into the **Clone Graph** problem\! This is a great problem for understanding how to handle **graph memory structure** and avoiding infinite loops when copying complex linked objects.

I will provide the explanation, real-world use case, step-by-step example, and the Java solution in the requested GitHub Markdown format.

-----

## 🔗 Problem: Clone Graph

### Problem Statement

Given a reference of a node in a **connected undirected graph**, return a **deep copy** (clone) of the graph.

The graph is represented by its nodes, where each node contains an integer value and a list of its neighbors.

  * A **deep copy** means creating a brand new graph where every node and every edge is newly constructed, but the structure remains identical to the original.
  * The graph nodes may contain cycles (a node can point back to a previously visited node).

### Real-World Use Case 🔄

The core concept of deep copying a linked structure is vital in several areas:

1.  **Serialization and Persistence:** When you want to save the entire **state** of a complex, interconnected object (like a graph, a game map, or a deep data model) to a file or database. You need a deep copy to ensure the saved data is completely independent of the running program's memory.
2.  **Snapshotting/Versioning:** Creating an immutable **snapshot** of a system's state before making experimental or risky changes. For instance, in a database transaction or a version control system, you need a precise, independent copy of the data structure.
3.  **Undo/Redo Functionality:** In graph editors or CAD tools, an **undo** feature often relies on having a safe, full copy of the graph's state before the last operation was performed.

-----

### Solution Strategy (The 'How')

We must traverse the original graph and create corresponding nodes for the new graph. The key challenge is handling **cycles** and **shared neighbors** without getting into an infinite loop or creating multiple copies of the same node.

The solution uses **Breadth-First Search (BFS)** or **Depth-First Search (DFS)**, combined with a **Hash Map** (or Dictionary).

1.  **Hash Map for Mapping:** We use a `HashMap<Node, Node>` to store the relationship between nodes in the original graph and their newly created clones.
      * **Key:** An original graph node.
      * **Value:** Its corresponding cloned node.
2.  **Start Traversal (BFS/DFS):** Start the traversal from the given source node.
3.  **Cloning Nodes:**
      * When visiting an **original node (O)**, check if it's already in the Hash Map.
      * If **not found**: This is the first time we see it. Create a **new clone node (C)** and store the pair in the map: `map.put(O, C)`.
4.  **Connecting Neighbors:** For the clone node **C**, iterate through the neighbors of the original node **O**.
      * For each original neighbor (**ON**), ensure its clone (**CN**) exists (by recursively calling DFS or checking/creating via BFS/HashMap).
      * Once the clone neighbor **CN** is ready, link it: `C.neighbors.add(CN)`.

-----

### Step-by-Step Example (BFS)

Consider a graph with nodes **1, 2, 3, 4**, where:

  * 1 is connected to 2 and 4.
  * 2 is connected to 1 and 3.
  * 3 is connected to 2 and 4.
  * 4 is connected to 1 and 3.

**Start Node: 1**

| Step | Queue (Nodes to Visit) | HashMap (`Original Node` -\> `Clone Node`) | Action |
| :--- | :--- | :--- | :--- |
| **1** | `[Original Node 1]` | `{}` | **Clone 1:** Create `Clone 1`. `map.put(1, Clone 1)`. |
| **2** | `[Original Node 2, Original Node 4]` | `{1 -> Clone 1}` | Dequeue 1. **Clone Neighbors of 1:** `(2, 4)`. |
| **3** | `[Original Node 4, Original Node 3]` | `{1 -> C1, 2 -> C2}` | Dequeue 2. **Clone 2:** Create `Clone 2`. `map.put(2, C2)`. |
| **4** | `[Original Node 3]` | `{1 -> C1, 2 -> C2, 4 -> C4}` | Dequeue 4. **Clone 4:** Create `Clone 4`. `map.put(4, C4)`. |
| **5** | `[]` | `{1 -> C1, ... 4 -> C4}` | Dequeue 3. **Clone 3:** Create `Clone 3`. `map.put(3, C3)`. |
| **6** | Final Map Lookups | | **Connect Edges:** Iterate through original neighbors and link their clones. |
| | | | **For C1:** Neighbors are C2 and C4. |
| | | | **For C2:** Neighbors are C1 and C3. |
| | | | **For C3:** Neighbors are C2 and C4. |
| | | | **For C4:** Neighbors are C1 and C3. |

**Result:** A fully independent copy of the original graph, starting with `Clone 1`.

-----

### Java Solution (GitHub Markdown Format)

```java
/*
 * Definition for a Node in the graph structure.
 * This structure would typically be provided by the problem platform.
 */
class Node {
    public int val;
    public List<Node> neighbors;

    public Node() {
        val = 0;
        neighbors = new ArrayList<Node>();
    }
    public Node(int _val) {
        val = _val;
        neighbors = new ArrayList<Node>();
    }
    public Node(int _val, ArrayList<Node> _neighbors) {
        val = _val;
        neighbors = _neighbors;
    }
}

/**
 * Clone Graph Problem - Solution using Depth-First Search (DFS)
 * The essential tool here is the HashMap to track which nodes have been cloned,
 * preventing infinite loops (due to cycles) and duplicate nodes.
 */
class Solution {
    // HashMap to map the original Node to its cloned Node.
    // This is the core mechanism for deep copying and cycle detection.
    private HashMap<Node, Node> visitedMap = new HashMap<>();

    /**
     * Main method to initiate the deep copy of the graph.
     * @param node The starting node of the original graph.
     * @return The starting node of the newly cloned graph.
     */
    public Node cloneGraph(Node node) {
        // Edge case: If the starting node is null (empty graph), return null.
        if (node == null) {
            return null;
        }

        // 1. Check if the current node has already been cloned.
        // This is crucial for handling cycles (like 1 -> 2 -> 1) 
        // and shared neighbors (like 1 -> 2 and 3 -> 2).
        if (visitedMap.containsKey(node)) {
            // If yes, just return its clone from the map.
            return visitedMap.get(node);
        }

        // 2. If not cloned, create the new clone node.
        // Important: Initialize the new node with the value, but an empty neighbor list for now.
        Node cloneNode = new Node(node.val);

        // 3. Register the clone in the map immediately. 
        // This ensures that any subsequent recursive call that encounters the original node
        // will find this clone and return it, preventing infinite recursion.
        visitedMap.put(node, cloneNode);

        // 4. Recursively clone and connect all neighbors.
        // For each neighbor of the original node...
        for (Node neighbor : node.neighbors) {
            // The recursive call returns the CLONE of the neighbor.
            Node clonedNeighbor = cloneGraph(neighbor); 
            
            // Add the cloned neighbor to the current clone's neighbor list.
            cloneNode.neighbors.add(clonedNeighbor);
        }

        // 5. Return the fully constructed clone node (with its now-connected neighbors).
        return cloneNode;
    }
}
```
-----

Would you like to move on to the next problem, **Pacific Atlantic Water Flow**, which uses a slightly different application of graph traversal?
```

**Complexity:** O(V + E) time and space.
**Debug tip:** Print mapping from original val → clone val and neighbors counts.

---

# Problem 3 — Course Schedule (Detect if possible / Topological sort)

### Problem

Given `numCourses` and `prerequisites` pairs `[a, b]` meaning to take `a` you must finish `b`. Determine if all courses can be finished (no cycle). Optionally produce a topological order.

### Data structure & approach

Model as directed graph. Use **Kahn’s algorithm** (BFS topological sort) or DFS-based cycle detection.

* Kahn: compute indegree[], push nodes with indegree 0, repeatedly remove and reduce indegrees. If processed nodes == numCourses → possible.

### Step-by-step

* Build adjacency list and indegree array.
* Start with queue of indegree 0. Pop, reduce neighbors’ indegree; if becomes 0 enqueue.
* If all nodes processed → no cycle.

### Java (Kahn)

```java
class CourseSchedule {
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) adj.add(new ArrayList<>());
        int[] indeg = new int[numCourses];
        for (int[] p : prerequisites) {
            int course = p[0], pre = p[1];
            adj.get(pre).add(course);
            indeg[course]++;
        }

        Queue<Integer> q = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) if (indeg[i] == 0) q.offer(i);

        int visited = 0;
        while (!q.isEmpty()) {
            int u = q.poll(); visited++;
            for (int v : adj.get(u)) {
                indeg[v]--;
                if (indeg[v] == 0) q.offer(v);
            }
        }
        return visited == numCourses;
    }
}
```

**Complexity:** O(V + E).
**Debug tip:** Print indegree array and queue contents at each step. To **return an order**, collect popped nodes into a list.

---

# Problem 4 — Pacific Atlantic Water Flow

### Problem

Given heights matrix, find all coordinates where water can flow to both Pacific (top/left edges) and Atlantic (bottom/right edges). Water flows from high to low or equal (can flow from a cell to neighbor if neighbor height ≤ current).

### Data structure & approach

Treat grid as graph. Key trick: **do two BFS/DFS from the oceans inward**:

* From all Pacific-border cells, mark reachable cells (can reach Pacific).
* From Atlantic-border cells, mark reachable cells (can reach Atlantic).
* Answer = intersection of reachable sets.

Why reverse? Instead of checking for each cell if it can reach oceans by descending paths (expensive), do multi-source BFS that moves to neighbors with equal or greater height (reverse flow).

### Step-by-step

* Build two visited boolean matrices pac[][] and atl[][].
* For all top-row and left-column cells, run DFS/BFS adding neighbors `if neighbor.height >= current.height`. Mark pac.
* Similarly for bottom-row and right-column mark atl.
* Collect cells where both true.

### Java (DFS)

```java
class PacificAtlantic {
    private int rows, cols;
    private int[][] heights;
    private int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

    public List<List<Integer>> pacificAtlantic(int[][] heights) {
        this.heights = heights;
        rows = heights.length;
        cols = heights[0].length;
        boolean[][] pac = new boolean[rows][cols];
        boolean[][] atl = new boolean[rows][cols];

        for (int c = 0; c < cols; c++) {
            dfs(0, c, pac);
            dfs(rows-1, c, atl);
        }
        for (int r = 0; r < rows; r++) {
            dfs(r, 0, pac);
            dfs(r, cols-1, atl);
        }

        List<List<Integer>> res = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (pac[r][c] && atl[r][c]) res.add(Arrays.asList(r, c));
            }
        }
        return res;
    }

    private void dfs(int r, int c, boolean[][] vis) {
        if (vis[r][c]) return;
        vis[r][c] = true;
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                && !vis[nr][nc] && heights[nr][nc] >= heights[r][c]) {
                dfs(nr, nc, vis);
            }
        }
    }
}
```

**Complexity:** O(R·C) — each cell visited at most twice.
**Debug tip:** Visualize one ocean BFS, check which cells are reachable; ensure inequality `>=` used (reverse flow).

---

# Problem 5 — Rotting Oranges

### Problem

Given a grid: 0 empty, 1 fresh orange, 2 rotten orange. Every minute, rotten oranges rot adjacent fresh ones. Return minutes until all fresh rotten, or -1 if impossible.

### Data structure & approach

Model as BFS multi-source: initial rotten oranges are sources with time 0. BFS levels represent minutes. Count fresh oranges; when a fresh becomes rotten, decrement fresh count and set its minute. After BFS, if fresh > 0 → -1 else max minutes.

### Step-by-step

* Push all initial rotten cells into queue, record time 0.
* BFS: pop cell, rot neighbors (fresh -> rotten) with time+1, enqueue.
* Track last time when rot occurred.

### Java

```java
class RottingOranges {
    public int orangesRotting(int[][] grid) {
        int rows = grid.length, cols = grid[0].length;
        Queue<int[]> q = new LinkedList<>();
        int fresh = 0;

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == 2) q.offer(new int[]{r,c});
                if (grid[r][c] == 1) fresh++;
            }

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        int minutes = 0;
        if (fresh == 0) return 0;

        while (!q.isEmpty()) {
            int size = q.size();
            boolean progressed = false;
            for (int i = 0; i < size; i++) {
                int[] cur = q.poll();
                for (int[] d : dirs) {
                    int nr = cur[0] + d[0], nc = cur[1] + d[1];
                    if (nr >=0 && nr < rows && nc >=0 && nc < cols && grid[nr][nc] == 1) {
                        grid[nr][nc] = 2;
                        fresh--;
                        q.offer(new int[]{nr,nc});
                        progressed = true;
                    }
                }
            }
            if (progressed) minutes++;
        }
        return fresh == 0 ? minutes : -1;
    }
}
```

**Complexity:** O(R·C).
**Debug tip:** print grid after each minute to visualize spread.

---

# Problem 6 — Network Delay Time

### Problem

Given directed, weighted edges `times[i] = (u, v, w)` and a starting node `K`, return the time it takes for all nodes to receive the signal (shortest path). If some node unreachable return -1.

### Data structure & approach

This is single-source shortest path with non-negative weights → **Dijkstra’s algorithm** using adjacency list and PriorityQueue.

### Step-by-step

* Build adjacency list: `adj[u] = list of (v,w)`.
* Dist[] initialized to INF, dist[K] = 0.
* PQ holds (dist, node). Pop smallest dist node, relax neighbors: if dist[u] + w < dist[v], update and push v.
* After PQ empties, max over dist[]; if INF present → -1.

### Java (Dijkstra)

```java
class NetworkDelay {
    public int networkDelayTime(int[][] times, int N, int K) {
        List<int[]>[] adj = new ArrayList[N+1];
        for (int i = 1; i <= N; i++) adj[i] = new ArrayList<>();
        for (int[] t : times) adj[t[0]].add(new int[]{t[1], t[2]});

        int[] dist = new int[N+1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[K] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        pq.offer(new int[]{0, K}); // {dist, node}

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int d = cur[0], u = cur[1];
            if (d > dist[u]) continue; // stale entry
            for (int[] edge : adj[u]) {
                int v = edge[0], w = edge[1];
                if (dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    pq.offer(new int[]{dist[v], v});
                }
            }
        }

        int ans = 0;
        for (int i = 1; i <= N; i++) {
            if (dist[i] == Integer.MAX_VALUE) return -1;
            ans = Math.max(ans, dist[i]);
        }
        return ans;
    }
}
```

**Complexity:** O((V + E) log V) with binary heap.
**Debug tip:** print dist[] after each relaxation and when extracting from PQ.

---

## Quick summary table

| Problem            |           Core Graph Idea | Algorithm            |           Time |
| ------------------ | ------------------------: | -------------------- | -------------: |
| Number of Islands  |    grid graph, components | BFS/DFS flood-fill   |         O(R·C) |
| Clone Graph        |  graph copy, avoid cycles | BFS/DFS + map        |         O(V+E) |
| Course Schedule    |  DAG cycle detection/topo | Kahn’s/BFS or DFS    |         O(V+E) |
| Pacific Atlantic   | multi-source reachability | multi-source DFS/BFS |         O(R·C) |
| Rotting Oranges    |   multi-source BFS levels | BFS                  |         O(R·C) |
| Network Delay Time |   shortest paths weighted | Dijkstra (PQ)        | O((V+E) log V) |

---

## Final debugging checklist (general)

* Verify graph construction: print adjacency lists.
* Initialize visited/dist/indegree correctly.
* For BFS, check queue seed nodes and termination.
* For DFS recursion, ensure base cases and visited marking to prevent infinite loops.
* Use small hand-drawn examples and step through queue/stack changes.

---
Yes, let's tackle **Pacific Atlantic Water Flow**. This problem presents a clever twist on standard graph traversal, requiring you to perform the search **in reverse**.

I'll provide the problem statement, real-world applications, the strategic solution using reverse traversal, a step-by-step example, and the Java solution in GitHub Markdown format.

-----

## 🌊 Problem: Pacific Atlantic Water Flow

### Problem Statement

You are given an $m \times n$ rectangular island represented by a grid of **heights** (integers). The island is bordered by two oceans:

1.  The **Pacific Ocean** touches the island's top and left edges.
2.  The **Atlantic Ocean** touches the island's bottom and right edges.

Water can flow from a cell to any of its four adjacent neighbors (horizontal or vertical) if the neighbor's height is **less than or equal to** the current cell's height.

Your goal is to find a list of grid coordinates $(r, c)$ from which water can flow to **both** the Pacific and Atlantic Oceans.

### Real-World Use Case 🗺️

This problem models situations where a flow must satisfy multiple boundary conditions:

1.  **Drainage and Watershed Modeling:** Determining areas where rainfall will drain into two or more distinct watersheds or river systems. The heights represent the terrain, and the oceans represent the collection points.
2.  **Resource Distribution Networks:** Identifying source points in a network (like a pipeline or electrical grid) that can simultaneously supply two separate major consumers (the "oceans"), based on flow constraints (like pressure or capacity, modeled by height).
3.  **Signal Transmission:** Finding nodes in a network that can successfully transmit a signal that reaches two specific, distant termination points, where signal strength must not drop below a certain threshold (the height constraint).

-----

### Solution Strategy (Reverse Traversal)

The difficulty lies in tracing the flow path from the center of the island outward to **both** oceans. Tracing flow forward is complex because a single cell can lead to multiple paths.

The key insight is to **reverse the problem**: Instead of seeing where water *can flow to* from a cell, let's see where water *can flow from* to reach the cell.

1.  **Reverse Flow Rule:** If water flows from cell $A$ to cell $B$ (meaning $\text{height}_A \ge \text{height}_B$), then in reverse, the "flow" (or path accessibility) goes from $B$ to $A$ (meaning $\text{height}_B \le \text{height}_A$).
2.  **Two Separate Traversal Sets:**
      * Initialize two separate $m \times n$ boolean matrices: `pacificReachable` and `atlanticReachable`.
3.  **Pacific Traversal:** Start a **DFS** (or BFS) from **all** cells on the **Pacific boundary** (top row and left column). During the traversal, if we move from cell $A$ to an adjacent cell $B$ where $\text{height}_B \ge \text{height}_A$, we mark $B$ as reachable by the Pacific.
4.  **Atlantic Traversal:** Perform the exact same **DFS** (or BFS) starting from **all** cells on the **Atlantic boundary** (bottom row and right column), marking cells in `atlanticReachable`.
5.  **Final Result:** Iterate through the entire grid one last time. Any cell $(r, c)$ where **both** `pacificReachable[r][c]` AND `atlanticReachable[r][c]` are true is a valid result.

-----

### Step-by-Step Example (DFS)

Consider a small height map:

| | 0 | 1 | 2 |
| :---: | :---: | :---: | :---: |
| **0** | **3** | **5** | **4** | $\leftarrow$ Pacific Top Border
| **1** | **2** | 1 | **6** |
| **2** | **3** | **7** | **8** | $\leftarrow$ Atlantic Bottom Border
| $\uparrow$ Pacific Left Border | | | $\uparrow$ Atlantic Right Border

#### 1\. Pacific Reachability (`P`)

  * **Start** from Pacific border cells: (0,0), (0,1), (0,2), (1,0), (2,0).
  * **DFS from (0,1) [Height 5]:** Can move to (1,1) [Height 1]? No, $1 < 5$ (reverse rule is $\text{neighbor} \ge \text{current}$). Can move to (0,0) [Height 3]? No.
  * **DFS from (2,0) [Height 3]:** Can move to (1,0) [Height 2]? No.
  * **DFS from (1,0) [Height 2]:** Can move to (0,0) [Height 3]. Yes, $3 \ge 2$. Mark (0,0) as P.
  * ...after full traversal...
  * **P-Reachable Cells:** (0,0), (0,1), (0,2), (1,0), (1,2), (2,0), (2,1), (2,2)

#### 2\. Atlantic Reachability (`A`)

  * **Start** from Atlantic border cells: (2,0), (2,1), (2,2), (0,2), (1,2).
  * **DFS from (1,2) [Height 6]:** Can move to (0,2) [Height 4]? No. Can move to (1,1) [Height 1]? No.
  * **DFS from (2,1) [Height 7]:** Can move to (1,1) [Height 1]? No. Can move to (2,0) [Height 3]? No.
  * **DFS from (0,2) [Height 4]:** Can move to (0,1) [Height 5]. Yes, $5 \ge 4$. Mark (0,1) as A.
  * ...after full traversal...
  * **A-Reachable Cells:** (0,1), (0,2), (1,2), (2,0), (2,1), (2,2)

#### 3\. Final Result

Check for cells marked **both P and A**:

  * (0,1): P and A $\rightarrow$ **Yes**
  * (0,2): P and A $\rightarrow$ **Yes**
  * (1,2): P and A $\rightarrow$ **Yes**
  * (2,0): P and A $\rightarrow$ **Yes**
  * (2,1): P and A $\rightarrow$ **Yes**
  * (2,2): P and A $\rightarrow$ **Yes**

**Result:** $[[0, 1], [0, 2], [1, 2], [2, 0], [2, 1], [2, 2]]$

-----

### Java Solution (GitHub Markdown Format)

```java
/**
 * Pacific Atlantic Water Flow Problem - Solution using Reverse Depth-First Search (DFS).
 * Instead of checking flow *out* from every cell, we check flow *in* from the oceans.
 * A cell (r, c) is a valid result if it can be reached by a reverse flow from BOTH oceans.
 */
class Solution {
    int rows;
    int cols;

    /**
     * Main method to find the coordinates that flow to both oceans.
     * @param heights The 2D grid of elevation heights.
     * @return A list of coordinates [r, c] that can flow to both oceans.
     */
    public List<List<Integer>> pacificAtlantic(int[][] heights) {
        // Handle edge case for an empty map
        if (heights == null || heights.length == 0 || heights[0].length == 0) {
            return new ArrayList<>();
        }

        this.rows = heights.length;
        this.cols = heights[0].length;

        // Two boolean matrices to track reachability from each ocean.
        // pacific[r][c] = true means water can flow *from* the Pacific *to* this cell (r, c).
        boolean[][] pacificReachable = new boolean[rows][cols];
        boolean[][] atlanticReachable = new boolean[rows][cols];

        // 1. Initiate DFS from all border cells.

        // DFS on Top (Pacific) and Bottom (Atlantic) rows
        for (int c = 0; c < cols; c++) {
            // Pacific border: Top row (r=0)
            dfs(heights, 0, c, pacificReachable); 
            // Atlantic border: Bottom row (r=rows-1)
            dfs(heights, rows - 1, c, atlanticReachable);
        }

        // DFS on Left (Pacific) and Right (Atlantic) columns
        for (int r = 0; r < rows; r++) {
            // Pacific border: Left column (c=0)
            dfs(heights, r, 0, pacificReachable); 
            // Atlantic border: Right column (c=cols-1)
            dfs(heights, r, cols - 1, atlanticReachable);
        }


        // 2. Collect the final result.
        List<List<Integer>> result = new ArrayList<>();
        
        // Iterate through all cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                
                // If a cell is reachable by reverse flow from BOTH oceans, 
                // it means water can flow *from* this cell *to* both oceans.
                if (pacificReachable[r][c] && atlanticReachable[r][c]) {
                    result.add(Arrays.asList(r, c));
                }
            }
        }

        return result;
    }

    /**
     * Depth-First Search function to find all cells reachable by a reverse flow 
     * starting from the ocean borders.
     * * @param heights The height map.
     * @param r The current row index.
     * @param c The current column index.
     * @param reachable The boolean matrix (pacificReachable or atlanticReachable) to mark.
     */
    private void dfs(int[][] heights, int r, int c, boolean[][] reachable) {
        // If the cell is already marked as reachable, stop and return.
        if (reachable[r][c]) {
            return;
        }

        // Mark the current cell as reachable.
        reachable[r][c] = true;

        // Define the possible moves: up, down, left, right.
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        // Explore all four neighbors
        for (int[] dir : directions) {
            int newR = r + dir[0];
            int newC = c + dir[1];

            // 1. Check boundary conditions
            if (newR < 0 || newR >= rows || newC < 0 || newC >= cols) {
                continue;
            }

            // 2. Check the reverse flow condition: 
            // The neighbor's height must be GREATER THAN OR EQUAL to the current cell's height.
            // This is because we are moving UP the slope in the reverse direction.
            if (heights[newR][newC] >= heights[r][c]) {
                dfs(heights, newR, newC, reachable);
            }
        }
    }
}
```

-----

Absolutely\! **Course Schedule** is a cornerstone problem for understanding **Topological Sorting** and detecting **cycles** in directed graphs, a crucial skill in advanced algorithms.

Here is the detailed explanation, use cases, step-by-step example, and the Java solution in GitHub Markdown format.

-----

## 📅 Problem: Course Schedule

### Problem Statement

You are given a total number of courses, `numCourses`, and a list of `prerequisites` pairs. Each pair `[a, b]` indicates that you must take course $b$ before taking course $a$.

Your task is to determine if it is possible to finish **all** courses.

**Example:** If the input is `[1, 0]`, it means to take course 1, you must first take course 0. This is possible.
**Goal:** Return `true` if all courses can be finished, and `false` otherwise.

### Real-World Use Case 🔄

This problem directly models **dependency resolution**—situations where tasks or events must occur in a specific order:

1.  **Software Build Systems (e.g., Makefiles, Gradle):** Determining the correct order to compile source files or modules. If Module A depends on Module B, B must be compiled first. A circular dependency (A depends on B, and B depends on A) makes the build impossible.
2.  **Job Scheduling:** Sequencing tasks in a production pipeline (e.g., manufacturing, data processing). If Task 1 must finish before Task 2 can start, a dependency exists. The graph shows the order.
3.  **Academic Curriculum Planning:** The problem itself\! Ensuring all prerequisite courses are satisfied before enrolling in advanced courses. An impossible schedule results from a prerequisite cycle (e.g., Math I requires Physics I, and Physics I requires Math I).

-----

### Solution Strategy (Topological Sorting via DFS/Kahn's Algorithm)

The core idea is that finishing all courses is only possible if the graph of prerequisites is a **Directed Acyclic Graph (DAG)**—meaning there are **no cycles**.

**Topological Sorting** is an algorithm used to find a linear ordering of nodes in a DAG where, for every directed edge from node $A$ to node $B$, node $A$ comes before node $B$ in the ordering.

We can solve this problem using **Kahn's Algorithm (BFS-based)**, which relies on tracking **in-degrees**:

1.  **Build the Graph and In-Degrees:**
      * Represent the courses and prerequisites as a **Directed Graph (Adjacency List)**.
      * Calculate the **In-Degree** (the number of incoming edges/prerequisites) for every course.
2.  **Initialize Queue:** Start a queue with all courses that have an **in-degree of 0** (i.e., courses with no prerequisites).
3.  **Process and Decrement:**
      * While the queue is not empty, dequeue a course. This course can now be "taken."
      * For every course that this "taken" course is a prerequisite for (its neighbors), **decrement their in-degree** by one.
      * If any neighbor's in-degree drops to **zero**, enqueue it (it can now be taken).
4.  **Check for Cycle:** Keep a count of the "taken" courses. If the final count equals `numCourses`, then a topological sort was possible (no cycle), and we return `true`. If the count is less than `numCourses`, a cycle exists (some courses could never reach an in-degree of zero), and we return `false`.

-----

### Step-by-Step Example (Kahn's Algorithm)

**Input:** `numCourses = 4`, `prerequisites = [[1, 0], [2, 0], [3, 1], [3, 2]]`

(Course 1 requires 0, 2 requires 0, 3 requires 1, 3 requires 2)

#### 1\. Build Graph and In-Degrees

| Course | Prerequisites (In-Degree) | Neighbors (Adjacency List) |
| :---: | :---: | :--- |
| **0** | **0** | [1, 2] |
| **1** | 1 (from 0) | [3] |
| **2** | 1 (from 0) | [3] |
| **3** | 2 (from 1, 2) | [ ] |

#### 2\. Initialize Queue

  * Only Course 0 has an in-degree of 0.
  * **Queue:** `[0]`
  * **Taken Count:** `0`

#### 3\. Process and Decrement

| Step | Queue Start | Course Taken | Neighbors Affected | New In-Degrees | Taken Count |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | `[0]` | **0** | 1, 2 | $\text{In-Deg}(1)=0, \text{In-Deg}(2)=0$ | 1 |
| 2 | `[1, 2]` | **1** | 3 | $\text{In-Deg}(3)=1$ | 2 |
| 3 | `[2, 3]` | **2** | 3 | $\text{In-Deg}(3)=0$ | 3 |
| 4 | `[3]` | **3** | None | | 4 |
| 5 | `[]` | Done | | | 4 |

#### 4\. Final Check

  * Taken Count: **4**
  * Num Courses: **4**
  * **Result:** $4 = 4$, so return **true**. It is possible to finish all courses.

-----

### Java Solution (GitHub Markdown Format)

```java
import java.util.*;

/**
 * Course Schedule Problem - Solution using Kahn's Algorithm (BFS/In-Degree Check).
 * The problem is solvable if and only if the dependency graph is a Directed Acyclic Graph (DAG).
 * Kahn's algorithm finds a topological sort, and if the number of sorted nodes 
 * equals the total number of courses, there is no cycle.
 */
class Solution {
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        
        // 1. Data Structures Setup
        // Adjacency List: graph[i] contains all courses that require course i as a prerequisite.
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < numCourses; i++) {
            adj.add(new ArrayList<>());
        }

        // In-Degree Array: inDegree[i] stores the count of prerequisites for course i.
        int[] inDegree = new int[numCourses];
        
        // 2. Build Graph and Calculate In-Degrees
        // A prerequisite [a, b] means: to take 'a', you need 'b'. 
        // So, the edge is b -> a.
        for (int[] prerequisite : prerequisites) {
            int course = prerequisite[0]; // Course 'a'
            int pre = prerequisite[1];    // Prerequisite 'b'

            // Add directed edge: pre -> course
            adj.get(pre).add(course); 
            
            // Increment the in-degree of the dependent course
            inDegree[course]++; 
        }

        // 3. Initialize Queue for Kahn's Algorithm
        // The queue starts with all nodes (courses) that have no prerequisites (in-degree == 0).
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }

        int coursesTakenCount = 0; // Tracks the number of courses successfully added to the topological order

        // 4. Process Courses (BFS-based Traversal)
        while (!queue.isEmpty()) {
            int course = queue.poll();
            coursesTakenCount++; // This course can be taken successfully.

            // Iterate through all courses that depend on the current course ('course' is their prerequisite)
            for (int dependentCourse : adj.get(course)) {
                
                // Since 'course' is now taken, one of the prerequisites for 'dependentCourse' is satisfied.
                inDegree[dependentCourse]--;

                // If the dependent course now has no remaining prerequisites (in-degree == 0),
                // it can be taken next, so add it to the queue.
                if (inDegree[dependentCourse] == 0) {
                    queue.offer(dependentCourse);
                }
            }
        }

        // 5. Final Check for Cycles
        // If coursesTakenCount equals numCourses, a full topological sort was possible, meaning no cycle exists.
        // If coursesTakenCount is less than numCourses, a cycle exists (the courses in the cycle never reached in-degree 0).
        return coursesTakenCount == numCourses;
    }
}
```

-----

Yes, let's proceed with **Graph Valid Tree**. This is a great conceptual problem that ties together fundamental graph properties.

Since this is a **Premium** problem, the structure and logic below represent the common approach to solving it, which relies on checking two key conditions that define a tree.

Here is the detailed explanation, use cases, the strategic solution, a step-by-step example, and the Java solution in the requested GitHub Markdown format.

-----

## 🌳 Problem: Graph Valid Tree

### Problem Statement

Given $n$ nodes labeled from $0$ to $n-1$ and a list of undirected edges (pairs of nodes), determine if the graph formed by these nodes and edges is a valid **tree**.

A **valid tree** must satisfy two fundamental conditions:

1.  It must be **fully connected**.
2.  It must contain **no cycles**.

### Real-World Use Case 🏷️

Checking if a given graph structure is a valid tree is common in data modeling and network management:

1.  **Hierarchical Data Structures:** Validating data integrity in structures like organizational charts, file system directories, or XML/JSON document models, all of which are fundamentally tree-based and must not contain cycles (e.g., a folder inside one of its own subfolders).
2.  **Network Spanning Trees:** When managing a network (like an Ethernet or Wi-Fi mesh), algorithms (like the Spanning Tree Protocol, STP) ensure that the network topology is a **spanning tree**—connected but without redundant paths (cycles) that can cause broadcast storms.
3.  **Abstract Syntax Trees (ASTs):** Compilers and interpreters use ASTs to represent code structure, which must always be a valid tree to ensure deterministic execution flow.

-----

### Solution Strategy (Two Conditions Check)

For a connected graph with $n$ nodes and $e$ edges to be a tree, it must satisfy two conditions simultaneously:

1.  **No Cycles (Acyclicity):** A graph with $n$ nodes and $e$ edges that is connected must have exactly $e = n - 1$ edges to be a tree. If $e > n - 1$, a cycle **must** exist. If $e < n - 1$, the graph **cannot** be connected.
      * **Initial Check:** If the number of edges is not equal to `n - 1`, we can immediately return `false`.
2.  **Full Connectivity:** We must confirm that all $n$ nodes are part of the single, resulting component. We can use **Depth-First Search (DFS)** or **Breadth-First Search (BFS)** starting from an arbitrary node (e.g., node 0) to count the number of reachable nodes.

The combined strategy:

1.  **Count Edges:** Verify that `edges.length == n - 1`. If not, return `false`.
2.  **Check Connectivity/Acyclicity (DFS/BFS):**
      * Use DFS (or BFS) starting from node 0.
      * Keep track of visited nodes using a boolean array or a set.
      * During the traversal, if we ever encounter an already visited node that is **not** the immediate parent of the current node, it means a **cycle exists** (this is the standard DFS cycle detection for undirected graphs, although the $e=n-1$ check handles this for connected graphs). *However, since we already checked $e=n-1$, we simplify the focus to connectivity.*
      * **Final Count:** After the traversal completes, check if the total number of visited nodes equals $n$. If it does, the graph is fully connected and, combined with the $e=n-1$ check, it's a valid tree.

-----

### Step-by-Step Example (Combined Check)

**Input 1 (Valid Tree):** $n=5$, `edges = [[0, 1], [0, 2], [0, 3], [3, 4]]`

#### 1\. Initial Edge Check

  * Nodes ($n$): 5
  * Edges ($e$): 4
  * Is $e = n - 1$? $4 = 5 - 1$. **YES.** Proceed to connectivity check.

#### 2\. Connectivity Check (DFS from node 0)

| Step | Current Node | Visited Set | Action |
| :--- | :--- | :--- | :--- |
| 1 | Start at **0** | $\{0\}$ | Visit neighbor 1. |
| 2 | Visit **1** | $\{0, 1\}$ | No unvisited neighbors. Backtrack. |
| 3 | Visit **2** | $\{0, 1, 2\}$ | No unvisited neighbors. Backtrack. |
| 4 | Visit **3** | $\{0, 1, 2, 3\}$ | Visit neighbor 4. |
| 5 | Visit **4** | $\{0, 1, 2, 3, 4\}$ | Traversal complete. |

#### 3\. Final Check

  * Visited Count: 5
  * Total Nodes ($n$): 5
  * Is Visted Count $= n$? $5 = 5$. **YES.**
  * **Result:** **True** (It is a valid tree).

-----

**Input 2 (Invalid - Cycle):** $n=5$, `edges = [[0, 1], [0, 2], [1, 2], [3, 4]]`

#### 1\. Initial Edge Check

  * Nodes ($n$): 5
  * Edges ($e$): 4
  * Is $e = n - 1$? $4 = 5 - 1$. **YES.** Proceed to connectivity check.

#### 2\. Connectivity Check (DFS from node 0)

| Step | Current Node | Visited Set | Notes |
| :--- | :--- | :--- | :--- |
| 1 | Start at **0** | $\{0\}$ | Visit neighbor 1. |
| 2 | Visit **1** | $\{0, 1\}$ | Visit neighbor 2. |
| 3 | Visit **2** | $\{0, 1, 2\}$ | Neighbor **0** is visited, but is 0 the parent of 2? No, 1 is the parent. $\rightarrow$ **Cycle Found.** |
| **Result:** **False** (A cycle exists: 0-1-2-0).

*(Note: Although $e=n-1$, the DFS confirms the cycle. This is the more robust way to solve it if the $e=n-1$ check is not done first, but in the combined check, we rely on DFS's cycle detection capability for correctness.)*

-----

### Java Solution (GitHub Markdown Format)

```java
import java.util.*;

/**
 * Graph Valid Tree Problem - Solution using DFS and Edge Count Check.
 * A graph with N nodes is a valid tree if and only if:
 * 1. It has exactly N - 1 edges (Necessary condition for acyclic and connected).
 * 2. It is fully connected (DFS/BFS traversal reaches all N nodes).
 */
class Solution {
    /**
     * Determines if the given graph structure is a valid tree.
     * @param n The total number of nodes (0 to n-1).
     * @param edges The list of undirected edges [u, v].
     * @return True if the graph is a valid tree, False otherwise.
     */
    public boolean validTree(int n, int[][] edges) {
        
        // --- Condition 1: Edge Count Check ---
        // A connected graph with N nodes must have exactly N-1 edges to be a tree.
        if (edges.length != n - 1) {
            // If e < n-1, it cannot be connected. If e > n-1, a cycle must exist.
            return false;
        }

        // --- Condition 2: Full Connectivity Check (via Adjacency List and DFS) ---

        // 1. Build Adjacency List (undirected graph)
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            adj.get(u).add(v);
            adj.get(v).add(u); // Since the graph is undirected
        }

        // 2. Perform DFS to check connectivity
        // We use a set to keep track of visited nodes.
        Set<Integer> visited = new HashSet<>();
        
        // Start DFS from an arbitrary node, typically node 0.
        // The parent parameter is crucial for DFS cycle detection in undirected graphs,
        // but since we checked e=n-1, we only need the side effect of counting visited nodes.
        dfs(adj, 0, -1, visited);

        // 3. Final Check
        // If the graph is fully connected, the number of visited nodes must equal n.
        return visited.size() == n;
    }

    /**
     * Depth-First Search to traverse the graph and mark visited nodes.
     * @param adj The adjacency list representation of the graph.
     * @param curr The current node being visited.
     * @param parent The node from which curr was visited (used to avoid treating 
     * the path back to the parent as a cycle).
     * @param visited The set of already visited nodes.
     */
    private void dfs(List<List<Integer>> adj, int curr, int parent, Set<Integer> visited) {
        // If node is already visited, stop recursion for this path.
        if (visited.contains(curr)) {
            return;
        }

        visited.add(curr);

        // Explore neighbors
        for (int neighbor : adj.get(curr)) {
            
            // Only recurse if the neighbor is not the immediate parent.
            if (neighbor != parent) {
                dfs(adj, neighbor, curr, visited);
            }
        }
    }
}
```

-----
Yes, let's cover **Number of Connected Components in an Undirected Graph**. This problem is a foundational concept in graph theory and has two excellent solution approaches: **Graph Traversal (DFS/BFS)** and **Union-Find**.

I will focus on the **Graph Traversal** approach first, as it directly builds on the techniques used in the previous problems, and then briefly mention the Union-Find alternative.

Here is the detailed explanation, use cases, strategic solution, a step-by-step example, and the Java solution in the requested GitHub Markdown format.

-----

## 🧩 Problem: Number of Connected Components in an Undirected Graph

### Problem Statement

You are given $n$ nodes labeled from $0$ to $n-1$ and a list of undirected `edges` (pairs of nodes).

Your task is to find the total **number of connected components** in the graph.

A **connected component** is a subgraph in which any two nodes are connected to each other by paths, and which is connected to no additional nodes in the supergraph. Essentially, it's a completely separate, isolated piece of the overall graph.

### Real-World Use Case 🗺️

Identifying connected components is a core task in analyzing networks and data clustering:

1.  **Social Network Analysis:** Finding distinct **communities** or **groups** where every person within the group is connected (directly or indirectly) to every other person in that group, but not connected to anyone outside the group.
2.  **Network Monitoring:** Determining how many **isolated subnets** or **clusters of devices** exist in a larger computer network. This helps in diagnosing segmentation or reachability issues.
3.  **Image Segmentation:** In image processing, identifying separate, distinct **objects** (components) within an image, often modeled as a grid graph.

-----

### Solution Strategy (Graph Traversal - DFS/BFS)

The most intuitive way to solve this is by systematically searching through the graph and keeping track of which nodes belong to which component.

1.  **Build the Graph:** Convert the list of edges into an **Adjacency List** to easily find a node's neighbors. Since the graph is undirected, add the connection in both directions (A $\rightarrow$ B and B $\rightarrow$ A).
2.  **Initialize Counter:** Start a counter, `component_count = 0`.
3.  **Track Visited Nodes:** Use a boolean array or a set (`visited`) to mark nodes that have already been included in a counted component.
4.  **Iterate Nodes:** Loop through every node from $0$ to $n-1$.
5.  **Start New Component:** If the current node $i$ has **not** been visited:
    a.  Increment `component_count`. This node marks the start of a **new, uncounted component**.
    b.  Start a **DFS** (or BFS) from node $i$.
    c.  The DFS/BFS will visit and **mark as visited** all nodes that belong to this single component.
6.  **Final Result:** After the loop finishes, `component_count` holds the total number of connected components.

-----

### Step-by-Step Example (DFS)

**Input:** $n=5$, `edges = [[0, 1], [2, 3]]`

#### 1\. Build Adjacency List

| Node | Neighbors |
| :---: | :---: |
| **0** | [1] |
| **1** | [0] |
| **2** | [3] |
| **3** | [2] |
| **4** | [ ] |

#### 2\. Traversal and Counting

  * **Visited Set:** `{}`
  * **Component Count:** $0$

| Step | Current Node (i) | Visited? | Action | Count | Visited Set After DFS | Component |
| :--- | :--- | :--- | :--- | :---: | :--- | :--- |
| 1 | **i = 0** | No | Start DFS. **Count = 1**. | 1 | $\{0, 1\}$ | $\{0, 1\}$ |
| 2 | **i = 1** | Yes | Skip. | 1 | $\{0, 1\}$ | |
| 3 | **i = 2** | No | Start DFS. **Count = 2**. | 2 | $\{0, 1, 2, 3\}$ | $\{2, 3\}$ |
| 4 | **i = 3** | Yes | Skip. | 2 | $\{0, 1, 2, 3\}$ | |
| 5 | **i = 4** | No | Start DFS. **Count = 3**. | 3 | $\{0, 1, 2, 3, 4\}$ | $\{4\}$ |
| 6 | **Loop Ends** | | | **3** | | |

**Result:** 3 Connected Components.

-----

### Alternative Solution: Union-Find (Disjoint Set Union)

For this specific problem, Union-Find is often the most efficient and elegant solution.

1.  **Initialize:** Start with $n$ separate components (each node is its own component), so `count = n`.
2.  **Process Edges:** Iterate through every edge $(u, v)$:
      * Use the `union(u, v)` operation. If $u$ and $v$ were already in the same component, do nothing.
      * If $u$ and $v$ belong to **different** components, merging them reduces the total number of connected components by 1. Decrement `count`.
3.  **Final Result:** The final value of `count` is the answer.

-----

### Java Solution (Graph Traversal - GitHub Markdown Format)

```java
import java.util.*;

/**
 * Number of Connected Components Problem - Solution using Depth-First Search (DFS).
 * The strategy is to iterate through all nodes, and every time we encounter an unvisited node, 
 * we increment the component count and use DFS to mark all nodes in that component as visited.
 */
class Solution {
    
    /**
     * Finds the total number of connected components in the undirected graph.
     * @param n The number of nodes (0 to n-1).
     * @param edges The list of undirected edges [u, v].
     * @return The total number of connected components.
     */
    public int countComponents(int n, int[][] edges) {
        
        // 1. Build the Adjacency List (undirected graph representation)
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            adj.get(u).add(v);
            adj.get(v).add(u); // Essential for undirected graph
        }
        
        // 2. Initialize tracking variables
        boolean[] visited = new boolean[n]; // Tracks nodes already assigned to a component
        int componentCount = 0;
        
        // 3. Iterate through every node to find the start of new components
        for (int i = 0; i < n; i++) {
            
            // If the node 'i' has not been visited, it must belong to a brand new component.
            if (!visited[i]) {
                componentCount++; // Found a new component!
                
                // Use DFS to find and mark ALL connected nodes in this component.
                dfs(adj, i, visited);
            }
        }
        
        return componentCount;
    }

    /**
     * Depth-First Search function to traverse and mark all nodes in a single connected component.
     * @param adj The adjacency list.
     * @param u The current node.
     * @param visited The boolean array tracking visited status.
     */
    private void dfs(List<List<Integer>> adj, int u, boolean[] visited) {
        
        // Base Case: If the node is already visited, we stop.
        if (visited[u]) {
            return;
        }

        // Action: Mark the current node as visited.
        visited[u] = true;

        // Recursive Step: Explore all unvisited neighbors.
        for (int v : adj.get(u)) {
            dfs(adj, v, visited);
        }
    }
}
```

-----

Yes, let's conclude this set of problems with **Longest Consecutive Sequence**. While it looks like an array problem, its solution cleverly models the elements and their relationships as chains or components in an implicit graph, which is why it's often grouped with graph concepts.

Here is the detailed explanation, use cases, the strategic solution, a step-by-step example, and the Java solution in the requested GitHub Markdown format.

-----

## 🔢 Problem: Longest Consecutive Sequence

### Problem Statement

Given an unsorted array of integers `nums`, find the length of the **longest consecutive elements sequence**.

The sequence is defined as a set of numbers $\{x, x+1, x+2, \dots, x+k\}$ where all elements are present in the input array `nums`.

Your algorithm should run in $O(n)$ time complexity.

### Real-World Use Case 📈

This problem relates to finding continuous ranges or clusters within a dataset, a common requirement in data analysis:

1.  **Data Clustering/Missing Data Analysis:** Identifying continuous ranges of existing IDs, timestamps, or sequential data points in a database or log file. This can highlight gaps where data is missing or where a continuous event occurred.
2.  **Resource Allocation:** Finding the largest continuous block of available memory addresses, server slots, or inventory IDs from a scattered list of used resources.
3.  **Pattern Recognition in Genomics:** Identifying the longest sequence of consecutive genetic markers or base pairs present in a sample, often treated as a set of values.

-----

### Solution Strategy (Optimized Set Lookup)

A brute-force approach (sorting the array) takes $O(n \log n)$, which is too slow. To achieve the required **$O(n)$** time complexity, we use a **Set** and a smart traversal technique.

1.  **Convert to Set:** First, insert all array elements into a **HashSet**. This allows us to perform $O(1)$ (average time) lookups for neighbors ($x-1$ and $x+1$).
2.  **Identify Sequence Start:** Iterate through each number $x$ in the original array (or the set). We only want to start tracking a new sequence if $x$ is the *starting element* of a consecutive sequence.
      * **How to find the start?** Check if the element $x - 1$ exists in the set.
      * If $x - 1$ **is present**, then $x$ is part of an existing sequence that started earlier, so we **skip $x$** and move to the next number.
      * If $x - 1$ **is NOT present**, then $x$ is the true **start** of a new consecutive sequence.
3.  **Traverse and Count:** Once we identify a start element $x$:
      * Initialize `current_length = 1`.
      * Check for $x + 1$, $x + 2$, $x + 3$, and so on, using the HashSet's $O(1)$ lookup.
      * Increment `current_length` for every subsequent consecutive element found.
      * Update the `longest_sequence` length with `current_length`.
4.  **$O(n)$ Time Complexity:** Although we have a nested loop structure (outer loop iterates through $n$ elements, inner loop traverses the sequence), the inner loop only executes for elements that are **sequence starts**. Each element in the set is checked against the $x-1$ condition once, and is traversed by the inner `while` loop at most once. Therefore, the total time complexity remains linear, $O(n)$.

-----

### Step-by-Step Example

**Input:** `nums = [100, 4, 200, 1, 3, 2]`

#### 1\. Convert to Set

`numSet = {100, 4, 200, 1, 3, 2}`

#### 2\. Iterate and Find Starts

| Element ($x$) | Is $x-1$ in Set? | Action | Current Seq Length | Longest Seq Length |
| :---: | :---: | :--- | :---: | :---: |
| **100** | No (99 not found) | **START.** Check 101. Not found. | 1 | **1** |
| **4** | No (3 found) | **SKIP.** (Wait, 3 *is* found in the set, but 4 is still the start of the sequence 1, 2, 3, 4. Let's re-run the check using the logic: Is 3 present? Yes. So 4 is not the start.) | - | 1 |
| **200** | No (199 not found) | **START.** Check 201. Not found. | 1 | 1 |
| **1** | No (0 not found) | **START.** Check 2. Found. | 2 | 2 |
| | | Check 3. Found. | 3 | 3 |
| | | Check 4. Found. | 4 | **4** |
| | | Check 5. Not found. | - | 4 |
| **3** | Yes (2 found) | **SKIP.** (Part of the sequence starting at 1). | - | 4 |
| **2** | Yes (1 found) | **SKIP.** (Part of the sequence starting at 1). | - | 4 |

**Result:** The Longest Consecutive Sequence length is **4** (Sequence: 1, 2, 3, 4).

*(Self-Correction in the table: The key element of the algorithm is that we only perform the inner `while` loop when $x$ is a confirmed **start** point, making the traversal for intermediate numbers highly efficient.)*

-----

### Java Solution (GitHub Markdown Format)

```java
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Longest Consecutive Sequence Problem - Solution using HashSet for O(1) lookups.
 * The key to achieving O(n) time is to only begin traversing a sequence when we find 
 * a confirmed starting element (a number x such that x-1 does NOT exist in the set).
 */
class Solution {
    /**
     * Finds the length of the longest consecutive elements sequence.
     * @param nums The unsorted array of integers.
     * @return The length of the longest sequence.
     */
    public int longestConsecutive(int[] nums) {
        // Handle edge case for an empty array
        if (nums == null || nums.length == 0) {
            return 0;
        }

        // 1. Insert all numbers into a HashSet for O(1) average time lookups.
        Set<Integer> numSet = new HashSet<>();
        for (int num : nums) {
            numSet.add(num);
        }

        int longestStreak = 0;

        // 2. Iterate through the numbers in the original array (or the set)
        for (int num : nums) {
            
            // Check if 'num' is the START of a sequence.
            // A number 'num' is the start if 'num - 1' is NOT present in the set.
            if (!numSet.contains(num - 1)) {
                
                // We found a sequence start! Begin traversing and counting the streak.
                int currentNum = num;
                int currentStreak = 1;

                // 3. Traverse the sequence by checking consecutive numbers (num + 1, num + 2, ...)
                while (numSet.contains(currentNum + 1)) {
                    currentNum += 1; // Move to the next consecutive number
                    currentStreak += 1; // Increment the sequence length
                }

                // 4. Update the overall longest streak found so far.
                longestStreak = Math.max(longestStreak, currentStreak);
            }
            
            // Note: If 'num - 1' IS present, we skip this number because it belongs 
            // to a sequence that started earlier, thus avoiding redundant work 
            // and guaranteeing O(n) complexity.
        }

        return longestStreak;
    }
}
```

-----