That's an excellent next step\! **Linked Lists** are fundamental data structures that rely heavily on manipulating **pointers** (or references in Java) and are key to understanding dynamic memory allocation.

Here is an explanation of the general operations of a Linked List, followed by a breakdown of the first problem, **Reverse a Linked List**.

-----

## 🔗 Linked Lists: General Operations

A **Linked List** is a linear collection of data elements called **nodes**, where each node points to the next node in the sequence. Unlike arrays, nodes are not stored at contiguous memory locations, but they are linked together via pointers.

### Node Structure

Each node typically contains two parts:

1.  **Data:** The value stored in the node.
2.  **Next Pointer:** A reference (pointer) to the next node in the sequence. The last node's pointer is usually **`null`**.

### Basic Operations

| Operation | Description | General Steps | Time Complexity |
| :--- | :--- | :--- | :---: |
| **Insertion** | Adding a new node to the list (e.g., at the head). | 1. Create the new node. 2. Set the **new node's `next` pointer** to point to the current **head**. 3. Update the **list's `head`** to be the new node. | $O(1)$ |
| **Deletion** | Removing a node (e.g., at the head). | 1. Store a temporary reference to the current **head**. 2. Update the **list's `head`** to point to the current head's **`next`** node. 3. (Optional) Dispose of the old head node. | $O(1)$ |
| **Traversal** | Visiting every node sequentially. | Start at the `head` and repeatedly follow the `next` pointer until a `null` pointer is reached. | $O(N)$ |
| **Search** | Finding a node with a specific value. | Traversal combined with a check for the target value at each node. | $O(N)$ |

These problems focus on advanced pointer manipulation techniques to perform more complex operations efficiently.

-----

## 1\. Reverse a Linked List

### Problem Statement

Given the `head` of a singly linked list, reverse the list, and return the **new head** of the reversed list.

**Example:**

  * Input: `1 -> 2 -> 3 -> 4 -> 5 -> NULL`
  * Output: `5 -> 4 -> 3 -> 2 -> 1 -> NULL`

### 💡 Pointer Reversal Strategy

The key idea is to iterate through the list and, at each step, change the direction of the **`next` pointer** from pointing forward to pointing **backward**.

We need three primary pointers to keep track of the process:

1.  **`prev` (Previous):** Initially set to `null`. This pointer will eventually become the new `tail` of the list. It tracks the node whose `next` pointer has already been flipped.
2.  **`current` (Current):** Initially set to `head`. This pointer iterates through the original list.
3.  **`next_node` (Next):** A temporary pointer used to store the **next** node in the original list before the `current` node's pointer is flipped. This is crucial for maintaining a reference to the rest of the list.

### 👣 Step-by-Step Example

Let's trace the execution for the input list: **`1 -> 2 -> 3 -> NULL`**

Initial state: `prev = NULL`, `current = 1`.

| Iteration | `current` | `current.val` | 1. Store `next_node` | 2. Reverse Pointer | 3. Advance `prev` | 4. Advance `current` | List State (Pointers Flipped) |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 1 | 1 | 1 | `next_node = 2` | `1.next = NULL` | `prev = 1` | `current = 2` | `NULL <- 1` `2 -> 3 -> NULL` |
| 2 | 2 | 2 | `next_node = 3` | `2.next = 1` | `prev = 2` | `current = 3` | `NULL <- 1 <- 2` `3 -> NULL` |
| 3 | 3 | 3 | `next_node = NULL` | `3.next = 2` | `prev = 3` | `current = NULL` | `NULL <- 1 <- 2 <- 3` |
| **End** | **NULL** | - | - | - | - | - | The loop terminates when `current` is `NULL`. The new head is `prev`. |

**Final New Head:** Node 3.

### 💻 Java Solution

