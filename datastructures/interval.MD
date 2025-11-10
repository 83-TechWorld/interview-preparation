Great — here’s a full guide, in the same style as before, for the **Intervals** topic: the 5 classic problems you listed (Insert Interval, Merge Intervals, Non-Overlapping Intervals, Meeting Rooms, Meeting Rooms II). I’ll include for each: the statement, an example, intuitive explanation, and a clean Java solution with comments. Then I’ll wrap up with a GitHub-style markdown you could drop into a `README.md`.

---

## What is the “Intervals” pattern?

Problems about intervals deal with ranges (e.g., time periods, numerical intervals) and often ask you to do one or more of: insert a new interval, merge overlapping intervals, count how many non-overlapping you must remove, count how many rooms/slots you need, etc. They share a key pattern: **sort by start times** (or end times), and then iterate tracking overlap/merging/choices. ([Medium][1])

### Core Concepts

* **Sort by interval start**: ensures that when you iterate, you see intervals in increasing order of where they begin.
* **Track current end** (or “last interval in result”): at each step you compare the current interval’s start to the “last” end time to decide overlap or new block.
* **Merging vs counting vs scheduling**: depending on the problem you either merge overlaps, count removals, allocate rooms, etc.
* **Greedy or sweep-line**: many of these problems reduce to greedy decisions once sorted.
* **State maintenance**: e.g., how many rooms currently active, what is the current merging end, etc.

### Why use these? Real-Use Cases

* Scheduling meetings (rooms, calendars) → you have many meeting intervals, you want to know how many rooms are needed.
* Booking systems, resource allocation: intervals represent resource usage, you want to avoid overlap or merge adjacent bookings.
* Data processing: merging time logs, combining overlapping time ranges.
* Interview questions: very common pattern for “interval scheduling / merging” categories in coding interviews. ([PandaC][2])

### Visual representation

