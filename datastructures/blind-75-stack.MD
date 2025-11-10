awesome 🎯 — we’re now entering the **Stack** family of problems — a crucial group in interviews that tests your ability to use the **Last-In-First-Out (LIFO)** property to manage order, reversals, and structural validation.

you’ll again get 👇
✅ clear problem statement
🎯 real-life analogy or where it’s useful
🧠 idea behind using stack
🔍 step-by-step dry run
💻 clean java code with reader comments
⚙️ time & space complexity

---

# 🧱 STACK PATTERN OVERVIEW

A **stack** is like a pile of plates —
you **push()** a new plate on top and **pop()** from the top first.
This “last in, first out” behavior makes it perfect for:

* Undo/redo
* Validating nesting (like parentheses)
* Backtracking
* Maintaining “next greater” or “previous smaller” elements

---

# 1️⃣ Valid Parentheses

---

### 🧩 Problem

Given a string containing only `'('`, `')'`, `'{'`, `'}'`, `'['`, and `']'`, determine if the input string is **valid**.
A string is valid if:

* Open brackets are closed by the same type.
* Brackets are closed in the correct order.

**Example:**
`"()[]{}"` ✅ valid
`"(]"` ❌ invalid

---

### 🎯 Real-life analogy

* Checking whether HTML/XML tags open and close properly.
* Compiler parsing parentheses or braces in source code.

---

### 🧠 Core Idea

Use a **stack** to keep track of *opening brackets*.
When a *closing bracket* appears:

* Check if it matches the **top** of the stack.
* If not, invalid.

---

### 🔍 Step-by-Step Example

Input: `"{[()]}"`
1️⃣ push `{`
2️⃣ push `[`
3️⃣ push `(`
4️⃣ see `)` → top=`(` → pop ✅
5️⃣ see `]` → top=`[` → pop ✅
6️⃣ see `}` → top=`{` → pop ✅
stack empty → valid ✅

---

### 💻 Java Code

```java
import java.util.*;

class ValidParentheses {
    public boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        for (char c : s.toCharArray()) {
            // Push opening brackets
            if (c == '(' || c == '{' || c == '[') {
                stack.push(c);
            } 
            // Handle closing brackets
            else {
                if (stack.isEmpty()) return false; // nothing to match
                char top = stack.pop();
                // ensure correct match type
                if ((c == ')' && top != '(') ||
                    (c == '}' && top != '{') ||
                    (c == ']' && top != '[')) {
                    return false;
                }
            }
        }
        // if stack still has unclosed brackets
        return stack.isEmpty();
    }
}
```

### ⚙️ Complexity

⏱ `O(n)` time
💾 `O(n)` space (worst case: all openings)

---

# 2️⃣ Min Stack

---

### 🧩 Problem

Design a stack that supports:

* `push(x)`
* `pop()`
* `top()`
* `getMin()` → returns **minimum element** in O(1) time.

**Example:**

```
push(-2), push(0), push(-3)
getMin() → -3
pop()
top() → 0
getMin() → -2
```

---

### 🎯 Real-life analogy

* Track **min stock price** while adding/removing days’ data.
* Maintain minimum temperature readings efficiently.

---

### 🧠 Core Idea

Maintain **two stacks**:
1️⃣ `mainStack` — all elements
2️⃣ `minStack` — current minimum at each push

When pushing:

* push element to `mainStack`
* if `minStack` empty OR `x <= minStack.peek()` → also push to `minStack`

When popping:

* if popped element equals `minStack.peek()` → pop from both.

---

### 🔍 Step-by-Step

Push sequence: `[-2, 0, -3]`

```
mainStack: [-2, 0, -3]
minStack:  [-2, -3]
```

Pop → removes -3 from both → minStack top = -2 → `getMin() = -2`

---

### 💻 Java Code

```java
import java.util.Stack;

class MinStack {
    private Stack<Integer> stack;
    private Stack<Integer> minStack;

    public MinStack() {
        stack = new Stack<>();
        minStack = new Stack<>();
    }

    public void push(int val) {
        stack.push(val);
        // push to minStack if it's the smallest so far
        if (minStack.isEmpty() || val <= minStack.peek()) {
            minStack.push(val);
        }
    }

    public void pop() {
        int popped = stack.pop();
        // if popped value is current min, remove from minStack
        if (popped == minStack.peek()) {
            minStack.pop();
        }
    }

    public int top() {
        return stack.peek();
    }

    public int getMin() {
        return minStack.peek();
    }
}
```

