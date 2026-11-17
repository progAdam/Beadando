package hu.projekt.rushhour.ui;

import hu.projekt.rushhour.model.Direction;
import hu.projekt.rushhour.model.Move;
import java.util.Locale;
import java.util.Optional;


public class CommandParser {


    public ParsedCommand parse(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return new ParsedCommand.InvalidCommand("Üres parancs. Írd be a 'HELP' szót a súgóhoz.");
        }
        String trimmed = rawInput.trim();
        String[] parts = trimmed.split("\\s+");
        String first = parts[0].toUpperCase(Locale.ROOT);

        return switch (first) {
            case "HELP", "?" -> new ParsedCommand.HelpCommand();
            case "QUIT", "EXIT" -> new ParsedCommand.QuitCommand();
            case "SCORES", "HIGHSCORE", "TOP" -> new ParsedCommand.HighScoreCommand();
            case "LOAD" -> parts.length >= 2
                    ? new ParsedCommand.LoadBoardCommand(parts[1])
                    : new ParsedCommand.InvalidCommand("Használat: LOAD <fajlnev.txt>");
            case "SAVE" -> parts.length >= 2
                    ? new ParsedCommand.SaveBoardCommand(parts[1])
                    : new ParsedCommand.InvalidCommand("Használat: SAVE <fajlnev.txt>");
            case "SAVE_XML" -> parts.length >= 2
                    ? new ParsedCommand.SaveXmlCommand(parts[1])
                    : new ParsedCommand.InvalidCommand("Használat: SAVE_XML <fajlnev.xml>");
            case "LOAD_XML" -> parts.length >= 2
                    ? new ParsedCommand.LoadXmlCommand(parts[1])
                    : new ParsedCommand.InvalidCommand("Használat: LOAD_XML <fajlnev.xml>");
            default -> parseMove(parts);
        };
    }

    private ParsedCommand parseMove(String[] parts) {
        if (parts.length != 3) {
            return new ParsedCommand.InvalidCommand(
                    "Ismeretlen parancs vagy hibás lépésformátum. Lépés: <JÁRMŰ> <IRÁNY> <LÉPÉSSZÁM> (pl. A RIGHT 2)"
            );
        }
        String vehicleId = parts[0].toUpperCase(Locale.ROOT);
        Optional<Direction> directionOpt = parseDirection(parts[1]);
        if (directionOpt.isEmpty()) {
            return new ParsedCommand.InvalidCommand(
                    "Érvénytelen irány: '" + parts[1] + "'. Használd: UP/FEL, DOWN/LE, LEFT/BALRA, RIGHT/JOBBRA"
            );
        }
        int steps;
        try {
            steps = Integer.parseInt(parts[2]);
            if (steps < 1) {
                return new ParsedCommand.InvalidCommand("A lépésszámnak legalább 1-nek kell lennie.");
            }
        } catch (NumberFormatException e) {
            return new ParsedCommand.InvalidCommand("Érvénytelen lépésszám: '" + parts[2] + "'");
        }
        return new ParsedCommand.MoveCommand(new Move(vehicleId, directionOpt.get(), steps));
    }

    private Optional<Direction> parseDirection(String token) {
        return switch (token.toUpperCase(Locale.ROOT)) {
            case "UP", "U", "FEL", "F" -> Optional.of(Direction.UP);
            case "DOWN", "D", "LE", "L" -> Optional.of(Direction.DOWN);
            case "LEFT", "BAL", "BALRA", "B" -> Optional.of(Direction.LEFT);
            case "RIGHT", "R", "JOBB", "JOBBRA", "J" -> Optional.of(Direction.RIGHT);
            default -> Optional.empty();
        };
    }
}
