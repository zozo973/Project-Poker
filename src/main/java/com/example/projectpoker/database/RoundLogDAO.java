package com.example.projectpoker.database;

import com.example.projectpoker.model.game.Round;
import com.example.projectpoker.model.game.RoundLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RoundLogDAO {
    private final Connection connection;

    public RoundLogDAO() {
        this.connection = DatabaseConnection.getInstance();
    }

    public void createTables() {
        createRoundTable();
        createActionTable();
    }

    private void createRoundTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS round_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    game_session_id INTEGER NOT NULL,
                    round_number INTEGER NOT NULL,
                    round_status TEXT NOT NULL,
                    bet_type TEXT NOT NULL,
                    pot_size INTEGER NOT NULL,
                    community_cards TEXT,
                    remaining_players TEXT,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (game_session_id) REFERENCES game_sessions(id)
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    private void createActionTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS round_actions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    round_log_id INTEGER NOT NULL,
                    action_order INTEGER NOT NULL,
                    player_name TEXT NOT NULL,
                    action TEXT NOT NULL,
                    to_call INTEGER NOT NULL,
                    bet_size INTEGER NOT NULL,
                    description TEXT NOT NULL,
                    FOREIGN KEY (round_log_id) REFERENCES round_logs(id)
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    public void insertRound(Round round, int gameSessionId) {
        String roundSql = """
                INSERT INTO round_logs (
                    game_session_id, round_number, round_status, bet_type, pot_size,
                    community_cards, remaining_players
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(roundSql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, gameSessionId);
            statement.setInt(2, round.getRoundNumber());
            statement.setString(3, round.getRoundStatus() == null ? "UNKNOWN" : round.getRoundStatus().name());
            statement.setString(4, round.getBetType().name());
            statement.setInt(5, round.getMainPot().getPotSize());
            statement.setString(6, round.getCommunityCardsAsString());
            statement.setString(7, round.getRemainingPlayersAsString());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    insertActions(keys.getInt(1), round);
                }
            }
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    private void insertActions(int roundLogId, Round round) throws SQLException {
        String actionSql = """
                INSERT INTO round_actions (
                    round_log_id, action_order, player_name, action, to_call, bet_size, description
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(actionSql)) {
            int actionOrder = 1;
            if (round.getRoundLog() == null) {
                return;
            }
            for (RoundLogEntry entry : round.getRoundLog()) {
                statement.setInt(1, roundLogId);
                statement.setInt(2, actionOrder++);
                statement.setString(3, entry.getPlayerName());
                statement.setString(4, entry.getAction().name());
                statement.setInt(5, entry.getToCall());
                statement.setInt(6, entry.getBetSize());
                statement.setString(7, entry.displayGameLogEntry());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
