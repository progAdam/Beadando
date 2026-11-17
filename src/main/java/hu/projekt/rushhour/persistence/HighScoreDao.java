package hu.projekt.rushhour.persistence;

import hu.projekt.rushhour.model.PlayerScore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HighScoreDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(HighScoreDao.class);
    private static final String DEFAULT_JDBC_URL = "jdbc:h2:file:./data/rushhour_scores;AUTO_SERVER=TRUE";

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public HighScoreDao() {
        this(DEFAULT_JDBC_URL, "sa", "");
    }

    public HighScoreDao(String jdbcUrl, String username, String password) {
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "JDBC URL must not be null");
        this.username = Objects.requireNonNull(username, "Username must not be null");
        this.password = Objects.requireNonNull(password, "Password must not be null");
        initializeSchema();
    }

    private void initializeSchema() {
        String ddl = """
                CREATE TABLE IF NOT EXISTS player_scores (
                    player_name VARCHAR(100) PRIMARY KEY,
                    wins INT NOT NULL
                )
                """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize player_scores table", e);
        }
    }


    public void recordWin(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return;
        }
        String normalized = playerName.trim();
        String mergeSql = """
                MERGE INTO player_scores (player_name, wins)
                KEY (player_name)
                VALUES (?, COALESCE((SELECT wins FROM player_scores WHERE player_name = ?), 0) + 1)
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(mergeSql)) {
            ps.setString(1, normalized);
            ps.setString(2, normalized);
            ps.executeUpdate();
            LOGGER.info("Recorded win for player '{}'", normalized);
        } catch (SQLException e) {
            LOGGER.error("Failed to record win for player '{}'", normalized, e);
        }
    }


    public List<PlayerScore> getTopScores() {
        String query = "SELECT player_name, wins FROM player_scores ORDER BY wins DESC, player_name ASC";
        List<PlayerScore> scores = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                scores.add(new PlayerScore(rs.getString("player_name"), rs.getInt("wins")));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to query high scores", e);
        }
        return List.copyOf(scores);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
