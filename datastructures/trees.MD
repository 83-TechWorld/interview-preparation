excellent 🌳 — now we’re stepping into one of the most conceptually rich parts of DSA: **Binary Trees & Binary Search Trees (BSTs)**.

trees are everywhere — compilers, file systems, databases, and AI models (like decision trees).

we’ll go through **14 important problems**, grouped naturally by category.
for each one you’ll get 👇

✅ the problem statement
🧱 what data structure is used
🧠 how the algorithm works
🔍 how to debug it step-by-step
💻 fully commented java code
⚙️ complexity

---

# 🌳 What is a Binary Tree?

A **Binary Tree** is a hierarchical data structure made of nodes.
Each node has:

```java
class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    TreeNode(int val) { this.val = val; }
}
```

🔹 **Root:** topmost node
🔹 **Left/Right Child:** pointers to subtrees
🔹 **Leaf:** node with no children

---

# 🌿 What is a Binary Search Tree (BST)?

A **BST** is a *special* binary tree with this property:

```
For every node:
  left subtree < node < right subtree
```

That’s why BSTs are great for **searching, inserting, deleting** efficiently (`O(log n)` on average).

---

# 🧩 Debugging Tips for Tree Problems

✅ Always **draw a small example** tree (4–5 nodes).
✅ Track recursive calls carefully.
✅ Print logs like `System.out.println("At node: " + node.val);`
✅ For BFS (Level Order) → visualize queue content.
✅ For recursion → note what each return means.

---

# 🌲 1. Invert Binary Tree

### 🧩 Problem

Swap the left and right children of every node.

**Example:**

```
Input:      Output:
   4           4
  / \         / \
 2   7  →    7   2
/ \ / \     / \ / \
1 3 6 9   9 6 3 1
```

### 🧱 Data Structure

Binary Tree (recursive traversal)

### 🧠 Approach

Use recursion:

* swap left and right recursively.

### 💻 Java Code

```java
class InvertBinaryTree {
    public TreeNode invertTree(TreeNode root) {
        if (root == null) return null;

        // swap
        TreeNode temp = root.left;
        root.left = root.right;
        root.right = temp;

        invertTree(root.left);
        invertTree(root.right);

        return root;
    }
}
```

### ⚙️ O(n)

**Debug Tip:** print node value before and after swap.

---

# 🌲 2. Maximum Depth of Binary Tree

### 🧩 Problem

Find the maximum depth (longest path from root to leaf).

### 🧱 Data Structure

Binary Tree

### 🧠 Approach

Recursively compute:
`maxDepth(root) = 1 + max(depth(left), depth(right))`

### 💻 Code

```java
class MaxDepth {
    public int maxDepth(TreeNode root) {
        if (root == null) return 0;
        return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
    }
}
```

### ⚙️ O(n)

**Debug Tip:** print depth returned at each node.

---

# 🌲 3. Same Tree

### 🧩 Problem

Check if two trees are identical in structure and values.

### 🧠 Approach

If both null → true
If one null → false
Else check value + recursively check children.

### 💻 Code

```java
class SameTree {
    public boolean isSameTree(TreeNode p, TreeNode q) {
        if (p == null && q == null) return true;
        if (p == null || q == null) return false;
        if (p.val != q.val) return false;
        return isSameTree(p.left, q.left) && isSameTree(p.right, q.right);
    }
}
```

### ⚙️ O(n)

**Debug Tip:** print pairs of compared nodes.

---

# 🌲 4. Subtree of Another Tree

### 🧩 Problem

Check if tree `t` is a subtree of `s`.

### 🧠 Approach

At each node of `s`, check if `s` and `t` are same trees.

### 💻 Code

```java
class SubtreeOfAnotherTree {
    public boolean isSubtree(TreeNode s, TreeNode t) {
        if (s == null) return false;
        if (isSameTree(s, t)) return true;
        return isSubtree(s.left, t) || isSubtree(s.right, t);
    }

    private boolean isSameTree(TreeNode a, TreeNode b) {
        if (a == null && b == null) return true;
        if (a == null || b == null || a.val != b.val) return false;
        return isSameTree(a.left, b.left) && isSameTree(a.right, b.right);
    }
}
```

### ⚙️ O(n * m)

**Debug Tip:** print whenever new subtree comparison starts.

---

# 🌲 5. Lowest Common Ancestor (LCA) of a BST

### 🧩 Problem

Find lowest node that is ancestor of both `p` and `q`.

**Example:**

```
      6
     / \
    2   8
   / \ / \
  0  4 7 9
     / \
    3  5

LCA(2,8)=6; LCA(2,4)=2
```

### 🧱 Data Structure

Binary Search Tree (BST)

### 🧠 Approach

* If both values < root → move left
* If both > root → move right
* Else current node = LCA

### 💻 Code

```java
class LCAofBST {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        while (root != null) {
            if (p.val < root.val && q.val < root.val)
                root = root.left;
            else if (p.val > root.val && q.val > root.val)
                root = root.right;
            else
                return root; // split point
        }
        return null;
    }
}
```

### ⚙️ O(h)

