package com.galaxy13.storage;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUStorage<K> implements Storage<K, Value> {

    private final ConcurrentMap<K, Node> storage;
    private final DoublyLinkedList deque;
    private final int capacity;
    private int currentSize;
    private final ReentrantReadWriteLock reentrantLock;

    public LRUStorage(int capacity) {
        this.storage = new ConcurrentHashMap<>(capacity);
        this.capacity = capacity;
        this.deque = new DoublyLinkedList();
        this.currentSize = 0;
        this.reentrantLock = new ReentrantReadWriteLock();
    }

    @Override
    public Optional<Value> get(K key) {
        Node node = storage.get(key);
        if (node == null) {
            return Optional.empty();
        }
        deque.addFirst(node);
        return Optional.of(node.value);
    }

    @Override
    public Optional<Value> remove(K key) {
        Node node = storage.remove(key);
        if (node == null) {
            return Optional.empty();
        }
        deque.remove(node);
        currentSize--;
        return Optional.of(node.value);
    }

    @Override
    public Value put(K key, Value value) {
        Node node = storage.get(key);
        if (node != null) {
            node.value = value;
            deque.remove(node);
            deque.addFirst(node);
        } else {
            if (currentSize == capacity) {
                deque.removeLast();
            }
            Node newNode = new Node(key, value);
            storage.put(key, newNode);
            deque.addFirst(newNode);
        }
        return value;
    }

    private class Node{
        K key;
        Value value;

        Node next;
        Node prev;

        public Node(K key, Value value) {
            this.key = key;
            this.value = value;
        }
    }

    private class DoublyLinkedList {
        private Node head;
        private Node tail;

        public void addFirst(Node node) {
            reentrantLock.writeLock().lock();
            try {
                if (head == null) {
                    head = tail = node;
                }
                else {
                    node.next = head;
                    head.prev = node;
                    head = node;
                }
            } finally {
                reentrantLock.writeLock().unlock();
            }
        }

        public void remove(Node node) {
            reentrantLock.writeLock().lock();
            try {
                if (node == head) {
                    head = head.next;
                } else if (node == tail) {
                    tail = tail.prev;
                }

                if (node.prev != null) {
                    node.prev.next = node.next;
                }
                if (node.next != null) {
                    node.next.prev = node.prev;
                }
            } finally {
                reentrantLock.writeLock().unlock();
            }
        }

        public Node removeLast(){
            reentrantLock.writeLock().lock();
            try {
                if (head == null){
                    return null;
                }
                Node last = tail;
                remove(tail);
                return last;
            } finally {
                reentrantLock.writeLock().unlock();
            }
        }
    }
}
