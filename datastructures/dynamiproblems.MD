Here’s a full-fledged guide to **Dynamic Programming (DP)** + the 10 classic problems you listed — explanation, core concepts, uses, and *Java* solutions with comments, examples, and visual representations. I’ll write this in a form you could drop into a GitHub README (Markdown) as well.

---

## What is Dynamic Programming?

### Explanation

In simple terms:

* You have a problem that you want to solve.
* You recognise that the problem can be broken down into **smaller sub-problems**.
* Moreover, many of these sub-problems are **overlapping**: you’ll compute the same sub-problem multiple times if you do it naively (e.g., via recursion).
* If you compute each sub-problem **once**, store (“memoize” or tabulate) its result, and reuse it when needed, you save a lot of repeated work.
* That’s the heart of dynamic programming: **break into sub-problems + store results + build up to the full solution**. ([GeeksforGeeks][1])
* Also, DP works well when the problem has **optimal substructure**: the optimal solution for the full problem can be built from optimal solutions of sub-problems. ([Wikipedia][2])
* And **overlapping subproblems**: the same sub-problem is solved many times in naive recursion. ([Wikipedia][2])

### Core Concepts

Here are the key ideas you must internalise:

1. **Optimal substructure**

   * If you have a problem (P), and you can express the solution of (P) in terms of solutions to smaller problems (P_1, P_2, …), then you have optimal substructure.
   * Example: The shortest path from A→D might go via B, so the optimal path from A→D is composed of optimal path A→B plus optimal path B→D.

2. **Overlapping subproblems**

   * In naive recursion you might compute the same thing many times. E.g., Fibonacci: fib(n) = fib(n-1) + fib(n-2). Without memo you compute fib(n-2) multiple times.
   * With DP you compute fib(n-2) just once and reuse it.

3. **Memoization vs Tabulation**

   * *Top-down* (memoization): You write a recursive function, you store results in a cache when you first compute them, then next time you just fetch from cache.
   * *Bottom-up* (tabulation): You build up from smallest sub-problems (base cases) to larger ones, filling a table/array, so you don’t even call recursion.
   * Both achieve the same idea: avoid redundant work.

4. **State definition**

   * A crucial step is to define the “state” of your DP: what parameters define a sub-problem?
   * Example: For “climbing stairs” you might define `dp[i] = number of ways to reach step i`.

5. **Transition/recurrence**

   * Once you define the state, you need the relation: how does state i relate to previous states?
   * e.g., `dp[i] = dp[i-1] + dp[i-2]` for climbing stairs (if you can climb 1 or 2 steps).

6. **Base cases**

   * The smallest trivial sub-problems whose results you know without further breakdown.
   * Example: `dp[0] = 1` (one way to stay at zero steps) or `dp[1] = 1` etc.

7. **Choice of implementation**

   * Choose whether memo or tabulation (or both).
   * Choose whether to optimise space (often you can reduce from O(n) to O(1) if only a few previous states matter).

### Why use DP / Real-Use Cases

**Why**: Because without DP many problems that have overlapping subproblems end up having *exponential* time complexity. With DP we often reduce them to *polynomial* time (often O(n), O(n^2), etc). ([GeeksforGeeks][1])
**Real-Use Cases**:

* In algorithmic coding interviews (lots of standard DP problems).
* In pathfinding, optimization tasks (e.g., shortest paths, knapsack, resource allocation).
* In machine learning / bioinformatics: sequence alignment in bioinformatics uses DP. ([Wikipedia][2])
* Finance / economics: dynamic programming is used in modelling multi-period decisions (Bellman equation) ([Wikipedia][2])
* Game theory, scheduling, resource allocation, network flows, etc.

### Visual representation

