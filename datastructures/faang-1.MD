# FAANG DSA Interview Preparation Guide - Complete

## 1. Graphs & Traversals

### Problem 1: Number of Islands

**Problem Expectation**: Given a 2D grid of '1's (land) and '0's (water), count the number of islands. An island is surrounded by water and formed by connecting adjacent lands horizontally or vertically.

```python
from collections import deque

def numIslands(grid):
    """
    Count connected components of '1's in a 2D grid using BFS/DFS
    Time: O(M*N), Space: O(min(M,N)) for BFS, O(M*N) worst case for DFS
    """
    if not grid or not grid[0]:
        return 0
    
    rows, cols = len(grid), len(grid[0])
    count = 0
    
    def bfs(i, j):
        """BFS to mark all connected lands as visited"""
        queue = deque([(i, j)])
        grid[i][j] = '0'  # Mark as visited
        
        directions = [(1,0), (-1,0), (0,1), (0,-1)]
        
        while queue:
            x, y = queue.popleft()
            for dx, dy in directions:
                nx, ny = x + dx, y + dy
                # Check boundaries and if it's land
                if 0 <= nx < rows and 0 <= ny < cols and grid[nx][ny] == '1':
                    grid[nx][ny] = '0'  # Mark visited
                    queue.append((nx, ny))
    
    for i in range(rows):
        for j in range(cols):
            if grid[i][j] == '1':
                count += 1
                bfs(i, j)  # Explore entire island
    
    return count

# Code Walkthrough:
# 1. Iterate through each cell in grid
# 2. When we find land ('1'), increment count and explore entire island
# 3. BFS explores all connected lands and marks them as visited ('0')
# 4. This ensures we don't count the same island multiple times

# Data Structure: 2D Array + Queue for BFS
# Operations: 
# - Grid access: O(1)
# - Queue operations: O(1) enqueue/dequeue
# - Neighbor exploration: O(1) per direction
```

---

### Problem 2: Word Ladder

**Problem Expectation**: Find the length of shortest transformation sequence from beginWord to endWord, where only one letter can be changed at a time and each intermediate word must exist in wordList.

```python
from collections import deque
import string

def ladderLength(beginWord, endWord, wordList):
    """
    BFS to find shortest path in word graph where words differ by one letter
    Time: O(M² * N) where M=word length, N=word list size
    Space: O(N) for word set and queue
    """
    wordSet = set(wordList)
    if endWord not in wordSet:
        return 0
    
    queue = deque([(beginWord, 1)])  # (word, steps)
    
    while queue:
        current_word, steps = queue.popleft()
        
        if current_word == endWord:
            return steps
        
        # Generate all possible one-letter transformations
        for i in range(len(current_word)):
            for char in string.ascii_lowercase:
                if char != current_word[i]:
                    new_word = current_word[:i] + char + current_word[i+1:]
                    
                    if new_word in wordSet:
                        wordSet.remove(new_word)  # Mark as visited
                        queue.append((new_word, steps + 1))
    
    return 0

# Code Walkthrough:
# 1. Use BFS to find shortest path
# 2. For each word, generate all possible one-letter changes
# 3. If new word exists in wordList, add to queue
# 4. Remove from wordSet to avoid cycles
# 5. Return steps when endWord is found

# Data Structure: Set + Queue
# Operations:
# - Set lookup/removal: O(1) average
# - Queue operations: O(1)
# - String manipulation: O(M) where M is word length
```

---

### Problem 3: Course Schedule (Cycle Detection)

**Problem Expectation**: Given numCourses and prerequisites, determine if you can finish all courses without cyclic dependencies.

