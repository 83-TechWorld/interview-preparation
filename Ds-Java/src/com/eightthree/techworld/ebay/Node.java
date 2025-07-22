package com.eightthree.techworld.ebay;

public class Node {
    int key, value;
    Node prev, next;

    Node(int key, int value) {
        this.key = key;
        this.value = value;
    }

    Node(int value){
        this.value=value;
        this.next = null;
    }
}
