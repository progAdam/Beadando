package hu.projekt.rushhour.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hu.projekt.rushhour.model.PlayerScore;
import java.util.List;
import org.junit.jupiter.api.Test;

class HighScoreDaoTest {

    @Test
    void testRecordWinsAndRetrieveOrderedHighScores() {
        String inMemoryUrl = "jdbc:h2:mem:test_scores_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1";
        HighScoreDao dao = new HighScoreDao(inMemoryUrl, "sa", "");

        dao.recordWin(null);
        dao.recordWin("   ");
        dao.recordWin("Anna");
        dao.recordWin("Bela");
        dao.recordWin("Anna");

        List<PlayerScore> scores = dao.getTopScores();
        assertEquals(2, scores.size());
        assertEquals(new PlayerScore("Anna", 2), scores.get(0));
        assertEquals(new PlayerScore("Bela", 1), scores.get(1));
    }
}
