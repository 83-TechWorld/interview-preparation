That's an excellent list of fundamental problems\! We'll tackle them one by one, focusing on **Arrays and Hashing** techniques.

Let's start with the first three. For each problem, I'll provide:

1.  **Problem Explanation**
2.  **Optimal Solution Approach** (including the core concept like Hashing or Two Pointers)
3.  **Step-by-Step Example**
4.  **Java Code** with detailed comments
5.  **Real-World Use Cases**

-----

## 1\. Two Sum

### ❓ Problem Explanation

The goal is to find two numbers in a given array (`nums`) that add up to a specific `target` value. You need to return the **indices** of these two numbers.

### 💡 Optimal Solution: Using a Hash Map (O(n) Time)

The brute-force approach (checking every pair) is $O(n^2)$. The optimal way uses a **Hash Map** (or `HashMap` in Java) to achieve $O(n)$ time complexity.

1.  We iterate through the array once.
2.  For each number $x$ (at index $i$), we calculate the **complement** needed: $c = target - x$.
3.  We check if this **complement** $c$ already exists as a key in our Hash Map.
      * If it does, we found the pair\! The current index $i$ and the index stored against $c$ in the map are our answer.
      * If it doesn't, we add the current number $x$ and its index $i$ to the Hash Map for future lookups.

### 👣 Step-by-Step Example

**Input:** `nums = [2, 7, 11, 15]`, `target = 9`

| Step | Current Element ($x$) | Current Index ($i$) | Complement ($c = 9 - x$) | Hash Map (key: number, value: index) | Action | Output? |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1** | 2 | 0 | $9 - 2 = 7$ | {} | **7** not found. Put (2, 0) into map. | No |
| **2** | 7 | 1 | $9 - 7 = 2$ | {2: 0} | **2** **found** in map\! Its index is 0. | **[0, 1]** |

### 💻 Java Solution

```java
class Solution {
    /**
     * Finds two numbers in the array that add up to the target.
     * Uses a HashMap for O(1) average time complexity lookups.
     * @param nums The array of integers.
     * @param target The target sum.
     * @return An array of two indices [index1, index2].
     */
    public int[] twoSum(int[] nums, int target) {
        // A HashMap to store the numbers we've seen so far and their indices.
        // Key: The number itself.
        // Value: The index of that number in the 'nums' array.
        Map<Integer, Integer> numMap = new HashMap<>();

        // Iterate through the array once. This makes the time complexity O(n).
        for (int i = 0; i < nums.length; i++) {
            int currentNum = nums[i];
            
            // 1. Calculate the 'complement' needed to reach the target.
            // complement + currentNum = target
            int complement = target - currentNum;

            // 2. Check if the complement already exists in our map.
            // If it exists, we've found our pair!
            if (numMap.containsKey(complement)) {
                // The current index 'i' and the index of the complement 
                // (stored in the map) are the answer.
                return new int[] { numMap.get(complement), i };
            }

            // 3. If the complement is not found, add the current number and its index
            // to the map for future lookups.
            numMap.put(currentNum, i);
        }

        // According to the problem constraints, a solution always exists.
        // If it didn't, we would throw an exception or return an empty array.
        return new int[0];
    }
}
```

### 🌍 Real-World Use Cases

  * **Financial Trading:** Checking if a **combination of two asset prices** meets a specific investment threshold or target profit value.
  * **Hash Table Collision Resolution:** A very simplified, internal view of how a system might quickly check for a match based on a specific hash code.
  * **Data Validation/Search:** Quickly confirming if a required **data pair** exists in a large dataset that sums to a required total (e.g., verifying transactions or inventory counts).

-----

-----

## 2\. Best Time to Buy and Sell Stock

### ❓ Problem Explanation

You are given an array where the $i$-th element is the **price of a given stock on day $i$**. You want to maximize your profit by choosing **one single day to buy** and **choosing a different day in the future to sell**. If you cannot achieve any profit, the maximum profit is 0.

### 💡 Optimal Solution: One-Pass Iteration (O(n) Time)

This problem is about finding the **maximum difference** (profit) between any two numbers in the array, where the smaller number (buy price) must appear *before* the larger number (sell price).

1.  We maintain two variables:
      * `minBuyPrice`: The lowest stock price seen **so far** as we iterate.
      * `maxProfit`: The highest profit calculated **so far**.
2.  We iterate through the prices:
      * Calculate the `currentProfit` if we sold **today**: `currentPrice - minBuyPrice`.
      * Update `maxProfit` if `currentProfit` is greater.
      * Update `minBuyPrice` if the `currentPrice` is lower than the current `minBuyPrice`.

### 👣 Step-by-Step Example

**Input:** `prices = [7, 1, 5, 3, 6, 4]`

| Step | Current Price | `minBuyPrice` (start: $\infty$) | Current Profit (Price - `minBuyPrice`) | `maxProfit` (start: 0) | Action |
| :---: | :---: | :---: | :---: | :---: | :---: |
| **1** | 7 | 7 | $7 - 7 = 0$ | 0 | Update `minBuyPrice` to 7. |
| **2** | 1 | 1 | $1 - 1 = 0$ | 0 | Update `minBuyPrice` to 1. |
| **3** | 5 | 1 | $5 - 1 = **4**$ | 4 | Update `maxProfit` to 4. |
| **4** | 3 | 1 | $3 - 1 = 2$ | 4 | Profit 2 is \< 4. `minBuyPrice` remains 1. |
| **5** | 6 | 1 | $6 - 1 = **5**$ | 5 | Update `maxProfit` to 5. |
| **6** | 4 | 1 | $4 - 1 = 3$ | 5 | Profit 3 is \< 5. `minBuyPrice` remains 1. |

**Final Max Profit: 5** (Buy at 1, Sell at 6)

### 💻 Java Solution