```python
from collections import deque, defaultdict

def canFinish(numCourses, prerequisites):
    """
    Detect cycle in directed graph using Kahn's algorithm (Topological Sort)
    Time: O(V + E), Space: O(V + E)
    """
    # Build graph and indegree array
    graph = defaultdict(list)
    indegree = [0] * numCourses
    
    for course, prereq in prerequisites:
        graph[prereq].append(course)
        indegree[course] += 1
    
    # Queue for nodes with 0 indegree
    queue = deque([i for i in range(numCourses) if indegree[i] == 0])
    count = 0  # Count of processed nodes
    
    while queue:
        current = queue.popleft()
        count += 1
        
        for neighbor in graph[current]:
            indegree[neighbor] -= 1
            if indegree[neighbor] == 0:
                queue.append(neighbor)
    
    return count == numCourses  # If we processed all nodes, no cycle

# Code Walkthrough:
# 1. Build adjacency list and indegree array
# 2. Start with courses having no prerequisites (indegree = 0)
# 3. Process each course, reduce indegree of neighbors
# 4. If neighbor's indegree becomes 0, add to queue
# 5. If processed count equals total courses, no cycle exists

# Data Structure: Adjacency List + Indegree Array + Queue
# Operations:
# - Graph traversal: O(V + E)
# - Indegree updates: O(1)
# - Queue operations: O(1)
```

---

## 2. Intervals, Events & Scheduling

### Problem 4: Meeting Rooms II

**Problem Expectation**: Given meeting time intervals, find minimum number of conference rooms required.

```python
import heapq

def minMeetingRooms(intervals):
    """
    Use min-heap to track ending times of ongoing meetings
    Time: O(N log N), Space: O(N)
    """
    if not intervals:
        return 0
    
    # Sort by start time
    intervals.sort(key=lambda x: x[0])
    
    # Min-heap to track ending times
    heap = []
    heapq.heappush(heap, intervals[0][1])
    
    for i in range(1, len(intervals)):
        current_start, current_end = intervals[i]
        
        # If room is free when current meeting starts
        if heap[0] <= current_start:
            heapq.heappop(heap)  # Free the room
        
        # Assign room for current meeting
        heapq.heappush(heap, current_end)
    
    return len(heap)

# Code Walkthrough:
# 1. Sort intervals by start time
# 2. Use min-heap to track earliest ending time
# 3. If earliest ending <= current start, reuse room
# 4. Otherwise, need new room
# 5. Heap size represents rooms needed

# Data Structure: Min-Heap
# Operations:
# - Heap push: O(log N)
# - Heap pop: O(log N)
# - Heap peek: O(1)
```

---

### Problem 5: Insert Interval

**Problem Expectation**: Given non-overlapping intervals sorted by start time, insert a new interval and merge if necessary.

```python
def insert(intervals, newInterval):
    """
    Three phases: add before, merge overlapping, add after
    Time: O(N), Space: O(N) for result
    """
    result = []
    i = 0
    n = len(intervals)
    
    # Add all intervals ending before newInterval starts
    while i < n and intervals[i][1] < newInterval[0]:
        result.append(intervals[i])
        i += 1
    
    # Merge all overlapping intervals
    while i < n and intervals[i][0] <= newInterval[1]:
        newInterval[0] = min(newInterval[0], intervals[i][0])
        newInterval[1] = max(newInterval[1], intervals[i][1])
        i += 1
    
    result.append(newInterval)
    
    # Add remaining intervals
    while i < n:
        result.append(intervals[i])
        i += 1
    
    return result

# Code Walkthrough:
# 1. Add all intervals that end before new interval starts
# 2. Merge all intervals that overlap with new interval
# 3. Add the merged interval
# 4. Add remaining intervals

# Data Structure: Array
# Operations:
# - Array traversal: O(N)
# - Comparison operations: O(1)
```

---

## 3. Matrix Manipulation & Backtracking

### Problem 6: Word Search

**Problem Expectation**: Given 2D board and word, determine if word exists in the board by sequentially adjacent cells.

```python
def exist(board, word):
    """
    Backtracking/DFS to search for word in matrix
    Time: O(M*N*4^L) where L is word length
    Space: O(L) for recursion stack
    """
    rows, cols = len(board), len(board[0])
    
    def backtrack(i, j, index):
        # Base case: found all characters
        if index == len(word):
            return True
        
        # Check boundaries and character match
        if i < 0 or i >= rows or j < 0 or j >= cols or board[i][j] != word[index]:
            return False
        
        # Mark cell as visited
        temp = board[i][j]
        board[i][j] = '#'
        
        # Explore all 4 directions
        found = (backtrack(i+1, j, index+1) or
                backtrack(i-1, j, index+1) or
                backtrack(i, j+1, index+1) or
                backtrack(i, j-1, index+1))
        
        # Restore cell
        board[i][j] = temp
        return found
    
    for i in range(rows):
        for j in range(cols):
            if backtrack(i, j, 0):
                return True
    
    return False

# Code Walkthrough:
# 1. Start from each cell in board
# 2. Use DFS/backtracking to explore all paths
# 3. Mark visited cells temporarily
# 4. Restore cells after exploration
# 5. Return True if any path finds the word

# Data Structure: 2D Array + Recursion Stack
# Operations:
# - Cell access: O(1)
# - Backtracking: O(4^L) paths explored
```

