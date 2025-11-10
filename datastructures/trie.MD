awesome 🌟 — you’re now entering one of the coolest and most **interview-loved data structures**:

# ✨ **Tries (Prefix Trees)**

let’s go step-by-step, from basics → implementation → real-world cases → solving 3 classic problems.

---

## 🌳 What is a Trie?

A **Trie** (pronounced “try”) is a *prefix tree* — a tree-like data structure used to **store and search strings efficiently**, especially when many strings share common prefixes.

each node:

* represents one **character**
* connects to children (possible next characters)
* may mark the **end of a valid word**

---

### 🧱 Trie Node Structure

in java, we usually define:

```java
class TrieNode {
    TrieNode[] children = new TrieNode[26]; // for 'a' to 'z'
    boolean isEndOfWord = false;
}
```

---

### 🔍 Visualization

let’s store: `"cat"`, `"car"`, `"dog"`

```
(root)
 ├── c
 │    └── a
 │         ├── t (end)
 │         └── r (end)
 └── d
      └── o
           └── g (end)
```

---

## ⚙️ When to Use a Trie

✅ Searching or autocompleting prefixes fast
✅ Dictionary word lookups
✅ Spell checkers
✅ Word games (like Boggle, Word Search)
✅ IP routing tables
✅ Search suggestions (like “g” → “google”, “gmail”)

---

## 💻 Basic Operations

| Operation     | Time Complexity | Description                                  |
| ------------- | --------------- | -------------------------------------------- |
| Insert word   | O(L)            | L = word length                              |
| Search word   | O(L)            | check if exists                              |
| Prefix search | O(L)            | check if prefix exists                       |
| Delete word   | O(L)            | recursively remove nodes if no longer needed |

---

# 🧩 Problem 1 — Implement Trie (Prefix Tree)

### 🧩 Problem

Design a Trie with operations:

* `insert(word)`
* `search(word)`
* `startsWith(prefix)`

---

### 🧠 Idea

use a root node (empty).
for each char in a word:

* if node doesn’t exist → create it
* move to next
  when end reached → mark `isEndOfWord = true`

---

### 💻 Java Code

```java
class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
}

class Trie {
    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    // Insert a word
    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int idx = c - 'a';
            if (node.children[idx] == null) {
                node.children[idx] = new TrieNode();
            }
            node = node.children[idx];
        }
        node.isEndOfWord = true;
    }

    // Search full word
    public boolean search(String word) {
        TrieNode node = findNode(word);
        return node != null && node.isEndOfWord;
    }

    // Check if prefix exists
    public boolean startsWith(String prefix) {
        return findNode(prefix) != null;
    }

    // Helper
    private TrieNode findNode(String s) {
        TrieNode node = root;
        for (char c : s.toCharArray()) {
            int idx = c - 'a';
            if (node.children[idx] == null) return null;
            node = node.children[idx];
        }
        return node;
    }
}
```

---

### ⚙️ Complexity

* Insert/Search/Prefix: `O(L)` (L = word length)
* Space: `O(26 * N * avgLen)` worst case

---

### 🧭 Debugging Tips

* print the traversal path of characters
* log when new nodes are created or existing reused
* check for off-by-one in char index (`c - 'a'`)

---

### 🎯 Real-World Example

* Autocomplete: `startsWith("go")` → ["good", "google", "golf"]
* Spell checker: `search("aple")` → false

---

# 🧩 Problem 2 — Design Add and Search Words Data Structure

*(Word Dictionary)*

### 🧩 Problem

Implement a data structure that supports:

* `addWord(word)`
* `search(pattern)` — where `pattern` can include `.` meaning *any character.*

**Example:**

```
addWord("bad")
addWord("dad")
addWord("mad")
search("pad") → false
search("bad") → true
search(".ad") → true
search("b..") → true
```

---

### 🧠 Core Idea

This is similar to `Trie`, but searching must handle wildcards (`.`).
If `.` found → try **all child nodes recursively.**

---

### 💻 Java Code

```java
class WordDictionary {
    private TrieNode root;

    public WordDictionary() {
        root = new TrieNode();
    }

    public void addWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int idx = c - 'a';
            if (node.children[idx] == null)
                node.children[idx] = new TrieNode();
            node = node.children[idx];
        }
        node.isEndOfWord = true;
    }

    public boolean search(String word) {
        return searchHelper(word, 0, root);
    }

    private boolean searchHelper(String word, int index, TrieNode node) {
        if (index == word.length()) return node.isEndOfWord;

        char c = word.charAt(index);
        if (c == '.') {
            // try all possibilities
            for (TrieNode child : node.children) {
                if (child != null && searchHelper(word, index + 1, child))
                    return true;
            }
            return false;
        } else {
            int idx = c - 'a';
            if (node.children[idx] == null) return false;
            return searchHelper(word, index + 1, node.children[idx]);
        }
    }
}
```