```java
class Solution {
    /**
     * Calculates the maximum profit achievable from a single buy and sell transaction.
     * Uses a single pass to track the minimum price seen so far.
     * @param prices The array of stock prices.
     * @return The maximum possible profit.
     */
    public int maxProfit(int[] prices) {
        // Initialize the max profit to 0, as you can always make zero profit
        // by choosing not to trade.
        int maxProfit = 0;
        
        // Initialize the minimum price seen so far to a very large number, 
        // or just the first price. Using a large initial value ensures the 
        // first price immediately becomes the min.
        // Integer.MAX_VALUE is a good, safe choice.
        int minBuyPrice = Integer.MAX_VALUE;

        // Iterate through each price in the array once (O(n) time).
        for (int price : prices) {
            
            // 1. Check if the current price is a new minimum buy price.
            // We always want to find the lowest possible price to buy.
            if (price < minBuyPrice) {
                minBuyPrice = price;
            } 
            // 2. Otherwise, this price could be a potential selling point.
            // Calculate the profit if we sell today.
            else {
                int currentProfit = price - minBuyPrice;
                
                // 3. Update the overall maximum profit if the current profit is higher.
                maxProfit = Math.max(maxProfit, currentProfit);
            }
            
            /* A cleaner, combined approach (conceptually identical):
               minBuyPrice = Math.min(minBuyPrice, price);
               maxProfit = Math.max(maxProfit, price - minBuyPrice);
            */
        }

        return maxProfit;
    }
}
```

### 🌍 Real-World Use Cases

  * **Algorithmic Trading/Finance:** The most direct application, forming the basis for simple trading strategies and benchmarks.
  * **Time Series Analysis:** Finding the **largest rise/peak** (maximum increase) in any time-ordered data, such as website traffic, server load, or temperature readings.
  * **Sensor Data Optimization:** Identifying the maximum gain in signal strength or energy output over a period of time, where the "buy" is the initial low value and the "sell" is the subsequent high value.

-----

-----

## 3\. Contains Duplicate

### ❓ Problem Explanation

Given an integer array `nums`, determine if any value appears **at least twice** in the array. If every element is distinct (unique), return `false`; otherwise, return `true`.

### 💡 Optimal Solution: Using a Hash Set (O(n) Time)

The most efficient way is to use a **Hash Set** (or `HashSet` in Java). Hash Sets are designed for fast $O(1)$ average time complexity for insertion and lookup operations.

1.  Initialize an empty `HashSet`.
2.  Iterate through the array.
3.  For each number:
      * Check if the number is **already present** in the set.
      * If it is, we have found a duplicate and can immediately return `true`.
      * If it is not, add the number to the set and continue.
4.  If the loop finishes without finding any duplicates, return `false`.

### 👣 Step-by-Step Example

**Input:** `nums = [1, 2, 3, 1]`

| Step | Current Element | Action | Hash Set | Output? |
| :---: | :---: | :---: | :---: | :---: |
| **1** | 1 | **1** not in set. Add 1. | {1} | No |
| **2** | 2 | **2** not in set. Add 2. | {1, 2} | No |
| **3** | 3 | **3** not in set. Add 3. | {1, 2, 3} | No |
| **4** | 1 | **1** **found** in set\! | {1, 2, 3} | **True** |

### 💻 Java Solution

```java
class Solution {
    /**
     * Checks if the array contains any duplicate elements.
     * Uses a HashSet for fast O(1) average time complexity lookups.
     * @param nums The array of integers.
     * @return True if a duplicate exists, False otherwise.
     */
    public boolean containsDuplicate(int[] nums) {
        // A HashSet is ideal because it only stores unique elements
        // and provides O(1) average time complexity for checking if an 
        // element already exists (the 'contains' operation).
        Set<Integer> seenNumbers = new HashSet<>();

        // Iterate through the array once (O(n) time).
        for (int num : nums) {
            
            // 1. Check if the current number has already been added to the set.
            if (seenNumbers.contains(num)) {
                // If the set already 'contains' this number, it means 
                // we have found a duplicate.
                return true;
            }

            // 2. If it's a unique number so far, add it to the set.
            seenNumbers.add(num);
        }

        // If the loop completes, it means we processed the whole array 
        // without finding any duplicates.
        return false;
    }
}
```

### 🌍 Real-World Use Cases

  * **Database Primary Keys:** Ensuring that a new record being inserted (e.g., a customer ID, account number) does not already exist, which would violate the database's uniqueness constraint.
  * **Data Deduplication:** Cleaning large datasets (like email lists or log files) by identifying and removing redundant entries to save storage or processing time.
  * **Web Development/Usernames:** When a new user signs up, the system quickly checks if the chosen **username** or **email address** is already in use by querying a unique index (a concept built on hashing).

-----
Yes, let's absolutely continue\! Those are three excellent and challenging problems that build on array manipulation skills.

Here are the explanations, solutions, and use cases for the next three problems:

-----

## 4\. Product of Array Except Self

### ❓ Problem Explanation

Given an integer array `nums`, you need to return an array `answer` where `answer[i]` is equal to the **product of all the elements of `nums` except `nums[i]`**.

The challenge is to solve this **without using division** and in $O(n)$ time complexity.

### 💡 Optimal Solution: Left and Right Products (O(n) Time)

Since division is forbidden, we can calculate the desired product by multiplying two components for each index $i$:

1.  **Left Product:** The product of all elements to the **left** of index $i$.
2.  **Right Product:** The product of all elements to the **right** of index $i$.

$$\text{answer}[i] = (\text{Product of } \text{nums}[0] \text{ to } \text{nums}[i-1]) \times (\text{Product of } \text{nums}[i+1] \text{ to } \text{nums}[n-1])$$

We use two passes over the array:

1.  **First Pass (Left Pass):** Create the `answer` array and populate it with the cumulative product from the left.
2.  **Second Pass (Right Pass):** Iterate backward, tracking the cumulative product from the right, and multiply this right product into the existing left product stored in the `answer` array.

### 👣 Step-by-Step Example

**Input:** `nums = [1, 2, 3, 4]`

