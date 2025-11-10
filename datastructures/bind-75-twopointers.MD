awesome—let’s tackle the **Two Pointers** technique with five classic problems. for each one, you’ll get:

* a plain-english problem statement
* why/when it’s useful (real-world analogies)
* the core idea (how the two pointers move)
* a tiny, step-by-step walk-through
* clean **Java** code with kid-friendly comments
* time/space complexity + common pitfalls

---

# 1) Valid Palindrome

**Problem**
Given a string, determine if it reads the same forward and backward **after** removing non-alphanumeric characters and ignoring case.
Example: `"A man, a plan, a canal: Panama"` → `true`

**Real use case**

* Normalizing user input (e.g., usernames, license plates) and checking symmetry/patterns.
* Text sanitation followed by checks (e.g., filtering, validations).

**Two-pointer idea**
Use `left` at start and `right` at end. Move inward:

* Skip non-letters/digits.
* Compare lowercase chars.
* If any mismatch → not palindrome.

**Step-by-step** (for `"Ab-:b a"` → alphanumerics are `abb a` → `"abba"`)

1. `l=0 (a)` vs `r=3 (a)` ✔
2. `l=1 (b)` vs `r=2 (b)` ✔
3. cross over → all matched → palindrome.

**Java (reader-friendly):**

```java
class ValidPalindrome {
    public boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;

        while (left < right) {
            // Move left pointer to next letter/digit
            while (left < right && !Character.isLetterOrDigit(s.charAt(left))) left++;
            // Move right pointer to previous letter/digit
            while (left < right && !Character.isLetterOrDigit(s.charAt(right))) right--;

            // Normalize to lowercase for case-insensitive compare
            char L = Character.toLowerCase(s.charAt(left));
            char R = Character.toLowerCase(s.charAt(right));

            if (L != R) return false; // mismatch → not palindrome

            left++;   // shrink from left
            right--;  // shrink from right
        }
        return true; // all pairs matched
    }
}
```

**Complexity**: `O(n)` time, `O(1)` space
**Pitfalls**: forgetting to skip non-alphanumerics; not lowercasing.

---

# 2) 3Sum

**Problem**
Given an integer array `nums`, find **all unique triplets** `(i, j, k)` such that `nums[i] + nums[j] + nums[k] == 0`.
Return no duplicates.

**Real use case**

* Financial risk checks: find three transactions that offset to net zero.
* Constraint satisfaction where triple values must balance.

**Two-pointer idea (after sort!)**

1. Sort the array.
2. For each index `i`, use two pointers on the subarray `(i+1..end)`:

   * `left` and `right`
   * If sum `< 0` → move `left++` (need bigger number)
   * If sum `> 0` → move `right--` (need smaller number)
   * If sum `== 0` → record and skip duplicates.

**Step-by-step** (for `[-1,0,1,2,-1,-4]` → sorted `[-4,-1,-1,0,1,2]`)

* `i=0 (-4)`: look for `+4` → pair scan finds none
* `i=1 (-1)`: need `+1` → `left=2(-1)`, `right=5(2)` → sum `0` → record `[-1,-1,2]`; skip dups; continue → next find `[-1,0,1]`
* `i=2 (-1)` duplicate of `i=1` → skip
* done.

**Java:**

```java
import java.util.*;

class ThreeSum {
    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums); // two pointers need order
        List<List<Integer>> res = new ArrayList<>();

        for (int i = 0; i < nums.length; i++) {
            // Skip duplicate 'first' values to avoid duplicate triplets
            if (i > 0 && nums[i] == nums[i - 1]) continue;

            int left = i + 1, right = nums.length - 1;
            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];

                if (sum == 0) {
                    res.add(Arrays.asList(nums[i], nums[left], nums[right]));

                    // Move left and right to next *distinct* values
                    int L = nums[left], R = nums[right];
                    while (left < right && nums[left] == L) left++;
                    while (left < right && nums[right] == R) right--;

                } else if (sum < 0) {
                    left++;  // need larger total → move left forward
                } else {
                    right--; // need smaller total → move right backward
                }
            }
        }
        return res;
    }
}
```