---

### ⚙️ Complexity

* Insert: `O(L)`
* Search: Worst `O(26^L)` if many wildcards
* Average much less

---

### 🧭 Debugging Tips

* Print recursion depth and current prefix
* Trace wildcard branches visually

---

### 🎯 Real-World Analogy

* Search bar with partial or fuzzy matching
* Regex engine simplified for word lookups

---

# 🧩 Problem 3 — Word Search II

*(Uses Trie for optimization)*

### 🧩 Problem

Given a board of letters and a list of words, find all words that exist in the grid.

Each word must be formed by **adjacent letters (up/down/left/right)** and can’t reuse the same cell twice.

**Example:**

```
board = [
  ['o','a','a','n'],
  ['e','t','a','e'],
  ['i','h','k','r'],
  ['i','f','l','v']
]
words = ["oath","pea","eat","rain"]

Output: ["oath","eat"]
```

---

### 🧱 Data Structures

* **Trie** → store all words for prefix lookup
* **DFS (Depth-First Search)** → explore board

---

### 🧠 Approach

1️⃣ Build Trie from all `words`
2️⃣ For each board cell:

* DFS to explore neighbors
* stop early if prefix not found in Trie
  3️⃣ Mark visited cells temporarily
  4️⃣ Collect words when `isEndOfWord=true`

---

### 💻 Java Code

```java
import java.util.*;

class WordSearchII {
    private Set<String> result = new HashSet<>();
    private int rows, cols;
    private TrieNode root;

    public List<String> findWords(char[][] board, String[] words) {
        // 1️⃣ Build Trie
        root = new TrieNode();
        for (String w : words) insert(w);

        rows = board.length;
        cols = board[0].length;

        // 2️⃣ DFS from each cell
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                dfs(board, i, j, root, "");
            }
        }
        return new ArrayList<>(result);
    }

    private void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int idx = c - 'a';
            if (node.children[idx] == null)
                node.children[idx] = new TrieNode();
            node = node.children[idx];
        }
        node.isEndOfWord = true;
    }

    private void dfs(char[][] board, int i, int j, TrieNode node, String word) {
        if (i < 0 || i >= rows || j < 0 || j >= cols || board[i][j] == '#')
            return;

        char c = board[i][j];
        TrieNode next = node.children[c - 'a'];
        if (next == null) return; // no prefix match

        word += c;
        if (next.isEndOfWord) result.add(word);

        board[i][j] = '#'; // mark visited
        dfs(board, i + 1, j, next, word);
        dfs(board, i - 1, j, next, word);
        dfs(board, i, j + 1, next, word);
        dfs(board, i, j - 1, next, word);
        board[i][j] = c; // backtrack
    }
}
```

---

### ⚙️ Complexity

* Building Trie: `O(W * L)`
* DFS: up to `O(M * N * 4^L)`
* With Trie pruning: much faster in practice

---

### 🧭 Debugging Tips

* Print prefix path in DFS
* Watch for visited cells (`#`)
* Verify when backtracking restores original char

---

### 🎯 Real-World Use Case

* Word puzzle solver (like Boggle or crossword)
* Autocomplete with spatial constraints
* AI for word games

---

## 🧩 Summary Table — Trie Problems

| # | Problem           | Key Operation            | DS Used          | Idea               | Time          | Real Analogy          |
| - | ----------------- | ------------------------ | ---------------- | ------------------ | ------------- | --------------------- |
| 1 | Implement Trie    | Insert, search, prefix   | Trie             | Character tree     | O(L)          | Autocomplete          |
| 2 | Add & Search Word | Search with '.' wildcard | Trie + Recursion | DFS search for '.' | O(26^L) worst | Regex-like dictionary |
| 3 | Word Search II    | Word discovery in grid   | Trie + DFS       | Prefix pruning     | O(MN·4^L)     | Crossword solver      |

---

### 🌟 Key Takeaways

* **Trie** is the go-to data structure for **prefix-based** search.
* Its biggest strength: **prefix pruning** — avoids redundant work.
* Combining **Trie + DFS** = magic for word problems.
* Mastering it helps you with **autocomplete systems, spell checkers, and AI assistants.**

---
