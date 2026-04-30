package com.example.projectpoker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_PATH_PROPERTY = "projectpoker.db.path";
    private static Connection instance = null;

    private DatabaseConnection() {
        try {
            instance = DriverManager.getConnection(getUrl());
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
            String databasePath = System.getProperty(DB_PATH_PROPERTY, "projectpoker.db");
            return "jdbc:sqlite:" + databasePath;
        }
        return getUrlFromConnection();
    }

    private static String getUrlFromConnection() {
        try {
            return instance.getMetaData().getURL();
        } catch (SQLException sqlEx) {
            throw new IllegalStateException("Failed to read SQLite database URL.", sqlEx);
        }
    }
}
