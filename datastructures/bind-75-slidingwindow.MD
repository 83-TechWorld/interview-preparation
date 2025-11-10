That's a fantastic set of problems for mastering the **Sliding Window** technique\! This technique is crucial for optimizing problems that deal with subarrays or substrings.

Here is a detailed explanation for the first problem: **Longest Substring Without Repeating Characters**, including the problem statement, a step-by-step example, a real-world use case, and the Java solution with helpful comments.

-----

## 💻 1. Longest Substring Without Repeating Characters

### Problem Statement

Given a string `s`, find the length of the **longest substring** without any repeating characters.

**Key Concept:** A **substring** is a contiguous sequence of characters within a string. "Without repeating characters" means every character in that substring must be unique.

| Input Example | Output | Explanation |
| :---: | :---: | :---: |
| `"abcabcbb"` | **3** | The longest substrings without repeating characters are `"abc"`, with a length of 3. |
| `"bbbbb"` | **1** | The longest substring is `"b"`, with a length of 1. |
| `"pwwkew"` | **3** | The longest substrings are `"wke"` or `"kew"`, with a length of 3. Note that `"pwke"` is a *subsequence* but not a *substring*. |

-----

### 💡 Sliding Window Solution Strategy

The Sliding Window approach uses two pointers, **`left`** (start of the window) and **`right`** (end of the window), to define the current substring/window.

1.  **Expand (Move `right`):** The `right` pointer moves forward, adding a new character to the window.
2.  **Check Condition:** We check if the new character is **already present** in the current window.
3.  **Contract (Move `left`):** If a duplicate is found, the window is invalid. We must move the `left` pointer forward, **shrinking** the window, until the duplicate is removed and the window is valid again.
4.  **Update Result:** After every expansion of the window (moving `right`), we calculate the length of the *current valid window* (`right - left + 1`) and update the **maximum length** found so far.

To efficiently check for duplicates, we'll use a **Hash Set (or Map)** to store the characters currently inside our window.

-----

### 🗺️ Real-World Use Case: Data Compression

This problem's concept is fundamental in various algorithms, notably:

  * **Data Compression (e.g., LZ77 algorithm):** Identifying the longest sequence of non-repeating data/characters is a step in determining how efficiently data can be compressed. A longer non-repeating sequence might suggest a more unique data block that needs to be encoded differently.
  * **Bioinformatics (DNA Sequencing):** Analyzing DNA sequences to find the longest unique (non-repeating) segments, which can be critical for gene mapping and mutation detection.

-----

### 👣 Step-by-Step Example

Let's trace the execution for the input string **`s = "a b c a b c b b"`**.

We use a **`Set`** to track characters in the window and two pointers: **`left`** and **`right`**. **`max_len`** tracks the longest length found.

| R (right) | Char `s[R]` | **Set (Window Content)** | L (left) | Condition Check | Action | `max_len` |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 0 | **`a`** | {a} | 0 | `a` is **NEW** | Add `a` to Set. Window: `[a]` | $0-0+1 = \mathbf{1}$ |
| 1 | **`b`** | {a, b} | 0 | `b` is **NEW** | Add `b` to Set. Window: `[a, b]` | $1-0+1 = \mathbf{2}$ |
| 2 | **`c`** | {a, b, c} | 0 | `c` is **NEW** | Add `c` to Set. Window: `[a, b, c]` | $2-0+1 = \mathbf{3}$ |
| 3 | **`a`** | {a, b, c} | 0 | `a` is **DUPLICATE** | **CONTRACT:** Remove `s[L]` (`a`) from Set. L becomes 1. Set: {b, c}. | 3 |
| 3 | **`a`** | {b, c, a} | 1 | Now `a` is **NEW** | Add `a` to Set. Window: `[b, c, a]` | $3-1+1 = \mathbf{3}$ |
| 4 | **`b`** | {b, c, a} | 1 | `b` is **DUPLICATE** | **CONTRACT:** Remove `s[L]` (`b`) from Set. L becomes 2. Set: {c, a}. | 3 |
| 4 | **`b`** | {c, a, b} | 2 | Now `b` is **NEW** | Add `b` to Set. Window: `[c, a, b]` | $4-2+1 = \mathbf{3}$ |
| 5 | **`c`** | {c, a, b} | 2 | `c` is **DUPLICATE** | **CONTRACT:** Remove `s[L]` (`c`) from Set. L becomes 3. Set: {a, b}. | 3 |
| 5 | **`c`** | {a, b, c} | 3 | Now `c` is **NEW** | Add `c` to Set. Window: `[a, b, c]` | $5-3+1 = \mathbf{3}$ |
| 6 | **`b`** | {a, b, c} | 3 | `b` is **DUPLICATE** | **CONTRACT:** Remove `s[L]` (`a`). L becomes 4. Set: {b, c}. | 3 |
| 6 | **`b`** | {b, c} | 4 | `b` is **DUPLICATE** | **CONTRACT:** Remove `s[L]` (`b`). L becomes 5. Set: {c}. | 3 |
| 6 | **`b`** | {c, b} | 5 | Now `b` is **NEW** | Add `b` to Set. Window: `[c, b]` | $6-5+1 = \mathbf{2}$ |
| 7 | **`b`** | {c, b} | 5 | `b` is **DUPLICATE** | **CONTRACT:** Remove `s[L]` (`c`). L becomes 6. Set: {b}. | 2 |
| 7 | **`b`** | {b} | 6 | `b` is **DUPLICATE** | **CONTRACT:** Remove `s[L]` (`b`). L becomes 7. Set: {}. | 2 |
| 7 | **`b`** | {b} | 7 | Now `b` is **NEW** | Add `b` to Set. Window: `[b]` | $7-7+1 = \mathbf{1}$ |