#### **Pass 1: Left Products**

| Index ($i$) | `nums[i]` | Left Product running total (starts at 1) | `answer[i]` (stores Left Product *up to* $i-1$) |
| :---: | :---: | :---: | :---: |
| **0** | 1 | 1 | `answer[0]` = 1 (empty product) |
| **1** | 2 | $1 \times 1 = 1$ | `answer[1]` = 1 |
| **2** | 3 | $1 \times 2 = 2$ | `answer[2]` = 2 |
| **3** | 4 | $2 \times 3 = 6$ | `answer[3]` = 6 |
| **End of Pass 1: `answer = [1, 1, 2, 6]`** |

#### **Pass 2: Right Products**

| Index ($i$) | `nums[i]` | Right Product running total (starts at 1) | `answer[i]` update: `answer[i] * Right Product` |
| :---: | :---: | :---: | :---: |
| **3** | 4 | 1 | `answer[3]` = $6 \times 1 = \mathbf{6}$ |
| **2** | 3 | $1 \times 4 = 4$ | `answer[2]` = $2 \times 4 = \mathbf{8}$ |
| **1** | 2 | $4 \times 3 = 12$ | `answer[1]` = $1 \times 12 = \mathbf{12}$ |
| **0** | 1 | $12 \times 2 = 24$ | `answer[0]` = $1 \times 24 = \mathbf{24}$ |
| **Final Answer: `[24, 12, 8, 6]`** |

### 💻 Java Solution

```java
class Solution {
    /**
     * Calculates the product of all elements except the element at the current index.
     * Achieved in O(n) time and O(1) extra space (excluding the output array)
     * by using two passes: one for left products and one for right products.
     * @param nums The input array.
     * @return The output array with the calculated products.
     */
    public int[] productExceptSelf(int[] nums) {
        int n = nums.length;
        // The result array will also store our running left products initially.
        int[] answer = new int[n];
        
        // --- PASS 1: Calculate Left Products ---
        
        // 'leftProduct' starts at 1, representing the product of an empty prefix.
        int leftProduct = 1;
        
        for (int i = 0; i < n; i++) {
            // 1. answer[i] is set to the product of elements BEFORE nums[i].
            answer[i] = leftProduct;
            
            // 2. Update the running product by including the current element for the next iteration.
            leftProduct *= nums[i];
        }
        
        // At the end of Pass 1, answer = [1, nums[0], nums[0]*nums[1], ...]
        
        // --- PASS 2: Incorporate Right Products ---
        
        // 'rightProduct' starts at 1, representing the product of an empty suffix.
        int rightProduct = 1;
        
        // Iterate backwards through the array.
        for (int i = n - 1; i >= 0; i--) {
            // 1. The existing answer[i] holds the LEFT product. 
            // We multiply it by the accumulated RIGHT product (product of elements AFTER nums[i]).
            answer[i] = answer[i] * rightProduct;
            
            // 2. Update the running product by including the current element for the previous index.
            rightProduct *= nums[i];
        }
        
        return answer;
    }
}
```

### 🌍 Real-World Use Cases

  * **Prefix/Suffix Arrays in Data Analysis:** While this specific problem is niche, the **prefix sum/product** technique is heavily used in competitive programming and dynamic array analysis to quickly calculate sums or products over sub-arrays.
  * **Weighted Averages/Scaling (without division):** In scenarios where division is computationally expensive or forbidden (e.g., in some modular arithmetic environments), this pattern allows calculating ratios or scaled values by multiplying factors (inverse products).

-----

## 5\. Maximum Subarray (Kadane's Algorithm)

### ❓ Problem Explanation

Given an integer array `nums`, find the **contiguous subarray** (a sequence of elements next to each other) which has the largest sum, and return that sum.

### 💡 Optimal Solution: Kadane's Algorithm (O(n) Time)

This problem is a classic example solved by **Kadane's Algorithm**, a simple but highly effective **Dynamic Programming** approach.

The core idea is: at any index $i$, the maximum subarray sum ending at $i$ is either:

1.  Just the current element `nums[i]`.
2.  The current element `nums[i]` **plus** the maximum subarray sum ending at the previous index $(i-1)$.

We maintain two variables:

  * `currentMax`: The maximum sum of a subarray **ending at the current position**.
  * `overallMax`: The **largest sum found anywhere** in the array so far.

### 👣 Step-by-Step Example

**Input:** `nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4]`

| Index ($i$) | `nums[i]` | `currentMax` = $\max(\text{nums}[i], \text{nums}[i] + \text{previous } currentMax)$ | `overallMax` = $\max(\text{overallMax}, \text{currentMax})$ |
| :---: | :---: | :---: | :---: |
| **0** | -2 | $\max(-2, -2 + 0) = -2$ | $\max(-\infty, -2) = -2$ |
| **1** | 1 | $\max(1, 1 + (-2)) = 1$ | $\max(-2, 1) = 1$ |
| **2** | -3 | $\max(-3, -3 + 1) = -2$ | $\max(1, -2) = 1$ |
| **3** | 4 | $\max(4, 4 + (-2)) = \mathbf{4}$ | $\max(1, 4) = 4$ |
| **4** | -1 | $\max(-1, -1 + 4) = 3$ | $\max(4, 3) = 4$ |
| **5** | 2 | $\max(2, 2 + 3) = \mathbf{5}$ | $\max(4, 5) = 5$ |
| **6** | 1 | $\max(1, 1 + 5) = \mathbf{6}$ | $\max(5, 6) = 6$ |
| **7** | -5 | $\max(-5, -5 + 6) = 1$ | $\max(6, 1) = 6$ |
| **8** | 4 | $\max(4, 4 + 1) = 5$ | $\max(6, 5) = 6$ |

**Final Max Sum: 6** (Subarray: `[4, -1, 2, 1]`)

### 💻 Java Solution