---

## 4. Binary Trees & BST

### Problem 7: Lowest Common Ancestor in Binary Tree

**Problem Expectation**: Find the lowest common ancestor (LCA) of two nodes in a binary tree.

```python
class TreeNode:
    def __init__(self, x):
        self.val = x
        self.left = None
        self.right = None

def lowestCommonAncestor(root, p, q):
    """
    Recursive approach to find LCA
    Time: O(N), Space: O(H) for recursion stack
    """
    if not root or root == p or root == q:
        return root
    
    # Search left and right subtrees
    left = lowestCommonAncestor(root.left, p, q)
    right = lowestCommonAncestor(root.right, p, q)
    
    # If both subtrees return non-null, current root is LCA
    if left and right:
        return root
    
    # Otherwise return non-null child
    return left if left else right

# Code Walkthrough:
# 1. If root is None or matches p/q, return root
# 2. Recursively search left and right subtrees
# 3. If both left and right found nodes, current root is LCA
# 4. Otherwise return whichever subtree found the nodes

# Data Structure: Binary Tree + Recursion Stack
# Operations:
# - Tree traversal: O(N)
# - Node comparison: O(1)
```

---

### Problem 8: Validate Binary Search Tree

**Problem Expectation**: Check if binary tree is a valid BST where left subtree contains only nodes with values less than node's value, and right subtree contains only nodes with values greater than node's value.

```python
def isValidBST(root):
    """
    Use inorder traversal or min/max bounds
    Time: O(N), Space: O(H) for recursion stack
    """
    def validate(node, low=float('-inf'), high=float('inf')):
        if not node:
            return True
        
        # Check current node value within bounds
        if not (low < node.val < high):
            return False
        
        # Validate left and right subtrees with updated bounds
        return (validate(node.left, low, node.val) and 
                validate(node.right, node.val, high))
    
    return validate(root)

# Code Walkthrough:
# 1. Start with infinite bounds
# 2. For each node, check if value within (low, high)
# 3. For left child, update high bound to current node value
# 4. For right child, update low bound to current node value
# 5. Recursively validate subtrees

# Data Structure: Binary Tree + Recursion Stack
# Operations:
# - Tree traversal: O(N)
# - Bound checking: O(1) per node
```

---

## 5. Sliding Window & Two Pointer

### Problem 9: Longest Substring Without Repeating Characters

**Problem Expectation**: Find length of longest substring without repeating characters.

```python
def lengthOfLongestSubstring(s):
    """
    Sliding window with hash set to track characters in current window
    Time: O(N), Space: O(min(N, M)) where M is character set size
    """
    char_set = set()
    left = 0
    max_length = 0
    
    for right in range(len(s)):
        # Shrink window until no duplicate
        while s[right] in char_set:
            char_set.remove(s[left])
            left += 1
        
        # Add current character and update max length
        char_set.add(s[right])
        max_length = max(max_length, right - left + 1)
    
    return max_length

# Code Walkthrough:
# 1. Use two pointers (left, right) to represent window
# 2. Expand right pointer to include new character
# 3. If duplicate found, shrink left pointer until no duplicate
# 4. Track maximum window size

# Data Structure: HashSet + Two Pointers
# Operations:
# - Set operations: O(1)
# - Pointer movement: O(N) total
```

---

### Problem 10: Minimum Window Substring

**Problem Expectation**: Find minimum window in s that contains all characters of t.

