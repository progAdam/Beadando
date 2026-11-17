package hu.projekt.rushhour.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public record Vehicle(
        String id,
        Position start,
        int length,
        Orientation orientation,
        VehicleRole role
) {

    public Vehicle {
        Objects.requireNonNull(id, "Vehicle id must not be null");
        Objects.requireNonNull(start, "Vehicle start position must not be null");
        Objects.requireNonNull(orientation, "Vehicle orientation must not be null");
        Objects.requireNonNull(role, "Vehicle role must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Vehicle id must not be blank");
        }
        if (length < 1) {
            throw new IllegalArgumentException("Vehicle length must be positive");
        }
        id = id.trim().toUpperCase();
    }


    public List<Position> occupiedPositions() {
        List<Position> cells = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            int r = orientation == Orientation.VERTICAL ? start.row() + i : start.row();
            int c = orientation == Orientation.HORIZONTAL ? start.col() + i : start.col();
            cells.add(new Position(r, c));
        }
        return List.copyOf(cells);
    }


    public Vehicle move(Direction direction, int steps) {
        return new Vehicle(id, start.shift(direction, steps), length, orientation, role);
    }
}
