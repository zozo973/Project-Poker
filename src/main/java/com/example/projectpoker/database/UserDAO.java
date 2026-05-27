package com.example.projectpoker.database;

import com.example.projectpoker.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection connection;

    // Uses the shared SQLite connection for all user queries.
    /**
     * Creates a DAO instance backed by the shared SQLite connection.
     */
    public UserDAO() {
        connection = DatabaseConnection.getInstance();
    }

    /**
     * Creates the {@code users} table if it does not already exist and adds any newer columns
     * that may be missing from an older database schema.
     */
    public void createTable() {
        try {
            Statement createTable = connection.createStatement();
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS users ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "username VARCHAR NOT NULL UNIQUE, "
                            + "password VARCHAR NOT NULL, "
                            + "email VARCHAR UNIQUE, "
                            + "totalHandsPlayed INTEGER NOT NULL, "
                            + "totalWins INTEGER NOT NULL, "
                            + "currentBalance INTEGER NOT NULL DEFAULT 1000"
                            + ")"
            );
            ensureColumnExists("currentBalance", "INTEGER NOT NULL DEFAULT 1000");
            ensureColumnExists("email", "VARCHAR");
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Adds one missing column without deleting existing user data.
     *
     * @param columnName the name of the column to add
     * @param definition the data type and constraints for the column
     */
    private void ensureColumnExists(String columnName, String definition) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        // SQLite has no simple "ADD COLUMN IF NOT EXISTS", so metadata is checked first.
        try (ResultSet columns = metadata.getColumns(null, null, "users", columnName)) {
            if (!columns.next()) {
                try (Statement alterTable = connection.createStatement()) {
                    alterTable.execute("ALTER TABLE users ADD COLUMN " + columnName + " " + definition);
                }
            }
        }
    }

    /**
     * Inserts a new user row into the database and writes the generated primary key
     * back into the {@link User} object.
     *
     * @param user the {@link User} to insert; its {@code id} field is updated on success
     */
    public void insert(User user) {
        try {
            PreparedStatement insertUser = connection.prepareStatement(
                    "INSERT INTO users (username, password, email, totalHandsPlayed, totalWins, currentBalance) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            insertUser.setString(1, user.getUsername());
            insertUser.setString(2, user.getPassword());
            insertUser.setString(3, user.getEmail());
            insertUser.setInt(4, user.getTotalHandsPlayed());
            insertUser.setInt(5, user.getTotalWins());
            insertUser.setInt(6, user.getCurrentBalance());
            insertUser.execute();

            // The generated id is needed later for updates and game session records.
            ResultSet keys = insertUser.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Updates the stored profile values (username, password, hands played, wins, balance)
     * for the user identified by their database id.
     *
     * @param user the {@link User} whose record should be updated
     */
    public void update(User user) {
        try {
            PreparedStatement updateUser = connection.prepareStatement(
                    "UPDATE users SET username = ?, password = ?, totalHandsPlayed = ?, totalWins = ?, currentBalance = ? WHERE id = ?"
            );
            updateUser.setString(1, user.getUsername());
            updateUser.setString(2, user.getPassword());
            updateUser.setInt(3, user.getTotalHandsPlayed());
            updateUser.setInt(4, user.getTotalWins());
            updateUser.setInt(5, user.getCurrentBalance());
            updateUser.setInt(6, user.getId());
            updateUser.execute();
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Updates only the chip balance for the user with the given id.
     * Prefer this over {@link #update(User)} when only the balance has changed,
     * to avoid accidentally overwriting newer statistics.
     *
     * @param userId         the primary key of the user to update
     * @param currentBalance the new balance value to store
     */
    public void updateCurrentBalance(int userId, int currentBalance) {
        try {
            PreparedStatement updateUser = connection.prepareStatement(
                    "UPDATE users SET currentBalance = ? WHERE id = ?"
            );
            updateUser.setInt(1, currentBalance);
            updateUser.setInt(2, userId);
            updateUser.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Atomically increments the {@code totalWins} counter for the user with the given id
     * directly in the database, avoiding stale in-memory state issues.
     *
     * @param userId the primary key of the user whose win count should be incremented
     */
    public void incrementTotalWins(int userId) {
        try {
            PreparedStatement updateUser = connection.prepareStatement(
                    "UPDATE users SET totalWins = totalWins + 1 WHERE id = ?"
            );
            updateUser.setInt(1, userId);
            updateUser.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Deletes the user row with the given id from the database.
     *
     * @param id the primary key of the user to delete
     */
    public void delete(int id) {
        try {
            PreparedStatement deleteUser = connection.prepareStatement(
                    "DELETE FROM users WHERE id = ?"
            );
            deleteUser.setInt(1, id);
            deleteUser.execute();
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Retrieves every row from the {@code users} table as a list of {@link User} objects.
     *
     * @return a list of all users; empty if the table contains no rows
     */
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        try {
            Statement getAll = connection.createStatement();
            ResultSet rs = getAll.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                users.add(
                        new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email"),
                                rs.getInt("totalHandsPlayed"),
                                rs.getInt("totalWins"),
                                rs.getInt("currentBalance")
                        )
                );
            }
        } catch (SQLException ex) {
            System.err.println(ex);
        }
        return users;
    }

    /**
     * Retrieves a single user by their database id.
     *
     * @param id the primary key to search for
     * @return the matching {@link User}, or {@code null} if no row with that id exists
     */
    public User getById(int id) {
        try {
            PreparedStatement getUser = connection.prepareStatement(
                    "SELECT * FROM users WHERE id = ?"
            );
            getUser.setInt(1, id);
            ResultSet rs = getUser.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getInt("totalHandsPlayed"),
                        rs.getInt("totalWins"),
                        rs.getInt("currentBalance")
                );
            }
        } catch (SQLException ex) {
            System.err.println(ex);
        }
        return null;
    }

    /**
     * Retrieves a single user by their username.
     *
     * @param username the username to search for (case-sensitive)
     * @return the matching {@link User}, or {@code null} if the username is not registered
     */
    public User getByUsername(String username) {
        try {
            PreparedStatement getUser = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ?"
            );
            getUser.setString(1, username);
            ResultSet rs = getUser.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getInt("totalHandsPlayed"),
                        rs.getInt("totalWins"),
                        rs.getInt("currentBalance")
                );
            }
        } catch (SQLException ex) {
            System.err.println(ex);
        }
        return null;
    }

    /**
     * Returns an existing account matching the given username, or creates and inserts a new
     * minimal user record if none is found.
     *
     * @param username       the username to look up or create
     * @param password       the password to assign to a newly created user (should be pre-hashed)
     * @param defaultBalance the starting balance for a newly created user
     * @return the existing or newly created {@link User}
     */
    public User getOrCreate(String username, String password, int defaultBalance) {
        User existingUser = getByUsername(username);
        if (existingUser != null) {
            return existingUser;
        }

        User newUser = new User(username, password, null);
        newUser.setCurrentBalance(defaultBalance);

        insert(newUser);
        return newUser;
    }

    /**
     * No-op method kept for DAO interface compatibility.
     * The shared database connection is managed centrally by {@link DatabaseConnection}.
     */
    public void close() {
        // Shared singleton connection is managed centrally.
    }
}
