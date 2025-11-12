package handson;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ArrayAnalysis {
    public static void main(String[] args) {
        int[] arr = {4, 2, 7, 2, 9, 4, 11, 3};
        generalProcess(arr);
        usingStreams(arr);
    }

    private static void usingStreams(int[] arr) {
        System.out.println("Using streams");
    int min = Arrays.stream(arr).min().getAsInt();
    int max = Arrays.stream(arr).max().getAsInt();
        Set<Integer> seen = new HashSet<>();
        Set<Integer> duplicates = Arrays.stream(arr)
                .filter(i -> !seen.add(i))
                .boxed()
                .collect(Collectors.toSet());
        System.out.println("Minimum: " + min);
        System.out.println("Maximum: " + max);
        System.out.println("Duplicates: " + duplicates);

    }

    private static void generalProcess(int[] arr) {
        int min = arr[0];
        int max = arr[0];
        Set<Integer> seen = new HashSet<>();
        Set<Integer> duplicates = new HashSet<>();
        for(int num: arr){
            if(num<min)
                min = num;
            if(num>max)
                max =num;
            if(!seen.add(num)){
                duplicates.add(num);
            }
        }
        System.out.println("Minimum element: " + min);
        System.out.println("Maximum element: " + max);
        System.out.println("Duplicate elements: " + duplicates);
    }
}
