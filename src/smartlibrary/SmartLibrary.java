package smartlibrary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main library service that implements the required library operations.
 * Available books are stored in a BST, while each student's loans are stored in
 * a stack so recent borrowing activity appears first.
 */
public class SmartLibrary implements LibraryADT {
    // Data is stored in simple text files so the console program can persist
    // books and loans without needing a database.
    private static final Path DATA_FILE = Path.of("smart_library_data.txt");
    private static final String CATALOGUE_HEADER = "[CATALOGUE]";
    private static final String LOANS_HEADER = "[LOANS]";
    private static final String OLD_HISTORY_HEADER = "[BORROW_HISTORY]";
    private static final String LEGACY_STUDENT_ID = "LEGACY_STUDENT";
    private static final int LOAN_DAYS = 14;

    private final BookBST catalogue;
    private final Map<String, BorrowHistoryStack> borrowHistories;

    /**
     * Loads existing saved data when the application starts.
     */
    public SmartLibrary() {
        catalogue = new BookBST();
        borrowHistories = new LinkedHashMap<>();
        loadFromFile();
        saveToFile();
    }

    @Override
    public boolean addBook(long isbn, String title, String author) {
        if (!isValidBookInput(isbn, title, author)) {
            return false;
        }

        boolean added = catalogue.insert(new Book(isbn, title.trim(), author.trim()));

        if (added) {
            saveToFile();
        }

        return added;
    }

    @Override
    public Book searchBook(long isbn) {
        if (isbn <= 0) {
            return null;
        }

        return catalogue.search(isbn);
    }

    @Override
    public LoanRecord borrowBook(String studentId, long isbn) {
        String normalizedStudentId = normalizeStudentId(studentId);
        if (normalizedStudentId == null || isbn <= 0) {
            return null;
        }

        Book book = catalogue.search(isbn);
        if (book == null) {
            return null;
        }

        // Borrowed books are removed from the available catalogue until they are
        // returned by the same student.
        catalogue.deleteByIsbn(isbn);

        LocalDate borrowDate = LocalDate.now();
        LoanRecord loanRecord = new LoanRecord(
                normalizedStudentId,
                book,
                borrowDate,
                borrowDate.plusDays(LOAN_DAYS),
                false
        );

        getHistoryStack(normalizedStudentId).push(loanRecord);
        saveToFile();
        return loanRecord;
    }

    @Override
    public LoanRecord returnBook(String studentId, long isbn) {
        String normalizedStudentId = normalizeStudentId(studentId);
        if (normalizedStudentId == null || isbn <= 0) {
            return null;
        }

        BorrowHistoryStack historyStack = borrowHistories.get(normalizedStudentId);
        if (historyStack == null) {
            return null;
        }

        LoanRecord activeLoan = historyStack.findActiveLoanByIsbn(isbn);
        if (activeLoan == null) {
            return null;
        }

        // Returning restores the same book object to the available catalogue and
        // keeps the loan record for the student's history.
        activeLoan.markReturned();
        catalogue.insert(activeLoan.getBook());
        saveToFile();
        return activeLoan;
    }

    @Override
    public List<LoanRecord> viewBorrowHistory(String studentId) {
        String normalizedStudentId = normalizeStudentId(studentId);
        if (normalizedStudentId == null) {
            return new ArrayList<>();
        }

        BorrowHistoryStack historyStack = borrowHistories.get(normalizedStudentId);
        if (historyStack == null) {
            return new ArrayList<>();
        }

        return historyStack.toList();
    }

    @Override
    public List<Book> viewAvailableBooks() {
        return catalogue.toList();
    }

    @Override
    public boolean editBook(long isbn, String title, String author) {
        if (!isValidBookInput(isbn, title, author) || catalogue.search(isbn) == null) {
            return false;
        }

        catalogue.deleteByIsbn(isbn);
        catalogue.insert(new Book(isbn, title.trim(), author.trim()));
        saveToFile();
        return true;
    }

    @Override
    public Book removeBook(long isbn) {
        if (isbn <= 0) {
            return null;
        }

        Book book = catalogue.search(isbn);
        if (book == null) {
            return null;
        }

        catalogue.deleteByIsbn(isbn);
        saveToFile();
        return book;
    }

    @Override
    public List<Book> searchByTitleOrAuthor(String keyword) {
        List<Book> matches = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) {
            return matches;
        }

        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        for (Book book : catalogue.toList()) {
            boolean titleMatches = book.getTitle().toLowerCase(Locale.ROOT).contains(normalizedKeyword);
            boolean authorMatches = book.getAuthor().toLowerCase(Locale.ROOT).contains(normalizedKeyword);

            if (titleMatches || authorMatches) {
                matches.add(book);
            }
        }