```java
/**
 * Definition for singly-linked list.
 * public class ListNode {
 * int val;
 * ListNode next;
 * ListNode(int x) { val = x; }
 * }
 */
class Solution {
    /**
     * Reverses a singly linked list iteratively by flipping pointers.
     * Time Complexity: O(N), where N is the number of nodes, as we visit each node exactly once.
     * Space Complexity: O(1), as we only use a few extra pointers (variables).
     */
    public ListNode reverseList(ListNode head) {
        
        // 1. 'prev' pointer: This will store the previously processed node. 
        // It starts at null because the new head (the old tail) will point to null.
        ListNode prev = null;
        
        // 2. 'current' pointer: This is our iterator, starting at the original head.
        ListNode current = head;
        
        // Loop continues until 'current' goes past the end of the list (becomes null).
        while (current != null) {
            
            // 3. Store 'next_node' (Temporary variable):
            // We must store the reference to the rest of the list BEFORE 
            // we change the 'current' node's next pointer.
            ListNode next_node = current.next;
            
            // 4. Reverse the Pointer:
            // This is the core reversal step. The current node's next pointer 
            // is redirected to point backward to 'prev'.
            current.next = prev;
            
            // 5. Advance 'prev' pointer:
            // 'prev' moves one step forward to become the node we just processed ('current').
            prev = current;
            
            // 6. Advance 'current' pointer:
            // 'current' moves one step forward using the stored 'next_node' reference.
            current = next_node;
        }
        
        // When the loop finishes, 'current' is null, and 'prev' is pointing to the 
        // last node of the original list, which is the NEW HEAD of the reversed list.
        return prev;
    }
}
```

-----

## 2\. Linked List Cycle

### Problem Statement

Given the `head` of a linked list, determine if the linked list has a **cycle** in it.

A cycle exists if any node in the list can be reached again by continuously following the `next` pointers.

**Return:** `true` if there is a cycle, and `false` otherwise.

### 💡 Fast & Slow Pointer (Floyd's Tortoise and Hare) Strategy

This is the classic use case for the **Fast and Slow Pointer** technique. The idea is that if there is a cycle, a faster moving pointer is guaranteed to eventually *catch up to* a slower moving pointer.

1.  **Initialize Pointers:**
      * **`slow` (Tortoise):** Moves one step at a time (`slow = slow.next`).
      * **`fast` (Hare):** Moves two steps at a time (`fast = fast.next.next`).
2.  **Detection:**
      * If the list has **no cycle**, the `fast` pointer will reach the end (`null`) first, and the loop terminates.
      * If the list has a **cycle**, the `fast` pointer will eventually re-enter the cycle and will eventually become **equal to** the `slow` pointer inside the cycle.
3.  **Termination:** The loop continues as long as `fast` and `fast.next` are not `null`.

### 👣 Step-by-Step Example

Input: `1 -> 2 -> 3 -> 4 -> 5` (where `5.next` points back to `3`)

| Step | Slow Pointer (`val`) | Fast Pointer (`val`) | Condition (`slow == fast`) | Cycle Detected? |
| :---: | :---: | :---: | :---: | :---: |
| Initial | 1 | 1 | No | No |
| 1 | 2 | 3 | No | No |
| 2 | 3 | 5 | No | No |
| 3 | 4 | 4 | **YES** | **True** |

### 💻 Java Solution

```java
class Solution {
    /**
     * Detects if a linked list contains a cycle using the Fast and Slow Pointer technique.
     * Time Complexity: O(N), where N is the number of nodes. The fast pointer traverses the list at most twice.
     * Space Complexity: O(1), as we only use two pointers.
     */
    public boolean hasCycle(ListNode head) {
        // Handle edge cases: empty list or list with only one node cannot have a cycle.
        if (head == null || head.next == null) {
            return false;
        }

        // Initialize two pointers: 'slow' moves one step, 'fast' moves two steps.
        ListNode slow = head;
        ListNode fast = head;

        // Loop condition: Continue as long as the 'fast' pointer can move two steps.
        // If 'fast' or 'fast.next' is null, it means we reached the end (no cycle).
        while (fast != null && fast.next != null) {
            // Move slow pointer one step
            slow = slow.next;
            
            // Move fast pointer two steps
            fast = fast.next.next;

            // Check if the pointers meet (cycle detected).
            if (slow == fast) {
                return true;
            }
        }

        // If the loop finishes, 'fast' hit null, meaning the end of the list was reached.
        return false;
    }
}
```