```java
class Solution {
    /**
     * Finds the maximum sum of a contiguous subarray using Kadane's Algorithm.
     * This is an O(n) dynamic programming approach.
     * @param nums The input array.
     * @return The maximum subarray sum.
     */
    public int maxSubArray(int[] nums) {
        // 'overallMax' stores the maximum sum found anywhere in the array so far.
        // Initialize it to the first element (or a very small number if empty array was possible).
        int overallMax = nums[0];
        
        // 'currentMax' stores the maximum sum of a subarray that *ends* at the current index.
        int currentMax = nums[0];

        // Start loop from the second element (index 1).
        for (int i = 1; i < nums.length; i++) {
            int num = nums[i];
            
            // 1. Dynamic Programming Step (CurrentMax update):
            // The max subarray sum ending at 'i' is the greater of:
            // a) Starting a NEW subarray at 'i' (just 'num').
            // b) Extending the previous max subarray (previous 'currentMax' + 'num').
            currentMax = Math.max(num, currentMax + num);
            
            // 2. Overall Max Update:
            // Check if the newly calculated 'currentMax' is the largest sum we've seen yet.
            overallMax = Math.max(overallMax, currentMax);
        }

        return overallMax;
    }
}
```

### 🌍 Real-World Use Cases

  * **Financial Market Analysis:** Identifying the **best period to hold a stock** to maximize profit, where the array elements represent daily changes (gains or losses) in the stock price. The positive sum represents the total gain over that period.
  * **Image Processing:** Finding the brightest/most contrast region (a "hotspot") in a 1D slice of an image by treating pixel values as array elements and looking for the maximum sum.
  * **Bioinformatics:** Analyzing gene sequences by assigning scores to base pairs and looking for segments with the highest scoring matches.

-----

## 6\. Maximum Product Subarray

### ❓ Problem Explanation

Given an integer array `nums`, find the **contiguous subarray** which has the largest **product**, and return that product.

### 💡 Optimal Solution: Tracking Max and Min Products (O(n) Time)

This problem is similar to Maximum Subarray, but with a critical difference: **negative numbers**. A small negative number multiplied by another large negative number can result in a very large positive product.

Therefore, at each index $i$, we must track **both** the maximum product **and** the minimum product ending at $i$.

1.  We maintain three variables:

      * `maxProduct`: Maximum product of a subarray **ending at the current position**.
      * `minProduct`: **Minimum** product of a subarray **ending at the current position**. (Used when a negative number turns the minimum into the new maximum).
      * `overallMax`: The **largest product found anywhere** in the array so far.

2.  When we encounter a negative number, `maxProduct` and `minProduct` effectively **swap roles** when multiplied by the current number.

### 👣 Step-by-Step Example

**Input:** `nums = [2, 3, -2, 4]`

| Index ($i$) | `nums[i]` | `tempMax` calculation | `maxProduct` | `minProduct` | `overallMax` |
| :---: | :---: | :---: | :---: | :---: | :---: |
| **0** | 2 | | 2 | 2 | 2 |
| **1** | 3 | $\max(3, 3 \times 2, 3 \times 2)$ | 6 | 6 | 6 |
| **2** | -2 | $\max(-2, -2 \times 6, -2 \times 6)$ | -2 | -12 | 6 |
| **3** | 4 | $\max(4, 4 \times (-2), 4 \times (-12))$ | 4 | -48 | 6 |
| **Final Max Product: 6** (Subarray: `[2, 3]`) |

**Example 2: With Two Negatives:** `nums = [-2, 3, -4]`

| Index ($i$) | `nums[i]` | `tempMax` calculation | `maxProduct` | `minProduct` | `overallMax` |
| :---: | :---: | :---: | :---: | :---: | :---: |
| **0** | -2 | | -2 | -2 | -2 |
| **1** | 3 | $\max(3, 3 \times (-2), 3 \times (-2))$ | 3 | -6 | 3 |
| **2** | -4 | **Swap Needed**: $\text{oldMax}=-6, \text{oldMin}=3$ | | | |
| **2** | -4 | $\max(-4, -4 \times 3, -4 \times (-6))$ | $\mathbf{24}$ | $-12$ | 24 |
| **Final Max Product: 24** (Subarray: `[-2, 3, -4]`) |

### 💻 Java Solution

```java
class Solution {
    /**
     * Finds the maximum product of a contiguous subarray.
     * It uses a single pass, tracking both maximum and minimum products ending at the current index.
     * @param nums The input array.
     * @return The maximum product subarray.
     */
    public int maxProduct(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        // overallMax holds the best product found across the entire array.
        int overallMax = nums[0];
        
        // maxProductEndingHere holds the max product of a subarray ending at the current index.
        int maxProductEndingHere = nums[0];
        
        // minProductEndingHere holds the MIN product of a subarray ending at the current index.
        // This is crucial because a small negative * current negative = large positive product.
        int minProductEndingHere = nums[0];

        for (int i = 1; i < nums.length; i++) {
            int currentNum = nums[i];
            
            // If the current number is negative, multiplying it by the current max will give the new min,
            // and multiplying it by the current min will give the new max (due to sign flip).
            // We temporarily store the old max before updating min/max for the current step.
            int tempMax = maxProductEndingHere;
            
            // 1. Calculate the new max product ending at 'i':
            // It's the maximum of 3 possibilities:
            // a) Starting a new subarray: currentNum
            // b) Extending the previous max subarray: maxProductEndingHere * currentNum
            // c) Extending the previous MIN subarray (if currentNum is negative): minProductEndingHere * currentNum
            maxProductEndingHere = Math.max(currentNum, 
                                            Math.max(maxProductEndingHere * currentNum, 
                                                     minProductEndingHere * currentNum));

            // 2. Calculate the new min product ending at 'i':
            // We must use the *old* max product (tempMax) for this calculation.
            minProductEndingHere = Math.min(currentNum, 
                                            Math.min(tempMax * currentNum, 
                                                     minProductEndingHere * currentNum));

            // 3. Update the overall maximum found so far.
            overallMax = Math.max(overallMax, maxProductEndingHere);
        }

        return overallMax;
    }
}
```

