package hu.projekt.rushhour.model;

import java.util.Objects;
import java.util.Optional;


public record MoveOutcome(BoardState nextState, Optional<PlayerTurn> winner) {

    public MoveOutcome {
        Objects.requireNonNull(nextState, "Next state must not be null");
        Objects.requireNonNull(winner, "Winner optional must not be null");
    }
}