-----

## 3\. Merge Two Sorted Lists

### Problem Statement

You are given the heads of two sorted linked lists, `list1` and `list2`. **Merge** the two lists into a single **sorted** list. The new list should be made by splicing together the nodes of the first two lists.

**Return:** The head of the merged linked list.

### 💡 Iterative Merging Strategy

Since both input lists are already sorted, we can use a simple **two-pointer (or two-head) comparison** approach:

1.  **Dummy Head:** Create a **dummy node** to serve as the starting point of the new merged list. This simplifies handling the beginning of the list.
2.  **Current Pointer:** Use a `current` pointer to build the new list, initially pointing to the dummy node.
3.  **Comparison Loop:** Iterate while both `list1` and `list2` are non-null:
      * Compare the values of the current nodes in `list1` and `list2`.
      * Append the node with the smaller value to `current.next`.
      * Advance the pointer of the list whose node was just moved.
      * Advance the `current` pointer to the newly added node.
4.  **Append Remainder:** After the loop, one list might still have remaining nodes (since they are sorted, all remaining nodes are larger than those already processed). Append the rest of that non-null list to `current.next`.
5.  **Return:** The merged list is `dummy.next`.

### 👣 Step-by-Step Example

Input: `list1 = [1 -> 2 -> 4]`, `list2 = [1 -> 3 -> 4]`

Initial state: `dummy = 0`, `current = dummy`.

| L1 | L2 | Compare | Action (Move to `current.next`) | `current` Moves To | Merged List (after `current`) |
| :---: | :---: | :---: | :---: | :---: | :---: |
| 1 | 1 | $1 \leq 1$ | Move 1 from `list1` | 1 (from L1) | 0 -\> 1 |
| 2 | 1 | $2 > 1$ | Move 1 from `list2` | 1 (from L2) | 0 -\> 1 -\> 1 |
| 2 | 3 | $2 < 3$ | Move 2 from `list1` | 2 (from L1) | 0 -\> 1 -\> 1 -\> 2 |
| 4 | 3 | $4 > 3$ | Move 3 from `list2` | 3 (from L2) | 0 -\> 1 -\> 1 -\> 2 -\> 3 |
| 4 | 4 | $4 \leq 4$ | Move 4 from `list1` | 4 (from L1) | 0 -\> 1 -\> 1 -\> 2 -\> 3 -\> 4 |
| NULL | 4 | `L1` is `NULL` | Append remainder of `L2` (Node 4) | 4 (from L2) | 0 -\> 1 -\> 1 -\> 2 -\> 3 -\> 4 -\> 4 |

**Final Result (return `dummy.next`):** `1 -> 1 -> 2 -> 3 -> 4 -> 4`

### 💻 Java Solution

```java
class Solution {
    /**
     * Merges two sorted linked lists into a single sorted list iteratively.
     * Time Complexity: O(N + M), where N and M are the lengths of list1 and list2, 
     * as we visit each node in both lists exactly once.
     * Space Complexity: O(1), as we only use a few extra pointers.
     */
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        
        // 1. Create a dummy node to act as the start of the merged list.
        // This simplifies the logic by not having to worry about the initial head assignment.
        ListNode dummy = new ListNode(0); 
        
        // 'current' pointer will build the new merged list.
        ListNode current = dummy;

        // 2. Comparison Loop: Iterate while both lists have nodes remaining.
        while (list1 != null && list2 != null) {
            
            if (list1.val <= list2.val) {
                // Attach the smaller node (from list1) to the merged list.
                current.next = list1;
                // Advance list1's pointer.
                list1 = list1.next;
            } else {
                // Attach the smaller node (from list2) to the merged list.
                current.next = list2;
                // Advance list2's pointer.
                list2 = list2.next;
            }
            
            // Advance the merged list pointer to the node that was just added.
            current = current.next;
        }

        // 3. Append Remainder: Attach the rest of the non-empty list.
        // Only one of these will ever be non-null.
        if (list1 != null) {
            current.next = list1;
        } else if (list2 != null) {
            current.next = list2;
        }

        // 4. Return the merged list's head (the node after the dummy node).
        return dummy.next;
    }
}
```

