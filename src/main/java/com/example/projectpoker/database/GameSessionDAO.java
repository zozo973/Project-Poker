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

    // Uses the shared SQLite connection for game session queries.
    /**
     * Creates a DAO instance backed by the shared SQLite connection.
     */
    public GameSessionDAO() {
        this.connection = DatabaseConnection.getInstance();
    }

    /**
     * Creates the {@code game_sessions} table if it does not already exist.
     * The table records configuration, status, and balance data for each poker game session.
     */
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

    /**
     * Inserts a new game session row and returns the generated session id.
     *
     * @param user   the logged-in {@link User} starting the session
     * @param game   the {@link Game} providing session configuration (difficulty, blind size, etc.)
     * @param player the human {@link Player} whose starting balance is recorded
     * @return the generated session id, or {@code -1} if the insert failed
     */
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

            // The id links later round logs and final results to this session.
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

    /**
     * Updates an existing session row to mark it as finished with the user's final balance.
     *
     * @param sessionId      the primary key of the session to finalise
     * @param endingBalance  the user's chip balance at the end of the game
     * @param status         the final status string (e.g. {@code "ENDED"})
     */
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
