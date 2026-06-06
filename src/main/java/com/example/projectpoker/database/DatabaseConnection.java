package com.example.projectpoker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_PATH_PROPERTY = "projectpoker.db.path";
    private static Connection instance = null;

    // Opens the single SQLite connection used by the DAO classes.
    private DatabaseConnection() {
        try {
            instance = DriverManager.getConnection(getUrl());
        } catch (SQLException sqlEx) {
            throw new IllegalStateException("Failed to connect to SQLite database.", sqlEx);
        }
    }

    /**
     * Returns the shared SQLite {@link Connection}, creating a new one if it does not yet exist
     * or has been closed.
     *
     * @return the open shared {@link Connection}
     * @throws IllegalStateException if the connection cannot be opened
     */
    public static Connection getInstance() {
        try {
            if (instance == null || instance.isClosed()) {
                instance = null;
                new DatabaseConnection();
            }
        } catch (SQLException sqlEx) {
            throw new IllegalStateException("Failed to access SQLite database connection.", sqlEx);
        }
        return instance;
    }

    /**
     * Closes the shared SQLite connection and releases all associated resources.
     * Safe to call when the application shuts down or tests need to reset state.
     *
     * @throws IllegalStateException if the connection cannot be closed
     */
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

    /**
     * Returns the JDBC URL for the active connection, or builds one from the system property
     * {@code projectpoker.db.path} (defaulting to {@code "projectpoker.db"}) if no connection exists yet.
     *
     * @return the JDBC URL string, e.g. {@code "jdbc:sqlite:projectpoker.db"}
     */
    public static String getUrl() {
        if (instance == null) {
            // Default to the real app database unless a test provides its own path.
            String databasePath = System.getProperty(DB_PATH_PROPERTY, "projectpoker.db");
            return "jdbc:sqlite:" + databasePath;
        }
        return getUrlFromConnection();
    }

    // Reads the URL from the active connection instead of rebuilding it.
    private static String getUrlFromConnection() {
        try {
            return instance.getMetaData().getURL();
        } catch (SQLException sqlEx) {
            throw new IllegalStateException("Failed to read SQLite database URL.", sqlEx);
        }
    }
}
