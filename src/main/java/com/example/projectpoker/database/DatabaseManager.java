package com.example.projectpoker.database;

import com.example.projectpoker.model.User;
import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.Round;

import java.util.List;

public class DatabaseManager {

    // Utility class, so it should not be created as an object.
    private DatabaseManager() {
    }

    /**
     * Creates every database table required by the application if they do not already exist.
     * Should be called once before any database reads or writes.
     */
    public static void initializeDatabase() {
        UserDAO userDAO = new UserDAO();
        userDAO.createTable();
        UserPreferencesDAO userPreferencesDAO = new UserPreferencesDAO();
        userPreferencesDAO.createTable();
        userPreferencesDAO.createDefaultRowsForMissingUsers();
        new GameSessionDAO().createTable();
        new RoundLogDAO().createTables();
    }

    /**
     * Returns an existing user matching the given username, or inserts a new one with the
     * specified default balance if none is found.
     *
     * @param username       the username to look up
     * @param password       the password to assign if a new user is created
     * @param defaultBalance the starting chip balance for a newly created user
     * @return the existing or newly created {@link User}
     */
    public static User getOrCreateUser(String username, String password, int defaultBalance) {
        initializeDatabase();
        UserDAO userDAO = new UserDAO();
        return userDAO.getOrCreate(username, password, defaultBalance);
    }

    /**
     * Inserts a new game session record for the given user and returns its generated session id.
     * Returns {@code -1} if {@code user} is {@code null}.
     *
     * @param user   the logged-in {@link User} starting the game
     * @param game   the {@link Game} instance containing session configuration
     * @param player the human {@link Player} whose starting balance is recorded
     * @return the generated session id, or {@code -1} if the insert was skipped
     */
    public static int createGameSession(User user, Game game, Player player) {
        if (user == null) {
            return -1;
        }
        initializeDatabase();
        return new GameSessionDAO().insert(user, game, player);
    }

    /**
     * Saves a completed round's log to the database, linked to the given session.
     * Does nothing if {@code gameSessionId} is non-positive.
     *
     * @param gameSessionId the session id that the round belongs to
     * @param round         the completed {@link Round} whose data should be persisted
     */
    public static void recordRound(int gameSessionId, Round round) {
        if (gameSessionId <= 0) {
            return;
        }
        initializeDatabase();
        new RoundLogDAO().insertRound(round, gameSessionId);
    }

    /**
     * Updates the user's balance in the database mid-game so partial progress is not lost
     * if the application exits unexpectedly.
     *
     * @param user   the {@link User} whose balance should be updated; ignored if {@code null}
     * @param player the {@link Player} whose current balance will be saved to the user record
     */
    public static void saveUserProgress(User user, Player player) {
        if (user == null || player == null) {
            return;
        }

        initializeDatabase();
        // Save the current chip count before the full game ends so partial progress is not lost.
        user.setCurrentBalance(player.getBalance());
        new UserDAO().update(user);
    }

    /**
     * Finalises the game session in the database: updates the user's balance, increments wins if
     * the user's ending balance exceeds their starting balance, and marks the session as finished.
     * Does nothing if {@code user} is {@code null}.
     *
     * @param gameSessionId the session id to finalise; a non-positive id skips the session update
     * @param user          the {@link User} whose profile stats are updated
     * @param game          the {@link Game} holding the starting balance reference
     * @param player        the {@link Player} whose final balance is written back to the user record
     */
    public static void finalizeGameSession(int gameSessionId, User user, Game game, Player player) {
        if (user == null) {
            return;
        }

        initializeDatabase();

        boolean wonGame = player.getBalance() > game.getStartingUserBalance();
        user.setCurrentBalance(player.getBalance());
        if (wonGame) {
            user.setTotalWins(user.getTotalWins() + 1);
        }

        UserDAO userDAO = new UserDAO();
        userDAO.update(user);

        if (gameSessionId > 0) {
            new GameSessionDAO().finish(gameSessionId, player.getBalance(), game.getGameStatus().name());
        }
    }

    /**
     * Returns every user profile stored in the database.
     *
     * @return a list of all {@link User} objects, or an empty list if the table is empty
     */
    public static List<User> getAllUsers() {
        initializeDatabase();
        return new UserDAO().getAll();
    }
}
