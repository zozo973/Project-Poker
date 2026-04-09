package com.example.projectpoker.database;

import com.example.projectpoker.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection connection;

    public UserDAO() {
        connection = DatabaseConnection.getInstance();
    }

    public void createTable() {
        try {
            Statement createTable = connection.createStatement();
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS users ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "username VARCHAR NOT NULL UNIQUE, "
                            + "password VARCHAR NOT NULL, "
                            + "totalHandsPlayed INTEGER NOT NULL, "
                            + "totalWins INTEGER NOT NULL, "
                            + "currentBalance INTEGER NOT NULL DEFAULT 1000"
                            + ")"
            );
            ensureColumnExists("currentBalance", "INTEGER NOT NULL DEFAULT 1000");
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    private void ensureColumnExists(String columnName, String definition) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet columns = metadata.getColumns(null, null, "users", columnName)) {
            if (!columns.next()) {
                try (Statement alterTable = connection.createStatement()) {
                    alterTable.execute("ALTER TABLE users ADD COLUMN " + columnName + " " + definition);
                }
            }
        }
    }

    public void insert(User user) {
        try {
            PreparedStatement insertUser = connection.prepareStatement(
                    "INSERT INTO users (username, password, totalHandsPlayed, totalWins, currentBalance) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            insertUser.setString(1, user.getUsername());
            insertUser.setString(2, user.getPassword());
            insertUser.setInt(3, user.getTotalHandsPlayed());
            insertUser.setInt(4, user.getTotalWins());
            insertUser.setInt(5, user.getCurrentBalance());
            insertUser.execute();

            ResultSet keys = insertUser.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

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

    public User getOrCreate(String username, String password, int defaultBalance) {
        User existingUser = getByUsername(username);
        if (existingUser != null) {
            return existingUser;
        }

        User newUser = new User(username, password, 0, 0, defaultBalance);
        insert(newUser);
        return newUser;
    }

    public void close() {
        // Shared singleton connection is managed centrally.
    }
}
