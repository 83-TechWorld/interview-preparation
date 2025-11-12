package handson;

import java.util.Arrays;

public class BinarySearchBasic {
    public static void main(String[] args) {
        System.out.println("Binary Search Basic");
        int[] data = {-1,0,3,5,9,12};
        int target = 5;
        System.out.println("final target in"+ searchIndex(data,target));
    }

    private static int searchIndex(int[] data, int target) {
       int left = 0, right = data.length-1;
        Arrays.sort(data);
        System.out.println("After sorting"+ Arrays.toString(data));
        int mid = (left + right) /2;
        System.out.println("mid"+ mid);
        if(data[mid]==target){
            return mid;
        }else if(target<data[mid]){ // mid greater than target
            while(left<mid){
                if(data[left]==target){
                  return left;
                }
                left++;
            }
        }else if(target>data[mid]){
            while(right>mid){
                if(data[right]==target){
                    return right;
                }
                right--;
            }
        }
        return -1;
    }
}
