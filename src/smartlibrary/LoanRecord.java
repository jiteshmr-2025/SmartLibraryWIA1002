package smartlibrary;

import java.time.LocalDate;

/**
 * Represents one borrowing transaction for a student and a book.
 * A loan starts as borrowed and is updated when the book is returned.
 */
public class LoanRecord {
    private final String studentId;
    private final Book book;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private boolean returned;

    /**
     * Creates a loan record with its borrowing dates and current return status.
     */
    public LoanRecord(String studentId, Book book, LocalDate borrowDate, LocalDate dueDate, boolean returned) {
        this.studentId = studentId;
        this.book = book;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returned = returned;
    }

    public String getStudentId() {
        return studentId;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isReturned() {
        return returned;
    }

    /**
     * Marks this loan as returned while keeping the record in history.
     */
    public void markReturned() {
        returned = true;
    }

    /**
     * Converts the boolean return flag into readable text for table output.
     */
    public String getStatus() {
        return returned ? "Returned" : "Borrowed";
    }

    @Override
    public String toString() {
        return book + " | Student ID: " + studentId
                + " | Borrowed: " + borrowDate
                + " | Due: " + dueDate
                + " | Status: " + getStatus();
    }
}
