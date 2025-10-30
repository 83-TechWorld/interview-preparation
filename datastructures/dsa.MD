I'll create a comprehensive guide for FAANG DSA interviews. Let me structure this as a GitHub markdown document:

```markdown
# FAANG DSA Interview Preparation Guide

## Essential Data Structures & Their Complexities

### 1. Arrays & Strings
**Time Complexities:**
- Access: O(1)
- Search: O(n)
- Insertion: O(n)
- Deletion: O(n)

**Key Concepts:**
- Two-pointer technique
- Sliding window
- Prefix sums

### 2. Linked Lists
**Time Complexities:**
- Access: O(n)
- Search: O(n)
- Insertion: O(1) at head
- Deletion: O(1) at head

### 3. Stacks & Queues
**Time Complexities:**
- Push/Enqueue: O(1)
- Pop/Dequeue: O(1)
- Search: O(n)

### 4. Hash Tables
**Time Complexities:**
- Insert: O(1) average
- Delete: O(1) average  
- Search: O(1) average

### 5. Trees & BST
**Time Complexities:**
- Access: O(log n) balanced
- Search: O(log n) balanced
- Insert: O(log n) balanced
- Delete: O(log n) balanced

### 6. Graphs
**Representations:**
- Adjacency List: O(V+E) space
- Adjacency Matrix: O(V²) space

---

## Commonly Asked FAANG Problems

## Problem 1: Two Sum

### Problem Statement
Given an array of integers `nums` and an integer `target`, return indices of the two numbers that add up to target.

### Solution
```python
def twoSum(nums, target):
    """
    Time Complexity: O(n)
    Space Complexity: O(n)
    """
    num_map = {}
    
    for i, num in enumerate(nums):
        complement = target - num
        
        if complement in num_map:
            return [num_map[complement], i]
        
        num_map[num] = i
    
    return []

# Example
nums = [2, 7, 11, 15]
target = 9
print(twoSum(nums, target))  # Output: [0, 1]
```

### Why Hash Table?
- **Fast Lookups**: O(1) average time for complement check
- **Trade-off**: Uses O(n) extra space for faster time
- **Alternative**: Brute force would be O(n²)

---

## Problem 2: Valid Parentheses

### Problem Statement
Given a string containing just characters '(', ')', '{', '}', '[' and ']', determine if input is valid.

### Solution
```python
def isValid(s):
    """
    Time Complexity: O(n)
    Space Complexity: O(n)
    """
    stack = []
    mapping = {')': '(', '}': '{', ']': '['}
    
    for char in s:
        if char in mapping.values():  # Opening bracket
            stack.append(char)
        elif char in mapping:  # Closing bracket
            if not stack or stack.pop() != mapping[char]:
                return False
    
    return not stack

# Example
print(isValid("()[]{}"))  # Output: True
print(isValid("([)]"))    # Output: False
```

### Why Stack?
- **LIFO Property**: Last opened bracket must be first closed
- **Natural Fit**: Perfect for nested structures
- **Efficiency**: O(1) push/pop operations

---

## Problem 3: Binary Tree Level Order Traversal

### Problem Statement
Given a binary tree, return the level order traversal of its nodes' values.

### Solution
```python
from collections import deque

class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

def levelOrder(root):
    """
    Time Complexity: O(n)
    Space Complexity: O(n)
    """
    if not root:
        return []
    
    result = []
    queue = deque([root])
    
    while queue:
        level_size = len(queue)
        current_level = []
        
        for _ in range(level_size):
            node = queue.popleft()
            current_level.append(node.val)
            
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
        
        result.append(current_level)
    
    return result

# Example
#     3
#    / \
#   9  20
#     /  \
#    15   7
root = TreeNode(3)
root.left = TreeNode(9)
root.right = TreeNode(20)
root.right.left = TreeNode(15)
root.right.right = TreeNode(7)

print(levelOrder(root))  # Output: [[3], [9, 20], [15, 7]]
```

### Why Queue (BFS)?
- **Level Processing**: Process nodes level by level
- **FIFO**: First nodes encountered are first processed
- **Space**: O(w) where w is maximum width

---

## Problem 4: LRU Cache

### Problem Statement
Design Least Recently Used (LRU) cache with O(1) time for get and put operations.

### Solution
```python
class ListNode:
    def __init__(self, key=0, val=0):
        self.key = key
        self.val = val
        self.prev = None
        self.next = None

class LRUCache:
    """
    Time Complexity: O(1) for both get and put
    Space Complexity: O(capacity)
    """
    
    def __init__(self, capacity: int):
        self.capacity = capacity
        self.cache = {}
        self.head = ListNode()
        self.tail = ListNode()
        self.head.next = self.tail
        self.tail.prev = self.head
    
    def _add_node(self, node):
        """Add node right after head"""
        node.prev = self.head
        node.next = self.head.next
        self.head.next.prev = node
        self.head.next = node
    
    def _remove_node(self, node):
        """Remove an existing node from linked list"""
        prev_node = node.prev
        next_node = node.next
        prev_node.next = next_node
        next_node.prev = prev_node
    
    def _move_to_head(self, node):
        """Move certain node to head"""
        self._remove_node(node)
        self._add_node(node)
    
    def _pop_tail(self):
        """Pop the current tail"""
        tail = self.tail.prev
        self._remove_node(tail)
        return tail

    def get(self, key: int) -> int:
        node = self.cache.get(key)
        if not node:
            return -1
        
        self._move_to_head(node)
        return node.val

    def put(self, key: int, value: int) -> None:
        node = self.cache.get(key)
        
        if not node:
            new_node = ListNode(key, value)
            self.cache[key] = new_node
            self._add_node(new_node)
            
            if len(self.cache) > self.capacity:
                tail = self._pop_tail()
                del self.cache[tail.key]
        else:
            node.val = value
            self._move_to_head(node)