![Image](https://media.geeksforgeeks.org/wp-content/uploads/20241106160440709079/_merge-overlapping-intervals-by-checking-overlapping-intervals-1.webp)

![Image](https://www.interviewbit.com/blog/wp-content/uploads/2021/11/merge-overlapping-intervals-951x1024.png)

![Image](https://cdn.prod.website-files.com/60d18919aeb33ceb2a53ae2e/63eba0d94a51bed91a05fcfc_6345c15ca02aaddb276d697c_Purprle%20gradient.webp)

![Image](https://www.researchgate.net/publication/222182690/figure/fig3/AS%3A305128163823617%401449759631182/Meeting-scheduler-system-architecture.png)

![Image](https://media.geeksforgeeks.org/wp-content/uploads/20241105165711001396/find-non-overlapping-intervals-by-sorting-by-starting-values-1.webp)

![Image](https://i.ytimg.com/vi/BTObFnHbD4U/maxresdefault.jpg)

*Use these diagrams to visualise intervals on a line, how they overlap, how they merge, and how you might allocate resources (rooms) based on them.*

---

## 5 Classic Interval Problems: Statement, Example, Intuition, Java Solution

### 1. Insert Interval

**Statement:**
You are given a list of non-overlapping intervals sorted by start time, and you are given a new interval. Insert the new interval into the list such that still the list remains sorted by start and still no overlapping intervals remain (merge if necessary).

**Example:**
Intervals = `[[1,3],[6,9]]`, newInterval = `[2,5]` → Result: `[[1,5],[6,9]]`. ([LeetCode][3])

**Intuition / Approach:**
When you insert the new interval it may overlap with one or many existing intervals. One approach: append the new interval, then sort and merge. But since the existing list is already sorted non-overlapping, you can optimize by:

* Add all intervals whose end < newInterval.start (they come before and do not overlap).
* Then merge all overlapping intervals (those with start ≤ newInterval.end).
* Finally add remaining intervals (those with start > newInterval.end).
  Sorting is O(n log n) in simpler version, merging is O(n).

**Java Solution:**

```java
import java.util.*;
public class InsertInterval {
    public int[][] insert(int[][] intervals, int[] newInterval) {
        List<int[]> result = new ArrayList<>();
        int i = 0;
        int n = intervals.length;
        // 1) add all intervals that end before newInterval starts
        while (i < n && intervals[i][1] < newInterval[0]) {
            result.add(intervals[i]);
            i++;
        }
        // 2) merge all intervals that overlap with newInterval
        while (i < n && intervals[i][0] <= newInterval[1]) {
            newInterval[0] = Math.min(newInterval[0], intervals[i][0]);
            newInterval[1] = Math.max(newInterval[1], intervals[i][1]);
            i++;
        }
        // add the merged newInterval
        result.add(newInterval);
        // 3) add remaining intervals
        while (i < n) {
            result.add(intervals[i]);
            i++;
        }
        // convert list back to array
        return result.toArray(new int[result.size()][]);
    }
}
```

*Comments:*

* The first loop handles intervals completely before the new one.
* The second loop merges any overlapping.
* The third loop handles intervals completely after.
* Works in O(n) time since no full resort needed (given sorted input).

---

### 2. Merge Intervals

**Statement:**
Given a collection of intervals, merge all overlapping intervals and return an array of the non-overlapping intervals that cover all the original intervals. ([LeetCode][4])

**Example:**
Intervals = `[[1,4],[2,5],[7,9]]` → After merging: `[[1,5],[7,9]]`.

**Intuition / Approach:**
Sort intervals by start time. Then iterate through sorted list maintaining a “last merged interval”. For each new interval:

* If current interval’s start ≤ lastMerged.end → they overlap → extend lastMerged.end = max(lastMerged.end, current.end)
* Else → no overlap → add current as new merged interval.
  Time: O(n log n) due to sort; O(n) space/time for loop. ([GeeksforGeeks][5])

**Java Solution:**

```java
import java.util.*;
public class MergeIntervals {
    public int[][] merge(int[][] intervals) {
        if (intervals == null || intervals.length <= 1) {
            return intervals;
        }
        // sort by start time
        Arrays.sort(intervals, (a,b) -> Integer.compare(a[0], b[0]));
        List<int[]> result = new ArrayList<>();
        int[] current = intervals[0];
        result.add(current);
        for (int[] interval : intervals) {
            if (interval[0] <= current[1]) {
                // overlap → merge
                current[1] = Math.max(current[1], interval[1]);
            } else {
                // no overlap → move current
                current = interval;
                result.add(current);
            }
        }
        return result.toArray(new int[result.size()][]);
    }
}
```

*Comments:*

* Sorting ensures overlapping intervals are consecutive.
* `current` tracks last merged interval; when you detect no overlap you start a new one.
* Conversion to array at the end for output.

---

### 3. Non-Overlapping Intervals

**Statement:**
Given an array of intervals, return the minimum number of intervals you need to remove so that the remaining intervals do not overlap. ([LeetCode][6])

**Example:**
Intervals = `[[1,2],[2,3],[3,4],[1,3]]` → One optimal answer is remove `[1,3]`, so minimum removals = 1.

**Intuition / Approach:**
This is essentially the “maximum number of non-overlapping intervals” problem, which is equivalent to scheduling as many intervals as possible without conflicts. Then the number to remove = total intervals − max non-overlapping you can keep.
Greedy strategy:

* Sort intervals by their **end time** (earliest finishing first).
* Then iterate, keep track of end of last selected interval, select the next interval if its start ≥ last selected end.
* Count how many we keep → answer = total − keepCount.

**Java Solution:**

```java
import java.util.*;
public class NonOverlappingIntervals {
    public int eraseOverlapIntervals(int[][] intervals) {
        if (intervals == null || intervals.length == 0) {
            return 0;
        }
        // sort by end time
        Arrays.sort(intervals, (a,b) -> Integer.compare(a[1], b[1]));
        int countKeep = 1; // keep first interval
        int lastEnd = intervals[0][1];
        for (int i = 1; i < intervals.length; i++) {
            if (intervals[i][0] >= lastEnd) {
                // can keep this interval
                countKeep++;
                lastEnd = intervals[i][1];
            }
            // else skip this interval (i.e., remove it)
        }
        return intervals.length - countKeep;
    }
}
```

*Comments:*

* We sort by end time to maximise the number of intervals we can keep.
* `lastEnd` tracks end time of the last interval we included.
* If next interval starts after or at that end, we can include it.
* Finally compute removals = total − kept.

---

### 4. Meeting Rooms (Premium)

**Statement:**
Given an array of meeting time intervals where each interval has a start and end time, determine if a person can attend all meetings. I.e., check whether any two meetings overlap. (Often called “Meeting Rooms I”.)

**Example:**
Intervals = `[[0,30],[5,10],[15,20]]` → returns `false` (because [0,30] overlaps [5,10]).
Intervals = `[[7,10],[2,4]]` → returns `true`.

**Intuition / Approach:**
If you sort intervals by start time, then for each interval check whether its start < end time of previous interval. If yes → overlap → cannot attend all.
Time: O(n log n) for sort; O(n) for loop.

**Java Solution:**

```java
import java.util.*;
public class MeetingRooms {
    public boolean canAttendMeetings(int[][] intervals) {
        if (intervals == null || intervals.length == 0) {
            return true;
        }
        // sort by start time
        Arrays.sort(intervals, (a,b) -> Integer.compare(a[0], b[0]));
        for (int i = 1; i < intervals.length; i++) {
            // if start of current < end of previous => overlap
            if (intervals[i][0] < intervals[i-1][1]) {
                return false;
            }
        }
        return true;
    }
}
```

*Comments:*

* We assume intervals sorted by start time.
* Only need to compare current start with previous’s end to detect overlap.
* If any overlap found, answer is false.

---

### 5. Meeting Rooms II (Premium)

**Statement:**
Given an array of meeting time intervals, determine the **minimum number of conference rooms** required. Each meeting interval [start, end) needs a room. If meetings overlap (i.e., one starts before another ends), they cannot share a room.

**Example:**
Intervals = `[[0,30],[5,10],[15,20]]` → returns 2.
Intervals = `[[7,10],[2,4]]` → returns 1.

**Intuition / Approach:**
One way (greedy + min‐heap):

* Sort intervals by start time.
* Use a min‐heap to track end times of ongoing meetings (room occupied until that time).
* For each meeting in order:

  * If the start time ≥ the smallest end time in heap → that room becomes free → pop from heap.
  * Then push the current meeting’s end time onto heap (assign a room).
* The size of the heap at max is the number of rooms needed.
  Alternatively you can use a “two arrays” approach: sort start times & sort end times and use two pointers to simulate how many rooms currently in use. Time: O(n log n).

**Java Solution:**

```java
import java.util.*;
public class MeetingRoomsII {
    public int minMeetingRooms(int[][] intervals) {
        if (intervals == null || intervals.length == 0) {
            return 0;
        }
        // sort by start time
        Arrays.sort(intervals, (a,b) -> Integer.compare(a[0], b[0]));
        // min-heap of end times
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        // add end time of first meeting
        heap.add(intervals[0][1]);
        for (int i = 1; i < intervals.length; i++) {
            int start = intervals[i][0];
            int end   = intervals[i][1];
            // if the earliest ending meeting is done before current starts, free it
            if (start >= heap.peek()) {
                heap.poll();
            }
            // assign current meeting (either new room or reused) by adding its end time
            heap.add(end);
        }
        // size of heap = number of rooms
        return heap.size();
    }
}
```

*Comments:*

* The heap always holds end times of meetings in currently occupied rooms.
* If the earliest room to free (smallest end time) is free by the time this meeting starts, we reuse it (pop).
* Then push the end of the current meeting.
* At end, number of elements in heap = number of rooms needed.

---

## 📋 Full Markdown for GitHub

```markdown
# Intervals — 5 Classic Problems

## What is the Intervals Pattern?
Problems that deal with intervals (e.g., time ranges, numeric ranges) often revolve around sorting, detecting/merging overlaps or scheduling resources.  
Key ideas: sort by start (or end), track current end, greedy/sweep-line decisions.

## Core Concepts
- Sort intervals by start (or sometimes by end).  
- Maintain a pointer or data structure tracking the “last/earliest finish” or “current active count”.  
- Decide: merge vs skip vs allocate a resource.  
- Greedy choice: e.g., pick earliest finishing interval to maximise non-overlapping, or reuse a room that frees earliest.

## Real-Use Cases
- Scheduling meetings and conference rooms.  
- Booking systems (rooms, machines) needing to avoid or merge overlapping time slots.  
- Data cleaning: merging overlapping time logs.  
- Interview problems: interval scheduling, merging, room allocation.

## Problems & Solutions

### 1. Insert Interval
**Statement:** *You are given a list of non-overlapping intervals … insert a new interval … merge if necessary.*  
**Example:** `[[1,3],[6,9]]` with new `[2,5]` → `[[1,5],[6,9]]`  
**Solution:** (See Java code above)  

### 2. Merge Intervals  
**Statement:** *Given a collection of intervals, merge all overlapping intervals.*  
**Example:** `[[1,4],[2,5],[7,9]]` → `[[1,5],[7,9]]`  
**Solution:** (See Java code above)  

### 3. Non-Overlapping Intervals  
**Statement:** *Given intervals, remove the minimum number so that the rest don’t overlap.*  
**Example:** `[[1,2],[2,3],[3,4],[1,3]]` → remove 1 → answer = 1  
**Solution:** (See Java code above)  

### 4. Meeting Rooms (I)  
**Statement:** *Given meeting intervals, can a person attend all (i.e., no overlapping meetings)?*  
**Example:** `[[0,30],[5,10],[15,20]]` → false  
**Solution:** (See Java code above)  

### 5. Meeting Rooms II  
**Statement:** *Given meeting intervals, what’s the minimum number of rooms required?*  
**Example:** `[[0,30],[5,10],[15,20]]` → answer = 2  
**Solution:** (See Java code above)  

## Summary
Once you recognise the “interval” pattern, many problems reduce to:  
- sort intervals  
- iterate keeping track of last finish or a min-heap for active resources  
- apply greedy logic  
Mastering this pattern gives you a strong foundation for a class of interview problems.

```

---

If you like, I can **generate also a full GitHub repository skeleton** for these 5 problems (folder structure, each with `.java` file + comments + tests) and I can include **visual diagrams** (PNG or ASCII) for each problem. Would you like that?

[1]: https://medium.com/%40piyushkashyap045/master-the-merge-intervals-pattern-your-secret-weapon-for-coding-interviews-aec54eb9023b?utm_source=chatgpt.com "Master the Merge Intervals Pattern: Your Secret Weapon ..."
[2]: https://pandac.in/blogs/interval-problem/?utm_source=chatgpt.com "Interval Problems with Code: Insert, Merge, and More - PandaC"
[3]: https://leetcode.com/problems/insert-interval/?utm_source=chatgpt.com "Insert Interval - LeetCode"
[4]: https://leetcode.com/problems/merge-intervals/?utm_source=chatgpt.com "Merge Intervals"
[5]: https://www.geeksforgeeks.org/dsa/merging-intervals/?utm_source=chatgpt.com "Merge Overlapping Intervals"
[6]: https://leetcode.com/problems/non-overlapping-intervals/?utm_source=chatgpt.com "Non-overlapping Intervals"
