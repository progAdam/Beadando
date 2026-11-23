package hu.projekt.rushhour.io;

import hu.projekt.rushhour.model.BoardState;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BoardFileManagerTest {

    private final BoardFileManager fileManager = new BoardFileManager();

    @Test
    void loadBoardFromFileShouldThrowIOExceptionWhenFileContainsInvalidNumber(@TempDir Path tempDir)
            throws IOException {
        Path badFile = tempDir.resolve("bad.txt");
        Files.writeString(badFile, "ROWS x\nCOLS 12\n", StandardCharsets.UTF_8);

        Assertions.assertThrows(IOException.class, () -> fileManager.loadBoardFromFile(badFile));
    }

    @Test
    void loadBoardFromFileShouldThrowNoSuchFileExceptionWhenFileDoesNotExist(@TempDir Path tempDir) {
        Path missingFile = tempDir.resolve("does-not-exist.txt");

        Assertions.assertThrows(NoSuchFileException.class, () -> fileManager.loadBoardFromFile(missingFile));
    }

    @Test
    void loadBoardOrDefaultShouldFallbackWhenFileIsCorrupt(@TempDir Path tempDir) throws IOException {
        Path badFile = tempDir.resolve("bad.txt");
        Files.writeString(badFile, "ROWS x\nCOLS 12\n", StandardCharsets.UTF_8);

        BoardState state = fileManager.loadBoardOrDefault(badFile);
        Assertions.assertNotNull(state);
        Assertions.assertFalse(state.vehicles().isEmpty());
    }

    @Test
    void saveAndLoadBoardRoundTripShouldPreserveBoardState(@TempDir Path tempDir) throws IOException {
        BoardState original = fileManager.loadDefaultBoard();
        Path savedFile = tempDir.resolve("saved-board.txt");

        fileManager.saveBoardToFile(original, savedFile);
        BoardState reloaded = fileManager.loadBoardFromFile(savedFile);

        Assertions.assertEquals(original.rows(), reloaded.rows());
        Assertions.assertEquals(original.cols(), reloaded.cols());
        Assertions.assertEquals(original.vehicles().size(), reloaded.vehicles().size());
    }
}