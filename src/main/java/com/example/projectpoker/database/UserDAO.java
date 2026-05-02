package com.example.projectpoker.database;

import com.example.projectpoker.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection connection;

    // Uses the shared SQLite connection for all user queries.
    public UserDAO() {
        connection = DatabaseConnection.getInstance();
    }

    // Creates the users table and adds newer columns if an older database is opened.
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

    // Adds one missing column without deleting existing user data.
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

    // Inserts a new user and copies the generated database id back into the User object.
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

    // Updates the stored profile values for an existing user.
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

    // Deletes one user by their database id.
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

    // Loads every user row and converts each row into a User object.
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

    // Finds one user by id, or returns null if no row matches.
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

    // Finds one user by username, or returns null if the username is not registered.
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

    // Reuses an existing account when possible, otherwise creates a basic user record.
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

    // Kept for DAO compatibility; the shared connection is closed through DatabaseConnection.
    public void close() {
        // Shared singleton connection is managed centrally.
    }
}
