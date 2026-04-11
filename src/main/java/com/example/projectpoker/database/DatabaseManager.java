package com.example.projectpoker.database;

import com.example.projectpoker.model.User;
import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;
import com.example.projectpoker.model.game.Round;

import java.util.List;

public class DatabaseManager {

    private DatabaseManager() {
    }

    public static void initializeDatabase() {
        UserDAO userDAO = new UserDAO();
        userDAO.createTable();
        new GameSessionDAO().createTable();
        new RoundLogDAO().createTables();
    }

    public static User getOrCreateUser(String username, String password, int defaultBalance) {
        initializeDatabase();
        UserDAO userDAO = new UserDAO();
        return userDAO.getOrCreate(username, password, defaultBalance);
    }

    public static int createGameSession(User user, Game game, Player player) {
        if (user == null) {
            return -1;
        }
        initializeDatabase();
        return new GameSessionDAO().insert(user, game, player);
    }

    public static void recordRound(int gameSessionId, Round round) {
        if (gameSessionId <= 0) {
            return;
        }
        initializeDatabase();
        new RoundLogDAO().insertRound(round, gameSessionId);
    }

    public static void finalizeGameSession(int gameSessionId, User user, Game game, Player player) {
        if (user == null) {
            return;
        }

        initializeDatabase();

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

    public static List<User> getAllUsers() {
        initializeDatabase();
        return new UserDAO().getAll();
    }
}
