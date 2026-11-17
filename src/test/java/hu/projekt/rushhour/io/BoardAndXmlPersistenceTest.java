package hu.projekt.rushhour.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.PlayerTurn;
import hu.projekt.rushhour.model.SavedGame;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BoardAndXmlPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void testLoadDefaultAndRoundTripTextBoard() throws IOException {
        BoardFileManager manager = new BoardFileManager();
        BoardState defaultBoard = manager.loadBoardOrDefault(tempDir.resolve("nonexistent.txt"));
        assertEquals(6, defaultBoard.rows());
        assertEquals(12, defaultBoard.cols());

        Path savedPath = tempDir.resolve("saved-board.txt");
        manager.saveBoardToFile(defaultBoard, savedPath);

        BoardState reloaded = manager.loadBoardFromFile(savedPath);
        assertEquals(defaultBoard, reloaded);
    }

    @Test
    void testInvalidBoardFileFallsBackToDefault() throws IOException {
        Path badFile = tempDir.resolve("broken.txt");
        Files.writeString(badFile, "INVALID_LINE 123");

        BoardFileManager manager = new BoardFileManager();
        BoardState fallback = manager.loadBoardOrDefault(badFile);
        assertEquals(6, fallback.rows());
        assertEquals(12, fallback.cols());

        assertThrows(IOException.class, () -> manager.loadBoardFromFile(badFile));
    }

    @Test
    void testXmlGameSaveAndRestoreRoundTrip() throws IOException {
        BoardFileManager boardManager = new BoardFileManager();
        BoardState board = boardManager.loadDefaultBoard();
        SavedGame original = new SavedGame("TesztElek", PlayerTurn.HUMAN, 5, board);

        XmlStateManager xmlManager = new XmlStateManager();
        Path xmlPath = tempDir.resolve("game-state.xml");
        xmlManager.saveGameToXml(original, xmlPath);

        SavedGame restored = xmlManager.loadGameFromXml(xmlPath);
        assertEquals(original, restored);
    }
}
