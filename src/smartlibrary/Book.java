package smartlibrary;

/**
 * Represents one book in the Smart Library catalogue.
 * The ISBN is treated as the unique identifier for searching and sorting.
 */
public class Book {
    private final long isbn;
    private final String title;
    private final String author;

    /**
     * Creates an immutable book record.
     */
    public Book(long isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public long getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        // Used by loan history output and simple debugging displays.
        return "ISBN: " + isbn + " | Title: " + title + " | Author: " + author;
    }
}