```python
from collections import defaultdict

def minWindow(s, t):
    """
    Sliding window with frequency maps
    Time: O(M + N), Space: O(M) where M is length of t
    """
    if not s or not t:
        return ""
    
    # Frequency map for t
    t_freq = defaultdict(int)
    for char in t:
        t_freq[char] += 1
    
    required = len(t_freq)  # Number of unique characters to match
    formed = 0  # Number of unique characters formed in current window
    
    # Sliding window
    window_freq = defaultdict(int)
    left = 0
    min_len = float('inf')
    result = ""
    
    for right in range(len(s)):
        char = s[right]
        window_freq[char] += 1
        
        # Check if current character completes requirement
        if char in t_freq and window_freq[char] == t_freq[char]:
            formed += 1
        
        # Try to contract window from left
        while left <= right and formed == required:
            # Update result if smaller window found
            if right - left + 1 < min_len:
                min_len = right - left + 1
                result = s[left:right+1]
            
            # Remove left character from window
            left_char = s[left]
            window_freq[left_char] -= 1
            
            if left_char in t_freq and window_freq[left_char] < t_freq[left_char]:
                formed -= 1
            
            left += 1
    
    return result

# Code Walkthrough:
# 1. Build frequency map for target string t
# 2. Use sliding window to find minimum window containing all characters of t
# 3. Expand right pointer to include characters
# 4. When all characters are included, contract left pointer to find minimum
# 5. Track the minimum valid window

# Data Structure: Hash Maps + Two Pointers
# Operations:
# - Frequency counting: O(1)
# - Window expansion/contraction: O(N)
```

---

## 6. Hashing & Sets

### Problem 11: Top K Frequent Elements

**Problem Expectation**: Given integer array, return k most frequent elements.

```python
import heapq
from collections import defaultdict

def topKFrequent(nums, k):
    """
    Bucket sort or min-heap approach
    Time: O(N log K), Space: O(N)
    """
    # Count frequencies
    freq_map = defaultdict(int)
    for num in nums:
        freq_map[num] += 1
    
    # Min-heap of size k
    heap = []
    for num, freq in freq_map.items():
        heapq.heappush(heap, (freq, num))
        if len(heap) > k:
            heapq.heappop(heap)  # Remove least frequent
    
    # Extract results
    return [num for freq, num in heap]

# Alternative Bucket Sort approach (O(N) time):
def topKFrequentBucket(nums, k):
    freq_map = defaultdict(int)
    for num in nums:
        freq_map[num] += 1
    
    # Create buckets
    buckets = [[] for _ in range(len(nums) + 1)]
    for num, freq in freq_map.items():
        buckets[freq].append(num)
    
    # Collect top k
    result = []
    for i in range(len(buckets)-1, 0, -1):
        if buckets[i]:
            result.extend(buckets[i])
        if len(result) >= k:
            break
    
    return result[:k]

# Code Walkthrough:
# Heap approach:
# 1. Count frequencies of all elements
# 2. Use min-heap to keep track of top k elements
# 3. Push (frequency, element) pairs into heap
# 4. Maintain heap size k by popping smallest frequency

# Bucket approach:
# 1. Count frequencies
# 2. Create buckets where index represents frequency
# 3. Add elements to their frequency buckets
# 4. Collect from highest frequency buckets

# Data Structure: Hash Map + Heap/Buckets
# Operations:
# - Frequency counting: O(N)
# - Heap operations: O(log K) per element
# - Bucket sorting: O(N)
```

---

## 7. Dynamic Programming

### Problem 12: Coin Change

**Problem Expectation**: Find minimum number of coins needed to make amount, return -1 if not possible.

```python
def coinChange(coins, amount):
    """
    DP bottom-up approach
    Time: O(S * N) where S is amount, N is number of coins
    Space: O(S) for DP array
    """
    # Initialize DP array with infinity
    dp = [float('inf')] * (amount + 1)
    dp[0] = 0  # Base case: 0 coins needed for amount 0
    
    for coin in coins:
        for i in range(coin, amount + 1):
            dp[i] = min(dp[i], dp[i - coin] + 1)
    
    return dp[amount] if dp[amount] != float('inf') else -1

# Code Walkthrough:
# 1. dp[i] represents minimum coins needed for amount i
# 2. Initialize with infinity except dp[0] = 0
# 3. For each coin, update dp array from coin value to amount
# 4. dp[i] = min(current, 1 + dp[i - coin])
# 5. Return result or -1 if not possible

# Data Structure: 1D DP Array
# Operations:
# - Array initialization: O(S)
# - Nested loops: O(S * N)
```