**Final Answer:** **3**

-----

### 💻 Java Solution

The solution uses a `HashSet<Character>` for $O(1)$ (constant time) insertion, deletion, and look-up, ensuring the overall time complexity is $O(N)$.

```java
import java.util.HashSet;
import java.util.Set;
import java.lang.Math;

class Solution {
    /**
     * Finds the length of the longest substring without repeating characters using the Sliding Window technique.
     * Time Complexity: O(N), where N is the length of the string, as each pointer (left and right) traverses the string once.
     * Space Complexity: O(min(N, K)), where K is the size of the character set (e.g., 26 for lowercase English letters, 128 for ASCII).
     */
    public int lengthOfLongestSubstring(String s) {
        // Handle edge case: if the string is null or empty, the length is 0.
        if (s == null || s.length() == 0) {
            return 0;
        }

        // 'charSet' will store the unique characters in the current sliding window.
        // We use a HashSet for O(1) average time complexity for add, remove, and contains operations.
        Set<Character> charSet = new HashSet<>();

        // 'n' is the length of the input string for loop boundaries.
        int n = s.length();
        
        // 'left' is the start pointer of the sliding window.
        int left = 0;
        
        // 'maxLength' stores the result: the length of the longest valid substring found so far.
        int maxLength = 0;

        // The 'right' pointer iterates through the string, expanding the window one character at a time.
        for (int right = 0; right < n; right++) {
            // Get the current character at the right end of the window.
            char currentChar = s.charAt(right);

            // *** 1. Contract the Window (Invalid State) ***
            // If the current character is ALREADY in the set, it means we have a duplicate.
            // We must shrink the window from the left until the duplicate is removed.
            while (charSet.contains(currentChar)) {
                // Remove the character at the 'left' pointer from the set.
                charSet.remove(s.charAt(left));
                
                // Move the 'left' pointer one step to the right, effectively shrinking the window.
                left++;
            }

            // *** 2. Expand the Window (Valid State) ***
            // After contracting (if necessary), the window is valid again.
            // Add the 'currentChar' (which is guaranteed to be unique now) to the set.
            charSet.add(currentChar);

            // *** 3. Update the Result ***
            // The length of the current valid window is 'right - left + 1'.
            // Update the maximum length found so far.
            maxLength = Math.max(maxLength, right - left + 1);
        }

        // Return the final maximum length.
        return maxLength;
    }
}
```

-----
Awesome\! Let's dive into the second problem, which introduces a slightly more complex condition to the Sliding Window technique: **Longest Repeating Character Replacement**.

-----

## 💻 2. Longest Repeating Character Replacement

### Problem Statement

You are given a string `s` and an integer `k`. You can choose any character in the string and change it to any other uppercase English character. You can perform this operation at most **`k`** times.

The goal is to find the length of the **longest substring** containing the **same letter** after performing the operations.

**Key Concept:** The sliding window defines the current substring. For this window to be valid, the count of its **most frequent character** plus the number of allowed **replacements (`k`)** must be greater than or equal to the total **window length**.

| Input Example | `k` | Output | Explanation |
| :---: | :---: | :---: | :---: |
| `"AABABBA"` | 1 | **4** | We can change the middle 'B' to 'A' to get "AAAABBA". The longest repeating substring is "AAAA" (length 4). |
| `"ABAB"` | 2 | **4** | We can change both 'B's to 'A's, or both 'A's to 'B's. Either way, the final string is "AAAA" or "BBBB" (length 4). |

-----

### 💡 Sliding Window Solution Strategy

The trick here is to use the sliding window to find the longest *valid* window. A window is valid if we can make all characters in it the same using at most `k` replacements.

1.  **Expand (Move `right`):** The `right` pointer moves, adding a character to the window. We update the frequency count for the new character.
2.  **Track Frequencies:** Inside the window, we must always know the count of the **most frequent character** (`max_freq`).
3.  **Check Validity (The Core Formula):** We check the current window validity using this crucial formula:
    $$\text{Window Length} - \text{Max Frequency} \leq k$$
      * $\text{Window Length} - \text{Max Frequency}$ gives us the count of characters that are **NOT** the most frequent character. These are exactly the characters that need to be replaced.
      * If this count is less than or equal to `k`, the window is **VALID**.
4.  **Contract (Move `left`):** If the window is **INVALID** (i.e., $\text{Window Length} - \text{Max Frequency} > k$), we must move the `left` pointer forward, shrinking the window. Before moving `left`, we decrement the frequency count of the character being removed.
5.  **Update Result:** Since we only contract the window when it becomes invalid, the window size only ever increases or stays the same (it never shrinks the *maximum* length). Therefore, we only update the result (`max_len`) when the window expands (moving `right`).

-----

### 🗺️ Real-World Use Case: Data Stream Pre-processing and Error Correction

  * **Error Correction Codes (ECC):** Imagine a stream of data where occasional bit flips (errors) occur. This problem is analogous to finding the longest segment of data that can be made perfectly consistent (e.g., all 0s or all 1s) with a limited number of allowed "flips" (`k`). It helps in identifying large, locally consistent blocks of data for efficient decoding or reconstruction.
  * **Bioinformatics (Consensus Sequences):** Similar to the previous problem, but here we look for the longest sequence segment where a consensus (the most frequent base) can be established, allowing for a few permissible mutations (`k`).

-----

### 👣 Step-by-Step Example

