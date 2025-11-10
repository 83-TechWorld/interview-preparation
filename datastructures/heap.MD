awesome ⚙️ — you’ve mastered trees and tries, and now we’re stepping into another powerful structure used everywhere from **search engines** to **task schedulers**:

# 🚀 **Heap / Priority Queue (4 Classic Problems)**

you’ll learn:
✅ what a heap really is
✅ when & why to use it
✅ java implementation details (`PriorityQueue`)
✅ step-by-step debugging flow
✅ full commented java code

---

## 🧱 What Is a Heap (Priority Queue)?

A **Heap** is a **complete binary tree** that satisfies the **heap property**:

* **Min-Heap** → every parent ≤ its children (smallest at root)
* **Max-Heap** → every parent ≥ its children (largest at root)

In Java, this is implemented using the **`PriorityQueue`** class.

### 💡 Default Behavior:

```java
PriorityQueue<Integer> pq = new PriorityQueue<>(); // min-heap
PriorityQueue<Integer> maxPQ = new PriorityQueue<>(Collections.reverseOrder()); // max-heap
```

| Operation          | Time Complexity | Description     |
| ------------------ | --------------- | --------------- |
| insert (offer/add) | O(log n)        | Add new element |
| remove top (poll)  | O(log n)        | Remove min/max  |
| peek               | O(1)            | View min/max    |

---

# 🧩 1️⃣ Kth Largest Element in an Array

### 🧩 Problem

Find the **k-th largest** element in an unsorted array.

**Example:**
`nums = [3,2,1,5,6,4], k=2` → **5**

---

### 🎯 Real-life analogy

Leaderboard ranking → find 2nd highest score efficiently.

---

### 🧠 Core Idea

Maintain a **min-heap** of size `k`:

* push elements
* if heap size > k → pop smallest
* after all → top = kth largest

---

### 💻 Java Code

```java
import java.util.*;

class KthLargestElement {
    public int findKthLargest(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        for (int n : nums) {
            minHeap.offer(n);
            if (minHeap.size() > k) {
                minHeap.poll(); // remove smallest
            }
        }
        return minHeap.peek(); // kth largest
    }
}
```

---

### 🔍 Debug Step-by-Step

```
nums = [3,2,1,5,6,4], k=2
heap: [3]
heap: [2,3]
heap: [3] (removed 2)
heap: [3,5]
heap: [5,6]
heap: [5,6,4] → remove smallest → [5,6]
peek = 5 ✅
```

---

### ⚙️ Complexity

* Time: O(n log k)
* Space: O(k)

**Debug Tip:** print heap contents after each insertion.

---

# 🧩 2️⃣ Top K Frequent Elements

### 🧩 Problem

Given a list of integers, return the `k` most frequent elements.

**Example:**
`nums = [1,1,1,2,2,3], k = 2` → `[1,2]`

---

### 🎯 Real-life analogy

Track **most searched keywords** or **most used hashtags**.

---

### 🧠 Core Idea

1️⃣ Count frequency using a **HashMap**
2️⃣ Use a **min-heap** to keep top `k` frequent numbers

* heap ordered by frequency
* if size > k → remove smallest frequency

---

### 💻 Java Code

```java
import java.util.*;

class TopKFrequentElements {
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums)
            freq.put(n, freq.getOrDefault(n, 0) + 1);

        PriorityQueue<Map.Entry<Integer, Integer>> minHeap =
            new PriorityQueue<>((a, b) -> a.getValue() - b.getValue());

        for (Map.Entry<Integer, Integer> entry : freq.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > k) minHeap.poll();
        }

        int[] result = new int[k];
        for (int i = k - 1; i >= 0; i--)
            result[i] = minHeap.poll().getKey();

        return result;
    }
}
```

---

### 🔍 Debug Example

```
nums = [1,1,1,2,2,3], k=2
freq = {1:3, 2:2, 3:1}
heap after processing:
[1:3, 2:2] → remove smallest → keep top 2 → [1,2]
output = [1,2]
```

---

### ⚙️ Complexity

* Time: O(n log k)
* Space: O(n)

**Debug Tip:** log `(element, freq)` pairs in heap at each step.

---

# 🧩 3️⃣ Merge K Sorted Lists

### 🧩 Problem

Merge `k` sorted linked lists into one sorted list.

**Example:**

```
Input:
1→4→5,
1→3→4,
2→6
Output: 1→1→2→3→4→4→5→6
```

---

### 🧱 Data Structure

* **Min-Heap** → store smallest head among lists.

---

### 🧠 Core Idea

