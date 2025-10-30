# FAANG DSA Interview Guide - Additional Problems

## 1. Employee Free Time (Amazon, Atlassian)

### Problem Explanation
**Problem Statement**: We are given a list of employees' working time intervals. Each employee has a list of intervals representing their working hours. We need to find all time periods when ALL employees are free. The output should be a list of common free time intervals for all employees.

**Key Points**:
- Input is a list of lists - each inner list contains intervals for one employee
- We need to find when NO employee is working
- The result should be merged free intervals
- Intervals are given in sorted order per employee
- We need to consider the union of all working hours first

### Solution for Freshers
```python
"""
Think of this like finding gaps between meetings across all people
If we flatten all intervals and sort them, we can find gaps where no one is working
"""

import heapq

def employeeFreeTime(schedule):
    """
    Time: O(N log N) where N is total intervals, Space: O(N)
    """
    # Step 1: Collect all intervals and sort by start time
    all_intervals = []
    for employee in schedule:
        for interval in employee:
            all_intervals.append(interval)
    
    all_intervals.sort(key=lambda x: x.start)
    
    # Step 2: Merge all working intervals
    merged = []
    current = all_intervals[0]
    
    for interval in all_intervals[1:]:
        if current.end >= interval.start:
            # Overlapping intervals, merge them
            current.end = max(current.end, interval.end)
        else:
            # No overlap, add current to merged
            merged.append(current)
            current = interval
    merged.append(current)
    
    # Step 3: Find gaps between merged intervals (free time)
    free_time = []
    for i in range(1, len(merged)):
        # Gap between end of previous and start of current
        if merged[i-1].end < merged[i].start:
            free_time.append(Interval(merged[i-1].end, merged[i].start))
    
    return free_time

# Alternative approach using heap (more efficient)
def employeeFreeTimeHeap(schedule):
    """
    Using min-heap to process intervals efficiently
    Time: O(N log K) where K is number of employees
    """
    result = []
    heap = []
    
    # Push first interval of each employee to heap
    for i, employee in enumerate(schedule):
        if employee:
            heapq.heappush(heap, (employee[0].start, i, 0))
    
    prev_end = float('inf')
    # Find the earliest start time among all employees
    if heap:
        prev_end = min(interval.start for employee in schedule for interval in employee)
    
    while heap:
        start, emp_idx, idx = heapq.heappop(heap)
        
        # If there's a gap between previous end and current start
        if prev_end < start:
            result.append(Interval(prev_end, start))
        
        # Update previous end
        prev_end = max(prev_end, schedule[emp_idx][idx].end)
        
        # Push next interval from same employee
        if idx + 1 < len(schedule[emp_idx]):
            next_interval = schedule[emp_idx][idx + 1]
            heapq.heappush(heap, (next_interval.start, emp_idx, idx + 1))
    
    return result
```

**Simple Explanation**:
1. Imagine all employees' working hours as colored blocks on a timeline
2. We first combine overlapping working hours across all employees
3. Then we look for gaps between these combined working blocks
4. These gaps represent times when NOBODY is working
5. Return these gap periods as the common free time

---

## 2. Minimum Number of Arrows to Burst Balloons

### Problem Explanation
**Problem Statement**: There are balloons spread across a 2D plane. Each balloon is represented as an interval [xstart, xend]. Arrows are shot vertically upward. If an arrow is shot at position x, it bursts all balloons where xstart ≤ x ≤ xend. Find the minimum number of arrows needed to burst all balloons.

**Key Points**:
- Balloons are horizontal intervals
- Arrows are shot vertically at specific x positions
- One arrow can burst multiple balloons if their intervals overlap
- We need to find minimum arrows to cover all intervals
- This is essentially finding non-overlapping groups of balloons

### Solution for Freshers
```python
def findMinArrowShots(points):
    """
    Greedy approach - sort by end points and shoot arrows at end points
    Time: O(N log N), Space: O(1)
    """
    if not points:
        return 0
    
    # Sort balloons by their end position
    points.sort(key=lambda x: x[1])
    
    arrows = 1
    first_end = points[0][1]
    
    for i in range(1, len(points)):
        current_start, current_end = points[i]
        
        # If current balloon starts after previous arrow position
        # We need a new arrow
        if current_start > first_end:
            arrows += 1
            first_end = current_end
    
    return arrows

# More intuitive approach
def findMinArrowShotsSimple(points):
    """
    Think of this as grouping overlapping intervals
    Each group can be burst with one arrow
    """
    if not points:
        return 0
    
    # Sort by start position
    points.sort(key=lambda x: x[0])
    
    arrows = 0
    current_end = float('-inf')
    
    for balloon in points:
        start, end = balloon
        
        # If this balloon doesn't overlap with current group
        if start > current_end:
            # Need new arrow for new group
            arrows += 1
            current_end = end
        else:
            # Overlaps, update current group's end to minimum
            current_end = min(current_end, end)
    
    return arrows
```

