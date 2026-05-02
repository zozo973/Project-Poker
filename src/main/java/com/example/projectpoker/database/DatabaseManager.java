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

    // Creates every database table the app needs before any reads or writes run.
    public static void initializeDatabase() {
        UserDAO userDAO = new UserDAO();
        userDAO.createTable();
        new GameSessionDAO().createTable();
        new RoundLogDAO().createTables();
    }

    // Loads an existing user, or creates one with the starting balance if none exists.
    public static User getOrCreateUser(String username, String password, int defaultBalance) {
        initializeDatabase();
        UserDAO userDAO = new UserDAO();
        return userDAO.getOrCreate(username, password, defaultBalance);
    }

    // Starts a database record for a poker game and returns its session id.
    public static int createGameSession(User user, Game game, Player player) {
        if (user == null) {
            return -1;
        }
        initializeDatabase();
        return new GameSessionDAO().insert(user, game, player);
    }

    // Saves one completed round and links it to the current game session.
    public static void recordRound(int gameSessionId, Round round) {
        if (gameSessionId <= 0) {
            return;
        }
        initializeDatabase();
        new RoundLogDAO().insertRound(round, gameSessionId);
    }

    // Updates the user's saved balance while a game is still in progress.
    public static void saveUserProgress(User user, Player player) {
        if (user == null || player == null) {
            return;
        }

        initializeDatabase();
        // Save the current chip count before the full game ends so partial progress is not lost.
        user.setCurrentBalance(player.getBalance());
        new UserDAO().update(user);
    }

    // Writes the final game result, including balance, hands played, wins, and session status.
    public static void finalizeGameSession(int gameSessionId, User user, Game game, Player player) {
        if (user == null) {
            return;
        }

        initializeDatabase();

        // Keep stored stats valid even if a game ends before a round is counted.
        int handsPlayed = Math.max(game.getHandsPlayed(), 0);
        boolean wonGame = player.getBalance() > game.getStartingUserBalance();
        user.setCurrentBalance(player.getBalance());
        user.setTotalHandsPlayed(user.getTotalHandsPlayed() + handsPlayed);
        if (wonGame) {
            user.setTotalWins(user.getTotalWins() + 1);
        }

        UserDAO userDAO = new UserDAO();
        userDAO.update(user);

        if (gameSessionId > 0) {
            new GameSessionDAO().finish(gameSessionId, player.getBalance(), game.getGameStatus().name());
        }
    }

    // Returns every user profile stored in the database.
    public static List<User> getAllUsers() {
        initializeDatabase();
        return new UserDAO().getAll();
    }
}