### ⚙️ Complexity

⏱ `O(1)` for all operations
💾 `O(n)` extra space

---

# 3️⃣ Largest Rectangle in Histogram

---

### 🧩 Problem

Given an array `heights` representing histogram bar heights (width = 1),
find the **area of the largest rectangle** that can be formed.

**Example:**
`heights = [2,1,5,6,2,3]` → max area = **10** (from bars 5 and 6)

---

### 🎯 Real-life analogy

* Finding largest block of land between varying heights (e.g., skyline, elevation maps).
* Storage/warehouse optimization (max continuous storage area).

---

### 🧠 Core Idea

Use a **monotonic stack** — it stores **indices of increasing bar heights**.
Why?
When we find a shorter bar, we can calculate area for all taller bars that ended before this one.

Algorithm:
1️⃣ Traverse bars
2️⃣ Push index if current height ≥ top of stack
3️⃣ When smaller height found → pop from stack and compute area

```
height = height[pop]
width = current_index - previous_smaller_index - 1
```

4️⃣ Repeat till end (flush stack)

---

### 🔍 Step-by-Step

Input: `[2,1,5,6,2,3]`

| Step                                        | Action                          | Stack   | Computed Area |
| ------------------------------------------- | ------------------------------- | ------- | ------------- |
| i=0                                         | push(0)                         | [0]     | -             |
| i=1                                         | height[1]=1 < 2 → pop(0)        | []      | 2×1=2         |
| push(1)                                     | [1]                             |         |               |
| i=2                                         | push(2)                         | [1,2]   |               |
| i=3                                         | push(3)                         | [1,2,3] |               |
| i=4                                         | height[4]=2 < 6 → pop(3): 6×1=6 | [1,2]   |               |
| pop(2): 5×2=10 ✅                            | [1]                             |         |               |
| push(4)                                     | [1,4]                           |         |               |
| i=5                                         | push(5)                         | [1,4,5] |               |
| end → flush stack → compute remaining areas |                                 |         |               |

Largest area = **10**

---

### 💻 Java Code

```java
import java.util.Stack;

class LargestRectangleHistogram {
    public int largestRectangleArea(int[] heights) {
        Stack<Integer> stack = new Stack<>();
        int maxArea = 0;

        for (int i = 0; i <= heights.length; i++) {
            // Sentinel value: height = 0 at end to flush stack
            int currHeight = (i == heights.length) ? 0 : heights[i];

            // Maintain increasing stack
            while (!stack.isEmpty() && currHeight < heights[stack.peek()]) {
                int height = heights[stack.pop()];
                // width = current index - index after previous smaller
                int width = stack.isEmpty() ? i : i - stack.peek() - 1;
                maxArea = Math.max(maxArea, height * width);
            }
            stack.push(i);
        }
        return maxArea;
    }
}
```

---

### ⚙️ Complexity

⏱ `O(n)` (each bar pushed/popped once)
💾 `O(n)` stack space

---

## 🧩 Summary Table — Stack Problems

| # | Problem                        | Structure | Key Operation   | Core Logic                | Time | Real Analogy                   |
| - | ------------------------------ | --------- | --------------- | ------------------------- | ---- | ------------------------------ |
| 1 | Valid Parentheses              | String    | Push/Pop        | Match open-close brackets | O(n) | Code syntax / XML check        |
| 2 | Min Stack                      | Stack     | Push/Pop        | Track current min         | O(1) | Stock/Temp min tracking        |
| 3 | Largest Rectangle in Histogram | Array     | Monotonic Stack | Pop on smaller height     | O(n) | Skyline / storage optimization |

---

### 🌟 Key Takeaways

* Stack helps in **reverse order dependency** problems.
* **Valid Parentheses** → matches pairs.
* **Min Stack** → tracks extra property (min).
* **Histogram** → tracks previous smaller element efficiently.

---

