excellent 🚀 — now you’re stepping into one of the most fundamental and **most asked** interview families:
👉 **Binary Search Problems (5 classics)**

these problems teach how to “cut search space in half” efficiently — a *thinking pattern* used in **arrays, trees, and even optimization** problems (like minimizing time, maximizing value, etc.).

you’ll get again:
✅ clear explanation
🎯 real-world use case
🧠 binary search intuition
🔍 step-by-step example
💻 fully commented java code
⚙️ time/space complexity

---

# ⚡ BINARY SEARCH — THE CORE IDEA

“Divide and conquer.”
You repeatedly split your search range in half based on a **condition** until you find the target (or the range becomes invalid).

**Why it’s powerful:**

* Reduces `O(n)` → `O(log n)`
* Works on sorted data or *monotonic* conditions (like “is possible or not”)

---

# 1️⃣ Binary Search (Basic)

---

### 🧩 Problem

Given a **sorted array** and a `target`, return the index of the target.
If not found, return `-1`.

**Example:**
`nums = [-1,0,3,5,9,12], target = 9` → `4`

---

### 🎯 Real-life analogy

Finding a word in a **dictionary** or a contact in a **sorted phone list** by halving the search space each time.

---

### 🧠 Core Idea

Maintain two pointers:

* `left = 0`
* `right = n - 1`

Find mid:

```java
mid = left + (right - left) / 2
```

If `nums[mid] == target` → found
If `nums[mid] < target` → search right half
If `nums[mid] > target` → search left half

---

### 🔍 Step-by-Step

`nums = [-1,0,3,5,9,12], target=9`
1️⃣ `mid=(0+5)/2=2 → nums[2]=3<9` → search right
2️⃣ `mid=(3+5)/2=4 → nums[4]=9` ✅ found

---

### 💻 Java Code

```java
class BinarySearchBasic {
    public int search(int[] nums, int target) {
        int left = 0, right = nums.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (nums[mid] == target) return mid;       // found
            else if (nums[mid] < target) left = mid + 1; // search right
            else right = mid - 1;                        // search left
        }
        return -1; // not found
    }
}
```

### ⚙️ Complexity

⏱ `O(log n)`
💾 `O(1)`

---

# 2️⃣ Search in Rotated Sorted Array

---

### 🧩 Problem

You are given a sorted array that was rotated at some pivot.
Find the target’s index or return `-1`.

**Example:**
`nums = [4,5,6,7,0,1,2], target = 0` → `4`

---

### 🎯 Real-life analogy

You have a circularly sorted list (like a clock). You still want to find your item efficiently.

---

### 🧠 Core Idea

Even though array is rotated, **one side is always sorted.**
1️⃣ Check if left half is sorted.
2️⃣ If yes, check if target lies in it → move left; else → move right.
3️⃣ Else right half is sorted → apply same logic.

---

### 🔍 Step-by-Step

`[4,5,6,7,0,1,2], target=0`

* `mid=3 (7)`
  left=4..7 → sorted
  target(0) not in left → move right
* `mid=5 (1)`
  right=0..2 sorted, target in that → move left
* `mid=4 (0)` ✅ found

---

### 💻 Java Code

```java
class SearchRotatedSortedArray {
    public int search(int[] nums, int target) {
        int left = 0, right = nums.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (nums[mid] == target) return mid;

            // left half is sorted
            if (nums[left] <= nums[mid]) {
                if (nums[left] <= target && target < nums[mid])
                    right = mid - 1; // target in left half
                else
                    left = mid + 1;  // search right half
            }
            // right half is sorted
            else {
                if (nums[mid] < target && target <= nums[right])
                    left = mid + 1;
                else
                    right = mid - 1;
            }
        }
        return -1;
    }
}
```

### ⚙️ Complexity

⏱ `O(log n)`
💾 `O(1)`

---

# 3️⃣ Find Minimum in Rotated Sorted Array

---

### 🧩 Problem

Given a rotated sorted array with **unique** elements, find the **minimum element**.

**Example:**
`nums = [3,4,5,1,2]` → min = `1`

---

### 🎯 Real-life analogy

Think of the array as a circular list — finding where the rotation “starts”.

---

### 🧠 Core Idea

At least **one half is sorted** —
The smallest element is where rotation happens.
We find that point using binary search.

If `nums[mid] > nums[right]` → min is in right half.
Else → min in left half.

---

