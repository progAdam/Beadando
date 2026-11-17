package hu.projekt.rushhour;

import hu.projekt.rushhour.io.BoardFileManager;
import hu.projekt.rushhour.io.XmlStateManager;
import hu.projekt.rushhour.persistence.HighScoreDao;
import hu.projekt.rushhour.service.Game;
import hu.projekt.rushhour.service.GameEngine;
import hu.projekt.rushhour.service.MoveValidator;
import hu.projekt.rushhour.service.RandomAiPlayer;
import hu.projekt.rushhour.ui.CommandParser;
import hu.projekt.rushhour.ui.ConsoleRenderer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Random;


public final class Main {

    private Main() {
    }


    public static void main(String[] args) {
        Path initialBoardPath = (args != null && args.length > 0 && !args[0].isBlank())
                ? Path.of(args[0])
                : Path.of("board.txt");

        MoveValidator moveValidator = new MoveValidator();
        GameEngine gameEngine = new GameEngine(moveValidator);
        RandomAiPlayer aiPlayer = new RandomAiPlayer(gameEngine, new Random());
        BoardFileManager boardFileManager = new BoardFileManager();
        XmlStateManager xmlStateManager = new XmlStateManager();
        HighScoreDao highScoreDao = new HighScoreDao();
        ConsoleRenderer renderer = new ConsoleRenderer(System.out);
        CommandParser commandParser = new CommandParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        Game game = new Game(
                gameEngine,
                aiPlayer,
                boardFileManager,
                xmlStateManager,
                highScoreDao,
                renderer,
                commandParser,
                reader,
                initialBoardPath
        );
        game.start();
    }
}
