package com.example.projectpoker.database;

import com.example.projectpoker.model.User;
import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.enums.Difficulty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseTest {
    private static final String DB_PATH_PROPERTY = "projectpoker.db.path";
    private static final Path DEMO_DATABASE_PATH = Path.of("demo-database-test.db");

    private Path testDatabasePath;

    @BeforeEach
    void setUp() throws IOException {
        DatabaseConnection.closeConnection();
        // Use a fixed SQLite file
        testDatabasePath = DEMO_DATABASE_PATH;
        Files.deleteIfExists(testDatabasePath);
        System.setProperty(DB_PATH_PROPERTY, testDatabasePath.toString());
        DatabaseManager.initializeDatabase();
    }

    @AfterEach
    void tearDown() throws IOException {
        DatabaseConnection.closeConnection();
        System.clearProperty(DB_PATH_PROPERTY);
        // Keep the demo database on disk so its tables and rows can be inspected visually.
    }

    @Test
    void initializeDatabaseCreatesAllRequiredTables() throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance();
             Statement statement = connection.createStatement()) {
            // This verifies the persistence layer creates the schema the application depends on.
            ResultSet resultSet = statement.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type = 'table' AND name IN "
                            + "('users', 'game_sessions', 'round_logs', 'round_actions')"
            );

            int tableCount = 0;
            while (resultSet.next()) {
                tableCount++;
            }

            assertEquals(4, tableCount);
        }
    }

    @Test
    void insertAndLoadUserPersistsRegisteredUserData() {
        UserDAO userDAO = new UserDAO();
        User user = new User("db_user", "hashed-password", "db_user@test.com");

        // Saving and reloading a user proves registration data is written to the database correctly.
        userDAO.insert(user);
        User loadedUser = userDAO.getByUsername("db_user");

        assertNotNull(loadedUser);
        assertTrue(loadedUser.getId() > 0);
        assertEquals("db_user", loadedUser.getUsername());
        assertEquals("db_user@test.com", loadedUser.getEmail());
        assertEquals(1000, loadedUser.getCurrentBalance());
    }

    @Test
    void updateUserPersistsBalanceAndStatsChanges() {
        UserDAO userDAO = new UserDAO();
        User user = new User("balance_user", "hashed-password", "balance@test.com");
        userDAO.insert(user);

        // This covers the expected behaviour when a returning player's profile is updated.
        user.setCurrentBalance(1450);
        user.setTotalHandsPlayed(8);
        user.setTotalWins(3);
        userDAO.update(user);

        User loadedUser = userDAO.getByUsername("balance_user");

        assertNotNull(loadedUser);
        assertEquals(1450, loadedUser.getCurrentBalance());
        assertEquals(8, loadedUser.getTotalHandsPlayed());
        assertEquals(3, loadedUser.getTotalWins());
    }

    @Test
    void saveUserProgressPersistsMidSessionBalance() {
        UserDAO userDAO = new UserDAO();
        User user = new User("progress_user", "hashed-password", "progress@test.com");
        userDAO.insert(user);

        TestPlayer player = new TestPlayer(user.getUsername(), user.getCurrentBalance());
        player.forceBalance(1325);

        // The balance bug fix depends on saving progress before the whole game session is finalized.
        DatabaseManager.saveUserProgress(user, player);

        User loadedUser = userDAO.getByUsername("progress_user");

        assertNotNull(loadedUser);
        assertEquals(1325, loadedUser.getCurrentBalance());
    }

    @Test
    void endGameFinalizesSessionAndPersistsUserBalance() throws SQLException {
        UserDAO userDAO = new UserDAO();
        User user = new User("session_user", "hashed-password", "session@test.com");
        userDAO.insert(user);

        TestPlayer player = new TestPlayer(user.getUsername(), user.getCurrentBalance());
        Game game = new Game(player, user, user.getCurrentBalance(), 4, 50, 5, 10, Difficulty.BABY);
        game.init();
        player.forceBalance(1600);

        // Ending a game should update both the user's saved balance and the session audit record.
        game.end();

        User loadedUser = userDAO.getByUsername("session_user");
        assertNotNull(loadedUser);
        assertEquals(1600, loadedUser.getCurrentBalance());
        assertEquals(1, loadedUser.getTotalWins());

        try (Connection connection = DatabaseConnection.getInstance();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT ending_balance, status, ended_at FROM game_sessions WHERE user_id = " + user.getId()
             )) {
            assertTrue(resultSet.next());
            assertEquals(1600, resultSet.getInt("ending_balance"));
            assertEquals("ENDED", resultSet.getString("status"));
            assertNotNull(resultSet.getString("ended_at"));
        }
    }

    private static class TestPlayer extends Player {
        TestPlayer(String name, int balance) {
            super(name, balance);
        }

        // Exposes the protected balance setter for database persistence tests only.
        void forceBalance(int balance) {
            setBalance(balance);
        }
    }
}
