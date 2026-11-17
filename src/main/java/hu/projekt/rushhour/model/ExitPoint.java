package hu.projekt.rushhour.model;

import java.util.Objects;


public record ExitPoint(Position position, Direction exitDirection) {

    public ExitPoint {
        Objects.requireNonNull(position, "Exit position must not be null");
        Objects.requireNonNull(exitDirection, "Exit direction must not be null");
    }
}