-----

I will continue with the remaining three problems: **Merge k Sorted Lists**, **Remove Nth Node From End of List**, and **Reorder List**.

## 4\. Merge k Sorted Lists (Optimal Solution with Heap)

### Problem Statement

You are given an array of $k$ linked lists, where each linked list is sorted in ascending order. **Merge** all the linked lists into one single sorted linked list and return it.

### 💡 Priority Queue (Min-Heap) Strategy

A simple, but less efficient approach would be to repeatedly use the `Merge Two Sorted Lists` function, but the optimal solution leverages a **Min-Heap (Priority Queue)**.

1.  **Initialize Heap:** Create a Min-Heap. The elements in the heap will be the **nodes** from the linked lists, and they will be ordered based on their `val`.
2.  **Initial Population:** Iterate through the array of $k$ lists. For every non-null list head, add the head node to the Min-Heap.
3.  **Extraction and Insertion Loop:**
      * Repeatedly **extract the minimum node** from the heap (this will be the smallest value across all $k$ lists).
      * Attach this extracted node to the tail of the merged list (using a dummy head/current pointer, as before).
      * If the extracted node has a **`next`** node, add that `next` node to the heap.
4.  **Termination:** The loop continues until the heap is empty.

### Complexity Analysis

  * $N$: Total number of nodes in all lists combined.
  * $k$: Number of linked lists.
  * **Time Complexity:** $O(N \log k)$. Each of the $N$ total nodes is added to the heap once, and extracted once. Heap operations (insertion/extraction) take $O(\log k)$ time, where $k$ is the maximum size of the heap.
  * **Space Complexity:** $O(k)$, as the heap will store at most one node from each of the $k$ lists at any given time.

### 💻 Java Solution

```java
import java.util.PriorityQueue;

class Solution {
    /**
     * Merges k sorted linked lists using a Min-Heap (Priority Queue).
     * Time Complexity: O(N log k), where N is total nodes, k is the number of lists.
     * Space Complexity: O(k) for the Priority Queue.
     */
    public ListNode mergeKLists(ListNode[] lists) {
        
        // Handle empty input list array.
        if (lists == null || lists.length == 0) {
            return null;
        }

        // 1. Initialize Min-Heap: 
        // We use a PriorityQueue to store ListNode objects, ordered by their 'val'.
        PriorityQueue<ListNode> minHeap = new PriorityQueue<>((a, b) -> a.val - b.val);

        // 2. Initial Population: Add the head of every non-null list to the heap.
        for (ListNode listHead : lists) {
            if (listHead != null) {
                minHeap.add(listHead);
            }
        }
        
        // Dummy node for the merged list construction.
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;

        // 3. Extraction and Insertion Loop:
        while (!minHeap.isEmpty()) {
            
            // Extract the node with the minimum value (The smallest node across all k lists).
            ListNode minNode = minHeap.poll();
            
            // Attach the minimum node to the merged list.
            current.next = minNode;
            // Move the current pointer forward.
            current = current.next;
            
            // If the extracted node has a 'next' node, add it to the heap.
            // This ensures the next smallest element from that list is considered.
            if (minNode.next != null) {
                minHeap.add(minNode.next);
            }
        }

        // Return the head of the merged list.
        return dummy.next;
    }
}
```

-----