perfect 🔥 — we’re moving to the **Queue / Monotonic Queue / Sliding Window** family — one of the most powerful and **commonly used** techniques in interviews.

these problems teach you to maintain *the best candidate(s)* in a **window** efficiently — ideal for **real-time systems, stock price trends, and stream processing.**

we’ll cover 👇
✅ clear problem statement
🎯 real-world analogy
🧠 idea behind queues / monotonic queues
🔍 step-by-step explanation
💻 clean Java code with rich comments
⚙️ time and space complexity

---

# 🚌 QUEUE PATTERN OVERVIEW

* **Queue (FIFO)** — first in, first out. Think of it like people lining up.
* **Monotonic Queue** — a *special queue* that keeps elements **sorted internally** (either increasing or decreasing), used to get the **max/min** in a current “window.”
* Great for **sliding window** problems, where a range moves one step at a time.

---

# 1️⃣ Sliding Window Maximum

---

### 🧩 Problem

Given an integer array `nums` and an integer `k`, find the **maximum value** in each sliding window of size `k`.

**Example:**
`nums = [1,3,-1,-3,5,3,6,7], k = 3`
👉 output: `[3,3,5,5,6,7]`

---

### 🎯 Real-life analogy

* Stock market: get **highest price in last 3 days** as window slides.
* Temperature monitoring: track **max temperature in last hour**.

---

### 🧠 Core Idea

Use a **Monotonic Decreasing Queue** that keeps elements **in decreasing order**:

* Front of queue = max element.
* When adding a new element:

  * Pop all smaller elements from the back (since they’ll never be useful).
* When sliding:

  * Remove front if it goes out of the window (index < i - k + 1).

---

### 🔍 Step-by-Step

`nums = [1,3,-1,-3,5,3,6,7], k=3`

| Step      | Window      | Queue (values in decreasing order) | Max |
| --------- | ----------- | ---------------------------------- | --- |
| [1,3,-1]  | → [3,-1]    | 3                                  |     |
| [3,-1,-3] | → [3,-1,-3] | 3                                  |     |
| [-1,-3,5] | → [5]       | 5                                  |     |
| [-3,5,3]  | → [5,3]     | 5                                  |     |
| [5,3,6]   | → [6]       | 6                                  |     |
| [3,6,7]   | → [7]       | 7                                  |     |

✅ Output = `[3,3,5,5,6,7]`

---

### 💻 Java Code

```java
import java.util.*;

class SlidingWindowMaximum {
    public int[] maxSlidingWindow(int[] nums, int k) {
        Deque<Integer> dq = new ArrayDeque<>(); // store indices
        int n = nums.length;
        int[] result = new int[n - k + 1];
        int idx = 0;

        for (int i = 0; i < n; i++) {
            // 1️⃣ Remove elements that are out of current window
            if (!dq.isEmpty() && dq.peekFirst() <= i - k) {
                dq.pollFirst();
            }

            // 2️⃣ Remove smaller elements from back
            while (!dq.isEmpty() && nums[dq.peekLast()] < nums[i]) {
                dq.pollLast();
            }

            // 3️⃣ Add current index
            dq.offerLast(i);

            // 4️⃣ Add max for window (once window of size k is formed)
            if (i >= k - 1) {
                result[idx++] = nums[dq.peekFirst()];
            }
        }
        return result;
    }
}
```

### ⚙️ Complexity

⏱ `O(n)` → each element enters/leaves deque once
💾 `O(k)` → queue size limited to window

---

# 2️⃣ Moving Average from Data Stream

---

### 🧩 Problem

Given a stream of integers and a window size `k`, calculate the **moving average** of all numbers in the sliding window.

**Example:**

```
MovingAverage m = new MovingAverage(3);
m.next(1) → 1.0
m.next(10) → (1+10)/2 = 5.5
m.next(3) → (1+10+3)/3 = 4.67
m.next(5) → (10+3+5)/3 = 6.0
```

---

### 🎯 Real-life analogy

* Real-time analytics (average response time in last 10s).
* IoT sensors (average temp in last N readings).

---

### 🧠 Core Idea

Keep a **queue** to store the last `k` numbers.
Maintain a **running sum** to compute average efficiently.

When adding new number:

* Add to queue & sum.
* If queue > k, remove oldest & subtract from sum.

