package smartlibrary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages registered users and persists them to a simple text file.
 * User IDs starting with A are admins, while IDs starting with S are students.
 */
public class UserStore {
    private static final Path USER_FILE = Path.of("user_info.txt");
    private static final String USERS_HEADER = "[USERS]";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String STUDENT_ROLE = "STUDENT";

    private final Map<String, User> users;

    /**
     * Loads saved users and creates default accounts when the file is empty.
     */
    public UserStore() {
        users = new LinkedHashMap<>();
        loadUsers();

        if (users.isEmpty()) {
            users.put("A001", new User("A001", "Default Admin", ADMIN_ROLE));
            users.put("S001", new User("S001", "Default Student", STUDENT_ROLE));
            saveUsers();
        }
    }

    /**
     * Finds a user by ID after normalizing the input.
     */
    public User findUser(String userId) {
        if (userId == null) {
            return null;
        }

        return users.get(normalizeUserId(userId));
    }

    /**
     * Registers a new user if the ID format is valid and unused.
     */
    public boolean registerUser(String userId, String name) {
        String normalizedUserId = normalizeUserId(userId);

        if (!isValidUserId(normalizedUserId) || name == null || name.isBlank() || users.containsKey(normalizedUserId)) {
            return false;
        }

        users.put(normalizedUserId, new User(normalizedUserId, name.trim(), getRoleFromUserId(normalizedUserId)));
        saveUsers();
        return true;
    }

    /**
     * Accepts only admin IDs beginning with A and student IDs beginning with S.
     */
    public boolean isValidUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }

        String normalizedUserId = normalizeUserId(userId);
        return normalizedUserId.startsWith("A") || normalizedUserId.startsWith("S");
    }

    /**
     * Converts the first letter of the user ID into the stored role name.
     */
    public String getRoleFromUserId(String userId) {
        String normalizedUserId = normalizeUserId(userId);

        if (normalizedUserId.startsWith("A")) {
            return ADMIN_ROLE;
        }

        if (normalizedUserId.startsWith("S")) {
            return STUDENT_ROLE;
        }

        return "";
    }

    /**
     * Reads saved users from the [USERS] section of the user file.
     */
    private void loadUsers() {
        if (!Files.exists(USER_FILE)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(USER_FILE);
            boolean inUsersSection = false;

            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }

                if (line.equals(USERS_HEADER)) {
                    inUsersSection = true;
                    continue;
                }

                if (!inUsersSection) {
                    continue;
                }

                User user = parseUser(line);
                if (user != null) {
                    users.put(user.getUserId(), user);
                }
            }
        } catch (IOException error) {
            System.out.println("Warning: Could not load user information.");
        }
    }

    /**
     * Writes all current users back to the user file.
     */
    private void saveUsers() {
        List<String> lines = new ArrayList<>();
        lines.add(USERS_HEADER);

        for (User user : users.values()) {
            lines.add(formatUser(user));
        }

        try {
            Files.write(USER_FILE, lines);
        } catch (IOException error) {
            System.out.println("Warning: Could not update " + USER_FILE + ".");
        }
    }

    /**
     * Parses one saved user row into a User object.
     */
    private User parseUser(String line) {
        List<String> parts = splitLine(line);

        if (parts.size() != 3) {
            return null;
        }

        String userId = normalizeUserId(parts.get(0));
        String name = parts.get(1);
        String role = parts.get(2).trim().toUpperCase();

        if (!isValidUserId(userId) || name.isBlank() || role.isBlank()) {
            return null;
        }

        return new User(userId, name, role);
    }

    /**
     * Formats one user as a pipe-delimited row.
     */
    private String formatUser(User user) {
        return escape(user.getUserId()) + "|" + escape(user.getName()) + "|" + escape(user.getRole());
    }

    private String normalizeUserId(String userId) {
        return userId.trim().toUpperCase();
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