# Example
cache = LRUCache(2)
cache.put(1, 1)
cache.put(2, 2)
print(cache.get(1))    # Returns 1
cache.put(3, 3)        # Evicts key 2
print(cache.get(2))    # Returns -1
```

### Why Doubly Linked List + Hash Map?
- **Fast Access**: Hash map provides O(1) access
- **Order Maintenance**: DLL maintains usage order
- **Efficient Eviction**: O(1) removal of LRU element

---

## Problem 5: Merge Intervals

### Problem Statement
Given array of intervals, merge all overlapping intervals.

### Solution
```python
def merge(intervals):
    """
    Time Complexity: O(n log n) due to sorting
    Space Complexity: O(n) for result
    """
    if not intervals:
        return []
    
    # Sort by start time - O(n log n)
    intervals.sort(key=lambda x: x[0])
    
    merged = []
    current_interval = intervals[0]
    
    for i in range(1, len(intervals)):
        next_interval = intervals[i]
        
        # Check for overlap
        if current_interval[1] >= next_interval[0]:
            # Merge intervals
            current_interval[1] = max(current_interval[1], next_interval[1])
        else:
            # No overlap, add current to result
            merged.append(current_interval)
            current_interval = next_interval
    
    merged.append(current_interval)
    return merged

# Example
intervals = [[1,3],[2,6],[8,10],[15,18]]
print(merge(intervals))  # Output: [[1,6],[8,10],[15,18]]
```

### Why Sorting?
- **Problem Reduction**: Makes overlapping intervals adjacent
- **Time Trade-off**: O(n log n) better than O(n²) brute force
- **Greedy Approach**: Process intervals in sorted order

---

## Problem 6: Word Break

### Problem Statement
Given string s and dictionary of words, determine if s can be segmented into space-separated dictionary words.

### Solution
```python
def wordBreak(s, wordDict):
    """
    Time Complexity: O(n³) - n^2 states * n for substring
    Space Complexity: O(n)
    """
    word_set = set(wordDict)
    n = len(s)
    dp = [False] * (n + 1)
    dp[0] = True  # Empty string
    
    for i in range(1, n + 1):
        for j in range(i):
            if dp[j] and s[j:i] in word_set:
                dp[i] = True
                break
    
    return dp[n]

# Example
s = "leetcode"
wordDict = ["leet", "code"]
print(wordBreak(s, wordDict))  # Output: True
```

### Why Dynamic Programming?
- **Overlapping Subproblems**: Same substrings checked multiple times
- **Optimal Substructure**: Solution built from smaller solutions
- **Memoization**: Store results to avoid recomputation

---

## Big O Analysis Guidelines

### Time Complexity Rules:
1. **Drop Constants**: O(2n) → O(n)
2. **Drop Non-Dominant Terms**: O(n² + n) → O(n²)
3. **Different Inputs**: O(a + b) not O(n)
4. **Logarithmic**: O(log n) for divide and conquer

### Space Complexity:
- **Auxiliary Space**: Extra space used by algorithm
- **Total Space**: Input space + auxiliary space

### Common Complexities:
- **O(1)**: Constant - Hash table operations
- **O(log n)**: Logarithmic - Binary search
- **O(n)**: Linear - Single loop
- **O(n log n)**: Linearithmic - Efficient sorting
- **O(n²)**: Quadratic - Nested loops
- **O(2ⁿ)**: Exponential - Recursion without memoization

---

## FAANG Interview Patterns

### 1. Sliding Window
**Use Cases**: Subarrays, substrings with constraints
**Examples**: Maximum sum subarray of size k, Longest substring with K distinct characters

### 2. Two Pointers
**Use Cases**: Sorted arrays, palindrome checks
**Examples**: Two sum, Trapping rain water

### 3. Fast & Slow Pointers
**Use Cases**: Linked list cycles, middle element
**Examples**: Detect cycle, Find middle

### 4. BFS & DFS
**Use Cases**: Tree/Graph traversal, level order
**Examples**: Level order traversal, Number of islands

### 5. Backtracking
**Use Cases**: Permutations, combinations, constraint satisfaction
**Examples**: N-queens, Sudoku solver

### 6. Dynamic Programming
**Use Cases**: Optimization problems, counting problems
**Examples**: Fibonacci, Knapsack, Longest common subsequence

---

## Preparation Strategy

### 1. Master Fundamentals
- Practice each data structure implementation
- Understand trade-offs between different structures
- Memorize time/space complexities

### 2. Pattern Recognition
- Identify problem patterns quickly
- Map problems to known solutions
- Practice category-wise problems

### 3. Communication
- Think aloud during interviews
- Explain your approach before coding
- Discuss time/space complexity

### 4. Testing
- Always test with edge cases
- Consider empty inputs, single elements, large inputs
- Verify your solution manually

---

## Recommended Practice Order

1. **Arrays & Strings** (30-40 problems)
2. **Linked Lists** (15-20 problems)  
3. **Trees & BST** (25-30 problems)
4. **Graphs** (20-25 problems)
5. **Dynamic Programming** (25-30 problems)
6. **System Design** (10-15 problems)

Remember: Consistency and understanding concepts deeply is more important than solving many problems superficially!
```