**Complexity**: Sorting `O(n log n)` + two-pointer scan per `i` → total `O(n^2)`
**Pitfalls**: not skipping duplicates; forgetting to sort.

---

# 3) Container With Most Water

**Problem**
Given an array `height` where each value is a vertical line at index `i`, choose two lines to form a container with the **max water area**.
Area = `min(height[i], height[j]) * (j - i)`.

**Real use case**

* Choosing two boundaries to maximize capacity (e.g., placing two walls or selecting two servers time-slots to maximize throughput window).
* General “maximize product of distance and minimum capacity” pattern.

**Two-pointer idea**
Start at extremes (`left=0`, `right=n-1`).

* Compute area.
* Move the **shorter** line inward (only moving the shorter line can potentially increase min-height).

**Step-by-step** (`[1,8,6,2,5,4,8,3,7]`)

* `l=0,h=1` & `r=8,h=7` → area `1*8=8`, move `l` (shorter)
* `l=1,h=8` & `r=8,h=7` → area `7*7=49` (best so far)
* Move `r` (shorter=7) to try taller right… continue until pointers meet. Best stays `49`.

**Java:**

```java
class ContainerWithMostWater {
    public int maxArea(int[] height) {
        int left = 0, right = height.length - 1;
        int best = 0;

        while (left < right) {
            // Width between lines
            int width = right - left;
            // Effective height is limited by the shorter line
            int h = Math.min(height[left], height[right]);
            best = Math.max(best, h * width);

            // Move the shorter side inward to *try* to find a taller boundary
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }
        return best;
    }
}
```

**Complexity**: `O(n)` time, `O(1)` space
**Pitfalls**: moving the taller line is fine when equal heights; the rule is “move the shorter (or either if equal).”

---

# 4) Trapping Rain Water

**Problem**
Given elevation heights, compute how much water is trapped after raining.
Water above index `i` = `min(maxLeft[i], maxRight[i]) - height[i]`, if positive.

**Real use case**

* Capacity planning: “valleys” in resource graphs can hold surplus.
* City planning / drainage simulations (simplified).
* Histogram cavities in image processing.

**Two-pointer idea (space-optimized)**
Maintain two running maxima while sweeping from both ends:

* If `leftMax < rightMax`, water at `left` is determined by `leftMax`. Move `left++`.
* Else, water at `right` is determined by `rightMax`. Move `right--`.

**Step-by-step** (`[0,1,0,2,1,0,1,3,2,1,2,1]`)

* Start `l=0,r=11,leftMax=0,rightMax=1`
* Since `leftMax < rightMax`, water at `l` is `0-0=0`; update `leftMax` and move `l`.
* Repeat: when on `i=2` with `leftMax=1`: water `1-0=1` stored.
* Continue until pointers meet; total = `6`.

**Java:**

```java
class TrappingRainWater {
    public int trap(int[] height) {
        int left = 0, right = height.length - 1;
        int leftMax = 0, rightMax = 0, water = 0;

        while (left < right) {
            // Update running maximums seen so far
            leftMax = Math.max(leftMax, height[left]);
            rightMax = Math.max(rightMax, height[right]);

            // The smaller side limits water now
            if (leftMax <= rightMax) {
                // Water above 'left' is bounded by leftMax (since right side is higher)
                water += leftMax - height[left];
                left++;
            } else {
                // Water above 'right' is bounded by rightMax
                water += rightMax - height[right];
                right--;
            }
        }
        return water;
    }
}
```

**Complexity**: `O(n)` time, `O(1)` space
**Pitfalls**: trying to compute water with local neighbors instead of global max; off-by-one accumulation.

---

# 5) Two Sum II – Input Array Is Sorted

