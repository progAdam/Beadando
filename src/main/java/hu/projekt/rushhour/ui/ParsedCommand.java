package hu.projekt.rushhour.ui;

import hu.projekt.rushhour.model.Move;


public sealed interface ParsedCommand {

    record MoveCommand(Move move) implements ParsedCommand {
    }

    record LoadBoardCommand(String filePath) implements ParsedCommand {
    }

    record SaveBoardCommand(String filePath) implements ParsedCommand {
    }

    record SaveXmlCommand(String filePath) implements ParsedCommand {
    }

    record LoadXmlCommand(String filePath) implements ParsedCommand {
    }

    record HighScoreCommand() implements ParsedCommand {
    }

    record HelpCommand() implements ParsedCommand {
    }

    record QuitCommand() implements ParsedCommand {
    }

    record InvalidCommand(String reason) implements ParsedCommand {
    }
}
