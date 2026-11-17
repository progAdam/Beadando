package hu.projekt.rushhour.model;

import java.util.Objects;


public record Move(String vehicleId, Direction direction, int steps) {

    public Move {
        Objects.requireNonNull(vehicleId, "Vehicle ID must not be null");
        Objects.requireNonNull(direction, "Direction must not be null");
        if (vehicleId.isBlank()) {
            throw new IllegalArgumentException("Vehicle ID must not be blank");
        }
        if (steps < 1) {
            throw new IllegalArgumentException("Steps must be at least 1");
        }
        vehicleId = vehicleId.trim().toUpperCase();
    }
}