## 5\. Remove Nth Node From End of List

### Problem Statement

Given the `head` of a linked list, remove the **$n$-th node from the end** of the list and return its head.

### 💡 Two-Pointer Gap Strategy

The trick to solving "from the end" problems in a single pass is to use two pointers with a fixed **gap** between them.

1.  **Dummy Head:** Use a dummy node to simplify edge cases, particularly when the head itself needs to be removed.
2.  **Initialize Pointers:**
      * **`slow`:** Starts at the dummy node. This pointer will stop just *before* the node to be removed.
      * **`fast`:** Starts at the dummy node.
3.  **Create Gap:** Move the `fast` pointer forward by **$n + 1$** steps. The extra step of 1 ensures that when `fast` hits `null` (end of list), `slow` will be pointing to the node *before* the target $n$-th node.
4.  **Simultaneous Traversal:** Move both `slow` and `fast` pointers forward one step at a time until the `fast` pointer reaches **`null`** (the end of the list).
5.  **Removal:** At this point, `slow` is pointing to the node just before the target node. Perform the removal operation: `slow.next = slow.next.next`.
6.  **Return:** Return `dummy.next`.

### 👣 Step-by-Step Example

Input: `1 -> 2 -> 3 -> 4 -> 5 -> NULL`, $n = 2$ (remove node 4)

Initial: `dummy=0`, `slow=0`, `fast=0`.

1.  **Create Gap ($n+1=3$ steps):** `fast` moves to 3.

      * `fast`: `0 -> 1 -> 2 -> 3`

2.  **Simultaneous Traversal (until `fast` is `NULL`):**

| `slow` | `fast` | Move Pointers | `fast.next` |
| :---: | :---: | :---: | :---: |
| 0 | 3 | slow $\to$ 1, fast $\to$ 4 | 4 |
| 1 | 4 | slow $\to$ 2, fast $\to$ 5 | 5 |
| **2** | **5** | slow $\to$ 3, fast $\to$ NULL | **NULL** |

3.  **Removal:** `slow` (Node 3) points to the node before the target (Node 4).
      * `slow.next` (Node 4) is removed by setting `slow.next = slow.next.next` (Node 5).
      * List becomes: `0 -> 1 -> 2 -> 3 -> 5 -> NULL`

**Final Result:** `1 -> 2 -> 3 -> 5`

### 💻 Java Solution

```java
class Solution {
    /**
     * Removes the n-th node from the end of a list using the Two-Pointer Gap technique.
     * Time Complexity: O(L), where L is the length of the list, as we make two passes 
     * (implicitly) in a single loop (O(n) + O(L-n) which simplifies to O(L)).
     * Space Complexity: O(1).
     */
    public ListNode removeNthFromEnd(ListNode head, int n) {
        
        // 1. Create a dummy node to handle the edge case where the head is removed.
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        // Initialize two pointers starting at the dummy node.
        ListNode slow = dummy;
        ListNode fast = dummy;

        // 2. Create the Gap (n + 1 steps for the fast pointer).
        // The +1 ensures 'slow' stops RIGHT BEFORE the target node.
        for (int i = 0; i < n + 1; i++) {
            // Check for edge case where n is greater than list length (shouldn't happen per constraints but good practice).
            if (fast == null) return head; 
            fast = fast.next;
        }

        // 3. Simultaneous Traversal: Move both pointers until 'fast' reaches null (the end).
        while (fast != null) {
            slow = slow.next;
            fast = fast.next;
        }

        // 4. Removal: 'slow' is now at the (n+1)-th node from the end. 
        // Skip the next node (the n-th node from the end).
        slow.next = slow.next.next;

        // Return the new head (the node after the dummy).
        return dummy.next;
    }
}
```

-----

## 6\. Reorder List

### Problem Statement

Given the `head` of a singly linked list: $L_0 \to L_1 \to L_2 \to \dots \to L_{n-1} \to L_n$.

