package com.example.projectpoker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection instance = null;
    private static final String URL = "jdbc:sqlite:projectpoker.db";

    private DatabaseConnection() {
        try {
            instance = DriverManager.getConnection(URL);
        } catch (SQLException sqlEx) {
            throw new IllegalStateException("Failed to connect to SQLite database.", sqlEx);
        }
    }

    public static Connection getInstance() {
        try {
            if (instance == null || instance.isClosed()) {
                new DatabaseConnection();
            }
        } catch (SQLException sqlEx) {
            throw new IllegalStateException("Failed to access SQLite database connection.", sqlEx);
        }
        return instance;
    }

    public static void closeConnection() {
        if (instance == null) {
            return;
        }
        try {
            if (!instance.isClosed()) {
                instance.close();
            }
            instance = null;
        } catch (SQLException sqlEx) {
            throw new IllegalStateException("Failed to close SQLite database connection.", sqlEx);
        }
    }

    public static String getUrl() {
        if (instance == null) {
            new DatabaseConnection();
        }
        return URL;
    }
}
