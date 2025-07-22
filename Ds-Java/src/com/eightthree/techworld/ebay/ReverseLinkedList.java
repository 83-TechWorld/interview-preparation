package com.eightthree.techworld.ebay;

import java.util.LinkedList;

public class ReverseLinkedList {


    public static void printList(Node head) {
        Node temp = head;
        while (temp != null) {
            System.out.print(temp.value + " -> ");
            temp = temp.next;
        }
        System.out.println("null");
    }

    public static void main(String[] args) {
        ReverseLinkedList rl = new ReverseLinkedList();
        Node head = new Node(10);
        head.next = new Node(20);
        head.next.next = new Node(40);
        head.next.next.next= new Node(60);
        head = rl.reverseList(head);
        System.out.println("Reversed List:");
        printList(head);
    }

    private Node reverseList(Node head) {
        Node prev = null;
        Node current = head;
        while(current!=null){
          Node nextNode = current.next;
          current.next = prev;
          prev = current;
          current = nextNode;
        }
        return prev;
    }
}