**Reorder** the list to be: $L_0 \to L_n \to L_1 \to L_{n-1} \to L_2 \to L_{n-2} \to \dots$

You may **not** modify the values in the list's nodes. Only the pointers can be changed.

### 💡 Multi-Step Strategy (Find Mid, Reverse, Merge)

This problem combines several linked list techniques:

1.  **Find the Middle:** Use the **Fast & Slow Pointer** method (from Problem 2) to find the middle node of the list. When `fast` reaches the end, `slow` is at the middle.
2.  **Split the List:** Cut the list into two halves. The first half ends at the node *before* the middle. The second half starts at the middle node.
3.  **Reverse the Second Half:** Use the **Pointer Reversal** method (from Problem 1) to reverse the second half of the list.
4.  **Merge (Zig-Zag):** Merge the two lists (First Half and Reversed Second Half) by taking nodes alternately from each list.

### 👣 Strategy Outline

Input: `1 -> 2 -> 3 -> 4 -> 5 -> 6 -> NULL`

1.  **Find Mid:** `slow` stops at 4. Cut the link:
      * List 1: `1 -> 2 -> 3 -> NULL`
      * List 2 (Starting at 4): `4 -> 5 -> 6 -> NULL`
2.  **Reverse List 2:**
      * Reversed List 2: `6 -> 5 -> 4 -> NULL`
3.  **Merge Alternately:**
      * Take 1 (L1), then 6 (L2).
      * Take 2 (L1), then 5 (L2).
      * Take 3 (L1), then 4 (L2).

**Final Result:** `1 -> 6 -> 2 -> 5 -> 3 -> 4 -> NULL`

### 💻 Java Solution

```java
class Solution {
    /**
     * Reorders a linked list into a specific alternating pattern.
     * This requires finding the middle, reversing the second half, and merging.
     * Time Complexity: O(N) (O(N) for each step: find mid, reverse, merge).
     * Space Complexity: O(1).
     */
    public void reorderList(ListNode head) {
        if (head == null || head.next == null) {
            return;
        }

        // --- Step 1: Find the middle of the list ---
        ListNode slow = head;
        ListNode fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // 'slow' is now at the start of the second half.
        
        // --- Step 2: Split and Reverse the second half ---
        ListNode head2 = slow.next; // Head of the second half
        slow.next = null;          // Cut the link to split the two halves

        // Reverse the second half (using the same logic as Problem 1)
        ListNode prev = null;
        ListNode current = head2;
        while (current != null) {
            ListNode next_node = current.next;
            current.next = prev;
            prev = current;
            current = next_node;
        }
        head2 = prev; // 'head2' is now the head of the reversed second half

        // --- Step 3: Merge the two lists alternately ---
        // head1 is the original head, head2 is the reversed second half head.
        ListNode head1 = head;
        
        // Loop continues until one list runs out of nodes (usually the second half first).
        while (head2 != null) {
            // Store the next nodes for traversal
            ListNode next1 = head1.next;
            ListNode next2 = head2.next;

            // Perform the alternating reordering
            head1.next = head2;
            head2.next = next1;

            // Advance pointers
            head1 = next1;
            head2 = next2;
        }
    }
}
```

perfect 💪 — you’re doing great!
this next family — **Linked List (5 classic problems)** — teaches **pointer manipulation**, **recursion**, and **slow–fast pointer techniques**, all of which are heavily tested in coding interviews (and appear in system-level code, too).

we’ll go through:
✅ beginner-friendly explanation
🎯 real-world analogy
🧠 core concept (with pointer movement)
🔍 step-by-step walkthrough
💻 full commented Java code
⚙️ complexity analysis

---

# 🧩 LINKED LIST OVERVIEW

a **linked list** is a chain of nodes, each having:

```java
class ListNode {
    int val;
    ListNode next;
}
```

unlike arrays, memory is *not contiguous*, so we traverse by following `next` pointers.

