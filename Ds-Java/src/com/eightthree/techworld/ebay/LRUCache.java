package com.eightthree.techworld.ebay;

import java.util.HashMap;
import java.util.Map;

/*
mplementing an LRU (Least Recently Used) Cache in Java can be done in multiple ways.
Here's a step-by-step explanation and implementation using a combination of HashMap and Doubly Linked List
 */
public class LRUCache {

    private int capacity;
    private Map<Integer, Node> map;
    private Node head, tail;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
        head = new Node(0, 0);
        tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }

    public void put(int key, int value) {
        if (map.containsKey(key)) {
            remove(map.get(key));   // Remove the old node
        }
       if(map.size()==capacity){
           Node lru = tail.prev;
           remove(lru);
       }
        Node newNode = new Node(key,value);
        insertToHead(newNode);
        map.put(value,newNode);
    }

    private void remove(Node node){
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void insertToHead(Node node) {
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
        node.prev = head;
    }

    public int get(int key) {
        if (!map.containsKey(key)) {
            return -1;
        }
        Node node = map.get(key);
        remove(node);
        insertToHead(node);
        return node.value;
    }

    public static void main(String[] args) {
        LRUCache cache = new LRUCache(2);
        cache.put(1, 1);  // Cache: {1=1}
        cache.put(2, 2);  // Cache: {2=2, 1=1}
        System.out.println(cache.get(1)); // returns 1, Cache: {1=1, 2=2}
        cache.put(3, 3);  // Evicts key 2, Cache: {3=3, 1=1}
        System.out.println(cache.get(2)); // returns -1 (not found)
        cache.put(4, 4);  // Evicts key 1, Cache: {4=4, 3=3}
        System.out.println(cache.get(1)); // returns -1 (not found)
        System.out.println(cache.get(3)); // returns 3
        System.out.println(cache.get(4)); // returns 4
    }

}