### 🌍 Real-World Use Cases

  * **Compound Growth Rate:** Finding the period in a financial sequence (where elements are growth multipliers, e.g., 1.1 for 10% growth, 0.9 for 10% loss) that resulted in the overall largest **compounding return**.
  * **Signal Processing:** Identifying a sequence of measurements or signals that, when multiplied together (e.g., representing successive amplification or attenuation factors), result in the highest possible cumulative output.

-----
Absolutely\! These last four problems introduce more specialized techniques, especially **Binary Search** in unusual scenarios and the **Two-Pointer** pattern.

Here are the explanations, solutions, and use cases for the remaining problems:

-----

## 7\. Find Minimum in Rotated Sorted Array

### ❓ Problem Explanation

A **sorted array** is **rotated** at some unknown pivot point. This means a sequence of elements from the end was moved to the beginning (e.g., `[0, 1, 2, 4, 5, 6, 7]` might become `[4, 5, 6, 7, 0, 1, 2]`). You need to find the **minimum element** in this rotated array.

The array contains no duplicate elements.

### 💡 Optimal Solution: Modified Binary Search (O($\log n$) Time)

A linear search is $O(n)$. Since the array is **mostly sorted**, we can use a modified **Binary Search** to achieve $O(\log n)$ time complexity. The minimum element is the only element that is smaller than its predecessor.

1.  Initialize pointers `left` (start) and `right` (end).
2.  If the array is not rotated (i.e., `nums[left] < nums[right]`), the minimum is simply `nums[left]`.
3.  In the loop, compare the middle element `nums[mid]` with the element at the `right` pointer (`nums[right]`):
      * If `nums[mid] > nums[right]`: The minimum must be in the **right half** (because the pivot/break must be after `mid`). So, move `left = mid + 1`.
      * If `nums[mid] < nums[right]`: The minimum is in the **left half** (or is `mid` itself) because the array section from `mid` to `right` is sorted correctly. So, move `right = mid`.
4.  The loop terminates when `left == right`, and `nums[left]` (or `nums[right]`) is the minimum.

### 👣 Step-by-Step Example

**Input:** `nums = [4, 5, 6, 7, 0, 1, 2]`

| Step | Left ($L$) | Right ($R$) | Mid ($M$) | `nums[M]` | Condition (`nums[M]` vs `nums[R]`) | New Range |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1** | 0 | 6 | 3 | 7 | $7 > 2$ (Min is in Right half) | $L = 4$ |
| **2** | 4 | 6 | 5 | 1 | $1 < 2$ (Min is in Left half, or is $M$) | $R = 5$ |
| **3** | 4 | 5 | 4 | 0 | $0 < 1$ (Min is in Left half, or is $M$) | $R = 4$ |
| **4** | 4 | 4 | - | - | Loop ends: $L=R=4$ | **Min = `nums[4]` = 0** |

### 💻 Java Solution

```java
class Solution {
    /**
     * Finds the minimum element in a rotated sorted array using Binary Search (O(log n)).
     * @param nums The rotated sorted array (no duplicates).
     * @return The minimum element.
     */
    public int findMin(int[] nums) {
        int left = 0;
        int right = nums.length - 1;

        // If the array is not rotated at all, the first element is the minimum.
        if (nums[left] <= nums[right]) {
            return nums[left];
        }

        // Standard Binary Search loop.
        while (left < right) {
            int mid = left + (right - left) / 2; // Safer way to calculate mid

            // Case 1: The pivot (minimum) is in the right half.
            // This happens when the middle element is GREATER than the rightmost element.
            // Example: [4, 5, 6, 7, 0, 1, 2] -> mid=7, right=2. 7 > 2. Minimum is after 7.
            if (nums[mid] > nums[right]) {
                // The minimum cannot be 'mid' or anything before it.
                left = mid + 1;
            } 
            // Case 2: The pivot (minimum) is in the left half, or is 'mid' itself.
            // This happens when the middle element is SMALLER than the rightmost element.
            // Example: [6, 7, 0, 1, 2, 4, 5] -> mid=1, right=5. 1 < 5. Minimum is 1 or before.
            else {
                // The minimum COULD be 'mid', so we can't discard it by setting right = mid - 1.
                right = mid;
            }
        }
        
        // When left == right, we have converged on the minimum element.
        return nums[left];
    }
}
```

### 🌍 Real-World Use Cases

  * **Circular Buffer Search:** Efficiently locating the oldest (minimum value) or newest element in a **circularly managed data buffer** (like a ring buffer) used in networking or real-time logging, which is conceptually a rotated array.
  * **Optimized Distributed Search:** In distributed databases or file systems where data blocks are sorted but managed across partitions in a circular fashion, this search minimizes the number of blocks that need to be queried.

-----

## 8\. Search in Rotated Sorted Array

### ❓ Problem Explanation

Given the same rotated sorted array (no duplicates) as the previous problem, you now need to **search for a specific `target` value**. If the target is found, return its index; otherwise, return -1.

### 💡 Optimal Solution: Modified Binary Search (O($\log n$) Time)

We must use Binary Search, but we need an extra step to figure out which half of the array is currently **sorted** and where the target might reside.

1.  Initialize pointers `left` and `right`.

2.  In the loop, calculate `mid`.

3.  Check which half the `mid` element belongs to by comparing `nums[left]` with `nums[mid]`.

      * **Case A: Left Half is Sorted** (`nums[left] <= nums[mid]`)

          * If `target` is within this sorted range (`nums[left] <= target < nums[mid]`), discard the right half: `right = mid - 1`.
          * Otherwise, the target must be in the unsorted right half: `left = mid + 1`.

      * **Case B: Right Half is Sorted** (`nums[left] > nums[mid]`)

          * If `target` is within this sorted range (`nums[mid] < target <= nums[right]`), discard the left half: `left = mid + 1`.
          * Otherwise, the target must be in the unsorted left half: `right = mid - 1`.