**Simple Explanation**:
1. Imagine balloons as horizontal lines on the x-axis
2. Sort balloons by their ending positions
3. Shoot first arrow at the end of first balloon
4. This arrow will burst all balloons that start before this position
5. For remaining balloons, repeat the process
6. Each arrow represents one group of overlapping balloons

---

## 3. Sudoku Solver

### Problem Explanation
**Problem Statement**: Given a 9×9 partially filled Sudoku board, write a program to solve the Sudoku puzzle. The rules are: each row must contain 1-9 without repetition, each column must contain 1-9 without repetition, and each 3×3 sub-box must contain 1-9 without repetition.

**Key Points**:
- Board is 9×9 grid with some cells filled (1-9) and some empty ('.')
- Need to fill empty cells following Sudoku rules
- Use backtracking to try all possibilities
- Must validate each placement immediately

### Solution for Freshers
```python
def solveSudoku(board):
    """
    Backtracking solution - try numbers 1-9 in empty cells
    Time: O(9^(N*N)) but much faster in practice, Space: O(N*N)
    """
    def is_valid(board, row, col, num):
        # Check row
        for i in range(9):
            if board[row][i] == num:
                return False
        
        # Check column
        for i in range(9):
            if board[i][col] == num:
                return False
        
        # Check 3x3 box
        box_row = (row // 3) * 3
        box_col = (col // 3) * 3
        for i in range(3):
            for j in range(3):
                if board[box_row + i][box_col + j] == num:
                    return False
        
        return True
    
    def solve():
        for i in range(9):
            for j in range(9):
                if board[i][j] == '.':
                    # Try numbers 1-9
                    for num in '123456789':
                        if is_valid(board, i, j, num):
                            board[i][j] = num
                            
                            # Recursively solve rest of board
                            if solve():
                                return True
                            
                            # Backtrack if solution not found
                            board[i][j] = '.'
                    
                    # No valid number found
                    return False
        return True  # All cells filled
    
    solve()
```

**Simple Explanation**:
1. Start from top-left cell, move left to right, top to bottom
2. When you find empty cell ('.'), try numbers 1-9
3. For each number, check if it's valid in current row, column and 3×3 box
4. If valid, place the number and move to next cell
5. If no number works, go back (backtrack) and change previous number
6. Continue until all cells are filled correctly

---

## 4. N-Queens

### Problem Explanation
**Problem Statement**: Place N queens on an N×N chessboard so that no two queens attack each other. Queens can attack horizontally, vertically, and diagonally. Return all distinct solutions.

**Key Points**:
- Each solution has exactly one queen per row and per column
- Queens cannot share same row, column, or diagonal
- Need to find ALL possible arrangements
- Classic backtracking problem

### Solution for Freshers
```python
def solveNQueens(n):
    """
    Backtracking with sets to track attacked positions
    Time: O(N!), Space: O(N)
    """
    def backtrack(row, cols, diag1, diag2):
        if row == n:
            # Found valid solution
            solutions.append(["".join(row) for row in board])
            return
        
        for col in range(n):
            # Check if current position is attacked
            d1 = row - col  # Diagonal 1 (top-left to bottom-right)
            d2 = row + col  # Diagonal 2 (top-right to bottom-left)
            
            if col not in cols and d1 not in diag1 and d2 not in diag2:
                # Place queen
                board[row][col] = 'Q'
                cols.add(col)
                diag1.add(d1)
                diag2.add(d2)
                
                # Move to next row
                backtrack(row + 1, cols, diag1, diag2)
                
                # Remove queen (backtrack)
                board[row][col] = '.'
                cols.remove(col)
                diag1.remove(d1)
                diag2.remove(d2)
    
    solutions = []
    board = [['.'] * n for _ in range(n)]
    backtrack(0, set(), set(), set())
    return solutions
```

**Simple Explanation**:
1. Start placing queens row by row (each row gets exactly one queen)
2. For each row, try placing queen in each column
3. Before placing, check if the column and both diagonals are not attacked
4. If safe, place queen and move to next row
5. If we reach last row successfully, we found a solution
6. If stuck, backtrack and try different position in previous row

---

## 5. Unique Paths II

### Problem Explanation
**Problem Statement**: A robot is at top-left corner of m×n grid. It can only move right or down. Some cells have obstacles (1) and cannot be passed. Find number of unique paths from top-left to bottom-right.

**Key Points**:
- Grid with obstacles (1 = obstacle, 0 = empty cell)
- Robot can only move right or down
- Need to count all possible paths avoiding obstacles
- Dynamic programming problem

