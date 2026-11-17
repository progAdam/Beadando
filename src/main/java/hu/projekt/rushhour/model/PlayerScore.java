package hu.projekt.rushhour.model;

import java.util.Objects;


public record PlayerScore(String playerName, int wins) {

    public PlayerScore {
        Objects.requireNonNull(playerName, "Player name must not be null");
    }
}