### 👣 Step-by-Step Example

**Input:** `nums = [4, 5, 6, 7, 0, 1, 2]`, `target = 0`

| Step | L | R | M | `nums[M]` | Target Found? | Sorted Half | Target Range? | New Range |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1** | 0 | 6 | 3 | 7 | No | Left ($4 \le 7$) | Target 0 is NOT in $[4, 7]$ | $L = 4$ |
| **2** | 4 | 6 | 5 | 1 | No | Right ($0 \le 1$) | Target 0 IS in $[0, 2]$ | $R = 4$ |
| **3** | 4 | 4 | 4 | 0 | **Yes** | - | - | **Return 4** |

### 💻 Java Solution

```java
class Solution {
    /**
     * Searches for a target in a rotated sorted array using Modified Binary Search (O(log n)).
     * @param nums The rotated sorted array.
     * @param target The value to search for.
     * @return The index of the target, or -1 if not found.
     */
    public int search(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            // 1. Check if we found the target
            if (nums[mid] == target) {
                return mid;
            }

            // 2. Determine which half of the array is properly sorted
            
            // Case A: Left half is sorted (from left to mid)
            if (nums[left] <= nums[mid]) {
                
                // Check if the target is within the sorted left range
                if (target >= nums[left] && target < nums[mid]) {
                    // Target is in the left sorted portion, discard right half
                    right = mid - 1;
                } else {
                    // Target is not in the left sorted portion, must be in the right unsorted portion
                    left = mid + 1;
                }
            } 
            // Case B: Right half is sorted (from mid to right)
            else { 
                
                // Check if the target is within the sorted right range
                if (target > nums[mid] && target <= nums[right]) {
                    // Target is in the right sorted portion, discard left half
                    left = mid + 1;
                } else {
                    // Target is not in the right sorted portion, must be in the left unsorted portion
                    right = mid - 1;
                }
            }
        }

        // If the loop completes, the target was not found.
        return -1;
    }
}
```

### 🌍 Real-World Use Cases

  * **Cyclic Data Indexing:** Searching data stored on storage media that is indexed in a cyclic or wrap-around manner (e.g., flash memory block management).
  * **Load Balancing/Round Robin:** If a distributed system uses a circular/rotated ordering to assign tasks (like a round-robin scheduler), this algorithm quickly finds the server responsible for a specific job ID after the sequence has "wrapped."

-----

## 9\. 3Sum

### ❓ Problem Explanation

Given an integer array `nums`, find all unique triplets `[a, b, c]` such that $a + b + c = 0$.

The solution set must not contain duplicate triplets (e.g., `[-1, 0, 1]` is the same as `[0, -1, 1]`).

### 💡 Optimal Solution: Sorting + Two Pointers (O($n^2$) Time)

A brute-force $O(n^3)$ solution checks every triplet. We can improve this drastically using **Sorting** and the **Two-Pointer** technique.

1.  **Sort the array:** This allows us to easily skip duplicates and use the two-pointer technique effectively.
2.  **Iterate with the first pointer (a):** Iterate through the array with a main loop for the first element $a$.
      * **Skip Duplicates for 'a':** If `nums[i]` is the same as `nums[i-1]`, skip the iteration to avoid duplicate triplets.
3.  **Use Two Pointers (b and c):** Set the second pointer $b$ (`left`) to $i+1$ and the third pointer $c$ (`right`) to the end of the array.
4.  **Find the Sum:** Calculate the current sum: $S = \text{nums}[i] + \text{nums}[\text{left}] + \text{nums}[\text{right}]$.
      * If $S < 0$: We need a larger sum, so move `left++`.
      * If $S > 0$: We need a smaller sum, so move `right--`.
      * If $S = 0$: We found a triplet\! Store it, then move both pointers (`left++` and `right--`). **Crucially, skip duplicates** for both `left` and `right` pointers *after* finding a match.

### 👣 Step-by-Step Example

**Input:** `nums = [-1, 0, 1, 2, -1, -4]`

**Sorted `nums`:** `[-4, -1, -1, 0, 1, 2]`

| Outer Loop (a = `nums[i]`) | Pointers (L, R) | `nums[L]` | `nums[R]` | Sum | Action | Result Triplet |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1. a = -4** (i=0) | L=1, R=5 | -1 | 2 | $-4 - 1 + 2 = -3$ | Sum $< 0$: $L++$ | |
| | L=2, R=5 | -1 | 2 | $-4 - 1 + 2 = -3$ | Sum $< 0$: $L++$ | |
| | L=3, R=5 | 0 | 2 | $-4 + 0 + 2 = -2$ | Sum $< 0$: $L++$ | |
| | L=4, R=5 | 1 | 2 | $-4 + 1 + 2 = -1$ | Sum $< 0$: $L++$ | |
| | L=5, R=5 | - | - | - | $L \not< R$: Stop | |
| **2. a = -1** (i=1) | L=2, R=5 | -1 | 2 | $-1 - 1 + 2 = \mathbf{0}$ | **Sum = 0**: Store, $L++, R--$. | **[-1, -1, 2]** |
| | $L=3, R=4$ | 0 | 1 | $-1 + 0 + 1 = \mathbf{0}$ | **Sum = 0**: Store, $L++, R--$. | **[-1, 0, 1]** |
| | $L=4, R=3$ | - | - | - | $L \not< R$: Stop | |
| **3. a = -1** (i=2) | **Skip**: `nums[2] == nums[1]`. $i++$ | | | | | |
| **4. a = 0** (i=3) | L=4, R=5 | 1 | 2 | $0 + 1 + 2 = 3$ | Sum $> 0$: $R--$ | |
| | L=4, R=4 | - | - | - | $L \not< R$: Stop | |

**Final Unique Triplets:** `[[-1, -1, 2], [-1, 0, 1]]`

### 💻 Java Solution

