package hu.projekt.rushhour.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hu.projekt.rushhour.io.BoardFileManager;
import hu.projekt.rushhour.io.XmlStateManager;
import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.Direction;
import hu.projekt.rushhour.model.ExitPoint;
import hu.projekt.rushhour.model.Orientation;
import hu.projekt.rushhour.model.Position;
import hu.projekt.rushhour.model.Vehicle;
import hu.projekt.rushhour.model.VehicleRole;
import hu.projekt.rushhour.persistence.HighScoreDao;
import hu.projekt.rushhour.ui.CommandParser;
import hu.projekt.rushhour.ui.ConsoleRenderer;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameLoopTest {

    @TempDir
    Path tempDir;

    @Test
    void testFullHumanVictoryFlowAndAllCliCommands() throws IOException {
        BoardFileManager boardManager = new BoardFileManager();
        BoardState nearWinBoard = new BoardState(
                6,
                6,
                List.of(
                        new Vehicle("A", new Position(2, 3), 2, Orientation.HORIZONTAL, VehicleRole.HUMAN),
                        new Vehicle("B", new Position(3, 3), 2, Orientation.HORIZONTAL, VehicleRole.AI)
                ),
                new ExitPoint(new Position(2, 5), Direction.RIGHT),
                new ExitPoint(new Position(3, 0), Direction.LEFT)
        );
        Path boardPath = tempDir.resolve("custom-board.txt");
        Path savedBoardPath = tempDir.resolve("saved-copy.txt");
        boardManager.saveBoardToFile(nearWinBoard, boardPath);
        Path xmlPath = tempDir.resolve("save.xml");

        String script = String.join("\n",
                "Kovacs Janos",
                "HELP",
                "SCORES",
                "INVALID_CMD",
                "A UP 1",
                "SAVE " + savedBoardPath,
                "LOAD " + savedBoardPath,
                "LOAD nonexistent-file.txt",
                "SAVE_XML " + xmlPath,
                "LOAD_XML " + xmlPath,
                "LOAD_XML nonexistent.xml",
                "A RIGHT 2"
        ) + "\n";

        String dbUrl = "jdbc:h2:mem:game_loop_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1";
        HighScoreDao dao = new HighScoreDao(dbUrl, "sa", "");
        GameEngine engine = new GameEngine(new MoveValidator());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Game game = new Game(
                engine,
                new RandomAiPlayer(engine, new Random(1)),
                boardManager,
                new XmlStateManager(),
                dao,
                new ConsoleRenderer(new PrintStream(baos, true, StandardCharsets.UTF_8)),
                new CommandParser(),
                new BufferedReader(new StringReader(script)),
                boardPath
        );

        game.start();

        String output = baos.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("GRATULÁLOK, Kovacs Janos!"));
        assertEquals(1, dao.getTopScores().size());
        assertEquals("Kovacs Janos", dao.getTopScores().get(0).playerName());
        assertEquals(1, dao.getTopScores().get(0).wins());
    }

    @Test
    void testAiTurnAndAiVictory() throws IOException {
        BoardFileManager boardManager = new BoardFileManager();
        // Vehicle A cannot move at all; Vehicle B can only slide left to win
        BoardState aiWinBoard = new BoardState(
                6,
                6,
                List.of(
                        new Vehicle("A", new Position(0, 0), 2, Orientation.HORIZONTAL, VehicleRole.HUMAN),
                        new Vehicle("C", new Position(0, 2), 2, Orientation.VERTICAL, VehicleRole.NEUTRAL),
                        new Vehicle("B", new Position(3, 1), 2, Orientation.HORIZONTAL, VehicleRole.AI)
                ),
                new ExitPoint(new Position(2, 5), Direction.RIGHT),
                new ExitPoint(new Position(3, 0), Direction.LEFT)
        );
        Path boardPath = tempDir.resolve("ai-win-board.txt");
        boardManager.saveBoardToFile(aiWinBoard, boardPath);

        String script = String.join("\n",
                "",
                "B RIGHT 1",
                "QUIT"
        ) + "\n";

        String dbUrl = "jdbc:h2:mem:ai_win_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1";
        HighScoreDao dao = new HighScoreDao(dbUrl, "sa", "");
        GameEngine engine = new GameEngine(new MoveValidator());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Game game = new Game(
                engine,
                new RandomAiPlayer(engine, new Random(1)),
                boardManager,
                new XmlStateManager(),
                dao,
                new ConsoleRenderer(new PrintStream(baos, true, StandardCharsets.UTF_8)),
                new CommandParser(),
                new BufferedReader(new StringReader(script)),
                boardPath
        );

        game.start();
    }
}