Let's trace the execution for the input string **`s = "A A B A B B A"`** and **`k = 1`**.

We use an array (`counts`) to track frequencies and two pointers: **`left`** and **`right`**. **`max_freq`** tracks the max frequency in the window, and **`max_len`** tracks the result.

| L | R | Char `s[R]` | **Window** | `counts` | `max_freq` | Window Len | Needed Replacements (Len - Freq) | Validity Check (Replacements $\leq k=1$) | Action | `max_len` |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 0 | 0 | `A` | [A] | A:1 | 1 | 1 | $1 - 1 = 0$ | $0 \leq 1$ (**VALID**) | Expand | $\mathbf{1}$ |
| 0 | 1 | `A` | [A A] | A:2 | 2 | 2 | $2 - 2 = 0$ | $0 \leq 1$ (**VALID**) | Expand | $\mathbf{2}$ |
| 0 | 2 | `B` | [A A B] | A:2, B:1 | 2 | 3 | $3 - 2 = 1$ | $1 \leq 1$ (**VALID**) | Expand | $\mathbf{3}$ |
| 0 | 3 | `A` | [A A B A] | A:3, B:1 | 3 | 4 | $4 - 3 = 1$ | $1 \leq 1$ (**VALID**) | Expand | $\mathbf{4}$ |
| 0 | 4 | `B` | [A A B A B] | A:3, B:2 | 3 | 5 | $5 - 3 = 2$ | $2 > 1$ (**INVALID**) | **Contract** | 4 |
| **1** | 4 | (remove `s[0]`=`A`) | [A B A B] | A:2, B:2 | 2 | 4 | $4 - 2 = 2$ | $2 > 1$ (**INVALID**) | **Contract** | 4 |
| **2** | 4 | (remove `s[1]`=`A`) | [B A B] | A:1, B:2 | 2 | 3 | $3 - 2 = 1$ | $1 \leq 1$ (**VALID**) | Expand | 4 |
| 2 | 5 | `B` | [B A B B] | A:1, B:3 | 3 | 4 | $4 - 3 = 1$ | $1 \leq 1$ (**VALID**) | Expand | $\mathbf{4}$ |
| 2 | 6 | `A` | [B A B B A] | A:2, B:3 | 3 | 5 | $5 - 3 = 2$ | $2 > 1$ (**INVALID**) | **Contract** | 4 |
| **3** | 6 | (remove `s[2]`=`B`) | [A B B A] | A:2, B:2 | 2 | 4 | $4 - 2 = 2$ | $2 > 1$ (**INVALID**) | **Contract** | 4 |
| **4** | 6 | (remove `s[3]`=`A`) | [B B A] | A:1, B:2 | 2 | 3 | $3 - 2 = 1$ | $1 \leq 1$ (**VALID**) | Expand | 4 |

**Final Answer:** **4**

-----

### 💻 Java Solution

We use an array of size 26 (since the problem specifies uppercase English letters) to efficiently track character frequencies.

```java
import java.lang.Math;

class Solution {
    /**
     * Finds the length of the longest substring with the same character after at most k replacements.
     * Time Complexity: O(N), where N is the length of the string. The 'right' pointer traverses N times.
     * The 'left' pointer also traverses at most N times in total (amortized O(1) for the inner loop).
     * Space Complexity: O(1), as the frequency array size is fixed at 26 (for 'A'-'Z').
     */
    public int characterReplacement(String s, int k) {
        // We use an array of size 26 to store the count of each uppercase letter ('A' through 'Z').
        // Since we only deal with uppercase English letters, an array is highly efficient.
        int[] counts = new int[26];
        
        // 'left' is the start pointer of the sliding window.
        int left = 0;
        
        // 'maxLength' stores the result: the length of the longest valid window found so far.
        int maxLength = 0;
        
        // 'maxFreq' tracks the frequency of the MOST frequent character within the current window [left, right].
        // This is crucial for calculating replacements needed.
        int maxFreq = 0;

        // The 'right' pointer iterates through the string, expanding the window.
        for (int right = 0; right < s.length(); right++) {
            
            // 1. EXPAND the window and update frequencies
            char currentChar = s.charAt(right);
            // Convert character to 0-25 index (e.g., 'A' -> 0, 'B' -> 1)
            int charIndex = currentChar - 'A';
            
            // Increment the count for the current character.
            counts[charIndex]++;
            
            // Update the frequency of the most frequent character in the current window.
            // Note: We don't need to recalculate maxFreq from scratch; we only update it if the current char's count is higher.
            maxFreq = Math.max(maxFreq, counts[charIndex]);

            // 2. CHECK for CONTRACTION (Validity Check)
            // The current window length is: (right - left + 1)
            // The characters that NEED replacement are: Window Length - maxFreq
            // If the number of characters to replace is greater than k, the window is INVALID and must shrink.
            int windowLength = right - left + 1;
            if (windowLength - maxFreq > k) {
                
                // *** Window is INVALID, so contract from the left ***
                
                // Get the character that is about to leave the window.
                char charLeaving = s.charAt(left);
                int leavingIndex = charLeaving - 'A';
                
                // Decrement its count since it is leaving the window.
                counts[leavingIndex]--;
                
                // Move the left pointer forward, shrinking the window.
                left++;
                
                // NOTE: We don't necessarily update maxFreq here. maxFreq might decrease, but
                // since we are only interested in finding the MAXIMUM length, we can afford
                // to keep an outdated (higher) maxFreq. A smaller maxFreq would only make the
                // contraction condition (windowLength - maxFreq > k) harder to satisfy, but 
                // since we are only aiming for max length, we don't need to update maxFreq 
                // in the contraction step. If maxFreq is kept high, the window size may not 
                // shrink, and the length remains the same or grows, which is what we want.
                
            }
            
            // 3. UPDATE Result
            // Since we only contract the window when it is too big (invalid), 'maxLength'
            // is only updated when the window successfully expands.
            // The window length at this point is either a new, longer valid window, or
            // the same length as the previous best valid window.
            maxLength = Math.max(maxLength, right - left + 1);
        }

        // Return the final maximum length found.
        return maxLength;
    }
}
```