### 🔍 Step-by-Step

`[3,4,5,1,2]`
1️⃣ `mid=2 (5)` → 5>2 → go right
2️⃣ `mid=3 (1)` → 1<2 → go left → found min = 1

---

### 💻 Java Code

```java
class FindMinRotatedSortedArray {
    public int findMin(int[] nums) {
        int left = 0, right = nums.length - 1;

        while (left < right) {
            int mid = left + (right - left) / 2;

            if (nums[mid] > nums[right]) {
                left = mid + 1; // min on right
            } else {
                right = mid;    // min could be mid or to the left
            }
        }
        return nums[left];
    }
}
```

### ⚙️ Complexity

⏱ `O(log n)`
💾 `O(1)`

---

# 4️⃣ Search for Range (Find First and Last Position of Element in Sorted Array)

---

### 🧩 Problem

Given a sorted array and a target, find the **first and last position** of the target.

**Example:**
`nums = [5,7,7,8,8,10], target=8` → `[3,4]`

---

### 🎯 Real-life analogy

In logs, find **start and end index** where an event occurs.

---

### 🧠 Core Idea

Use **binary search twice**:

* One for **first occurrence** (when target found, move left).
* One for **last occurrence** (when target found, move right).

---

### 💻 Java Code

```java
class SearchRange {
    public int[] searchRange(int[] nums, int target) {
        return new int[]{findBound(nums, target, true), findBound(nums, target, false)};
    }

    private int findBound(int[] nums, int target, boolean first) {
        int left = 0, right = nums.length - 1, ans = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (nums[mid] == target) {
                ans = mid;
                if (first) right = mid - 1; // look on left
                else left = mid + 1;        // look on right
            } else if (nums[mid] < target)
                left = mid + 1;
            else
                right = mid - 1;
        }
        return ans;
    }
}
```

### ⚙️ Complexity

⏱ `O(log n)`
💾 `O(1)`

---

# 5️⃣ Peak Element (Find Any Peak)

---

### 🧩 Problem

A **peak element** is one that’s greater than its neighbors.
Find one peak’s index (any one).

**Example:**
`nums = [1,2,1,3,5,6,4]` → output can be `1` or `5` (both peaks)

---

### 🎯 Real-life analogy

Finding the **highest point in a terrain** or stock chart locally.

---

### 🧠 Core Idea

We can apply binary search:
If `nums[mid] < nums[mid+1]`, peak is on right.
Else, peak is on left (including mid).

---

### 🔍 Step-by-Step

`[1,2,1,3,5,6,4]`
1️⃣ `mid=3(3)<5` → go right
2️⃣ `mid=5(6)>4` → found peak = index 5

---

### 💻 Java Code

```java
class FindPeakElement {
    public int findPeakElement(int[] nums) {
        int left = 0, right = nums.length - 1;

        while (left < right) {
            int mid = left + (right - left) / 2;

            if (nums[mid] < nums[mid + 1]) {
                left = mid + 1; // peak lies to right
            } else {
                right = mid; // peak lies to left (or is mid)
            }
        }
        return left;
    }
}
```

### ⚙️ Complexity

⏱ `O(log n)`
💾 `O(1)`

---

## 🧩 Summary Table — Binary Search Problems

| # | Problem                 | Pattern       | Key Condition            | Result        | Time     | Analogy                 |
| - | ----------------------- | ------------- | ------------------------ | ------------- | -------- | ----------------------- |
| 1 | Binary Search           | Classic       | mid compare target       | find index    | O(log n) | find word in dictionary |
| 2 | Search in Rotated Array | Modified      | one half sorted          | target index  | O(log n) | rotated clock search    |
| 3 | Find Minimum Rotated    | Modified      | compare mid vs right     | min value     | O(log n) | start of rotation       |
| 4 | Search Range            | Double Search | find left & right bounds | [start, end]  | O(log n) | log intervals           |
| 5 | Peak Element            | Modified      | mid vs mid+1             | index of peak | O(log n) | mountain peak           |

---

### 🌟 Key Takeaways

* Binary Search is not just about numbers — it’s about **conditions that are monotonic** (true on one side, false on other).
* You can modify it to find **minimum**, **maximum**, **boundary**, or **feasible value**.
* You’ll reuse this technique in advanced problems like **Koko Eating Bananas**, **Capacity to Ship Packages**, and even **Minimum Time to Complete Jobs**.

---

