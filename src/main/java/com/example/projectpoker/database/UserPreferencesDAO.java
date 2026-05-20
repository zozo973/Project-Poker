package com.example.projectpoker.database;

import com.example.projectpoker.model.GamePreferences;
import com.example.projectpoker.model.game.enums.Difficulty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserPreferencesDAO {
    private final Connection connection;

    public UserPreferencesDAO() {
        connection = DatabaseConnection.getInstance();
    }

    public void createTable() {
        try (Statement createTable = connection.createStatement()) {
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS user_preferences ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "user_id INTEGER NOT NULL UNIQUE, "
                            + "opponent_count INTEGER NOT NULL DEFAULT " + GamePreferences.DEFAULT_OPPONENTS + ", "
                            + "difficulty VARCHAR NOT NULL DEFAULT '" + GamePreferences.DEFAULT_DIFFICULTY.name() + "', "
                            + "card_back_key VARCHAR NOT NULL DEFAULT '" + GamePreferences.DEFAULT_CARD_BACK_KEY + "', "
                            + "board_key VARCHAR NOT NULL DEFAULT '" + GamePreferences.DEFAULT_BOARD_KEY + "', "
                            + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                            + ")"
            );
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    public void createDefaultRowsForMissingUsers() {
        try (PreparedStatement insertDefaults = connection.prepareStatement(
                "INSERT INTO user_preferences (user_id, opponent_count, difficulty, card_back_key, board_key) "
                        + "SELECT id, ?, ?, ?, ? FROM users "
                        + "WHERE NOT EXISTS ("
                        + "SELECT 1 FROM user_preferences WHERE user_preferences.user_id = users.id"
                        + ")"
        )) {
            insertDefaults.setInt(1, GamePreferences.DEFAULT_OPPONENTS);
            insertDefaults.setString(2, GamePreferences.DEFAULT_DIFFICULTY.name());
            insertDefaults.setString(3, GamePreferences.DEFAULT_CARD_BACK_KEY);
            insertDefaults.setString(4, GamePreferences.DEFAULT_BOARD_KEY);
            insertDefaults.execute();
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    public GamePreferences getByUserId(int userId) {
        try (PreparedStatement getPreferences = connection.prepareStatement(
                "SELECT opponent_count, difficulty, card_back_key, board_key FROM user_preferences WHERE user_id = ?"
        )) {
            getPreferences.setInt(1, userId);
            ResultSet rs = getPreferences.executeQuery();
            if (rs.next()) {
                return new GamePreferences(
                        rs.getInt("opponent_count"),
                        difficultyFromName(rs.getString("difficulty")),
                        rs.getString("card_back_key"),
                        rs.getString("board_key")
                );
            }
        } catch (SQLException ex) {
            System.err.println(ex);
        }
        return GamePreferences.defaults();
    }

    public void saveForUserId(int userId, GamePreferences preferences) {
        if (preferences == null || userId <= 0) {
            return;
        }

        try (PreparedStatement savePreferences = connection.prepareStatement(
                "INSERT INTO user_preferences (user_id, opponent_count, difficulty, card_back_key, board_key) "
                        + "VALUES (?, ?, ?, ?, ?) "
                        + "ON CONFLICT(user_id) DO UPDATE SET "
                        + "opponent_count = excluded.opponent_count, "
                        + "difficulty = excluded.difficulty, "
                        + "card_back_key = excluded.card_back_key, "
                        + "board_key = excluded.board_key"
        )) {
            savePreferences.setInt(1, userId);
            savePreferences.setInt(2, preferences.getOpponentCount());
            savePreferences.setString(3, preferences.getDifficulty().name());
            savePreferences.setString(4, preferences.getCardBackKey());
            savePreferences.setString(5, preferences.getBoardKey());
            savePreferences.execute();
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    private Difficulty difficultyFromName(String difficultyName) {
        try {
            return Difficulty.valueOf(difficultyName);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return GamePreferences.DEFAULT_DIFFICULTY;
        }
    }
}