**Problem**
Given a **sorted** array and a `target`, return indices (often 1-based in LeetCode) of two numbers that add up to `target`. Exactly one solution exists.

**Real use case**

* Price matching: find two prices that hit a gift-card value.
* Pairing resources to meet exact budget/limit when items are sorted.

**Two-pointer idea**
Because it’s sorted, use `left` at start and `right` at end:

* If `nums[left] + nums[right] < target` → need bigger sum → `left++`
* If `> target` → need smaller sum → `right--`
* If `== target` → found!

**Step-by-step** (`[2,7,11,15], target=9`)

* `l=0(2), r=1(7)` → `2+7=9` → return `(1,2)` (1-based)

**Java:**

```java
class TwoSumII {
    public int[] twoSum(int[] numbers, int target) {
        int left = 0, right = numbers.length - 1;

        while (left < right) {
            int sum = numbers[left] + numbers[right];

            if (sum == target) {
                // LeetCode expects 1-based indices
                return new int[]{left + 1, right + 1};
            } else if (sum < target) {
                left++;   // need a larger sum
            } else {
                right--;  // need a smaller sum
            }
        }
        // Problem guarantees exactly one answer; this is just a safety net.
        return new int[]{-1, -1};
    }
}
```

**Complexity**: `O(n)` time, `O(1)` space
**Pitfalls**: forgetting that the array is already sorted (don’t sort again and lose indices).

---

## Quick Comparison & When to Use Which

* **Valid Palindrome** → clean/compare from both ends while skipping noise → **string sanitization**.
* **3Sum** → sort + inner two pointers to fix triplets → **balancing triples**.
* **Container With Most Water** → extremes inward; move **shorter** side → **maximize capacity with two verticals**.
* **Trapping Rain Water** → two pointers with running maxima → **sum of valley capacities**.
* **Two Sum II** → sorted pair search with inward scan → **exact budget match**.

---

perfect 👏 — let’s now move to the **next tier of Two-Pointer (and Sliding Window) classics**, which appear in 90% of interviews and teach you *how to control two pointers dynamically*.

you’ll learn each problem with:

* ✅ easy problem statement
* 🎯 real-life analogy or use case
* 🧠 how the two pointers move step-by-step
* 💻 clean java code with college-level comments
* ⚙️ time/space complexity

---

# 6️⃣ Remove Duplicates from Sorted Array

---

### 🧩 Problem

Given a **sorted array**, remove duplicates **in-place** so that each element appears only once, and return the new length.
Don’t use extra array space.

**Example:**
`nums = [1,1,2,2,3]` → output `3` (`nums` becomes `[1,2,3,...]`)

---

### 🎯 Real-life use case

* Removing duplicate IDs in a sorted user list.
* Compressing logs where repeated consecutive entries occur.

---

### 🧠 Core Idea

* Keep two pointers:

  * `slow` → last unique element position
  * `fast` → scans for next unique
* When `nums[fast] != nums[slow]`, move `slow` one step and copy that unique value forward.

---

### 🔍 Step-by-Step

`[1,1,2,2,3]`
1️⃣ `slow=0, fast=1` → `nums[fast]==nums[slow]` → skip
2️⃣ `fast=2` → new unique (2) found → move slow to 1 → copy `nums[1]=2`
3️⃣ `fast=3` skip (duplicate)
4️⃣ `fast=4` new unique (3) → move slow to 2 → copy `nums[2]=3`
✅ Done, unique length = `slow+1 = 3`

---

### 💻 Java Code

```java
class RemoveDuplicatesSortedArray {
    public int removeDuplicates(int[] nums) {
        if (nums.length == 0) return 0;

        int slow = 0; // points to last unique element
        for (int fast = 1; fast < nums.length; fast++) {
            if (nums[fast] != nums[slow]) {
                slow++;                // found new unique element
                nums[slow] = nums[fast]; // move it forward
            }
        }
        return slow + 1; // number of unique elements
    }
}
```

### ⚙️ Complexity

