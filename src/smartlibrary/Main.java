package smartlibrary;

import java.util.List;
import java.util.Scanner;

/**
 * Console user interface for the Smart Library application.
 * This class handles menus, input validation, and output formatting while the
 * library logic stays inside SmartLibrary.
 */
public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final LibraryADT LIBRARY = new SmartLibrary();
    private static final UserStore USER_STORE = new UserStore();

    /**
     * User roles are derived from the first letter of the registered user ID.
     */
    private enum UserRole {
        ADMIN,
        STUDENT
    }

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            printEntryMenu();
            int choice = readMenuChoice();

            switch (choice) {
                case 1 -> login();
                case 2 -> registerUser();
                case 3 -> {
                    System.out.println("Thank you for using Smart Library. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please select a number from 1 to 3.");
            }
        }
    }

    /**
     * Prints the first menu shown before a user logs in.
     */
    private static void printEntryMenu() {
        System.out.println();
        System.out.println("===== Smart Library =====");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.println("Admin ID starts with A. Example: A001");
        System.out.println("Student ID starts with S. Example: S001");
        System.out.print("Enter your choice: ");
    }

    /**
     * Authenticates an existing user ID and opens the matching role menu.
     */
    private static void login() {
        System.out.println();
        System.out.println("===== Login Page =====");
        System.out.print("Enter user ID: ");

        String userId = SCANNER.nextLine().trim().toUpperCase();

        if (userId.isEmpty()) {
            System.out.println("User ID cannot be empty.");
            return;
        }

        User user = USER_STORE.findUser(userId);
        if (user == null) {
            System.out.println("User ID not found. Please register first.");
            return;
        }

        UserRole role = identifyRole(user);
        System.out.println("Welcome, " + user.getName() + " (" + user.getUserId() + ").");

        if (role == UserRole.ADMIN) {
            runAdminMenu(user.getUserId());
        } else {
            runStudentMenu(user.getUserId());
        }
    }

    /**
     * Registers either an admin or student account based on the ID prefix.
     */
    private static void registerUser() {
        System.out.println();
        System.out.println("===== Register Page =====");
        System.out.println("Use an ID starting with A for Admin or S for Student.");

        String userId = readRequiredText("Enter new user ID: ");
        if (userId == null) {
            return;
        }

        userId = userId.toUpperCase();
        if (!USER_STORE.isValidUserId(userId)) {
            System.out.println("Registration failed. Admin IDs must start with A, and Student IDs must start with S.");
            return;
        }

        String name = readRequiredText("Enter full name: ");
        if (name == null) {
            return;
        }

        boolean registered = USER_STORE.registerUser(userId, name);
        if (registered) {
            System.out.println("Registration successful. You can now login using ID " + userId + ".");
        } else {
            System.out.println("Registration failed. The user ID may already exist.");
        }
    }

    /**
     * Converts the registered user ID prefix into a menu role.
     */
    private static UserRole identifyRole(User user) {
        if (user.getUserId().startsWith("A")) {
            return UserRole.ADMIN;
        }

        if (user.getUserId().startsWith("S")) {
            return UserRole.STUDENT;
        }

        return null;
    }

    /**
     * Keeps showing admin options until the admin chooses to log out.
     */
    private static void runAdminMenu(String adminId) {
        boolean loggedIn = true;

        while (loggedIn) {
            printAdminMenu(adminId);
            int choice = readMenuChoice();

            switch (choice) {
                case 1 -> addBook();
                case 2 -> searchBookByIsbn();
                case 3 -> viewAllAvailableBooks();
                case 4 -> editBookDetails();
                case 5 -> removeBook();
                case 6 -> searchByTitleOrAuthor();
                case 7 -> viewStudentHistoryByAdmin();
                case 8 -> {
                    System.out.println("Logged out from Admin account " + adminId + ".");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice. Please select a number from 1 to 8.");
            }
        }
    }

    private static void printAdminMenu(String adminId) {
        System.out.println();
        System.out.println("===== Smart Library Admin Menu (" + adminId + ") =====");
        System.out.println("1. Add Book");
        System.out.println("2. Search Book by ISBN");
        System.out.println("3. View All Available Books");
        System.out.println("4. Edit Book Details");
        System.out.println("5. Remove Book");
        System.out.println("6. Search by Title or Author");
        System.out.println("7. View Student History");
        System.out.println("8. Logout");
        System.out.print("Enter your choice: ");
    }

    /**
     * Keeps showing student options until the student chooses to log out.
     */
    private static void runStudentMenu(String studentId) {
        boolean loggedIn = true;

        while (loggedIn) {
            printStudentMenu(studentId);
            int choice = readMenuChoice();

            switch (choice) {
                case 1 -> searchBookByIsbn();
                case 2 -> borrowBook(studentId);
                case 3 -> returnBook(studentId);
                case 4 -> viewStudentHistory(studentId);
                case 5 -> viewAllAvailableBooks();
                case 6 -> searchByTitleOrAuthor();
                case 7 -> {
                    System.out.println("Logged out from Student account " + studentId + ".");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid choice. Please select a number from 1 to 7.");
            }
        }
    }

    private static void printStudentMenu(String studentId) {
        System.out.println();
        System.out.println("===== Smart Library Student Menu (" + studentId + ") =====");
        System.out.println("1. Search Book by ISBN");
        System.out.println("2. Borrow Book");
        System.out.println("3. Return Book");
        System.out.println("4. View My History");
        System.out.println("5. View All Available Books");
        System.out.println("6. Search by Title or Author");
        System.out.println("7. Logout");
        System.out.print("Enter your choice: ");
    }

    /**
     * Reads a numeric menu choice. Invalid numbers are returned as -1 so each
     * menu can handle the error in its default switch branch.
     */
    private static int readMenuChoice() {
        String input = SCANNER.nextLine().trim();

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException error) {
            return -1;
        }
    }

    /**
     * Collects book details from the admin and sends them to the library layer.
     */
    private static void addBook() {
        Long isbn = readPositiveIsbn("Enter ISBN: ");
        if (isbn == null) {
            return;
        }

        String title = readRequiredText("Enter title: ");
        if (title == null) {
            return;
        }

        String author = readRequiredText("Enter author: ");
        if (author == null) {
            return;
        }

        boolean added = LIBRARY.addBook(isbn, title, author);

        if (added) {
            System.out.println("Book added successfully.");
        } else {
            System.out.println("Book could not be added. The ISBN may already exist or the input is invalid.");
        }
    }

    private static void searchBookByIsbn() {
        Long isbn = readPositiveIsbn("Enter ISBN to search: ");
        if (isbn == null) {
            return;
        }

        Book book = LIBRARY.searchBook(isbn);

        if (book == null) {
            System.out.println("No available book found with ISBN " + isbn + ".");
        } else {
            System.out.println("Book found:");
            printBooks(List.of(book));
        }
    }

    private static void borrowBook(String studentId) {
        Long isbn = readPositiveIsbn("Enter ISBN to borrow: ");
        if (isbn == null) {
            return;
        }

        LoanRecord loanRecord = LIBRARY.borrowBook(studentId, isbn);

        if (loanRecord == null) {
            System.out.println("Borrow failed. No available book found with ISBN " + isbn + ".");
        } else {
            System.out.println("Borrowed successfully:");
            printLoanRecords(List.of(loanRecord));
        }
    }

    private static void returnBook(String studentId) {
        Long isbn = readPositiveIsbn("Enter ISBN to return: ");
        if (isbn == null) {
            return;
        }

        LoanRecord loanRecord = LIBRARY.returnBook(studentId, isbn);

        if (loanRecord == null) {
            System.out.println("Return failed. No active loan found for this student and ISBN.");
        } else {
            System.out.println("Returned successfully:");
            printLoanRecords(List.of(loanRecord));
        }
    }

    private static void viewStudentHistoryByAdmin() {
        String studentId = readRequiredText("Enter student ID: ");
        if (studentId == null) {
            return;
        }

        viewStudentHistory(studentId);
    }

    private static void viewStudentHistory(String studentId) {
        List<LoanRecord> loanRecords = LIBRARY.viewBorrowHistory(studentId);

        if (loanRecords.isEmpty()) {
            System.out.println("No borrowing history found for student " + studentId + ".");
            return;
        }

        System.out.println("Borrowing History for " + studentId + " (Most Recent First):");
        printLoanRecords(loanRecords);
    }

    private static void viewAllAvailableBooks() {
        List<Book> books = LIBRARY.viewAvailableBooks();

        if (books.isEmpty()) {
            System.out.println("No books are currently available in the catalogue.");
            return;
        }

        System.out.println("Available Books (Sorted by ISBN):");
        printBooks(books);
    }

    private static void editBookDetails() {
        Long isbn = readPositiveIsbn("Enter ISBN to edit: ");
        if (isbn == null) {
            return;
        }

        Book currentBook = LIBRARY.searchBook(isbn);
        if (currentBook == null) {
            System.out.println("Edit failed. Only available catalogue books can be edited.");
            return;
        }

        System.out.println("Current book:");
        printBooks(List.of(currentBook));

        String title = readRequiredText("Enter new title: ");
        if (title == null) {
            return;
        }

        String author = readRequiredText("Enter new author: ");
        if (author == null) {
            return;
        }

        boolean edited = LIBRARY.editBook(isbn, title, author);

        if (edited) {
            System.out.println("Book details updated successfully.");
        } else {
            System.out.println("Edit failed. Please check the ISBN and input values.");
        }
    }

    private static void removeBook() {
        Long isbn = readPositiveIsbn("Enter ISBN to remove: ");
        if (isbn == null) {
            return;
        }

        Book removedBook = LIBRARY.removeBook(isbn);

        if (removedBook == null) {
            System.out.println("Remove failed. Only available catalogue books can be removed.");
        } else {
            System.out.println("Book removed permanently:");
            printBooks(List.of(removedBook));
        }
    }

    private static void searchByTitleOrAuthor() {
        String keyword = readRequiredText("Enter title or author keyword: ");
        if (keyword == null) {
            return;
        }

        List<Book> matches = LIBRARY.searchByTitleOrAuthor(keyword);

        if (matches.isEmpty()) {
            System.out.println("No available books matched \"" + keyword + "\".");
            return;
        }

        System.out.println("Matching Available Books:");
        printBooks(matches);
    }

    /**
     * Prints book records in a table whose column widths match the content.
     */
    private static void printBooks(List<Book> books) {
        int noWidth = Math.max(2, String.valueOf(books.size()).length());
        int isbnWidth = "ISBN".length();
        int titleWidth = "Title".length();
        int authorWidth = "Author".length();

        for (Book book : books) {
            isbnWidth = Math.max(isbnWidth, String.valueOf(book.getIsbn()).length());
            titleWidth = Math.max(titleWidth, book.getTitle().length());
            authorWidth = Math.max(authorWidth, book.getAuthor().length());
        }

        // Build the divider from the same widths so long titles do not break
        // the table layout.
        printBookDivider(noWidth, isbnWidth, titleWidth, authorWidth);
        System.out.printf("| %-" + noWidth + "s | %-" + isbnWidth + "s | %-" + titleWidth + "s | %-" + authorWidth + "s |%n",
                "No", "ISBN", "Title", "Author");
        printBookDivider(noWidth, isbnWidth, titleWidth, authorWidth);

        for (int index = 0; index < books.size(); index++) {
            Book book = books.get(index);
            System.out.printf("| %" + noWidth + "d | %-" + isbnWidth + "d | %-" + titleWidth + "s | %-" + authorWidth + "s |%n",
                    index + 1, book.getIsbn(), book.getTitle(), book.getAuthor());
        }

        printBookDivider(noWidth, isbnWidth, titleWidth, authorWidth);
    }

    private static void printBookDivider(int noWidth, int isbnWidth, int titleWidth, int authorWidth) {
        System.out.println("+-" + "-".repeat(noWidth)
                + "-+-" + "-".repeat(isbnWidth)
                + "-+-" + "-".repeat(titleWidth)
                + "-+-" + "-".repeat(authorWidth)
                + "-+");
    }

    /**
     * Prints loan records in a table with dynamic column widths.
     */
    private static void printLoanRecords(List<LoanRecord> loanRecords) {
        int noWidth = Math.max(2, String.valueOf(loanRecords.size()).length());
        int isbnWidth = "ISBN".length();
        int titleWidth = "Title".length();
        int borrowWidth = "Borrowed".length();
        int dueWidth = "Due".length();
        int statusWidth = "Status".length();

        for (LoanRecord loanRecord : loanRecords) {
            isbnWidth = Math.max(isbnWidth, String.valueOf(loanRecord.getBook().getIsbn()).length());
            titleWidth = Math.max(titleWidth, loanRecord.getBook().getTitle().length());
            borrowWidth = Math.max(borrowWidth, loanRecord.getBorrowDate().toString().length());
            dueWidth = Math.max(dueWidth, loanRecord.getDueDate().toString().length());
            statusWidth = Math.max(statusWidth, loanRecord.getStatus().length());
        }

        printLoanDivider(noWidth, isbnWidth, titleWidth, borrowWidth, dueWidth, statusWidth);
        System.out.printf("| %-" + noWidth + "s | %-" + isbnWidth + "s | %-" + titleWidth + "s | %-" + borrowWidth + "s | %-" + dueWidth + "s | %-" + statusWidth + "s |%n",
                "No", "ISBN", "Title", "Borrowed", "Due", "Status");
        printLoanDivider(noWidth, isbnWidth, titleWidth, borrowWidth, dueWidth, statusWidth);

        for (int index = 0; index < loanRecords.size(); index++) {
            LoanRecord loanRecord = loanRecords.get(index);
            System.out.printf("| %" + noWidth + "d | %-" + isbnWidth + "d | %-" + titleWidth + "s | %-" + borrowWidth + "s | %-" + dueWidth + "s | %-" + statusWidth + "s |%n",
                    index + 1,
                    loanRecord.getBook().getIsbn(),
                    loanRecord.getBook().getTitle(),
                    loanRecord.getBorrowDate(),
                    loanRecord.getDueDate(),
                    loanRecord.getStatus());
        }

        printLoanDivider(noWidth, isbnWidth, titleWidth, borrowWidth, dueWidth, statusWidth);
    }

    private static void printLoanDivider(int noWidth, int isbnWidth, int titleWidth, int borrowWidth, int dueWidth, int statusWidth) {
        System.out.println("+-" + "-".repeat(noWidth)
                + "-+-" + "-".repeat(isbnWidth)
                + "-+-" + "-".repeat(titleWidth)
                + "-+-" + "-".repeat(borrowWidth)
                + "-+-" + "-".repeat(dueWidth)
                + "-+-" + "-".repeat(statusWidth)
                + "-+");
    }

    /**
     * Reads an ISBN and rejects blank, non-numeric, or non-positive values.
     */
    private static Long readPositiveIsbn(String prompt) {
        System.out.print(prompt);
        String input = SCANNER.nextLine().trim();

        try {
            long isbn = Long.parseLong(input);
            if (isbn <= 0) {
                System.out.println("ISBN must be a positive number.");
                return null;
            }
            return isbn;
        } catch (NumberFormatException error) {
            System.out.println("Invalid ISBN. Please enter numbers only.");
            return null;
        }
    }

    /**
     * Reads a required text field and returns null when the user leaves it blank.
     */
    private static String readRequiredText(String prompt) {
        System.out.print(prompt);
        String input = SCANNER.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("This field cannot be empty.");
            return null;
        }

        return input;
    }
}