**key idea:** we manipulate these pointers — reversing, merging, detecting loops — using *two-pointer* or *recursive* patterns.

---

# 1️⃣ Reverse Linked List

---

### 🧩 Problem

Given the head of a singly linked list, reverse the list and return the new head.

**Example:**
Input → `1 → 2 → 3 → 4 → 5`
Output → `5 → 4 → 3 → 2 → 1`

---

### 🎯 Real-life analogy

Think of flipping a **train** so that the first carriage becomes the last.

---

### 🧠 Core Idea

Iterate through the list and **reverse the direction** of each `next` pointer.

Use three pointers:

* `prev` (initially `null`)
* `curr` (starting at `head`)
* `next` (temporarily store next node)

---

### 🔍 Step-by-Step

`1→2→3→4`
1️⃣ save `next=2`
2️⃣ `curr.next=prev` → `1→null`
3️⃣ move `prev=1`, `curr=2`
repeat until done → `4→3→2→1`

---

### 💻 Java Code

```java
class ReverseLinkedList {
    public ListNode reverseList(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;

        while (curr != null) {
            ListNode nextNode = curr.next; // temporarily store next
            curr.next = prev;              // reverse link
            prev = curr;                   // move prev
            curr = nextNode;               // move curr forward
        }
        return prev; // new head
    }
}
```

### ⚙️ Complexity

⏱ `O(n)` time
💾 `O(1)` space

---

# 2️⃣ Merge Two Sorted Lists

---

### 🧩 Problem

Merge two **sorted linked lists** into one sorted list.

**Example:**
`list1 = 1→2→4`, `list2 = 1→3→4`
Output → `1→1→2→3→4→4`

---

### 🎯 Real-life analogy

Like merging two **sorted queues** (e.g., sorted waiting lists).

---

### 🧠 Core Idea

Use a **dummy head** and two pointers (`l1`, `l2`):
Compare values, attach the smaller node, move that pointer.

---

### 🔍 Step-by-Step

`l1=1, l2=1` → attach `1(l1)` → move `l1`
then attach `1(l2)` → move `l2`
keep repeating until both lists done.

---

### 💻 Java Code

```java
class MergeTwoSortedLists {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        ListNode dummy = new ListNode(-1);
        ListNode tail = dummy;

        while (list1 != null && list2 != null) {
            if (list1.val <= list2.val) {
                tail.next = list1;
                list1 = list1.next;
            } else {
                tail.next = list2;
                list2 = list2.next;
            }
            tail = tail.next; // advance merged list tail
        }

        // Attach the remaining nodes
        if (list1 != null) tail.next = list1;
        if (list2 != null) tail.next = list2;

        return dummy.next;
    }
}
```

### ⚙️ Complexity

⏱ `O(n + m)`
💾 `O(1)`

---

# 3️⃣ Reorder List

---

### 🧩 Problem

Reorder list as:
`L0 → Ln → L1 → Ln-1 → L2 …`

**Example:**
`1→2→3→4→5` → `1→5→2→4→3`

---

### 🎯 Real-life analogy

Reordering a queue where first and last alternately come to the front (like dance pairing 🎶).

---

### 🧠 Core Idea

1️⃣ Find middle using **slow/fast pointers**
2️⃣ Reverse the **second half**
3️⃣ Merge first and reversed second alternately

---

### 🔍 Step-by-Step

`1→2→3→4→5`

* middle → `3`
* reverse second half → `5→4`
* merge → `1→5→2→4→3`

---

### 💻 Java Code

```java
class ReorderList {
    public void reorderList(ListNode head) {
        if (head == null || head.next == null) return;

        // 1️⃣ Find middle (slow-fast pointers)
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // 2️⃣ Reverse second half
        ListNode prev = null, curr = slow.next;
        slow.next = null; // break list into two parts
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }

        // 3️⃣ Merge both halves
        ListNode first = head, second = prev;
        while (second != null) {
            ListNode t1 = first.next, t2 = second.next;
            first.next = second;
            second.next = t1;
            first = t1;
            second = t2;
        }
    }
}
```