-----

Excellent\! The third problem, **Minimum Window Substring**, is often considered the most challenging of the basic Sliding Window problems, as it involves tracking character counts across two different strings.

-----

## 💻 3. Minimum Window Substring

### Problem Statement

Given two strings, `s` (the source string) and `t` (the target string), find the **shortest contiguous substring** in `s` that contains **all the characters** from `t`, including duplicates, if they exist.

If no such substring exists, return an empty string `""`.

**Key Concept:** This problem is a "matching" problem. The sliding window in `s` must be checked against the character frequency map of `t`. We are looking for the *minimum* length window that satisfies the condition.

| Input Example | `s` (Source) | `t` (Target) | Output | Explanation |
| :---: | :---: | :---: | :---: | :---: |
| 1 | `"ADOBECODEBANC"` | `"ABC"` | `"BANC"` | The shortest substring in `s` containing 'A', 'B', and 'C' is "BANC". |
| 2 | `"a"` | `"a"` | `"a"` | The shortest substring is `s` itself. |
| 3 | `"a"` | `"aa"` | `""` | `s` only has one 'a', but `t` requires two. No valid substring exists. |

-----

### 💡 Sliding Window Solution Strategy

The strategy involves using two frequency maps (or arrays) and a count of how many characters from `t` are currently "matched" inside the window of `s`.

1.  **Preparation (Target Map):** Create a frequency map (`t_map`) for all characters in the target string `t`. Also, initialize a variable, `required`, which is the count of *unique* characters in `t` (or simply the size of the `t_map`).
2.  **Tracking (Window Map):** Use a second frequency map (`window_map`) to track the character counts *inside* the current sliding window of `s`.
3.  **Matching Variable:** Initialize a variable, `formed`, to track how many characters in `t` have had their *required frequency* met within the current window of `s`.
4.  **Expand (Move `right`):**
      * Add `s[right]` to the window and update `window_map`.
      * **Crucial Step:** Check if the count of `s[right]` in `window_map` now matches the required count in `t_map`. If it does, increment `formed`.
5.  **Contract (Move `left`):** Once `formed` equals `required` (meaning the window is a **valid match**), we try to shrink the window from the left to find the minimum possible length:
      * Record the current window's length and position if it is the new minimum.
      * Remove `s[left]` from the window and update `window_map`.
      * **Crucial Step:** Check if removing `s[left]` caused the count in `window_map` to drop **below** the required count in `t_map`. If it did, decrement `formed`.
      * Move `left` forward and repeat the check until `formed` is less than `required` (window is now invalid).

-----

### 🗺️ Real-World Use Case: Log Analysis and Pattern Detection

  * **Log Filtering:** Imagine you're analyzing server logs and need to find the shortest possible sequence of log entries that contains a specific set of critical events (e.g., `"Authentication Failed"`, `"Database Disconnect"`, and `"System Restart"`) in any order. `s` is the entire log file, and `t` is the set of critical events. Finding the minimum window helps isolate the core problem sequence.
  * **Bioinformatics (Motif Finding):** Searching a large DNA sequence (`s`) for the shortest region that contains a specific collection of necessary sub-sequences or "motifs" (`t`).

-----

### 👣 Step-by-Step Example

Let's trace the execution for the input string **`s = "A D O B E C O D E B A N C"`** and **`t = "A B C"`**.

**Preparation:** `t_map` = {A: 1, B: 1, C: 1}. `required` = 3.

We track `window_map`, `formed`, `left`, `right`, and the best result found so far (`min_len`, `start_index`).