⏱ `O(n)`
💾 `O(1)`

---

# 7️⃣ Move Zeroes

---

### 🧩 Problem

Move all `0`s to the end while maintaining the order of non-zero elements.

**Example:**
`[0,1,0,3,12]` → `[1,3,12,0,0]`

---

### 🎯 Real-life use case

* Reordering data for compression (move “empty slots” to end).
* Cleaning up sparse arrays or null entries in memory blocks.

---

### 🧠 Core Idea

Two pointers:

* `slow` keeps track of where the next **non-zero** should go.
* `fast` scans all elements.

Each time you find non-zero → swap with `slow` → move `slow++`.

---

### 🔍 Step-by-Step

`[0,1,0,3,12]`
1️⃣ `fast=0` (0) → skip
2️⃣ `fast=1` (1) → swap with slow=0 → `[1,0,0,3,12]` → slow=1
3️⃣ `fast=3` (3) → swap with slow=1 → `[1,3,0,0,12]` → slow=2
4️⃣ `fast=4` (12) → swap with slow=2 → `[1,3,12,0,0]`

---

### 💻 Java Code

```java
class MoveZeroes {
    public void moveZeroes(int[] nums) {
        int slow = 0; // position for next non-zero element
        for (int fast = 0; fast < nums.length; fast++) {
            if (nums[fast] != 0) {
                // Swap non-zero forward
                int temp = nums[slow];
                nums[slow] = nums[fast];
                nums[fast] = temp;
                slow++;
            }
        }
    }
}
```

### ⚙️ Complexity

⏱ `O(n)`
💾 `O(1)`

---

# 8️⃣ Longest Substring Without Repeating Characters

---

### 🧩 Problem

Given a string `s`, find the **length** of the longest substring without repeating characters.

**Example:**
`s = "abcabcbb"` → answer = 3 (`"abc"`)

---

### 🎯 Real-life use case

* Tracking longest sequence of unique user actions or keystrokes.
* Detecting non-repetitive signal patterns.

---

### 🧠 Core Idea (Sliding Window)

Use two pointers `left` and `right` to form a **window**.
Use a `Set` to store characters in current window.

When a duplicate appears → move `left` until duplicate is removed.
Each time, update `maxLen`.

---

### 🔍 Step-by-Step

`s = "abcabcbb"`

* start → window `"abc"` → max=3
* duplicate `'a'` at index 3 → shrink left until `'a'` removed → new window `"bca"`
* continue → next duplicate `'b'` shrink again → longest = 3

---

### 💻 Java Code

```java
import java.util.*;

class LongestSubstringNoRepeat {
    public int lengthOfLongestSubstring(String s) {
        Set<Character> seen = new HashSet<>();
        int left = 0, maxLen = 0;

        for (int right = 0; right < s.length(); right++) {
            // If duplicate found, shrink window from left
            while (seen.contains(s.charAt(right))) {
                seen.remove(s.charAt(left));
                left++;
            }
            // Expand window by adding current char
            seen.add(s.charAt(right));
            // Update maximum length found so far
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
    }
}
```

### ⚙️ Complexity

⏱ `O(n)` — each char added/removed once
💾 `O(k)` — where k = alphabet size (~128 for ASCII)

---

# 9️⃣ Remove Element

---

### 🧩 Problem

Given an array `nums` and a value `val`, remove all instances of that value in-place and return the new length.
Order may be changed.

**Example:**
`nums = [3,2,2,3], val=3` → output `2` (`[2,2,...]`)

---

### 🎯 Real-life use case

* Removing flagged items from a dataset without extra space.
* Cleaning unwanted IDs in logs.

---

### 🧠 Core Idea

Two pointers again:

* `slow` marks next place to keep non-val elements.
* `fast` scans the array.

---

### 🔍 Step-by-Step