        return matches;
    }

    private boolean isValidBookInput(long isbn, String title, String author) {
        return isbn > 0 && title != null && !title.isBlank() && author != null && !author.isBlank();
    }

    /**
     * Normalizes student IDs before storing or searching loan histories.
     */
    private String normalizeStudentId(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return null;
        }

        return studentId.trim();
    }

    private BorrowHistoryStack getHistoryStack(String studentId) {
        return borrowHistories.computeIfAbsent(studentId, key -> new BorrowHistoryStack());
    }

    /**
     * Reads the saved catalogue and loan sections from the data file.
     */
    private void loadFromFile() {
        if (!Files.exists(DATA_FILE)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(DATA_FILE);
            Map<String, List<LoanRecord>> loadedLoans = new LinkedHashMap<>();
            String section = "";

            for (String line : lines) {
                // Section headers decide whether the next lines are books,
                // modern loan records, or old loan-history records.
                if (line.isBlank()) {
                    continue;
                }

                if (line.equals(CATALOGUE_HEADER) || line.equals(LOANS_HEADER) || line.equals(OLD_HISTORY_HEADER)) {
                    section = line;
                    continue;
                }

                if (section.equals(CATALOGUE_HEADER)) {
                    Book book = parseBook(line);
                    if (book != null) {
                        catalogue.insert(book);
                    }
                } else if (section.equals(LOANS_HEADER)) {
                    LoanRecord loanRecord = parseLoanRecord(line);
                    if (loanRecord != null) {
                        loadedLoans.computeIfAbsent(loanRecord.getStudentId(), key -> new ArrayList<>()).add(loanRecord);
                    }
                } else if (section.equals(OLD_HISTORY_HEADER)) {
                    LoanRecord migratedLoan = parseLegacyLoanRecord(line);
                    if (migratedLoan != null) {
                        loadedLoans.computeIfAbsent(LEGACY_STUDENT_ID, key -> new ArrayList<>()).add(migratedLoan);
                    }
                }
            }

            loadHistoryStacks(loadedLoans);
        } catch (IOException error) {
            System.out.println("Warning: Could not load saved library data.");
        }
    }

    /**
     * Restores stack order after reading loan records from the file.
     */
    private void loadHistoryStacks(Map<String, List<LoanRecord>> loadedLoans) {
        for (Map.Entry<String, List<LoanRecord>> entry : loadedLoans.entrySet()) {
            BorrowHistoryStack historyStack = getHistoryStack(entry.getKey());
            List<LoanRecord> loanRecords = entry.getValue();

            // Push in reverse order because each push places the record on top.
            for (int index = loanRecords.size() - 1; index >= 0; index--) {
                historyStack.push(loanRecords.get(index));
            }
        }
    }

    /**
     * Writes the current catalogue and all loan histories back to the data file.
     */
    private void saveToFile() {
        List<String> lines = new ArrayList<>();
        lines.add(CATALOGUE_HEADER);

        for (Book book : catalogue.toList()) {
            lines.add(formatBook(book));
        }

        lines.add("");
        lines.add(LOANS_HEADER);

        for (BorrowHistoryStack historyStack : borrowHistories.values()) {
            for (LoanRecord loanRecord : historyStack.toList()) {
                lines.add(formatLoanRecord(loanRecord));
            }
        }

        try {
            Files.write(DATA_FILE, lines);
        } catch (IOException error) {
            System.out.println("Warning: Could not update " + DATA_FILE + ".");
        }
    }

    /**
     * Formats one available book as a pipe-delimited row.
     */
    private String formatBook(Book book) {
        return book.getIsbn() + "|" + escape(book.getTitle()) + "|" + escape(book.getAuthor());
    }

    /**
     * Formats one loan record as a pipe-delimited row.
     */
    private String formatLoanRecord(LoanRecord loanRecord) {
        Book book = loanRecord.getBook();
        return escape(loanRecord.getStudentId())
                + "|" + book.getIsbn()
                + "|" + escape(book.getTitle())
                + "|" + escape(book.getAuthor())
                + "|" + loanRecord.getBorrowDate()
                + "|" + loanRecord.getDueDate()
                + "|" + loanRecord.isReturned();
    }

    /**
     * Parses one saved catalogue row into a Book object.
     */
    private Book parseBook(String line) {
        List<String> parts = splitLine(line);

        if (parts.size() != 3) {
            return null;
        }

        try {
            long isbn = Long.parseLong(parts.get(0));
            String title = parts.get(1);
            String author = parts.get(2);

            if (!isValidBookInput(isbn, title, author)) {
                return null;
            }

            return new Book(isbn, title, author);
        } catch (NumberFormatException error) {
            return null;
        }
    }

    /**
     * Parses one saved loan row into a LoanRecord object.
     */
    private LoanRecord parseLoanRecord(String line) {
        List<String> parts = splitLine(line);

        if (parts.size() != 7) {
            return null;
        }

        try {
            String studentId = normalizeStudentId(parts.get(0));
            long isbn = Long.parseLong(parts.get(1));
            String title = parts.get(2);
            String author = parts.get(3);
            LocalDate borrowDate = LocalDate.parse(parts.get(4));
            LocalDate dueDate = LocalDate.parse(parts.get(5));
            boolean returned = Boolean.parseBoolean(parts.get(6));

            if (studentId == null || !isValidBookInput(isbn, title, author)) {
                return null;
            }

            return new LoanRecord(studentId, new Book(isbn, title, author), borrowDate, dueDate, returned);
        } catch (DateTimeParseException | NumberFormatException error) {
            return null;
        }
    }

    /**
     * Converts older borrow-history rows into modern loan records.
     */
    private LoanRecord parseLegacyLoanRecord(String line) {
        Book book = parseBook(line);
        if (book == null) {
            return null;
        }

        LocalDate borrowDate = LocalDate.now();
        return new LoanRecord(LEGACY_STUDENT_ID, book, borrowDate, borrowDate.plusDays(LOAN_DAYS), false);
    }

    /**
     * Splits pipe-delimited rows while respecting escaped pipe characters.
     */
    private List<String> splitLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);

            if (escaped) {
                current.append(character);
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == '|') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        if (escaped) {
            current.append('\\');
        }

        parts.add(current.toString());
        return parts;
    }

    /**
     * Escapes characters that would otherwise break the pipe-delimited file.
     */
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\|");
    }
}