| R | Char `s[R]` | `window_map` | `formed` | **Validity** | L | Action | `min_len` / Substring |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 0 | `A` | {A: 1} | $\to$ 1 (A matches) | No | 0 | Expand | $\infty$ |
| 1 | `D` | {A: 1, D: 1} | 1 | No | 0 | Expand | $\infty$ |
| 2 | `O` | {A: 1, O: 1} | 1 | No | 0 | Expand | $\infty$ |
| 3 | `B` | {A: 1, B: 1} | $\to$ 2 (B matches) | No | 0 | Expand | $\infty$ |
| 4 | `E` | {...} | 2 | No | 0 | Expand | $\infty$ |
| 5 | `C` | {... C: 1} | $\to$ 3 (C matches) | **YES** | 0 | **Contract Phase** | $\infty$ |
| **Contract (Window: ADOBEC)** |
| 5 | `C` | {...} | 3 | YES | 0 | **New Min\!** $5-0+1=6$. Start=0. | $\mathbf{6}$ / ADOBEC |
| - | `s[L]`=`A`. Remove A. | {A: 0} | $\to$ 2 (A drops below 1) | **No** | $\to$ 1 | Stop contraction. | 6 / ADOBEC |
| **Expand Phase** |
| 6 | `O` | {...} | 2 | No | 1 | Expand | 6 / ADOBEC |
| 7 | `D` | {...} | 2 | No | 1 | Expand | 6 / ADOBEC |
| 8 | `E` | {...} | 2 | No | 1 | Expand | 6 / ADOBEC |
| 9 | `B` | {... B: 1} | $\to$ 3 (B matches) | **YES** | 1 | **Contract Phase** | 6 / ADOBEC |
| **Contract (Window: DOBECODEB)** |
| 9 | `B` | {...} | 3 | YES | 1 | Min: 6. $9-1+1=9$. (Keep 6) | 6 / ADOBEC |
| - | `s[L]`=`D`. Remove D. | {...} | 3 | YES | $\to$ 2 | Shrink. | 6 / ADOBEC |
| - | `s[L]`=`O`. Remove O. | {...} | 3 | YES | $\to$ 3 | Shrink. | 6 / ADOBEC |
| - | `s[L]`=`B`. Remove B. | {B: 0} | $\to$ 2 (B drops below 1) | **No** | $\to$ 4 | Stop contraction. | 6 / ADOBEC |
| **Expand Phase** |
| 10 | `A` | {... A: 1} | $\to$ 3 (A matches) | **YES** | 4 | **Contract Phase** | 6 / ADOBEC |
| **Contract (Window: EC O D E B A)** |
| 10 | `A` | {...} | 3 | YES | 4 | Min: 6. $10-4+1=7$. (Keep 6) | 6 / ADOBEC |
| - | `s[L]`=`E`. Remove E. | {...} | 3 | YES | $\to$ 5 | Shrink. | 6 / ADOBEC |
| - | `s[L]`=`C`. Remove C. | {C: 0} | $\to$ 2 (C drops below 1) | **No** | $\to$ 6 | Stop contraction. | 6 / ADOBEC |
| **Expand Phase** |
| 11 | `N` | {...} | 2 | No | 6 | Expand | 6 / ADOBEC |
| 12 | `C` | {... C: 1} | $\to$ 3 (C matches) | **YES** | 6 | **Contract Phase** | 6 / ADOBEC |
| **Contract (Window: CODEBANC)** |
| 12 | `C` | {...} | 3 | YES | 6 | **New Min\!** $12-6+1=7$. (Keep 6) | 6 / ADOBEC |
| - | `s[L]`=`O`. Remove O. | {...} | 3 | YES | $\to$ 7 | Shrink. | 6 / ADOBEC |
| - | `s[L]`=`D`. Remove D. | {...} | 3 | YES | $\to$ 8 | Shrink. | 6 / ADOBEC |
| - | `s[L]`=`E`. Remove E. | {...} | 3 | YES | $\to$ 9 | Shrink. | 6 / ADOBEC |
| - | `s[L]`=`B`. Remove B. | {B: 0} | $\to$ 2 (B drops below 1) | **No** | $\to$ 10 | Stop contraction. | 6 / ADOBEC |
| ... The loop ends. |

Wait, let's re-examine the last contraction carefully, the example above missed a smaller window in the final contract step:

| R | Char `s[R]` | Window | L | Action | Current Substring | Window Len | Min Len | Start Index |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 12 | `C` | ADOBECODEBANC | 6 | **Contract Phase** (Window: CODEBANC) | CODEBANC | 7 | 6 | 0 |
| **L=6** | `s[6]`=`O`. Remove O. | CODEBANC | 7 | Shrink. | ODEBANC | 6 | 6 | 0 |
| **L=7** | `s[7]`=`D`. Remove D. | CODEBANC | 8 | Shrink. | DEBANC | 5 | $\to \mathbf{5}$ | $\to \mathbf{8}$ |
| **L=8** | `s[8]`=`E`. Remove E. | CODEBANC | 9 | Shrink. | EBANC | 4 | $\to \mathbf{4}$ | $\to \mathbf{9}$ |
| **L=9** | `s[9]`=`B`. Remove B. | **B** in window drops to 0. | 10 | **Formed $\to$ 2**. Stop. | BANC | 4 | 4 | 9 |

The substring at $L=9, R=12$ is **"BANC"** with length 4.

**Final Answer:** **"BANC"**

-----

### 💻 Java Solution

This solution uses two `HashMaps` for robust character counting, supporting any character set (not just uppercase letters).