```java
class Solution {
    /**
     * Finds all unique triplets in the array that sum up to zero.
     * Uses a Sorting + Two-Pointer approach (O(n^2)).
     * @param nums The input array.
     * @return A list of lists, where each inner list is a unique triplet.
     */
    public List<List<Integer>> threeSum(int[] nums) {
        // 1. Sort the array. This is O(n log n) and is crucial for Two Pointers.
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();
        int n = nums.length;

        // Iterate through the array with the first pointer (i). 
        // We only need to go up to n-2 since we need two other elements (left, right).
        for (int i = 0; i < n - 2; i++) {
            
            // SKIP DUPLICATES for the first element 'a'. 
            // If the current element is the same as the previous one, 
            // any triplets found will be identical, so we skip it.
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }

            // Target for the remaining two elements (b + c) is -nums[i].
            int target = -nums[i];
            
            // 2. Initialize Two Pointers for the rest of the array.
            int left = i + 1;
            int right = n - 1;

            while (left < right) {
                int sum = nums[left] + nums[right];

                if (sum == target) {
                    // Found a triplet! Store the result.
                    result.add(Arrays.asList(nums[i], nums[left], nums[right]));

                    // Move pointers inward.
                    left++;
                    right--;
                    
                    // Crucial: Skip duplicates for 'b' (left) and 'c' (right)
                    // We need to keep moving 'left' as long as it equals the number just used.
                    while (left < right && nums[left] == nums[left - 1]) {
                        left++;
                    }
                    // Same for 'right'.
                    while (left < right && nums[right] == nums[right + 1]) {
                        right--;
                    }
                } else if (sum < target) {
                    // Sum is too small, need a larger number, move 'left' pointer right.
                    left++;
                } else { // sum > target
                    // Sum is too large, need a smaller number, move 'right' pointer left.
                    right--;
                }
            }
        }

        return result;
    }
}
```

### 🌍 Real-World Use Cases

  * **Financial Risk Modeling:** Identifying combinations of three asset returns (or three risk factors) that collectively result in a net-zero return, which is essential for hedging or complex portfolio balancing.
  * **Physics/Engineering Constraints:** Finding three input variables (like forces, voltages, or chemical concentrations) in a system that must sum to zero to maintain a state of equilibrium or balance.
  * **Data Mining/Association Rules:** A more general pattern of finding combinations of items (triplets) in transactional data that satisfy a specific numerical property (though often extended to $k$-sum problems).

-----

## 10\. Container With Most Water

### ❓ Problem Explanation

You are given an integer array `height` of length $n$. Imagine $n$ vertical lines are drawn such that the two endpoints of line $i$ are $(i, 0)$ and $(i, \text{height}[i])$. Find two lines that, together with the x-axis, form a container that holds the most water. Return the maximum amount of water the container can store.

The area of a container is calculated by:
$$\text{Area} = \text{width} \times \text{height}$$
where $\text{width} = \text{right index} - \text{left index}$ and $\text{height} = \min(\text{height}[left], \text{height}[right])$.

### 💡 Optimal Solution: Two Pointers (O(n) Time)

A brute-force $O(n^2)$ approach checks every pair. The optimal solution uses a **Two-Pointer** technique for $O(n)$ time.

1.  Initialize pointers `left` (start) and `right` (end) of the array.
2.  The maximum possible width is achieved initially (`right - left`).
3.  Calculate the area for the current pair: $\text{Area} = (\text{right} - \text{left}) \times \min(\text{height}[left], \text{height}[right])$.
4.  The key insight: To find a potentially larger area, we must try to increase the limiting height.
      * If $\text{height}[left] < \text{height}[right]$: The height is limited by the left line. We move the **left pointer inward** (`left++`), hoping to find a taller left line that can hold more water.
      * If $\text{height}[left] \ge \text{height}[right]$: The height is limited by the right line. We move the **right pointer inward** (`right--`).

By always moving the pointer corresponding to the **shorter line**, we prioritize gaining height, even though we are losing width.

### 👣 Step-by-Step Example

**Input:** `height = [1, 8, 6, 2, 5, 4, 8, 3, 7]`

| L | R | `height[L]` | `height[R]` | Min Height | Width (R - L) | Area | Max Area | Action |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **0** | **8** | 1 | 7 | 1 | 8 | $1 \times 8 = 8$ | 8 | $H[L] < H[R]$: $L++$ |
| **1** | 8 | 8 | 7 | 7 | 7 | $7 \times 7 = 49$ | 49 | $H[L] > H[R]$: $R--$ |
| 1 | **7** | 8 | 3 | 3 | 6 | $3 \times 6 = 18$ | 49 | $H[L] > H[R]$: $R--$ |
| 1 | **6** | 8 | 8 | 8 | 5 | $8 \times 5 = 40$ | 49 | $H[L] = H[R]$: $L++$ (or $R--$) |
| **2** | 6 | 6 | 8 | 6 | 4 | $6 \times 4 = 24$ | 49 | $H[L] < H[R]$: $L++$ |
| **3** | 6 | 2 | 8 | 2 | 3 | $2 \times 3 = 6$ | 49 | $H[L] < H[R]$: $L++$ |
| **4** | 6 | 5 | 8 | 5 | 2 | $5 \times 2 = 10$ | 49 | $H[L] < H[R]$: $L++$ |
| **5** | 6 | 4 | 8 | 4 | 1 | $4 \times 1 = 4$ | 49 | $L$ stops when $L \ge R$ |

**Final Max Area: 49**

### 💻 Java Solution