**Debug Tip:** trace values of root, p, q at each iteration.

---

# 🌲 6. Binary Tree Level Order Traversal

### 🧩 Problem

Return node values **level by level** (BFS).

### 🧱 Data Structure

Use **Queue (FIFO)** for breadth-first search.

### 💻 Code

```java
import java.util.*;

class LevelOrderTraversal {
    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> res = new ArrayList<>();
        if (root == null) return res;

        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);

        while (!q.isEmpty()) {
            int size = q.size();
            List<Integer> level = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                TreeNode node = q.poll();
                level.add(node.val);
                if (node.left != null) q.offer(node.left);
                if (node.right != null) q.offer(node.right);
            }
            res.add(level);
        }
        return res;
    }
}
```

### ⚙️ O(n)

**Debug Tip:** print queue content each level.

---

# 🌲 7. Validate Binary Search Tree

### 🧩 Problem

Check if given tree follows BST property.

### 🧠 Approach

Recursively check if each node value lies within `(min, max)` bounds.

### 💻 Code

```java
class ValidateBST {
    public boolean isValidBST(TreeNode root) {
        return validate(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    private boolean validate(TreeNode node, long min, long max) {
        if (node == null) return true;
        if (node.val <= min || node.val >= max) return false;
        return validate(node.left, min, node.val) &&
               validate(node.right, node.val, max);
    }
}
```

### ⚙️ O(n)

**Debug Tip:** log min and max range per node.

---

# 🌲 8. Kth Smallest Element in a BST

### 🧩 Problem

Find the `k`-th smallest element in a BST.

### 🧠 Approach

Use **Inorder Traversal** (Left → Root → Right) → returns sorted order.
Stop when k == 0.

### 💻 Code

```java
class KthSmallestBST {
    private int count, result;

    public int kthSmallest(TreeNode root, int k) {
        count = k;
        inorder(root);
        return result;
    }

    private void inorder(TreeNode node) {
        if (node == null) return;
        inorder(node.left);
        count--;
        if (count == 0) {
            result = node.val;
            return;
        }
        inorder(node.right);
    }
}
```

### ⚙️ O(h + k)

**Debug Tip:** print value and k on each visit.

---

# 🌲 9. Construct Binary Tree from Preorder and Inorder Traversal

### 🧩 Problem

Given preorder & inorder arrays, rebuild the binary tree.

**Example:**
`preorder = [3,9,20,15,7]`
`inorder = [9,3,15,20,7]`

### 🧠 Approach

* Preorder gives **root first**.
* Inorder splits into left & right subtrees.

Use recursion:

* Pick root from preorder
* Split inorder
* Recurse left/right

### 💻 Code

```java
import java.util.*;

class BuildTree {
    private Map<Integer, Integer> inorderMap;
    private int preIndex = 0;

    public TreeNode buildTree(int[] preorder, int[] inorder) {
        inorderMap = new HashMap<>();
        for (int i = 0; i < inorder.length; i++) {
            inorderMap.put(inorder[i], i);
        }
        return helper(preorder, 0, inorder.length - 1);
    }

    private TreeNode helper(int[] preorder, int left, int right) {
        if (left > right) return null;

        int rootVal = preorder[preIndex++];
        TreeNode root = new TreeNode(rootVal);

        int mid = inorderMap.get(rootVal);
        root.left = helper(preorder, left, mid - 1);
        root.right = helper(preorder, mid + 1, right);

        return root;
    }
}
```

### ⚙️ O(n)

**Debug Tip:** print preorder index and inorder split range.

---

# 🌲 10. Binary Tree Maximum Path Sum

### 🧩 Problem

Find maximum path sum (any path connecting two nodes).

### 🧠 Approach

For each node, compute:
`max_gain = node.val + max(0, left_gain, right_gain)`
Keep global max for `left + node + right`.

### 💻 Code

```java
class MaxPathSum {
    private int maxSum = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        gain(root);
        return maxSum;
    }

    private int gain(TreeNode node) {
        if (node == null) return 0;
        int left = Math.max(gain(node.left), 0);
        int right = Math.max(gain(node.right), 0);

        maxSum = Math.max(maxSum, node.val + left + right);
        return node.val + Math.max(left, right);
    }
}
```

### ⚙️ O(n)

**Debug Tip:** print gain and global max at each node.

---

# 🌲 11. Serialize and Deserialize Binary Tree

### 🧩 Problem

Convert tree → string and back.

### 🧱 Data Structure

Queue for level-order (BFS) approach.

### 💻 Code

```java
import java.util.*;

class Codec {
    public String serialize(TreeNode root) {
        if (root == null) return "null";
        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        StringBuilder sb = new StringBuilder();

        while (!q.isEmpty()) {
            TreeNode node = q.poll();
            if (node == null) {
                sb.append("null,");
                continue;
            }
            sb.append(node.val).append(",");
            q.offer(node.left);
            q.offer(node.right);
        }
        return sb.toString();
    }

    public TreeNode deserialize(String data) {
        if (data.equals("null")) return null;
        String[] vals = data.split(",");
        TreeNode root = new TreeNode(Integer.parseInt(vals[0]));
        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);

        int i = 1;
        while (!q.isEmpty() && i < vals.length) {
            TreeNode node = q.poll();
            if (!vals[i].equals("null")) {
                node.left = new TreeNode(Integer.parseInt(vals[i]));
                q.offer(node.left);
            }
            i++;
            if (!vals[i].equals("null")) {
                node.right = new TreeNode(Integer.parseInt(vals[i]));
                q.offer(node.right);
            }
            i++;
        }
        return root;
    }
}
```