```java
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

class Solution {
    /**
     * Finds the minimum window substring using the Sliding Window technique with two HashMaps.
     * Time Complexity: O(S + T), where S and T are the lengths of strings s and t.
     * The right pointer traverses S once, and the left pointer traverses at most S times.
     * Space Complexity: O(K), where K is the size of the character set (at most 52 for standard ASCII/letters).
     */
    public String minWindow(String s, String t) {
        // Handle edge cases
        if (s.length() == 0 || t.length() == 0 || s.length() < t.length()) {
            return "";
        }

        // 1. Preparation: Create a frequency map for the target string 't'
        // This stores the COUNT of characters we MUST include.
        Map<Character, Integer> t_map = new HashMap<>();
        for (char c : t.toCharArray()) {
            t_map.put(c, t_map.getOrDefault(c, 0) + 1);
        }

        // 'required' is the number of unique characters in 't' that must be matched.
        int required = t_map.size();
        
        // 'window_map' stores the frequency of characters in the current sliding window [left, right] of 's'.
        Map<Character, Integer> window_map = new HashMap<>();
        
        // 'formed' counts how many unique characters in 't' have met their required frequency in the window.
        int formed = 0;

        // Pointers for the sliding window
        int left = 0;
        
        // Variables to store the result: minimum length, and the start index of that minimum window.
        int minLength = Integer.MAX_VALUE;
        int minStart = 0;
        
        // The 'right' pointer expands the window.
        for (int right = 0; right < s.length(); right++) {
            char charRight = s.charAt(right);

            // 1. EXPAND the window (move right)
            
            // Only consider characters that are actually in 't'
            if (t_map.containsKey(charRight)) {
                // Add the character to the window's frequency map
                window_map.put(charRight, window_map.getOrDefault(charRight, 0) + 1);

                // Check if the current character's count in the window now satisfies the requirement in 't'
                if (window_map.get(charRight).intValue() == t_map.get(charRight).intValue()) {
                    formed++; // One more unique required character is now 'formed'
                }
            }

            // 2. CONTRACT the window (move left)
            // When 'formed' equals 'required', we have a valid window. Now, try to minimize it.
            while (left <= right && formed == required) {
                // Get the current window length
                int currentLength = right - left + 1;
                
                // Update the minimum length and its start index if the current window is smaller
                if (currentLength < minLength) {
                    minLength = currentLength;
                    minStart = left;
                }

                // Get the character leaving the window from the left
                char charLeft = s.charAt(left);

                // Check if the character leaving is one we care about (i.e., in 't')
                if (t_map.containsKey(charLeft)) {
                    // Check if its removal will break the 'formed' condition
                    // If the current window count EQUALS the required count, removing it breaks the match
                    if (window_map.get(charLeft).intValue() == t_map.get(charLeft).intValue()) {
                        formed--; // Decrement 'formed' because this required character is now 'unformed'
                    }
                    // Decrease the count in the window map
                    window_map.put(charLeft, window_map.get(charLeft) - 1);
                }

                // Shrink the window by moving the left pointer
                left++;
            }
        }

        // After the loop, if minLength is still the initial value, no valid window was found.
        return minLength == Integer.MAX_VALUE 
               ? "" 
               : s.substring(minStart, minStart + minLength);
    }
}
```

-----
Absolutely\! The fourth problem, **Permutation in String**, is a fantastic application of the Sliding Window combined with frequency maps, much like the previous problem, but simpler because we only care about the *count* of characters, not their relative order.

-----

## 💻 4. Permutation in String

### Problem Statement

Given two strings, `s1` and `s2`, return `true` if `s2` contains a **permutation** of `s1`, or `false` otherwise.

A permutation of a string is simply a rearrangement of its characters. In other words, we are looking for a **substring** in `s2` that has the exact same character counts as `s1`.

**Key Concept:** The sliding window in `s2` must be the same length as `s1`. We check if the frequency map of the current window matches the frequency map of `s1`.