---

### 🔍 Step-by-Step

For `k=3`, stream: [1, 10, 3, 5]

```
Queue: [1]      sum=1     avg=1.0
Queue: [1,10]   sum=11    avg=5.5
Queue: [1,10,3] sum=14    avg=4.67
Queue: [10,3,5] sum=18    avg=6.0
```

---

### 💻 Java Code

```java
import java.util.*;

class MovingAverage {
    private Queue<Integer> queue;
    private int size;
    private double sum;

    public MovingAverage(int size) {
        this.queue = new LinkedList<>();
        this.size = size;
        this.sum = 0;
    }

    public double next(int val) {
        queue.offer(val);
        sum += val;

        // Remove oldest if window exceeds size
        if (queue.size() > size) {
            sum -= queue.poll();
        }

        return sum / queue.size();
    }
}
```

### ⚙️ Complexity

⏱ `O(1)` per operation
💾 `O(k)` storage

---

# 3️⃣ First Negative Number in Every Window of Size K

---

### 🧩 Problem

Given an array and an integer `k`, print the **first negative integer** in every window of size `k`.
If no negative exists in a window → output `0`.

**Example:**
`nums = [12, -1, -7, 8, -15, 30, 16, 28], k = 3`
✅ output → `[-1, -1, -7, -15, -15, 0]`

---

### 🎯 Real-life analogy

* Real-time signal analysis: find first “drop” (negative trend) in each interval.
* Sentiment analysis over streaming text (detect first negative tone).

---

### 🧠 Core Idea

Use a **Queue** to store **indices of negative numbers** within the current window.
When window slides:

* Remove elements that fall out (index < i - k + 1)
* Add new negatives to queue
* The **front** of queue is the first negative in the current window.

---

### 🔍 Step-by-Step

`nums = [12, -1, -7, 8, -15, 30, 16, 28], k=3`

| Window      | Queue (neg indices) | Output |
| ----------- | ------------------- | ------ |
| [12,-1,-7]  | [-1,-7]             | -1     |
| [-1,-7,8]   | [-1,-7]             | -1     |
| [-7,8,-15]  | [-7,-15]            | -7     |
| [8,-15,30]  | [-15]               | -15    |
| [-15,30,16] | [-15]               | -15    |
| [30,16,28]  | []                  | 0      |

✅ Output: `[-1, -1, -7, -15, -15, 0]`

---

### 💻 Java Code

```java
import java.util.*;

class FirstNegativeInWindow {
    public List<Integer> firstNegativeInWindow(int[] nums, int k) {
        Deque<Integer> dq = new ArrayDeque<>();
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < nums.length; i++) {
            // Remove out-of-window elements
            if (!dq.isEmpty() && dq.peekFirst() <= i - k) {
                dq.pollFirst();
            }

            // Add index if element is negative
            if (nums[i] < 0) {
                dq.offerLast(i);
            }

            // Once we have a window, record result
            if (i >= k - 1) {
                result.add(dq.isEmpty() ? 0 : nums[dq.peekFirst()]);
            }
        }
        return result;
    }
}
```

### ⚙️ Complexity

⏱ `O(n)` time
💾 `O(k)` space

---

## 🧩 Summary Table — Queue / Monotonic Queue Problems

| # | Problem                  | Structure                  | Key Concept               | Core Logic          | Time | Real Analogy         |
| - | ------------------------ | -------------------------- | ------------------------- | ------------------- | ---- | -------------------- |
| 1 | Sliding Window Maximum   | Monotonic Decreasing Queue | Maintain decreasing order | Front always max    | O(n) | Stock / Sensor peaks |
| 2 | Moving Average Stream    | Queue                      | Running window sum        | Add new, remove old | O(1) | Live average metrics |
| 3 | First Negative in Window | Queue                      | Index tracking            | Queue of negatives  | O(n) | Signal dips          |

---

### 🌟 Key Takeaways

* A **queue** is ideal for real-time or streaming window computations.
* **Monotonic queues** let you efficiently track **min/max** values.
* Each element enters and exits the queue at most once → `O(n)` efficiency.
* Perfect for **data stream**, **stock analysis**, **real-time dashboards**, **IoT sensor monitoring**.

---
