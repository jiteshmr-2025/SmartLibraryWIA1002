package smartlibrary;

import java.util.List;

/**
 * Defines the main operations supported by the Smart Library system.
 * The implementation hides the data structures used to manage books and loans.
 */
public interface LibraryADT {
    /**
     * Adds a new available book to the catalogue.
     */
    boolean addBook(long isbn, String title, String author);

    /**
     * Finds an available book by ISBN.
     */
    Book searchBook(long isbn);

    /**
     * Borrows a book for a student and removes it from the available catalogue.
     */
    LoanRecord borrowBook(String studentId, long isbn);

    /**
     * Returns a student's active loan and makes the book available again.
     */
    LoanRecord returnBook(String studentId, long isbn);

    /**
     * Lists one student's borrowing history with the newest record first.
     */
    List<LoanRecord> viewBorrowHistory(String studentId);

    /**
     * Lists every book currently available in the catalogue.
     */
    List<Book> viewAvailableBooks();

    /**
     * Updates the title and author of an available catalogue book.
     */
    boolean editBook(long isbn, String title, String author);

    /**
     * Removes an available book from the catalogue.
     */
    Book removeBook(long isbn);

    /**
     * Searches available books by a title or author keyword.
     */
    List<Book> searchByTitleOrAuthor(String keyword);
}
