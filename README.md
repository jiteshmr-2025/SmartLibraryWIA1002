# Smart Library (WIA1002)

A console-based Java library management system built for the WIA1002 Data Structures
course. Available books are stored in a **Binary Search Tree** (sorted by ISBN) and
each student's borrowing activity is stored in a **Stack** so the most recent loan
appears first. User accounts and library data are persisted to plain text files,
so no database setup is required.

## Features

### Admin (user ID starts with `A`, e.g. `A001`)
1. Add a book to the catalogue
2. Search a book by ISBN
3. View all available books (sorted by ISBN)
4. Edit a book's title / author
5. Remove a book from the catalogue
6. Search available books by title or author keyword
7. View any student's borrowing history

### Student (user ID starts with `S`, e.g. `S001`)
1. Search a book by ISBN
2. Borrow a book (14-day loan period)
3. Return a borrowed book
4. View own borrowing history (newest first)
5. View all available books
6. Search by title or author keyword

## Project Structure

```
SmartLibraryWIA1002/
├── src/smartlibrary/        # Source code
│   ├── Main.java                  # Console UI, menus, input handling
│   ├── LibraryADT.java            # Library operations contract
│   ├── SmartLibrary.java          # Library service + file persistence
│   ├── BookBST.java               # Binary search tree for the catalogue
│   ├── BorrowHistoryStack.java    # Stack of LoanRecords per student
│   ├── Book.java                  # Book entity (ISBN, title, author)
│   ├── LoanRecord.java            # Loan entity (book, borrow/due dates, status)
│   ├── User.java                  # User entity (id, name, role)
│   └── UserStore.java             # User registration / persistence
├── dist/
│   ├── SmartLibraryWIA1002.jar    # Pre-built executable JAR
│   ├── smart_library_data.txt     # Sample catalogue + loans (sample data)
│   └── user_info.txt              # Sample registered users (sample data)
├── build.xml                # NetBeans Ant build script
├── manifest.mf              # JAR manifest (Main-Class: smartlibrary.Main)
└── nbproject/               # NetBeans project metadata
```

## Requirements

- **Java 17** or newer (the code uses `switch` expressions with arrow syntax)
- A terminal (Windows PowerShell, Command Prompt, or any Unix shell)

Verify Java is installed:

```powershell
java -version
```

## How to Run the JAR

The pre-built JAR is already in `dist/`. **Run it from inside the `dist` folder** so
the program can find (and create) its data files in the working directory.

### Windows (PowerShell)

```powershell
cd dist
java -jar SmartLibraryWIA1002.jar
```

### Windows (Command Prompt)

```cmd
cd dist
java -jar SmartLibraryWIA1002.jar
```

### macOS / Linux

```bash
cd dist
java -jar SmartLibraryWIA1002.jar
```

> **Note:** Running the JAR from outside `dist/` will cause it to read/write the
> data files in whatever directory you're in. Always `cd` into `dist/` first (or
> copy the two `.txt` files alongside the JAR wherever you place it).

## Data Files

The application persists state in two pipe-delimited text files in the working
directory:

### `user_info.txt`
Holds all registered users.

```
[USERS]
A001|Default Admin|ADMIN
S001|Default Student|STUDENT
A002|Chee|ADMIN
S002|Tan Chee Keat|STUDENT
```

Format per row: `userId | name | role` (role is `ADMIN` or `STUDENT`).
If this file is missing or empty, two default accounts are created automatically:
`A001` (admin) and `S001` (student).

### `smart_library_data.txt`
Holds the book catalogue and active/historical loans.

```
[CATALOGUE]
9780132350884|Clean Code|Robert C. Martin
9780134685991|Effective Java|Joshua Bloch
...

[LOANS]
S002|9781491950357|Designing Data-Intensive Applications|Martin Kleppmann|2026-06-10|2026-06-24|true
```

- `[CATALOGUE]` row: `isbn | title | author`
- `[LOANS]` row: `studentId | isbn | title | author | borrowDate | dueDate | active`

Both files are created automatically if they do not exist. The `dist/` folder
already ships with sample data so you have something to interact with on first
launch.

## Default Login Credentials

On a fresh start (no `user_info.txt`), the following accounts are created:

| User ID | Name            | Role    |
|---------|-----------------|---------|
| A001    | Default Admin   | Admin   |
| S001    | Default Student | Student |

Additional sample users included in the shipped `user_info.txt`:

| User ID | Name           | Role    |
|---------|----------------|---------|
| A002    | Chee           | Admin   |
| A005    | jitesh         | Admin   |
| S002    | Tan Chee Keat  | Student |

You can also register new accounts from the main menu — any new ID starting with
`A` becomes an admin and any starting with `S` becomes a student.

## Building From Source

The project ships as a standard NetBeans Ant project.

### Using NetBeans
1. Open the project folder in NetBeans (`File → Open Project`).
2. Right-click the project → `Clean and Build`.
3. The rebuilt JAR is written to `dist/SmartLibraryWIA1002.jar`.

### Using Ant from the command line

```bash
ant clean jar
```

### Using `javac` directly

```bash
mkdir build
javac -d build src/smartlibrary/*.java
java -cp build smartlibrary.Main
```

## Data Structures Used

| Structure              | Where                       | Why                                                |
|------------------------|-----------------------------|----------------------------------------------------|
| Binary Search Tree     | `BookBST` (catalogue)       | O(log n) search/insert by ISBN; in-order traversal yields a sorted listing |
| Stack                  | `BorrowHistoryStack`        | LIFO — most recent loan appears first when viewing history |
| LinkedHashMap          | `UserStore`, loan histories | Preserves insertion order for predictable file output |

## Loan Rules

- Each loan has a fixed **14-day** due date.
- A student cannot borrow a book that is not in the available catalogue.
- Returning a book makes it available again and marks the loan inactive.
- History is preserved even after a book is returned (status changes to `Returned`).

## Troubleshooting

- **"Warning: Could not load …"** — the program could not read a data file
  (permissions or corrupt format). Delete the offending file and restart to
  regenerate it from defaults.
- **`java: command not found`** — install a JDK (17+) and ensure `java` is on your
  `PATH`.
- **Data not persisting between runs** — make sure you are running the JAR from
  the same directory each time (typically `dist/`).

## License

See [LICENSE](LICENSE).