```java
class Solution {
    /**
     * Finds the maximum area of water that can be contained between two lines.
     * Uses the Two-Pointer technique (O(n)).
     * @param height The array representing the height of the vertical lines.
     * @return The maximum area of water.
     */
    public int maxArea(int[] height) {
        // Initialize two pointers: one at the start and one at the end.
        int left = 0;
        int right = height.length - 1;
        
        // Stores the largest area found so far.
        int maxArea = 0;

        // The loop runs as long as the left pointer is to the left of the right pointer.
        while (left < right) {
            
            // The height of the container is limited by the shorter line.
            int minHeight = Math.min(height[left], height[right]);
            
            // The width is the distance between the two lines.
            int width = right - left;
            
            // Calculate the current area and update the overall max.
            int currentArea = minHeight * width;
            maxArea = Math.max(maxArea, currentArea);

            // Move the pointer that points to the shorter line inward.
            // This is the optimization: moving the taller line inward guarantees a loss of width, 
            // and no gain in height (it's still limited by the shorter line). 
            // We must move the shorter line to potentially gain height.
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }

        return maxArea;
    }
}
```

### 🌍 Real-World Use Cases

  * **Logistics/Warehouse Design:** Optimizing the spacing and dimensions of storage racks or silos (represented by heights) to maximize the overall storage capacity (area/volume) given physical constraints.
  * **Hydraulics/Irrigation:** A very simplified model for maximizing the flow or capacity of a channel formed by two walls of varying height.

-----

Yes, let's cover the final problem on your list: **Longest Consecutive Sequence**. This is a great problem that fully leverages the power of **Hashing**.

-----

## 11\. Longest Consecutive Sequence

### ❓ Problem Explanation

Given an unsorted array of integers `nums`, the goal is to find the length of the **longest consecutive elements sequence**.

A consecutive sequence means the numbers follow each other in order, like `[100, 101, 102]`. The elements in the input array don't have to be sorted to form this sequence.

The challenge is to solve this in $O(n)$ time complexity.

### 💡 Optimal Solution: Using a Hash Set (O(n) Time)

A brute-force solution would be $O(n \log n)$ (by sorting) or $O(n^3)$ (checking every pair). To achieve $O(n)$ time complexity, we must use a **Hash Set** (`HashSet` in Java) for $O(1)$ average time lookups.

1.  **Populate the Hash Set:** Put all numbers from the input array `nums` into a `HashSet`. This provides fast $O(1)$ lookups.
2.  **Iterate and Check for Starts:** We iterate through the original array (or the set). For each number $x$, we check if it is the **start** of a consecutive sequence.
      * A number $x$ is the **start** of a sequence if $x - 1$ is **NOT** present in the set.
3.  **Count the Sequence:** If $x$ is the start:
      * We initialize the `currentLength` to 1.
      * We start checking for $x+1$, $x+2$, $x+3$, and so on, as long as these numbers exist in the set.
      * For every number found, we increment the `currentLength`.
4.  **Update Maximum:** We compare the `currentLength` with the overall `longestSequence` found so far and update it.

This process ensures that every number is visited and processed as the *start* of a sequence **only once**, leading to $O(n)$ total time.

### 👣 Step-by-Step Example

**Input:** `nums = [100, 4, 200, 1, 3, 2]`

**1. Populate Hash Set:** `numSet = {100, 4, 200, 1, 3, 2}`

| Number ($x$) | `x - 1` in Set? | Start of Sequence? | Sequence Count | New Longest |
| :---: | :---: | :---: | :---: | :---: |
| **100** | No (99 not in set) | **YES** | Check $101, 102, \dots$: None found. Length = 1. | 1 |
| **4** | No (3 is in set, but we skip checking 4 until we find 3's start) | **SKIP** (This is the crucial step - 3 is not the start, so we wait for its start, which is 1) | | |
| **200** | No (199 not in set) | **YES** | Check $201, 202, \dots$: None found. Length = 1. | 1 |
| **1** | No (0 not in set) | **YES** | \* Start with 1. Length = 1. \* Check 2 (Found). Length = 2. \* Check 3 (Found). Length = 3. \* Check 4 (Found). Length = 4. \* Check 5 (Not Found). | 4 |
| **3** | Yes (2 is in set) | NO | Skip (3 will be counted when we process 1) | 4 |
| **2** | Yes (1 is in set) | NO | Skip (2 will be counted when we process 1) | 4 |

**Final Longest Consecutive Sequence Length: 4** (The sequence is `[1, 2, 3, 4]`)

### 💻 Java Solution

```java
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

class Solution {
    /**
     * Finds the length of the longest consecutive elements sequence.
     * Uses a HashSet and a smart check for sequence starts to achieve O(n) time complexity.
     * @param nums The input array of integers.
     * @return The length of the longest consecutive sequence.
     */
    public int longestConsecutive(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        // 1. Store all numbers in a HashSet for O(1) average time lookups.
        Set<Integer> numSet = new HashSet<>();
        for (int num : nums) {
            numSet.add(num);
        }

        int longestSequence = 0;

        // 2. Iterate through the set (or the original array) to check for sequence starts.
        for (int num : numSet) {
            
            // Check for the "start" of a sequence. 
            // 'num' is the start if 'num - 1' is NOT in the set.
            // This is the key O(n) optimization: we only start counting 
            // a sequence once, at its smallest element.
            if (!numSet.contains(num - 1)) {
                
                int currentNum = num;
                int currentLength = 1;

                // 3. Count the sequence: Check for num+1, num+2, etc.
                // This 'while' loop only executes a total of N times across all sequences combined.
                while (numSet.contains(currentNum + 1)) {
                    currentNum++;
                    currentLength++;
                }

                // 4. Update the overall maximum length found.
                longestSequence = Math.max(longestSequence, currentLength);
            }
        }

        return longestSequence;
    }
}
```

### 🌍 Real-World Use Cases

  * **Data Stream Analysis:** Quickly identifying the longest continuous stretch of sequential data points (like sensor readings, stock ticks, or server logs) to detect stability, continuous activity, or data completeness.
  * **Database/Index Fragmentation:** Analyzing the keys in a database index to find the longest contiguous block of used keys, which can indicate efficient storage or lack of fragmentation.
  * **Resource Allocation:** In operating systems or memory management, determining the largest available continuous block of resource IDs or memory addresses to fulfill a large allocation request.

-----