package com.eightthree.techworld.ebay;

/*
Given an integer array nums, you need to find one continuous subarray
such that if you only sort this subarray in non-decreasing order,
then the whole array will be sorted in non-decreasing order.

i.e- Find the smallest window (subarray) in the array such
that sorting only this window results in the whole array being sorted.
Return the length of that window.

Return the shortest such subarray and output its length.
Example 1:
Input: num's = [2,6,4,8,10,9,15]
Understanding the problem with an example:

Here, the array is not fully sorted.

If you look closely:
2 is in the correct place.
6 and 4 are out of order → that breaks the sort.
Later, 10 and 9 are also out of order.
If you sort the subarray [6, 4, 8, 10, 9], the entire array becomes:


Output: 5
Explanation: You need to sort [6, 4, 8, 10, 9] in ascending order to make the whole array sorted in ascending order.
Example 2:
Input: nums = [1,2,3,4]
Output: 0
Example 3:
Input: nums = [1]
Output: 0
 */
public class ContinuousSubArray {

    private void findSubArray(int[] input) {

        int startIndex = 0;
        int maxIndex = Integer.MAX_VALUE;
        int sorted[] = new int[input.length];
        for(int i =0;i<input.length;i++){
            if(input[i]>input[i+1]){
                startIndex=i+1;
            }else{
                maxIndex=i+1;
            }
        }

    }

    public static void main(String[] args) {
        int[] input  = new int[]{2,6,4,8,10,9,15};
        ContinuousSubArray subArray = new ContinuousSubArray();
        subArray.findSubArray(input);
    }

}