| Input Example | `s1` (Target) | `s2` (Source) | Output | Explanation |
| :---: | :---: | :---: | :---: | :---: |
| 1 | `"ab"` | `"eidbaooo"` | **true** | `"ba"` is a permutation of `"ab"` and is a substring of `s2`. |
| 2 | `"ab"` | `"eidboaoo"` | **false** | No substring of length 2 in `s2` has one 'a' and one 'b'. |
| 3 | `"aabc"` | `"abcbaac"` | **true** | `"cbaa"` (indices 3-6) is a permutation of `"aabc"` ($\text{2 A's, 1 B, 1 C}$). |

-----

### 💡 Sliding Window Solution Strategy

Since a permutation must have the same length, this is a fixed-size sliding window problem.

1.  **Preparation (Target Map):** Create a frequency map (`s1_map`) for all characters in the target string `s1`. Since the problem deals with lowercase letters, a simple array of size 26 is efficient.
2.  **Initial Window:** Create the first sliding window in `s2` that has the same length as `s1`. Create a frequency map for this initial window (`s2_window_map`).
3.  **Check Match:** Compare `s1_map` and `s2_window_map`. If they are identical, return `true`.
4.  **Slide the Window:** Use a loop to move the window one step at a time, starting from the second character in `s2`.
      * **Add (Move `right`):** Increment the frequency count of the new character entering the window.
      * **Remove (Move `left`):** Decrement the frequency count of the character leaving the window.
5.  **Re-Check Match:** After each slide, check if the updated `s2_window_map` matches `s1_map`. If a match is found, return `true`. If the loop finishes without a match, return `false`.

**Optimization:** Instead of comparing the entire 26-element arrays (or maps) in every step, we can track a single variable, `matches`, which counts how many of the 26 possible character counts in the window currently match the required counts in $s1$.

-----

### 🗺️ Real-World Use Case: Code & Virus Detection

  * **Malware Signature Detection:** A simplified version of this problem is used in virus scanning. A known virus signature (`s1`) is often a specific sequence or pattern of bytes. If a small, fixed-length section of a file (`s2`) has the exact same byte frequency or content as the signature, it strongly suggests a match, regardless of the order of the bytes (i.e., a permutation).
  * **Anagram Grouping:** Although not a sliding window directly, the core idea of using character counts to define permutations is fundamental to grouping anagrams (words that are permutations of each other) in large text datasets.

-----

### 👣 Step-by-Step Example (Optimized with `matches` count)

Let's trace the execution for the input string **`s1 = "ab"`** and **`s2 = "eidbaooo"`**.

**Preparation:** Length of `s1` is $L=2$. Target map (`s1_map`) = {a: 1, b: 1}. We use arrays for simplicity.

| Array Index | 0 (a) | 1 (b) | ... | 25 (z) |
| :---: | :---: | :---: | :---: | :---: |
| **`s1_map`** | 1 | 1 | 0 | 0 |

**Sliding Window (`s2_window_map`)**

| L | R | Window (`s2[L:R]`) | Char OUT (`s2[L-1]`) | Char IN (`s2[R]`) | `s2_window_map` (a, b, d, e, i, o) | `matches` (Target: 1a, 1b) | Match? | Result |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 0 | 1 | **"ei"** | - | e, i | {e:1, i:1} | 0 | No | - |
| **1** | **2** | **"id"** | e (out) | d (in) | {d:1, i:1} | 0 | No | - |
| **2** | **3** | **"db"** | i (out) | b (in) | {d:1, b:1} | 1 (b matches) | No | - |
| **3** | **4** | **"ba"** | d (out) | a (in) | {b:1, a:1} | **2 (a, b match)** | **YES** | **True** |

**Final Answer:** **true**

-----

### 💻 Java Solution

This solution uses two 26-element integer arrays to track frequencies, which is faster than using HashMaps for a fixed alphabet size. The optimized method tracks the number of **matching** counts to avoid full array comparisons.

```java
import java.util.Arrays;

class Solution {
    /**
     * Checks if s2 contains a permutation of s1 using a fixed-size Sliding Window.
     * Time Complexity: O(L1 + L2), where L1 is s1.length() and L2 is s2.length().
     * We build the s1 map (O(L1)) and then slide the window across s2 (O(L2)).
     * Space Complexity: O(1), as the space used by the frequency arrays is fixed at 26.
     */
    public boolean checkInclusion(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        // If s1 is longer than s2, s2 cannot possibly contain a permutation of s1.
        if (len1 > len2) {
            return false;
        }

        // We will use two arrays (frequency maps) of size 26 for 'a' through 'z'.
        // This is efficient because we know the character set is fixed and small.
        int[] s1_map = new int[26];
        int[] window_map = new int[26];

        // 1. Initialize s1_map and the initial window_map (first s1.length() characters of s2)
        for (int i = 0; i < len1; i++) {
            // Count characters for s1
            s1_map[s1.charAt(i) - 'a']++;
            // Count characters for the initial window in s2
            window_map[s2.charAt(i) - 'a']++;
        }
        
        // 'matches' tracks how many frequency counts in the window EXACTLY match the required counts in s1.
        // We only need 26 matches (all character counts must be the same).
        int matches = 0;
        for (int i = 0; i < 26; i++) {
            if (s1_map[i] == window_map[i]) {
                matches++;
            }
        }

        // 2. Slide the window one character at a time
        // The loop starts where the initial window ends.
        for (int right = len1; right < len2; right++) {
            // Check for a match BEFORE sliding
            if (matches == 26) {
                return true; // Found a permutation!
            }

            // A. Character Enters (Expansion)
            char charIn = s2.charAt(right);
            int indexIn = charIn - 'a';

            // Check if adding this character breaks or forms a match
            if (s1_map[indexIn] == window_map[indexIn]) {
                matches--; // This character was matched, but the count is about to change, so un-match it.
            }
            window_map[indexIn]++; // Add the character
            if (s1_map[indexIn] == window_map[indexIn]) {
                matches++; // Now the count matches again (or matches for the first time).
            }

            // B. Character Leaves (Contraction)
            int left = right - len1; // Index of the character leaving the window
            char charOut = s2.charAt(left);
            int indexOut = charOut - 'a';
            
            // Check if removing this character breaks or forms a match
            if (s1_map[indexOut] == window_map[indexOut]) {
                matches--; // This character was matched, but the count is about to change, so un-match it.
            }
            window_map[indexOut]--; // Remove the character
            if (s1_map[indexOut] == window_map[indexOut]) {
                matches++; // Now the count matches again (usually when s1_map[indexOut] is 0 and window_map[indexOut] returns to 0).
            }
        }

        // 3. Final Check (for the very last window)
        // The check inside the loop covers the rest, but the final window check must happen after the last slide.
        return matches == 26;
    }
}
```

-----

Great\! The fifth and final problem, **Sliding Window Maximum**, is a classic that introduces the concept of a **Monotonic Queue (or Deque)** to efficiently solve the problem.

-----

## 💻 5. Sliding Window Maximum

### Problem Statement

You are given an array of integers `nums` and an integer `k`, which is the size of the sliding window. The window moves from the far left to the far right. At each step, the window slides one position to the right.

Your task is to return an array containing the **maximum value** in the sliding window for every position it stops at.

**Key Concept:** Instead of recalculating the maximum of the entire window of size $k$ every time it slides, we use a **Double-Ended Queue (Deque)** to store only the *indices* of the potentially largest elements in the current window. This queue is kept **monotonically decreasing** (largest element at the front).

| Input Example | `nums` | `k` (Window Size) | Output (Max at each step) | Explanation |
| :---: | :---: | :---: | :---: | :---: |
| 1 | `[1, 3, -1, -3, 5, 3, 6, 7]` | 3 | `[3, 3, 5, 5, 6, 7]` | Max values: $\max(\{1, 3, -1\})=3$, $\max(\{3, -1, -3\})=3$, $\max(\{-1, -3, 5\})=5$, etc. |
| 2 | `[1, -1]` | 1 | `[1, -1]` | Window size 1, so the max is just the element itself. |

-----

### 💡 Sliding Window + Deque Strategy

The key to achieving an $O(N)$ solution (where $N$ is the array length) is using a **Deque** (Double-Ended Queue) to store **indices** with these rules:

1.  **Maintain Monotonicity (Adding `nums[right]`):** Before adding the new index `right`, look at the **back** of the Deque. As long as the number at the back (`nums[Deque.peekLast()]`) is **smaller** than the new number (`nums[right]`), remove the index from the back. We do this because the smaller number will *never* be the maximum as long as the new, larger number is in the window.
2.  **Add New Element:** Add the current index `right` to the **back** of the Deque.
3.  **Check Window Boundary (Removing `nums[left]`):** After adding, check the **front** of the Deque. If the index at the front (`Deque.peekFirst()`) is now outside the window (i.e., less than or equal to `right - k`), remove it from the **front**.
4.  **Record Maximum:** Once the window is fully formed (i.e., `right >= k - 1`), the maximum element in the current window is always at the **index stored at the front** of the Deque. Record `nums[Deque.peekFirst()]` in the result array.

-----

### 🗺️ Real-World Use Case: Stock Market and Data Stream Analysis

  * **Stock Market Ticker Analysis:** This is the most common example. Analysts often need to know the **highest price** a stock reached within the last $k$ minutes (a moving average maximum). The array is the stock price stream, $k$ is the time window, and the output is the maximum price for every window.
  * **Sensor Data Spike Detection:** In large-scale sensor networks (e.g., temperature, pressure), you might want to track the peak reading within the last $k$ data points to detect short, localized spikes or anomalies efficiently without iterating over $k$ points every time.
  * **Game Development (Ledge Detection):** In some physics engines, determining the maximum height of the terrain within a certain range ($k$) is needed to efficiently check if a character can walk up a slope or is at a "ledge" point.

-----

### 👣 Step-by-Step Example

Let's trace the execution for **`nums = [1, 3, -1, -3, 5, 3, 6, 7]`** and **`k = 3`**.

**Deque (stores indices) must be Monotonically Decreasing based on `nums[index]` value.**

| R | `nums[R]` | **Deque Maintenance (Pop Back)** | **Deque After Pop Back** | **Boundary Check (Pop Front)** | Deque Final | Window Formed? (`R $\geq$ 2`) | Output (Max is `nums[Front]`) | Result Array |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 0 | **1** | None | [0] | None | [0] | No | - | [] |
| 1 | **3** | Pop 0 (since $1 < 3$) | [1] | None | [1] | No | - | [] |
| 2 | **-1** | None | [1, 2] | None | [1, 2] | **YES** | `nums[1] = 3` | [3] |
| 3 | **-3** | None | [1, 2, 3] | Pop 1 ($\text{1} = \text{3} - \text{3} + \text{1} \Rightarrow \text{out}$) | [2, 3] | YES | `nums[2] = 3` | [3, 3] |
| 4 | **5** | Pop 3 (-3 \< 5), Pop 2 (-1 \< 5) | [4] | None | [4] | YES | `nums[4] = 5` | [3, 3, 5] |
| 5 | **3** | None | [4, 5] | None | [4, 5] | YES | `nums[4] = 5` | [3, 3, 5, 5] |
| 6 | **6** | Pop 5 (3 \< 6), Pop 4 (5 \< 6) | [6] | Pop 4 ($\text{4} = \text{6} - \text{3} + \text{1} \Rightarrow \text{out}$) | [6] | YES | `nums[6] = 6` | [3, 3, 5, 5, 6] |
| 7 | **7** | Pop 6 (6 \< 7) | [7] | None | [7] | YES | `nums[7] = 7` | [3, 3, 5, 5, 6, 7] |

**Final Answer:** **`[3, 3, 5, 5, 6, 7]`**

-----

### 💻 Java Solution

This solution uses Java's `ArrayDeque` to implement the monotonic queue efficiently.

```java
import java.util.ArrayDeque;
import java.util.Deque;

class Solution {
    /**
     * Finds the maximum element in every sliding window of size k using a Monotonic Deque.
     * Time Complexity: O(N), where N is the length of the array. Each element is added to the Deque once 
     * and removed from the Deque at most twice (once from the back, once from the front).
     * Space Complexity: O(k), as the Deque stores at most k indices.
     */
    public int[] maxSlidingWindow(int[] nums, int k) {
        // Handle edge cases
        if (nums == null || nums.length == 0 || k == 0) {
            return new int[0];
        }

        int n = nums.length;
        // The result array size will be N - k + 1 (the number of full windows).
        int[] result = new int[n - k + 1];
        int resultIndex = 0;

        // Deque (Double-Ended Queue) stores the INDICES of elements.
        // It maintains a monotonically DECREASING sequence of values (nums[index] is decreasing from front to back).
        Deque<Integer> deque = new ArrayDeque<>();

        // The 'right' pointer iterates through the array, expanding the window.
        for (int right = 0; right < n; right++) {
            
            // 1. Maintain Monotonicity (Contraction from the Back)
            // If the new element (nums[right]) is GREATER than the element at the back of the Deque,
            // the element at the back can never be the maximum in the future (because nums[right] is now in the window and is larger).
            // Therefore, we remove it from the back until the Deque is monotonically decreasing again.
            while (!deque.isEmpty() && nums[deque.peekLast()] <= nums[right]) {
                deque.removeLast();
            }

            // 2. Add New Element
            // Add the index of the current element to the back of the Deque.
            deque.addLast(right);

            // 3. Check Window Boundary (Contraction from the Front)
            // The index at the front of the Deque is the current maximum.
            // If this index is outside the current window (i.e., older than 'k' positions ago), remove it from the front.
            // The current window starts at 'right - k + 1'. The index to remove is when 'peekFirst' is less than this.
            int windowStart = right - k + 1;
            if (deque.peekFirst() < windowStart) {
                deque.removeFirst();
            }

            // 4. Record Maximum (Only after the window is fully formed)
            // A full window is formed when the 'right' index has reached at least 'k - 1'.
            if (right >= k - 1) {
                // The maximum for the current window is always the element at the index stored at the front.
                result[resultIndex++] = nums[deque.peekFirst()];
            }
        }

        return result;
    }
}
```

-----