### Solution for Freshers
```python
def uniquePathsWithObstacles(obstacleGrid):
    """
    DP where dp[i][j] = paths to reach (i,j)
    Time: O(M*N), Space: O(M*N) can be optimized to O(N)
    """
    if not obstacleGrid or obstacleGrid[0][0] == 1:
        return 0
    
    m, n = len(obstacleGrid), len(obstacleGrid[0])
    dp = [[0] * n for _ in range(m)]
    
    # Starting point
    dp[0][0] = 1
    
    # Fill first row (can only come from left)
    for j in range(1, n):
        if obstacleGrid[0][j] == 0:
            dp[0][j] = dp[0][j-1]
    
    # Fill first column (can only come from top)
    for i in range(1, m):
        if obstacleGrid[i][0] == 0:
            dp[i][0] = dp[i-1][0]
    
    # Fill rest of the grid
    for i in range(1, m):
        for j in range(1, n):
            if obstacleGrid[i][j] == 0:
                dp[i][j] = dp[i-1][j] + dp[i][j-1]
    
    return dp[m-1][n-1]

# Space optimized version
def uniquePathsWithObstaclesOptimized(obstacleGrid):
    """
    Space: O(N) using single array
    """
    if not obstacleGrid or obstacleGrid[0][0] == 1:
        return 0
    
    n = len(obstacleGrid[0])
    dp = [0] * n
    dp[0] = 1
    
    for row in obstacleGrid:
        for j in range(n):
            if row[j] == 1:
                dp[j] = 0
            elif j > 0:
                dp[j] += dp[j-1]
    
    return dp[n-1]
```

**Simple Explanation**:
1. Create a DP table where each cell stores paths to reach that cell
2. If starting cell has obstacle, answer is 0
3. First row cells can only be reached from left (if no obstacle)
4. First column cells can only be reached from top (if no obstacle)
5. Other cells: paths = paths from top + paths from left
6. If cell has obstacle, paths = 0

---

## 6. Kth Smallest Element in BST

### Problem Explanation
**Problem Statement**: Given a binary search tree, find the kth smallest element. BST property: left subtree has smaller values, right subtree has larger values.

**Key Points**:
- BST is sorted in inorder traversal
- Kth smallest = kth element in inorder traversal
- Can be solved with iterative DFS or recursion
- Optimize by stopping early when kth element found

### Solution for Freshers
```python
def kthSmallest(root, k):
    """
    Inorder traversal until kth element
    Time: O(H + k) where H is tree height, Space: O(H)
    """
    stack = []
    current = root
    
    while current or stack:
        # Go to leftmost node
        while current:
            stack.append(current)
            current = current.left
        
        # Process node
        current = stack.pop()
        k -= 1
        
        # If k becomes 0, we found kth smallest
        if k == 0:
            return current.val
        
        # Move to right subtree
        current = current.right
    
    return -1  # k is larger than tree size

# Recursive approach
def kthSmallestRecursive(root, k):
    """
    Recursive inorder with early stopping
    """
    result = [0]  # Store kth value
    count = [0]   # Store current count
    
    def inorder(node):
        if not node:
            return
        
        inorder(node.left)
        
        count[0] += 1
        if count[0] == k:
            result[0] = node.val
            return
        
        if count[0] < k:  # Only traverse right if we haven't found kth
            inorder(node.right)
    
    inorder(root)
    return result[0]
```

**Simple Explanation**:
1. In BST, inorder traversal gives elements in sorted order
2. Use stack to simulate inorder traversal
3. Keep going left until you can't, then process node
4. After processing each node, decrement k
5. When k becomes 0, current node is the kth smallest
6. If we reach end without finding, k is too large

---

## 7. Longest Continuous Subarray with Absolute Diff ≤ Limit

### Problem Explanation
**Problem Statement**: Given an array of integers, find the longest non-empty subarray where the absolute difference between any two elements is at most the given limit.

**Key Points**:
- Need to track min and max in current window
- Use two deques to track min and max efficiently
- Sliding window approach
- Expand right pointer, shrink left when diff > limit

### Solution for Freshers
```python
from collections import deque

def longestSubarray(nums, limit):
    """
    Use two deques to track min and max in current window
    Time: O(N), Space: O(N)
    """
    min_deque = deque()  # Increasing deque (min at front)
    max_deque = deque()  # Decreasing deque (max at front)
    left = 0
    max_length = 0
    
    for right in range(len(nums)):
        # Maintain min_deque (increasing order)
        while min_deque and nums[min_deque[-1]] > nums[right]:
            min_deque.pop()
        min_deque.append(right)
        
        # Maintain max_deque (decreasing order)
        while max_deque and nums[max_deque[-1]] < nums[right]:
            max_deque.pop()
        max_deque.append(right)
        
        # Check if current window violates limit
        while nums[max_deque[0]] - nums[min_deque[0]] > limit:
            # Shrink window from left
            if min_deque[0] == left:
                min_deque.popleft()
            if max_deque[0] == left:
                max_deque.popleft()
            left += 1
        
        # Update max length
        max_length = max(max_length, right - left + 1)
    
    return max_length
```