`nums = [3,2,2,3], val=3`
1️⃣ `fast=0` skip (3)
2️⃣ `fast=1` keep 2 → copy to `slow=0`
3️⃣ `fast=2` keep 2 → copy to `slow=1`
✅ Result `[2,2,...]`, length=2

---

### 💻 Java Code

```java
class RemoveElement {
    public int removeElement(int[] nums, int val) {
        int slow = 0;
        for (int fast = 0; fast < nums.length; fast++) {
            if (nums[fast] != val) {
                nums[slow] = nums[fast];
                slow++;
            }
        }
        return slow; // number of kept elements
    }
}
```

### ⚙️ Complexity

⏱ `O(n)`
💾 `O(1)`

---

# 🔟 Sort Colors (a.k.a. Dutch National Flag)

---

### 🧩 Problem

Given an array with values `0, 1, 2` → sort **in-place** so all 0s first, then 1s, then 2s.
Do not use counting sort.

**Example:**
`[2,0,2,1,1,0]` → `[0,0,1,1,2,2]`

---

### 🎯 Real-life use case

* Sorting categorical data efficiently (e.g., status codes).
* Grouping small fixed categories in O(n) time.

---

### 🧠 Core Idea

Use **three pointers**:

* `low` = next position for 0
* `mid` = current element
* `high` = next position for 2

Algorithm:

* If `nums[mid] == 0` → swap with `low`, `low++`, `mid++`
* If `nums[mid] == 1` → just `mid++`
* If `nums[mid] == 2` → swap with `high`, `high--` (don’t mid++ yet!)

---

### 🔍 Step-by-Step

`[2,0,2,1,1,0]`

* swap `nums[0]` with `nums[5]` → `[0,0,2,1,1,2]`
* keep processing → result `[0,0,1,1,2,2]`

---

### 💻 Java Code

```java
class SortColors {
    public void sortColors(int[] nums) {
        int low = 0, mid = 0, high = nums.length - 1;

        while (mid <= high) {
            if (nums[mid] == 0) {
                // Move 0 to the left
                int temp = nums[low];
                nums[low] = nums[mid];
                nums[mid] = temp;
                low++;
                mid++;
            } else if (nums[mid] == 1) {
                mid++; // already in correct zone
            } else {
                // Move 2 to the right
                int temp = nums[high];
                nums[high] = nums[mid];
                nums[mid] = temp;
                high--;
            }
        }
    }
}
```

### ⚙️ Complexity

⏱ `O(n)`
💾 `O(1)`

---

## 🧩 Summary Table of Two-Pointer/Sliding Window Patterns

| #  | Problem                   | Structure    | Pointer Type      | Key Move Logic               | Time  | Real Analogy           |
| -- | ------------------------- | ------------ | ----------------- | ---------------------------- | ----- | ---------------------- |
| 1  | Valid Palindrome          | String       | Inward (ends)     | Compare and skip non-letters | O(n)  | Text symmetry check    |
| 2  | 3Sum                      | Array        | Inner pair scan   | Move based on sum sign       | O(n²) | Financial balancing    |
| 3  | Container With Most Water | Array        | Outward shrink    | Move shorter line            | O(n)  | Maximize capacity      |
| 4  | Trapping Rain Water       | Array        | Two ends + height | Move smaller max             | O(n)  | Drainage calc          |
| 5  | Two Sum II                | Sorted array | Outward shrink    | Move based on target         | O(n)  | Price pair match       |
| 6  | Remove Duplicates         | Sorted array | Forward scan      | Copy unique values           | O(n)  | Remove repeats         |
| 7  | Move Zeroes               | Array        | Forward scan      | Swap non-zeros               | O(n)  | Compact data           |
| 8  | Longest Substring         | String       | Sliding window    | Shrink until unique          | O(n)  | Longest unique actions |
| 9  | Remove Element            | Array        | Forward scan      | Skip target                  | O(n)  | Data cleanup           |
| 10 | Sort Colors               | Array        | 3-way partition   | Swap by value                | O(n)  | Group categorical data |

---