![Image](https://afteracademy.com/images/optimal-substructure-and-overlapping-subproblems-image-1-85e303e566cc1a1b.png)

![Image](https://media.geeksforgeeks.org/wp-content/cdn-uploads/Tabulation-vs-Memoization-1.png)

![Image](https://www.hello-algo.com/en/chapter_dynamic_programming/dp_problem_features.assets/min_cost_cs_dp.png)

* The first image: shows how a naive recursion branches and re-computes same sub-problems many times.
* The second: shows memoization (cache) and tabulation (table) side by side.
* The third: shows a generic state transition diagram (like dp[i] depends on dp[i-1], dp[i-2], etc).

---

## 10 Classic DP Problems: Statement, Use Case, Example + Java Solution (with comments)

Below are the ten problems you asked for. For each I’ll provide: the problem statement (in plain language), an example, intuitive explanation, then a clean Java solution with line-by-line comments.

---

### 1. Climbing Stairs

**Statement:**
You are climbing a staircase that has `n` steps. You can take either 1 step or 2 steps at a time. How many distinct ways can you reach the top (i.e., step `n`)?

**Example:**
n = 3 → Ways:

* 1 + 1 + 1
* 1 + 2
* 2 + 1
  So result = 3.

**Intuition / DP approach:**
Define `dp[i]` = number of ways to reach step `i`.
You can get to step `i` either from `i-1` (taking one step) or from `i-2` (taking two steps).
So `dp[i] = dp[i-1] + dp[i-2]`.
Base cases: `dp[0] = 1` (one way to “stand still”), `dp[1] = 1`.
Then compute up to `dp[n]`.

**Java Solution:**

```java
// Climbing Stairs: count ways to reach step n by taking 1 or 2 steps at a time.
public class ClimbingStairs {
    public int climbStairs(int n) {
        if (n <= 1) {
            return 1; // If 0 or 1 step, only one way (do nothing or one step)
        }
        int[] dp = new int[n + 1];        // dp[i] will hold number of ways to reach i
        dp[0] = 1;                         // base case: one way to stand at step 0
        dp[1] = 1;                         // base case: one way to reach step 1
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2]; // recurrence: from i-1 or i-2
        }
        return dp[n];                      // answer: number of ways to reach step n
    }
}
```

**Space optimized alternative:**
You only need the previous two values, so you can use two variables instead of array to get O(1) space.

---

### 2. Coin Change

**Statement:**
Given an integer array `coins` of different coin denominations and an integer `amount`, return the number of combinations that make up that amount. If that amount of money cannot be made up by any combination of the coins, return 0. You may assume that you have infinite number of each coin.

**Example:**
coins = [1,2,5], amount = 5 → combinations:

* 5
* 2+2+1
* 2+1+1+1
* 1+1+1+1+1
  So result = 4.

**Intuition / DP approach:**
Define `dp[i]` = number of ways to make amount = i.
Initialize `dp[0] = 1` (one way to make 0: no coins).
For each coin `c` in coins: for amount `i` from `c` to `amount`:
`dp[i] += dp[i - c]`
Explanation: ways to make amount i by using coin c = ways to make amount (i-c) then use coin c.
By iterating coins outer loop ensures combinations are counted without regard to order (i.e., coin order doesn’t matter).

**Java Solution:**

```java
import java.util.*;

public class CoinChange {
    public int change(int amount, int[] coins) {
        int n = coins.length;
        int[] dp = new int[amount + 1];
        dp[0] = 1; // one way to make 0 – pick no coin
        for (int coin : coins) {
            // for each coin, update ways for all amounts >= coin
            for (int i = coin; i <= amount; i++) {
                dp[i] += dp[i - coin];
            }
        }
        return dp[amount];
    }
}
```

---

### 3. Longest Increasing Subsequence (LIS)

**Statement:**
Given an integer array `nums`, return the length of the longest strictly increasing subsequence.

**Example:**
nums = [10,9,2,5,3,7,101,18] → The LIS is [2,3,7,101] → length = 4.

**Intuition / DP approach:**
Define `dp[i]` = length of longest increasing subsequence **ending at** index `i`.
Then for each `i`, check all `j < i` such that `nums[j] < nums[i]`:
`dp[i] = max(dp[i], dp[j] + 1)`
Base case: each `dp[i] = 1` (each element itself is LIS of length 1).
Finally answer = max of all `dp[i]`.

**Java Solution:**

```java
public class LongestIncreasingSubsequence {
    public int lengthOfLIS(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        int n = nums.length;
        int[] dp = new int[n];
        Arrays.fill(dp, 1); // each element alone is a subsequence of length 1
        int maxLen = 1;
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            maxLen = Math.max(maxLen, dp[i]);
        }
        return maxLen;
    }
}
```

---

### 4. House Robber

**Statement:**
You are a robber planning to rob houses along a street. Each house has some amount of money. Adjacent houses have security system connected, so you **cannot** rob two adjacent houses. Given `nums` array of non-negative integers (money in each house), find the maximum money you can rob without alerting the police (i.e., without robbing two adjacent).

**Example:**
nums = [1,2,3,1] → Optimal: rob house1 (1) + house3 (3) = 4.

**Intuition / DP approach:**
Define `dp[i]` = maximum money you can rob **up to** (and including) house `i`.
Either you rob house `i` (so you can’t rob `i-1`, but you add `dp[i-2] + nums[i]`) or you skip `i` (and you get `dp[i-1]`).
So: `dp[i] = Math.max(dp[i-1], dp[i-2] + nums[i])`.
Base cases: `dp[0] = nums[0]`; `dp[1] = max(nums[0], nums[1])`.

**Java Solution:**

```java
public class HouseRobber {
    public int rob(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        int n = nums.length;
        if (n == 1) {
            return nums[0];
        }
        int[] dp = new int[n];
        dp[0] = nums[0];
        dp[1] = Math.max(nums[0], nums[1]);
        for (int i = 2; i < n; i++) {
            dp[i] = Math.max(dp[i - 1], dp[i - 2] + nums[i]);
        }
        return dp[n - 1];
    }
}
```

---

### 5. House Robber II

**Statement:**
Same as House Robber, but houses are arranged **in a circle** (so house 0 and house n-1 are adjacent). Given the `nums` array, maximize robbery amount such that no two adjacent houses (including first and last) are robbed.

**Example:**
nums = [2,3,2] → You can either rob house1 (3) => result = 3.

**Intuition / DP approach:**
Since the circle makes first and last adjacent, you can’t rob both. So you reduce to two cases:

* Case 1: exclude first house (so consider houses [1..n-1])
* Case 2: exclude last house (so consider houses [0..n-2])
  Compute max for both with the linear House Robber method, then take max of both.
  Edge cases when n small.

**Java Solution:**

```java
public class HouseRobberII {
    public int rob(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        int n = nums.length;
        if (n == 1) {
            return nums[0];
        }
        // Case 1: exclude last house
        int case1 = robLinear(nums, 0, n - 2);
        // Case 2: exclude first house
        int case2 = robLinear(nums, 1, n - 1);
        return Math.max(case1, case2);
    }
    private int robLinear(int[] nums, int start, int end) {
        int incl = 0; // max including previous house
        int excl = 0; // max excluding previous house
        for (int i = start; i <= end; i++) {
            int new_excl = Math.max(excl, incl);
            incl = excl + nums[i];
            excl = new_excl;
        }
        return Math.max(incl, excl);
    }
}
```

**Comments:**

* `robLinear` solves the linear version between indices `start` and `end`.
* We track two variables `incl` and `excl`: incl = max if we rob current; excl = max if we skip current.
* This uses O(1) space.

---

### 6. Decode Ways

**Statement:**
Given a string `s` containing only digits, return the number of ways to decode it. The mapping is: `'A' = 1, 'B' = 2, …, 'Z' = 26`. You must decode the entire string. Leading zeros are invalid (e.g., “06” is invalid).

**Example:**
s = “12” → Ways: “AB” (1,2) or “L” (12) → result = 2.
s = “226” → Ways: “BZ” (2,26), “VF” (22,6), “BBF” (2,2,6) → result = 3.

**Intuition / DP approach:**
Define `dp[i]` = number of ways to decode substring `s[0..i-1]` (i.e., length i).
Base case: `dp[0] = 1` (empty string has one way).
Then for each position `i` (1-based for ease):

* If `s.charAt(i-1)` ≠ '0', then `dp[i] += dp[i-1]`. (The single digit works)
* If two-digit number formed by `s.charAt(i-2..i-1)` is between 10 and 26 inclusive, then `dp[i] += dp[i-2]`.
  Return `dp[n]`.

**Java Solution:**

```java
public class DecodeWays {
    public int numDecodings(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        int n = s.length();
        int[] dp = new int[n + 1];
        dp[0] = 1; // one way to decode empty string
        // dp[1] depends on first char
        dp[1] = s.charAt(0) != '0' ? 1 : 0;
        for (int i = 2; i <= n; i++) {
            char c1 = s.charAt(i - 1);
            char c2 = s.charAt(i - 2);
            // single digit decode
            if (c1 != '0') {
                dp[i] += dp[i - 1];
            }
            // two digits decode
            int twoDigit = (c2 - '0') * 10 + (c1 - '0');
            if (twoDigit >= 10 && twoDigit <= 26) {
                dp[i] += dp[i - 2];
            }
        }
        return dp[n];
    }
}
```

---

### 7. Unique Paths

**Statement:**
You are in an `m × n` grid, starting at the top-left corner (0,0) and you want to reach bottom-right corner (m-1,n-1). You can only move **down** or **right** at any step. How many unique paths exist?

**Example:**
m = 3, n = 7 → there are 28 unique paths.

**Intuition / DP approach:**
Define `dp[i][j]` = number of unique paths to cell (i,j).
You can come to (i,j) from (i-1,j) [down move] or (i,j-1) [right move].
So: `dp[i][j] = dp[i-1][j] + dp[i][j-1]`.
Base cases: first row `dp[0][j] = 1` (only one way: move right repeatedly) and first column `dp[i][0] = 1` (only move down repeatedly).
Compute until `dp[m-1][n-1]`.

**Java Solution:**

```java
public class UniquePaths {
    public int uniquePaths(int m, int n) {
        int[][] dp = new int[m][n];
        // first row
        for (int j = 0; j < n; j++) {
            dp[0][j] = 1;
        }
        // first column
        for (int i = 0; i < m; i++) {
            dp[i][0] = 1;
        }
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
            }
        }
        return dp[m - 1][n - 1];
    }
}
```

---

### 8. Jump Game

**Statement:**
Given an array of non-negative integers `nums`, where `nums[i]` is the maximum jump length from that position, determine if you can reach the last index starting from the first index.

**Example:**
nums = [2,3,1,1,4] → Yes, you can jump 0→1 (jump length 2) then 1→4.
nums = [3,2,1,0,4] → No, you will get stuck at index 3 (value 0) and cannot reach index 4.

**Intuition / DP / Greedy approach:**
One DP approach: `dp[i]` = whether you can reach position `i`.
Then `dp[0] = true`. For each `i`, if `dp[i] = true`, then you can mark `dp[j] = true` for `j ∈ [i+1, i+nums[i]]`. Finally check `dp[n-1]`.
But more optimally you can use a greedy variable `reach` = farthest index you can reach so far; iterate and update `reach = max(reach, i + nums[i])`. If at any point `i > reach`, you cannot proceed → return false.
If you finish loop, return true.

**Java Solution (Greedy):**

```java
public class JumpGame {
    public boolean canJump(int[] nums) {
        int reach = 0; // farthest index we can reach so far
        for (int i = 0; i < nums.length; i++) {
            if (i > reach) {
                // we are at a position beyond what we can reach → stuck
                return false;
            }
            reach = Math.max(reach, i + nums[i]);
        }
        return true;
    }
}
```

---

### 9. Word Break

**Statement:**
Given a non-empty string `s` and a list of non-empty words `wordDict`, determine if `s` can be segmented into a space-separated sequence of one or more dictionary words. Each word can be used unlimited times.

**Example:**
s = “leetcode”, wordDict = [“leet”, “code”] → return true (“leet code”).
s = “applepenapple”, wordDict = [“apple”, “pen”] → return true (“apple pen apple”).
s = “catsandog”, wordDict = [“cats”, “dog”, “sand”, “and”, “cat”] → return false.

**Intuition / DP approach:**
Define `dp[i]` = whether substring `s[0..i-1]` can be segmented.
Base: `dp[0] = true` (empty string).
Then for `i` from 1 to `n`: for each `j` from 0 to `i-1`:
if `dp[j]` is true **and** `s.substring(j,i)` is in `wordDict`, then `dp[i] = true` and break.
Finally return `dp[n]`.

**Java Solution:**

```java
import java.util.*;

public class WordBreak {
    public boolean wordBreak(String s, List<String> wordDict) {
        Set<String> dict = new HashSet<>(wordDict);
        int n = s.length();
        boolean[] dp = new boolean[n + 1];
        dp[0] = true; // empty string can be segmented
        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < i; j++) {
                if (dp[j] && dict.contains(s.substring(j, i))) {
                    dp[i] = true;
                    break;
                }
            }
        }
        return dp[n];
    }
}
```

---

### 10. Combination Sum IV

**Statement:**
Given an integer array `nums` of **unique** positive integers and an integer `target`, return the number of possible **ordered** combinations that add up to `target`. (Order matters.)

**Example:**
nums = [1,2,3], target = 4 → combinations:
1+1+1+1
1+1+2
1+2+1
2+1+1
2+2
1+3
3+1
So result = 7.

**Intuition / DP approach:**
Define `dp[i]` = number of combinations that sum to `i`.
Base: `dp[0] = 1` (one way to make sum 0: pick nothing).
For `i` from 1 to `target`: for each `num` in `nums`: if `i-num >= 0`, then `dp[i] += dp[i-num]`.
Because order matters, we loop amounts outer, numbers inner (so we consider all orders).
Return `dp[target]`.

**Java Solution:**

```java
public class CombinationSumIV {
    public int combinationSum4(int[] nums, int target) {
        long[] dp = new long[target + 1]; // use long to avoid overflow
        dp[0] = 1;
        for (int i = 1; i <= target; i++) {
            for (int num : nums) {
                if (i - num >= 0) {
                    dp[i] += dp[i - num];
                }
            }
        }
        return (int) dp[target];
    }
}
```

---

## 📋 Full Markdown for GitHub

Here’s how you could put all this into a `README.md` or similar on GitHub.

```markdown
# Dynamic Programming (DP) – 10 Classic Problems

## What is Dynamic Programming?  
Dynamic Programming (DP) is a method for solving problems by breaking them down into simpler overlapping sub-problems, storing the results of those sub-problems, and combining them to form a solution.  
When a problem has *optimal substructure* (an optimal solution can be built from optimal solutions of sub-problems) and *overlapping sub-problems* (sub-problems repeat), DP is the tool. [Sources: see e.g. GeeksforGeeks, Wikipedia]  
DP typically transforms exponential-time recursion into polynomial time by caching results.

## Core Concepts  
1. **Optimal substructure** – the property that optimal solution to the problem can be composed of optimal solutions to sub-problems.  
2. **Overlapping sub-problems** – repeated solving of the same sub-problems in naive recursion.  
3. **Memoization (top-down)** vs **Tabulation (bottom-up)** – two implementation flavours.  
4. **State definition** – choose the parameters that define a sub-problem.  
5. **Recurrence/transition** – how each state derives from smaller states.  
6. **Base cases** – smallest sub-problems with known answer.  
7. **Implementation & optimisation** – time & space trade-offs.

## Why and Where to Use DP  
- Efficiency: avoids redundant work, often reducing exponential time to polynomial.  
- Use cases: algorithmic problems (coding interviews), pathfinding/graphs, resource allocation, bioinformatics (e.g., sequence alignment), operations research (multi-period decisions) etc.

## Problems & Solutions  
Below are 10 classic DP problems, each with statement, example, intuition, and Java code.

### 1. Climbing Stairs  
**Statement:** …  
**Example:** …  
**Solution:** (Java code)  
*(see above)*

### 2. Coin Change  
… *(use the content above)*

### 3. Longest Increasing Subsequence  
…  

### 4. House Robber  
…  

### 5. House Robber II  
…  

### 6. Decode Ways  
…  

### 7. Unique Paths  
…  

### 8. Jump Game  
…  

### 9. Word Break  
…  

### 10. Combination Sum IV  
…  

---

## Visual Summary  
*(You may insert diagrams e.g., overlapping recursion tree, memo vs tabulation table, generic state transition graph.)*

---

## How to Use This Repository  
1. Clone/download the repo.  
2. Navigate to each problem’s folder (if you break them into files).  
3. Compile and run the Java code.  
4. Modify inputs and test your understanding.  
5. Use the code as a template for further DP practice.

---

## Final Thoughts  
DP often looks intimidating at first, but if you master the *state → recurrence → base* pattern, you’ll start recognising DP patterns across many problems. A good strategy:  
- Try the naive recursive version (to understand problem)  
- See its repeated calculations (overlapping)  
- Add memo or convert to tabulation  
- Check space/time trade-offs  

Once you internalise the framework, tackling new DP problems becomes much easier.

Happy coding!

```

---