### ⚙️ Complexity

⏱ `O(n)`
💾 `O(1)`

---

# 4️⃣ Linked List Cycle Detection (Floyd’s Algorithm)

---

### 🧩 Problem

Given a linked list, determine if it has a **cycle**.

**Example:**
`3→2→0→-4` and tail connects to node `2` → true

---

### 🎯 Real-life analogy

Imagine runners on a circular track — if one runs faster, they’ll eventually meet if a loop exists.

---

### 🧠 Core Idea — *Floyd’s Tortoise & Hare* 🐢🐇

Use two pointers:

* `slow` moves 1 step
* `fast` moves 2 steps

If there’s a cycle → they’ll eventually meet.

---

### 🔍 Step-by-Step

`1→2→3→4→2`
slow(1), fast(1)
→ (2,3)
→ (3,5)
→ meet at node 2 ✅

---

### 💻 Java Code

```java
class LinkedListCycle {
    public boolean hasCycle(ListNode head) {
        if (head == null) return false;

        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;

            if (slow == fast) return true; // they met!
        }
        return false;
    }
}
```

### ⚙️ Complexity

⏱ `O(n)`
💾 `O(1)`

---

# 5️⃣ Remove N-th Node From End of List

---

### 🧩 Problem

Given `head` and integer `n`, remove the **n-th node from the end** and return new head.

**Example:**
`1→2→3→4→5, n=2` → remove `4` → `1→2→3→5`

---

### 🎯 Real-life analogy

Deleting an element **n steps from the tail** without knowing total length — e.g., removing the second most recent log entry.

---

### 🧠 Core Idea

Use **two pointers**:

* Move `fast` ahead by `n` steps.
* Then move both `fast` and `slow` until `fast` hits end.
* `slow` now points **before** node to delete.

---

### 🔍 Step-by-Step

`[1,2,3,4,5], n=2`
1️⃣ `fast` moves to node 2 ahead (→3)
2️⃣ move both until `fast` at end
3️⃣ `slow` before node 4 → delete `4`

---

### 💻 Java Code

```java
class RemoveNthNodeFromEnd {
    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;

        ListNode slow = dummy, fast = dummy;

        // Move fast ahead by n+1 to maintain gap
        for (int i = 0; i <= n; i++) {
            fast = fast.next;
        }

        // Move both pointers
        while (fast != null) {
            slow = slow.next;
            fast = fast.next;
        }

        // Delete nth node
        slow.next = slow.next.next;

        return dummy.next;
    }
}
```

### ⚙️ Complexity

⏱ `O(n)`
💾 `O(1)`

---

## 🧩 Summary Table — Linked List Problems

| # | Problem              | Technique               | Key Idea            | Time   | Space | Analogy            |
| - | -------------------- | ----------------------- | ------------------- | ------ | ----- | ------------------ |
| 1 | Reverse Linked List  | Iterative               | Flip next pointers  | O(n)   | O(1)  | Flip a train       |
| 2 | Merge Two Sorted     | Dummy node              | Compare and attach  | O(n+m) | O(1)  | Merge queues       |
| 3 | Reorder List         | Split + Reverse + Merge | Alternate nodes     | O(n)   | O(1)  | Dance pairing      |
| 4 | Detect Cycle         | Fast–Slow pointer       | Meet if loop exists | O(n)   | O(1)  | Runners on track   |
| 5 | Remove N-th from End | Two-pointer gap         | Delete nth from end | O(n)   | O(1)  | Delete last-n logs |

---

### 🌟 Key Takeaways

* Most linked list problems revolve around **pointer movement**.
* The **dummy node pattern** simplifies edge cases (head deletion).
* The **slow–fast pointer** trick is powerful — used in cycle detection, middle finding, and palindrome checks.
* Practice visualizing pointer updates — that’s the key to mastery!

---