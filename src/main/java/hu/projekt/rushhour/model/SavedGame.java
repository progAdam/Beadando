package hu.projekt.rushhour.model;

import java.util.Objects;


public record SavedGame(
        String playerName,
        PlayerTurn currentTurn,
        int turnCount,
        BoardState boardState
) {

    public SavedGame {
        Objects.requireNonNull(playerName, "Player name must not be null");
        Objects.requireNonNull(currentTurn, "Current turn must not be null");
        Objects.requireNonNull(boardState, "Board state must not be null");
    }
}
