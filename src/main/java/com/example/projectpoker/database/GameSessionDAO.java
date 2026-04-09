package com.example.projectpoker.database;

import com.example.projectpoker.model.User;
import com.example.projectpoker.model.game.Game;
import com.example.projectpoker.model.game.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GameSessionDAO {
    private final Connection connection;

    public GameSessionDAO() {
        this.connection = DatabaseConnection.getInstance();
    }

    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS game_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    difficulty TEXT NOT NULL,
                    player_count INTEGER NOT NULL,
                    user_buy_in INTEGER NOT NULL,
                    starting_blind INTEGER NOT NULL,
                    blind_increase_round INTEGER NOT NULL,
                    game_length INTEGER NOT NULL,
                    starting_balance INTEGER NOT NULL,
                    ending_balance INTEGER,
                    status TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    ended_at TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    public int insert(User user, Game game, Player player) {
        String sql = """
                INSERT INTO game_sessions (
                    user_id, difficulty, player_count, user_buy_in, starting_blind,
                    blind_increase_round, game_length, starting_balance, status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, user.getId());
            statement.setString(2, game.getDifficulty().name());
            statement.setInt(3, game.getNumPlayers());
            statement.setInt(4, game.getUserBuyIn());
            statement.setInt(5, game.getBlindSize());
            statement.setInt(6, game.getWhenIncreaseBlinds());
            statement.setInt(7, game.getGameLength());
            statement.setInt(8, player.getBalance());
            statement.setString(9, game.getGameStatus() == null ? "CREATED" : game.getGameStatus().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException ex) {
            System.err.println(ex);
        }
        return -1;
    }

    public void finish(int sessionId, int endingBalance, String status) {
        String sql = """
                UPDATE game_sessions
                SET ending_balance = ?, status = ?, ended_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, endingBalance);
            statement.setString(2, status);
            statement.setInt(3, sessionId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }
}