### ⚙️ O(n)

**Debug Tip:** print string before/after deserialization.

---

# 🌲 12. Balanced Binary Tree

### 🧩 Problem

Check if tree’s left & right subtrees differ in height ≤ 1.

### 🧠 Approach

Recursive:

* compute height of each subtree
* if abs difference > 1 → unbalanced

### 💻 Code

```java
class BalancedBinaryTree {
    public boolean isBalanced(TreeNode root) {
        return height(root) != -1;
    }

    private int height(TreeNode node) {
        if (node == null) return 0;
        int left = height(node.left);
        int right = height(node.right);
        if (left == -1 || right == -1 || Math.abs(left - right) > 1) return -1;
        return 1 + Math.max(left, right);
    }
}
```

### ⚙️ O(n)

**Debug Tip:** print height at each node.

---

# 🌲 13. Count Complete Tree Nodes

### 🧩 Problem

Count total nodes in a complete binary tree.

### 🧱 Data Structure

Binary Tree (special case: perfect tree subtrees)

### 🧠 Approach

If left and right subtree heights are equal → subtree is perfect:
`nodes = 2^height - 1`, else count recursively.

### 💻 Code

```java
class CountCompleteTreeNodes {
    public int countNodes(TreeNode root) {
        if (root == null) return 0;
        int left = leftHeight(root);
        int right = rightHeight(root);
        if (left == right) return (1 << left) - 1;
        return 1 + countNodes(root.left) + countNodes(root.right);
    }

    private int leftHeight(TreeNode node) {
        int h = 0;
        while (node != null) {
            h++;
            node = node.left;
        }
        return h;
    }

    private int rightHeight(TreeNode node) {
        int h = 0;
        while (node != null) {
            h++;
            node = node.right;
        }
        return h;
    }
}
```

### ⚙️ O((log n)²)

**Debug Tip:** print left/right heights at each step.

---

# 🌲 14. Binary Tree Right Side View

### 🧩 Problem

Return nodes visible from the right side.

**Example:**

```
Input: [1,2,3,null,5,null,4]
Output: [1,3,4]
```

### 🧠 Approach

BFS level order → take **last node** of each level.

### 💻 Code

```java
import java.util.*;

class RightSideView {
    public List<Integer> rightSideView(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null) return result;

        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);

        while (!q.isEmpty()) {
            int size = q.size();
            for (int i = 0; i < size; i++) {
                TreeNode node = q.poll();
                if (i == size - 1) result.add(node.val); // rightmost
                if (node.left != null) q.offer(node.left);
                if (node.right != null) q.offer(node.right);
            }
        }
        return result;
    }
}
```

### ⚙️ O(n)

**Debug Tip:** print nodes in each level.

---

## 🧩 Summary Table — Tree Problems

| #  | Problem               | Type        | Approach                   | DS Used         | Time        | Debug Tip             |
| -- | --------------------- | ----------- | -------------------------- | --------------- | ----------- | --------------------- |
| 1  | Invert Tree           | Binary Tree | Swap children recursively  | Recursion       | O(n)        | Print before swap     |
| 2  | Max Depth             | Binary Tree | DFS depth                  | Recursion       | O(n)        | Print depth           |
| 3  | Same Tree             | Binary Tree | Compare recursively        | Recursion       | O(n)        | Print compared pairs  |
| 4  | Subtree               | Binary Tree | Check each node as root    | Recursion       | O(n*m)      | Log subtree roots     |
| 5  | LCA of BST            | BST         | Property-based traversal   | BST             | O(h)        | Track current root    |
| 6  | Level Order           | Binary Tree | BFS                        | Queue           | O(n)        | Print queue per level |
| 7  | Validate BST          | BST         | Range recursion            | Recursion       | O(n)        | Log min/max           |
| 8  | Kth Smallest          | BST         | Inorder traversal          | Stack/Recursion | O(k)        | Log visited values    |
| 9  | Construct Tree        | Binary Tree | Build via preorder/inorder | Recursion + Map | O(n)        | Print preorder index  |
| 10 | Max Path Sum          | Binary Tree | DFS + global max           | Recursion       | O(n)        | Log gains             |
| 11 | Serialize/Deserialize | Binary Tree | BFS encode/decode          | Queue           | O(n)        | Print encoded data    |
| 12 | Balanced Tree         | Binary Tree | Height diff ≤ 1            | Recursion       | O(n)        | Print heights         |
| 13 | Count Complete        | Binary Tree | Height comparison          | Recursion       | O((log n)²) | Print heights         |
| 14 | Right Side View       | Binary Tree | BFS → last per level       | Queue           | O(n)        | Print per level       |

---