---

### Problem 13: Longest Increasing Subsequence

**Problem Expectation**: Find length of longest strictly increasing subsequence.

```python
def lengthOfLIS(nums):
    """
    Patience sorting approach
    Time: O(N log N), Space: O(N)
    """
    if not nums:
        return 0
    
    # tails[i] stores smallest ending element of increasing subsequence of length i+1
    tails = []
    
    for num in nums:
        # Binary search to find position in tails
        left, right = 0, len(tails)
        while left < right:
            mid = (left + right) // 2
            if tails[mid] < num:
                left = mid + 1
            else:
                right = mid
        
        # If num is larger than all ending elements, append to tails
        if left == len(tails):
            tails.append(num)
        else:
            # Replace existing element
            tails[left] = num
    
    return len(tails)

# Code Walkthrough:
# 1. tails array maintains smallest possible ending elements
# 2. For each number, find its position in tails using binary search
# 3. If number is larger than all tails, extend the sequence
# 4. Otherwise, replace existing element to allow longer sequences
# 5. Length of tails is the LIS length

# Data Structure: Array + Binary Search
# Operations:
# - Binary search: O(log N) per element
# - Array manipulation: O(1) for replacements
```

---

## 8. Stack & Monotonic Patterns

### Problem 14: Daily Temperatures

**Problem Expectation**: For each day, find how many days you have to wait until warmer temperature.

```python
def dailyTemperatures(temperatures):
    """
    Monotonic decreasing stack
    Time: O(N), Space: O(N)
    """
    n = len(temperatures)
    result = [0] * n
    stack = []  # Store indices
    
    for i in range(n):
        # While current temp is higher than stack top
        while stack and temperatures[i] > temperatures[stack[-1]]:
            prev_index = stack.pop()
            result[prev_index] = i - prev_index
        
        stack.append(i)
    
    return result

# Code Walkthrough:
# 1. Use stack to store indices of temperatures
# 2. Maintain monotonic decreasing order
# 3. When warmer temperature found, calculate days difference
# 4. Update result for all cooler temperatures in stack
# 5. Push current temperature index to stack

# Data Structure: Stack
# Operations:
# - Stack push/pop: O(1)
# - Each element pushed and popped once: O(N) total
```

---

### Problem 15: Largest Rectangle in Histogram

**Problem Expectation**: Find largest rectangular area in histogram.

```python
def largestRectangleArea(heights):
    """
    Monotonic increasing stack
    Time: O(N), Space: O(N)
    """
    stack = []  # Store (index, height)
    max_area = 0
    n = len(heights)
    
    for i in range(n):
        start = i
        # Pop from stack while current height is smaller
        while stack and stack[-1][1] > heights[i]:
            index, height = stack.pop()
            max_area = max(max_area, height * (i - index))
            start = index  # Extend current bar to the left
        stack.append((start, heights[i]))
    
    # Process remaining bars in stack
    for i, h in stack:
        max_area = max(max_area, h * (n - i))
    
    return max_area

# Code Walkthrough:
# 1. Use stack to store (starting index, height)
# 2. Maintain monotonic increasing heights
# 3. When smaller height found, calculate areas for taller bars
# 4. Update starting index for current bar
# 5. Process remaining bars at the end

# Data Structure: Stack
# Operations:
# - Stack operations: O(1)
# - Each bar pushed and popped once: O(N) total
```

## Key Data Structure Operations Summary

| Data Structure | Key Operations | Time Complexity | Common Use Cases |
|---------------|----------------|-----------------|------------------|
| **Array/List** | Access, Insert, Delete | O(1), O(n), O(n) | Sequential data storage |
| **Hash Set/Map** | Insert, Search, Delete | O(1) avg | Frequency counting, lookups |
| **Stack** | Push, Pop, Peek | O(1) | DFS, monotonic problems |
| **Queue** | Enqueue, Dequeue | O(1) | BFS, sliding window |
| **Heap** | Push, Pop, Peek | O(log n), O(1) | Priority queues, top K |
| **Binary Tree** | Traversal, Search | O(h) | Hierarchical data |
| **Graph** | BFS, DFS | O(V+E) | Networks, relationships |