1️⃣ Push first node of each list into heap
2️⃣ Pop smallest node
3️⃣ Append to merged list
4️⃣ Push its `next` node into heap

---

### 💻 Java Code

```java
import java.util.*;

class MergeKSortedLists {
    public ListNode mergeKLists(ListNode[] lists) {
        PriorityQueue<ListNode> pq = new PriorityQueue<>(
            (a, b) -> a.val - b.val
        );

        for (ListNode node : lists)
            if (node != null) pq.offer(node);

        ListNode dummy = new ListNode(0), tail = dummy;

        while (!pq.isEmpty()) {
            ListNode node = pq.poll();
            tail.next = node;
            tail = tail.next;
            if (node.next != null) pq.offer(node.next);
        }

        return dummy.next;
    }
}
```

---

### 🔍 Debug Example

```
heap: [1(list1), 1(list2), 2(list3)]
poll → 1 → push next(4)
heap: [1,2,4]
poll → 1 → push next(3)
heap: [2,3,4]
poll → 2 → push next(6)
...
```

---

### ⚙️ Complexity

* Time: O(N log K) (N = total nodes)
* Space: O(K)

**Debug Tip:** print heap.peek() each iteration.

---

### 🎯 Real-world Analogy

* Merging multiple **sorted logs** or **database cursors** efficiently.
* File merging during **external sorting**.

---

# 🧩 4️⃣ Find Median from Data Stream

### 🧩 Problem

Design a data structure to **add numbers** and **find median** efficiently.

**Example:**

```
add(1)
add(2)
findMedian() → 1.5
add(3)
findMedian() → 2.0
```

---

### 🧱 Data Structure

Two Heaps:

* **Max-Heap** → left half (smaller numbers)
* **Min-Heap** → right half (larger numbers)

Balance both so that:

* sizes differ ≤ 1
* median = avg(peek(left), peek(right)) if even
* median = peek(left) if odd

---

### 🧠 Core Idea

Insert new number:

* if smaller → maxHeap
* else → minHeap
  Then **rebalance**.

---

### 💻 Java Code

```java
import java.util.*;

class MedianFinder {
    private PriorityQueue<Integer> leftMax; // smaller half
    private PriorityQueue<Integer> rightMin; // larger half

    public MedianFinder() {
        leftMax = new PriorityQueue<>(Collections.reverseOrder());
        rightMin = new PriorityQueue<>();
    }

    public void addNum(int num) {
        if (leftMax.isEmpty() || num <= leftMax.peek())
            leftMax.offer(num);
        else
            rightMin.offer(num);

        // Rebalance heaps
        if (leftMax.size() > rightMin.size() + 1)
            rightMin.offer(leftMax.poll());
        else if (rightMin.size() > leftMax.size())
            leftMax.offer(rightMin.poll());
    }

    public double findMedian() {
        if (leftMax.size() == rightMin.size())
            return (leftMax.peek() + rightMin.peek()) / 2.0;
        return leftMax.peek();
    }
}
```

---

### 🔍 Debug Example

```
add 1 → left=[1], right=[]
add 2 → left=[1], right=[2]
median = (1+2)/2=1.5
add 3 → left=[2,1], right=[3]
median = 2
```

---

### ⚙️ Complexity

* Add: O(log n)
* Median: O(1)
* Space: O(n)

**Debug Tip:** print both heap contents after each insertion.

---

## 🧩 Summary Table — Heap / Priority Queue Problems

| # | Problem                 | Data Structure     | Key Idea            | Time       | Real-world Analogy    |
| - | ----------------------- | ------------------ | ------------------- | ---------- | --------------------- |
| 1 | Kth Largest Element     | Min-Heap           | Keep top k largest  | O(n log k) | Leaderboard           |
| 2 | Top K Frequent Elements | Min-Heap + HashMap | Keep top freq       | O(n log k) | Trending hashtags     |
| 3 | Merge K Sorted Lists    | Min-Heap           | Merge smallest head | O(N log K) | Merging logs          |
| 4 | Find Median from Stream | Two Heaps          | Balance halves      | O(log n)   | Live median analytics |

---

## 🧭 Debugging Patterns

✅ Always print heap contents after insertion/deletion
✅ For two-heap median, check size balance after every `add()`
✅ Visualize as two “buckets”: smaller numbers (max-heap) | larger numbers (min-heap)

---

## 🎯 Real-World Use Cases

* **Kth Largest:** Leaderboards, streaming analytics
* **Top K Frequent:** Recommender systems, trending analysis
* **Merge K Lists:** External merge sort, multi-file data merging
* **Median from Stream:** Real-time dashboards, IoT sensor median smoothing

---
