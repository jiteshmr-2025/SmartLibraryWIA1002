package smartlibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * Binary search tree that stores all currently available books.
 * Books are ordered by ISBN so search, insert, and delete can follow the tree
 * instead of scanning every book in the catalogue.
 */
public class BookBST {
    private Node root;

    /**
     * One node in the binary search tree.
     */
    private static class Node {
        private Book book;
        private Node left;
        private Node right;

        private Node(Book book) {
            this.book = book;
        }
    }

    /**
     * Adds a book to the tree.
     *
     * @return true when the book is inserted, or false when the ISBN already exists.
     */
    public boolean insert(Book book) {
        if (root == null) {
            root = new Node(book);
            return true;
        }

        return insertRecursive(root, book);
    }

    /**
     * Recursively follows the left or right branch based on the ISBN value.
     */
    private boolean insertRecursive(Node current, Book book) {
        if (book.getIsbn() == current.book.getIsbn()) {
            return false;
        }

        if (book.getIsbn() < current.book.getIsbn()) {
            if (current.left == null) {
                current.left = new Node(book);
                return true;
            }
            return insertRecursive(current.left, book);
        }

        if (current.right == null) {
            current.right = new Node(book);
            return true;
        }
        return insertRecursive(current.right, book);
    }

    /**
     * Searches for a book by ISBN.
     *
     * @return the matching book, or null when the book is not available.
     */
    public Book search(long isbn) {
        return searchRecursive(root, isbn);
    }

    private Book searchRecursive(Node current, long isbn) {
        if (current == null) {
            return null;
        }

        if (isbn == current.book.getIsbn()) {
            return current.book;
        }

        if (isbn < current.book.getIsbn()) {
            return searchRecursive(current.left, isbn);
        }

        return searchRecursive(current.right, isbn);
    }

    /**
     * Removes a book from the tree if the ISBN exists.
     *
     * @return true when a book was removed.
     */
    public boolean deleteByIsbn(long isbn) {
        if (search(isbn) == null) {
            return false;
        }

        root = deleteRecursive(root, isbn);
        return true;
    }

    private Node deleteRecursive(Node current, long isbn) {
        if (current == null) {
            return null;
        }

        if (isbn < current.book.getIsbn()) {
            current.left = deleteRecursive(current.left, isbn);
            return current;
        }

        if (isbn > current.book.getIsbn()) {
            current.right = deleteRecursive(current.right, isbn);
            return current;
        }

        if (current.left == null) {
            return current.right;
        }

        if (current.right == null) {
            return current.left;
        }

        // For a node with two children, replace it with the smallest book from
        // the right subtree so the BST ordering remains valid.
        Node successor = findSmallest(current.right);
        current.book = successor.book;
        current.right = deleteRecursive(current.right, successor.book.getIsbn());
        return current;
    }

    private Node findSmallest(Node current) {
        if (current.left == null) {
            return current;
        }

        return findSmallest(current.left);
    }

    /**
     * Returns all available books sorted from the smallest ISBN to the largest.
     */
    public List<Book> toList() {
        List<Book> books = new ArrayList<>();
        addBooksInOrder(root, books);
        return books;
    }

    /**
     * In-order traversal visits left branch, current book, then right branch.
     */
    private void addBooksInOrder(Node current, List<Book> books) {
        if (current == null) {
            return;
        }

        addBooksInOrder(current.left, books);
        books.add(current.book);
        addBooksInOrder(current.right, books);
    }
}