**Simple Explanation**:
1. Use two special queues: one tracks minimum, one tracks maximum
2. min_deque keeps elements in increasing order (smallest at front)
3. max_deque keeps elements in decreasing order (largest at front)
4. Expand window by moving right pointer
5. When max-min > limit, shrink window from left
6. Track the longest valid window

---

## 8. Longest Increasing Subsequence

### Problem Explanation
**Problem Statement**: Given an integer array, find the length of longest strictly increasing subsequence (elements in order, but not necessarily consecutive).

**Key Points**:
- Subsequence means we can skip elements
- Need longest increasing sequence
- Can be solved with DP (O(n²)) or binary search (O(n log n))
- Binary search approach is more efficient

### Solution for Freshers
```python
def lengthOfLIS(nums):
    """
    Patience sorting with binary search
    Time: O(N log N), Space: O(N)
    """
    if not nums:
        return 0
    
    # tails[i] = smallest ending element of increasing subsequence of length i+1
    tails = []
    
    for num in nums:
        # Binary search to find where num should be placed
        left, right = 0, len(tails)
        while left < right:
            mid = (left + right) // 2
            if tails[mid] < num:
                left = mid + 1
            else:
                right = mid
        
        # If num is larger than all ending elements, extend sequence
        if left == len(tails):
            tails.append(num)
        else:
            # Replace existing element to allow longer sequences
            tails[left] = num
    
    return len(tails)

# DP approach (easier to understand but slower)
def lengthOfLIS_DP(nums):
    """
    DP where dp[i] = LIS ending at i
    Time: O(N²), Space: O(N)
    """
    if not nums:
        return 0
    
    n = len(nums)
    dp = [1] * n  # Each element is LIS of length 1
    
    for i in range(n):
        for j in range(i):
            if nums[j] < nums[i]:
                dp[i] = max(dp[i], dp[j] + 1)
    
    return max(dp)
```

**Simple Explanation**:
1. Maintain an array `tails` where tails[i] = smallest ending of sequence length i+1
2. For each number, find its position in tails using binary search
3. If number > all tails elements, append it (sequence gets longer)
4. Otherwise, replace existing element (helps form longer sequences later)
5. Length of tails is the LIS length

---

## 9. Largest Rectangle in Histogram

### Problem Explanation
**Problem Statement**: Given an array of integers representing histogram's bar heights, find the largest rectangle area. Each bar has width 1.

**Key Points**:
- Need to find maximum area rectangle under histogram
- Width = distance between smaller bars on left and right
- Use monotonic increasing stack
- Track indices of bars in stack

### Solution for Freshers
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
            # Calculate area with popped height
            max_area = max(max_area, height * (i - index))
            start = index  # Extend current bar to the left
        stack.append((start, heights[i]))
    
    # Process remaining bars in stack
    for i, h in stack:
        max_area = max(max_area, h * (n - i))
    
    return max_area

# Alternative cleaner approach
def largestRectangleAreaClean(heights):
    """
    Using stack with sentinel values
    """
    stack = [-1]  # Initialize with sentinel
    max_area = 0
    heights.append(0)  # Add sentinel at end
    
    for i in range(len(heights)):
        while stack[-1] != -1 and heights[stack[-1]] > heights[i]:
            height = heights[stack.pop()]
            width = i - stack[-1] - 1
            max_area = max(max_area, height * width)
        stack.append(i)
    
    heights.pop()  # Remove sentinel
    return max_area
```

**Simple Explanation**:
1. Use stack to track bars in increasing height order
2. When we find a smaller bar, calculate areas for taller bars in stack
3. Area = height × width, where width extends until smaller bar
4. Keep track of starting index for each bar
5. Process remaining bars at the end
6. Return maximum area found

## Key Takeaways for Freshers

1. **Backtracking Problems** (Sudoku, N-Queens):
   - Try possibilities systematically
   - Validate immediately
   - Backtrack when stuck

2. **Sliding Window Problems**:
   - Maintain window invariants
   - Use appropriate data structures (deques, heaps)
   - Expand right, contract left

3. **DP Problems**:
   - Define state clearly
   - Find recurrence relation
   - Build solution bottom-up

4. **Tree Problems**:
   - Leverage tree properties (BST sortedness)
   - Use appropriate traversals
   - Consider iterative and recursive approaches
