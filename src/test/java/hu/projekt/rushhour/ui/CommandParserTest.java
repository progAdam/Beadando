package hu.projekt.rushhour.ui;

import hu.projekt.rushhour.model.Direction;
import hu.projekt.rushhour.model.Move;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CommandParserTest {

    private final CommandParser parser = new CommandParser();

    @ParameterizedTest
    @ValueSource(strings = {"HELP", "help", "?", "  ?  "})
    void parseShouldRecognizeHelpCommands(String input) {
        ParsedCommand result = parser.parse(input);
        Assertions.assertInstanceOf(ParsedCommand.HelpCommand.class, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"QUIT", "quit", "EXIT", "  exit "})
    void parseShouldRecognizeQuitCommands(String input) {
        ParsedCommand result = parser.parse(input);
        Assertions.assertInstanceOf(ParsedCommand.QuitCommand.class, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SCORES", "HIGHSCORE", "TOP", "scores"})
    void parseShouldRecognizeHighScoreCommands(String input) {
        ParsedCommand result = parser.parse(input);
        Assertions.assertInstanceOf(ParsedCommand.HighScoreCommand.class, result);
    }

    @ParameterizedTest
    @CsvSource({
            "LOAD board.txt, board.txt",
            "load custom/map.txt, custom/map.txt"
    })
    void parseShouldRecognizeLoadBoardCommands(String input, String expectedPath) {
        ParsedCommand result = parser.parse(input);
        ParsedCommand.LoadBoardCommand cmd =
                Assertions.assertInstanceOf(ParsedCommand.LoadBoardCommand.class, result);
        Assertions.assertEquals(expectedPath, cmd.filePath());
    }

    @ParameterizedTest
    @CsvSource({
            "SAVE out.txt, out.txt",
            "save dir/out.txt, dir/out.txt"
    })
    void parseShouldRecognizeSaveBoardCommands(String input, String expectedPath) {
        ParsedCommand result = parser.parse(input);
        ParsedCommand.SaveBoardCommand cmd =
                Assertions.assertInstanceOf(ParsedCommand.SaveBoardCommand.class, result);
        Assertions.assertEquals(expectedPath, cmd.filePath());
    }

    @ParameterizedTest
    @CsvSource({
            "SAVE_XML state.xml, state.xml",
            "save_xml dir/state.xml, dir/state.xml"
    })
    void parseShouldRecognizeSaveXmlCommands(String input, String expectedPath) {
        ParsedCommand result = parser.parse(input);
        ParsedCommand.SaveXmlCommand cmd =
                Assertions.assertInstanceOf(ParsedCommand.SaveXmlCommand.class, result);
        Assertions.assertEquals(expectedPath, cmd.filePath());
    }

    @ParameterizedTest
    @CsvSource({
            "LOAD_XML state.xml, state.xml",
            "load_xml dir/state.xml, dir/state.xml"
    })
    void parseShouldRecognizeLoadXmlCommands(String input, String expectedPath) {
        ParsedCommand result = parser.parse(input);
        ParsedCommand.LoadXmlCommand cmd =
                Assertions.assertInstanceOf(ParsedCommand.LoadXmlCommand.class, result);
        Assertions.assertEquals(expectedPath, cmd.filePath());
    }

    @ParameterizedTest
    @CsvSource({
            "A RIGHT 2, A, RIGHT, 2",
            "a jobbra 1, A, RIGHT, 1",
            "B LEFT 3, B, LEFT, 3",
            "b balra 1, B, LEFT, 1",
            "C UP 1, C, UP, 1",
            "c fel 2, C, UP, 2",
            "D DOWN 4, D, DOWN, 4",
            "d le 1, D, DOWN, 1",
            "e r 1, E, RIGHT, 1",
            "f b 1, F, LEFT, 1",
            "g u 1, G, UP, 1",
            "h l 1, H, DOWN, 1"
    })
    void parseShouldRecognizeValidMoveCommands(
            String input,
            String expectedVehicleId,
            Direction expectedDirection,
            int expectedSteps
    ) {
        ParsedCommand result = parser.parse(input);
        ParsedCommand.MoveCommand cmd =
                Assertions.assertInstanceOf(ParsedCommand.MoveCommand.class, result);
        Move move = cmd.move();
        Assertions.assertEquals(expectedVehicleId, move.vehicleId());
        Assertions.assertEquals(expectedDirection, move.direction());
        Assertions.assertEquals(expectedSteps, move.steps());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "UNKNOWN",
            "LOAD",
            "SAVE",
            "SAVE_XML",
            "LOAD_XML",
            "A RIGHT",
            "A RIGHT 1 EXTRA",
            "A DIAGONAL 1",
            "A RIGHT abc",
            "A RIGHT 0",
            "A RIGHT -2"
    })
    void parseShouldReturnInvalidCommandForBadInputs(String input) {
        ParsedCommand result = parser.parse(input);
        Assertions.assertInstanceOf(ParsedCommand.InvalidCommand.class, result);
    }
}
