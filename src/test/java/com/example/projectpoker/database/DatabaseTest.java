package com.example.projectpoker.database;

import com.example.projectpoker.model.GamePreferences;
import com.example.projectpoker.model.User;
import com.example.projectpoker.model.game.Card;
import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.RoleUtil;
import com.example.projectpoker.model.game.Round;
import com.example.projectpoker.model.game.enums.Action;
import com.example.projectpoker.model.game.enums.Difficulty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
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
    private static final String DEMO_USERNAME = "demo_user";
    private static final String DEMO_EMAIL = "demo_user@test.com";

    private Path testDatabasePath;

    @BeforeEach
    void setUp() throws IOException {
        DatabaseConnection.closeConnection();
        // Use a separate demo database file so tests do not conflict with the real app database.
        testDatabasePath = DEMO_DATABASE_PATH;
        Files.deleteIfExists(testDatabasePath);
        System.setProperty(DB_PATH_PROPERTY, testDatabasePath.toString());
        DatabaseManager.initializeDatabase();
    }

    @AfterEach
    void tearDown() throws IOException {
        DatabaseConnection.closeConnection();
        System.clearProperty(DB_PATH_PROPERTY);
        // Keeps the demo database on disk so its tables and rows can be inspected visually.
    }

    @Test
        // first test checks that when the application initializes the database,
        // the required tables are actually created. That verifies the schema setup for users,
        // game_sessions, round_logs, and round_actions.
    void initializeDatabaseCreatesAllRequiredTables() throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance();
             Statement statement = connection.createStatement()) {
            // This verifies the persistence layer creates the schema the application depends on.
            ResultSet resultSet = statement.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type = 'table' AND name IN "
                            + "('users', 'user_preferences', 'game_sessions', 'round_logs', 'round_actions')"
            );

            int tableCount = 0;
            while (resultSet.next()) {
                tableCount++;
            }

            assertEquals(5, tableCount);
        }
    }

    @Test
        //second test checks that when a user is inserted into the database, that user can be loaded back correctly.
        // This proves registration data is being persisted.
    void insertAndLoadUserPersistsRegisteredUserData() {
        UserDAO userDAO = new UserDAO();
        User user = new User(DEMO_USERNAME, "hashed-password", DEMO_EMAIL);

        // Saving and reloading a user proves registration data is written to the database correctly.
        userDAO.insert(user);
        User loadedUser = userDAO.getByUsername(DEMO_USERNAME);

        assertNotNull(loadedUser);
        assertTrue(loadedUser.getId() > 0);
        assertEquals(DEMO_USERNAME, loadedUser.getUsername());
        assertEquals(DEMO_EMAIL, loadedUser.getEmail());
        assertEquals(1000, loadedUser.getCurrentBalance());
    }

    @Test
    //third test checks that when a user’s balance and statistics are updated, the database stores the new values correctly.
        // This verifies update behaviour for returning users.
    void updateUserPersistsBalanceAndStatsChanges() {
        UserDAO userDAO = new UserDAO();
        User user = new User(DEMO_USERNAME, "hashed-password", DEMO_EMAIL);
        userDAO.insert(user);

        // This covers the expected behaviour when a returning player's profile is updated.
        user.setCurrentBalance(1450);
        user.setTotalHandsPlayed(8);
        user.setTotalWins(3);
        userDAO.update(user);

        User loadedUser = userDAO.getByUsername(DEMO_USERNAME);

        assertNotNull(loadedUser);
        assertEquals(1450, loadedUser.getCurrentBalance());
        assertEquals(8, loadedUser.getTotalHandsPlayed());
        assertEquals(3, loadedUser.getTotalWins());
    }

    @Test
    void saveAndLoadUserPreferencesPersistsOptions() {
        UserDAO userDAO = new UserDAO();
        User user = new User(DEMO_USERNAME, "hashed-password", DEMO_EMAIL);
        userDAO.insert(user);

        UserPreferencesDAO preferencesDAO = new UserPreferencesDAO();
        GamePreferences preferences = new GamePreferences(3, Difficulty.Professional, "back4", "classic3");

        preferencesDAO.saveForUserId(user.getId(), preferences);
        GamePreferences loadedPreferences = preferencesDAO.getByUserId(user.getId());

        assertEquals(3, loadedPreferences.getOpponentCount());
        assertEquals(Difficulty.Professional, loadedPreferences.getDifficulty());
        assertEquals("back4", loadedPreferences.getCardBackKey());
        assertEquals("classic3", loadedPreferences.getBoardKey());
    }

    @Test
    void initializeDatabaseCreatesDefaultPreferencesForExistingUsers() {
        UserDAO userDAO = new UserDAO();
        User user = new User(DEMO_USERNAME, "hashed-password", DEMO_EMAIL);
        userDAO.insert(user);

        DatabaseManager.initializeDatabase();
        GamePreferences loadedPreferences = new UserPreferencesDAO().getByUserId(user.getId());

        assertEquals(GamePreferences.DEFAULT_OPPONENTS, loadedPreferences.getOpponentCount());
        assertEquals(GamePreferences.DEFAULT_DIFFICULTY, loadedPreferences.getDifficulty());
        assertEquals(GamePreferences.DEFAULT_CARD_BACK_KEY, loadedPreferences.getCardBackKey());
        assertEquals(GamePreferences.DEFAULT_BOARD_KEY, loadedPreferences.getBoardKey());
    }

    @Test
    // forth test checks that a completed round writes both a round log row and action rows to the database.
    void recordRoundPersistsRoundLogsAndActions() throws SQLException {
        UserDAO userDAO = new UserDAO();
        User user = new User(DEMO_USERNAME, "hashed-password", DEMO_EMAIL);
        userDAO.insert(user);

        TestPlayer userPlayer = new TestPlayer(user.getUsername(), 1000);
        TestPlayer aiOne = new TestPlayer("ai_one", 1000);
        TestPlayer aiTwo = new TestPlayer("ai_two", 1000);
        ArrayList<Player> players = new ArrayList<>();
        players.add(userPlayer);
        players.add(aiOne);
        players.add(aiTwo);
        RoleUtil.delegateRoles(players, new int[]{0, 1, 2});

        Game game = new Game(userPlayer, user, 1000, 3, 50, 5, 10, Difficulty.Baby);
        int gameSessionId = DatabaseManager.createGameSession(user, game, userPlayer);

        Round round = new Round(players, 50, gameSessionId, 1);
        round.init();
        round.setCommunityCards(new ArrayList<>(java.util.List.of(Card.CA, Card.DK, Card.HQ)));
        round.setRoundStatus(com.example.projectpoker.model.game.enums.RoundStatus.FLOP);

        userPlayer.setAction(Action.CHECK);
        round.recordPlayerAction(userPlayer);
        aiOne.setAction(Action.FOLD);
        round.recordPlayerAction(aiOne);

        DatabaseManager.recordRound(gameSessionId, round);

        try (Connection connection = DatabaseConnection.getInstance();
             Statement statement = connection.createStatement()) {
            try (ResultSet roundLogResult = statement.executeQuery(
                    "SELECT game_session_id, round_number, round_status, community_cards FROM round_logs WHERE game_session_id = " + gameSessionId
            )) {
                assertTrue(roundLogResult.next());
                assertEquals(gameSessionId, roundLogResult.getInt("game_session_id"));
                assertEquals(1, roundLogResult.getInt("round_number"));
                assertEquals("FLOP", roundLogResult.getString("round_status"));
                assertEquals("Ace of Clubs,King of Diamonds,Queen of Hearts", roundLogResult.getString("community_cards"));
            }

            try (ResultSet actionCountResult = statement.executeQuery(
                    "SELECT COUNT(*) AS action_count FROM round_actions"
            )) {
                assertTrue(actionCountResult.next());
                assertEquals(2, actionCountResult.getInt("action_count"));
            }
        }
    }

    private static class TestPlayer extends Player {
        TestPlayer(String name, int balance) {
            super(name, balance);
        }

        // exposes the protected balance setter for database persistence tests only.
        void forceBalance(int balance) {
            setBalance(balance);
        }
    }
}
