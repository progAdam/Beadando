package hu.projekt.rushhour.service;

import hu.projekt.rushhour.io.BoardFileManager;
import hu.projekt.rushhour.io.XmlStateManager;
import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.Move;
import hu.projekt.rushhour.model.MoveOutcome;
import hu.projekt.rushhour.model.PlayerTurn;
import hu.projekt.rushhour.model.SavedGame;
import hu.projekt.rushhour.persistence.HighScoreDao;
import hu.projekt.rushhour.ui.CommandParser;
import hu.projekt.rushhour.ui.ConsoleRenderer;
import hu.projekt.rushhour.ui.ParsedCommand;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game {

    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);

    private final GameEngine gameEngine;
    private final RandomAiPlayer aiPlayer;
    private final BoardFileManager boardFileManager;
    private final XmlStateManager xmlStateManager;
    private final HighScoreDao highScoreDao;
    private final ConsoleRenderer renderer;
    private final CommandParser commandParser;
    private final BufferedReader inputReader;
    private final Path initialBoardPath;

    public Game(
            GameEngine gameEngine,
            RandomAiPlayer aiPlayer,
            BoardFileManager boardFileManager,
            XmlStateManager xmlStateManager,
            HighScoreDao highScoreDao,
            ConsoleRenderer renderer,
            CommandParser commandParser,
            BufferedReader inputReader,
            Path initialBoardPath
    ) {
        this.gameEngine = Objects.requireNonNull(gameEngine);
        this.aiPlayer = Objects.requireNonNull(aiPlayer);
        this.boardFileManager = Objects.requireNonNull(boardFileManager);
        this.xmlStateManager = Objects.requireNonNull(xmlStateManager);
        this.highScoreDao = Objects.requireNonNull(highScoreDao);
        this.renderer = Objects.requireNonNull(renderer);
        this.commandParser = Objects.requireNonNull(commandParser);
        this.inputReader = Objects.requireNonNull(inputReader);
        this.initialBoardPath = initialBoardPath;
    }

    public void start() {
        renderer.printMessage("=== RUSH HOUR NxM (2 Játékos) ===");
        renderer.printMessage("Add meg a játékosneved: ");
        String playerName = readPlayerName();
        LOGGER.info("Starting game session for player '{}'", playerName);

        BoardState boardState = boardFileManager.loadBoardOrDefault(initialBoardPath);
        PlayerTurn currentTurn = PlayerTurn.HUMAN;
        int turnCount = 1;

        renderer.renderHelp();

        while (true) {
            renderer.renderBoard(boardState);

            if (currentTurn == PlayerTurn.HUMAN) {
                renderer.printMessage(String.format("[%d. kör - %s (A)] Lépés vagy parancs > ", turnCount, playerName));
                String line = readLineSafe();
                if (line == null) {
                    renderer.printMessage("Bemenet vége, kilépés...");
                    break;
                }

                ParsedCommand command = commandParser.parse(line);
                if (command instanceof ParsedCommand.QuitCommand) {
                    renderer.printMessage("Viszlát, " + playerName + "!");
                    break;
                } else if (command instanceof ParsedCommand.HelpCommand) {
                    renderer.renderHelp();
                } else if (command instanceof ParsedCommand.HighScoreCommand) {
                    renderer.renderHighScores(highScoreDao.getTopScores());
                } else if (command instanceof ParsedCommand.LoadBoardCommand loadCmd) {
                    BoardState loaded = loadBoardSafely(loadCmd.filePath());
                    if (loaded != null) {
                        boardState = loaded;
                        currentTurn = PlayerTurn.HUMAN;
                        turnCount = 1;
                        renderer.printMessage("Pálya sikeresen betöltve: " + loadCmd.filePath());
                    }
                } else if (command instanceof ParsedCommand.SaveBoardCommand saveCmd) {
                    try {
                        boardFileManager.saveBoardToFile(boardState, Path.of(saveCmd.filePath()));
                        renderer.printMessage("Pálya sikeresen elmentve: " + saveCmd.filePath());
                    } catch (IOException e) {
                        renderer.printMessage("Hiba a pálya mentésekor: " + e.getMessage());
                    }
                } else if (command instanceof ParsedCommand.SaveXmlCommand saveXmlCmd) {
                    try {
                        SavedGame snapshot = new SavedGame(playerName, currentTurn, turnCount, boardState);
                        xmlStateManager.saveGameToXml(snapshot, Path.of(saveXmlCmd.filePath()));
                        renderer.printMessage("Játékállás sikeresen mentve XML-be: " + saveXmlCmd.filePath());
                    } catch (IOException e) {
                        renderer.printMessage("Hiba az XML mentésekor: " + e.getMessage());
                    }
                } else if (command instanceof ParsedCommand.LoadXmlCommand loadXmlCmd) {
                    try {
                        SavedGame restored = xmlStateManager.loadGameFromXml(Path.of(loadXmlCmd.filePath()));
                        playerName = restored.playerName();
                        currentTurn = restored.currentTurn();
                        turnCount = restored.turnCount();
                        boardState = restored.boardState();
                        renderer.printMessage("Játékállás sikeresen visszatöltve XML-ből: " + loadXmlCmd.filePath());
                    } catch (NoSuchFileException e) {
                        renderer.printMessage("Hiba az XML visszatöltésekor: a fájl nem található ("
                                + loadXmlCmd.filePath() + ")");
                    } catch (IOException | IllegalArgumentException e) {
                        renderer.printMessage("Hiba az XML visszatöltésekor: " + e.getMessage());
                    }
                } else if (command instanceof ParsedCommand.InvalidCommand invalidCmd) {
                    renderer.printMessage("Hiba: " + invalidCmd.reason());
                } else if (command instanceof ParsedCommand.MoveCommand moveCmd) {
                    try {
                        MoveOutcome outcome = gameEngine.applyMove(boardState, moveCmd.move(), PlayerTurn.HUMAN);
                        boardState = outcome.nextState();
                        if (outcome.winner().isPresent()) {
                            handleVictory(outcome.winner().get(), playerName, boardState);
                            break;
                        }
                        currentTurn = PlayerTurn.AI;
                    } catch (IllegalArgumentException e) {
                        renderer.printMessage("Érvénytelen lépés! Az út akadályozott, rossz az irány, "
                                + "vagy nem engedélyezett kijáratot használnál.");
                    }
                }
            } else {
                renderer.printMessage(String.format("[%d. kör - Gépi játékos (B)] Gondolkodik...", turnCount));
                Optional<Move> aiMoveOpt = chooseLegalAiMove(boardState);
                if (aiMoveOpt.isEmpty()) {
                    renderer.printMessage("A gépi játékosnak nincs lehetséges lépése! Döntetlen.");
                    break;
                }
                Move aiMove = aiMoveOpt.get();
                renderer.printMessage(String.format("Gépi játékos lépése: %s %s %d",
                        aiMove.vehicleId(), aiMove.direction(), aiMove.steps()));
                MoveOutcome outcome = gameEngine.applyMove(boardState, aiMove, PlayerTurn.AI);
                boardState = outcome.nextState();
                if (outcome.winner().isPresent()) {
                    handleVictory(outcome.winner().get(), playerName, boardState);
                    break;
                }
                currentTurn = PlayerTurn.HUMAN;
                turnCount++;
            }
        }
    }

    private Optional<Move> chooseLegalAiMove(BoardState boardState) {
        Optional<Move> chosen = aiPlayer.chooseMove(boardState);
        if (chosen.isEmpty()) {
            return Optional.empty();
        }
        List<Move> legalAiMoves = gameEngine.getAllValidMoves(boardState, PlayerTurn.AI);
        if (legalAiMoves.contains(chosen.get())) {
            return chosen;
        }
        return legalAiMoves.isEmpty() ? Optional.empty() : Optional.of(legalAiMoves.getFirst());
    }

    private BoardState loadBoardSafely(String filePath) {
        try {
            return boardFileManager.loadBoardFromFile(Path.of(filePath));
        } catch (NoSuchFileException e) {
            renderer.printMessage("Hiba a pálya betöltésekor: a fájl nem található (" + filePath + ")");
        } catch (IOException | IllegalArgumentException e) {
            renderer.printMessage("Hiba a pálya betöltésekor: " + e.getMessage());
        }
        return null;
    }

    private void handleVictory(PlayerTurn winner, String playerName, BoardState finalState) {
        renderer.renderBoard(finalState);
        if (winner == PlayerTurn.HUMAN) {
            renderer.printMessage("GRATULÁLOK, " + playerName + "! Kivezetted az 'A' céljárművet, NYERTÉL!");
            highScoreDao.recordWin(playerName);
        } else {
            renderer.printMessage("A gépi játékos kivezette a 'B' céljárművet! A gép nyert.");
            highScoreDao.recordWin("AI");
        }
        renderer.renderHighScores(highScoreDao.getTopScores());
    }

    private String readPlayerName() {
        String line = readLineSafe();
        if (line == null || line.isBlank()) {
            return "Játékos";
        }
        return line.trim();
    }

    private String readLineSafe() {
        try {
            return inputReader.readLine();
        } catch (IOException e) {
            LOGGER.error("Error reading from input stream", e);
            return null;
        }
    }
}