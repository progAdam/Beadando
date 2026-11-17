package hu.projekt.rushhour.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public record BoardState(
        int rows,
        int cols,
        List<Vehicle> vehicles,
        ExitPoint humanExit,
        ExitPoint aiExit
) {

    public BoardState {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Board dimensions must be positive");
        }
        Objects.requireNonNull(vehicles, "Vehicles list must not be null");
        Objects.requireNonNull(humanExit, "Human exit must not be null");
        Objects.requireNonNull(aiExit, "AI exit must not be null");
        vehicles = List.copyOf(vehicles);
    }


    public Optional<Vehicle> findVehicle(String id) {
        if (id == null) {
            return Optional.empty();
        }
        String normalized = id.trim().toUpperCase();
        return vehicles.stream()
                .filter(v -> v.id().equals(normalized))
                .findFirst();
    }


    public BoardState withUpdatedVehicle(Vehicle updated) {
        List<Vehicle> nextVehicles = new ArrayList<>(vehicles.size());
        for (Vehicle v : vehicles) {
            if (v.id().equals(updated.id())) {
                nextVehicles.add(updated);
            } else {
                nextVehicles.add(v);
            }
        }
        return new BoardState(rows, cols, nextVehicles, humanExit, aiExit);
    }


    public boolean isInside(Position pos) {
        return pos.row() >= 0 && pos.row() < rows && pos.col() >= 0 && pos.col() < cols;
    }
}
