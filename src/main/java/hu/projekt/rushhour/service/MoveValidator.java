package hu.projekt.rushhour.service;

import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.Direction;
import hu.projekt.rushhour.model.ExitPoint;
import hu.projekt.rushhour.model.Move;
import hu.projekt.rushhour.model.Position;
import hu.projekt.rushhour.model.Vehicle;
import hu.projekt.rushhour.model.VehicleRole;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class MoveValidator {


    public boolean isValidMove(BoardState state, Move move) {
        if (state == null || move == null) {
            return false;
        }
        Optional<Vehicle> vehicleOpt = state.findVehicle(move.vehicleId());
        if (vehicleOpt.isEmpty()) {
            return false;
        }
        Vehicle vehicle = vehicleOpt.get();
        if (vehicle.orientation() != move.direction().getOrientation()) {
            return false;
        }

        Set<Position> occupiedByOthers = collectOtherVehicleCells(state, vehicle.id());
        ExitPoint heroExit = getHeroExitForVehicle(state, vehicle);

        for (int step = 1; step <= move.steps(); step++) {
            Vehicle stepped = vehicle.move(move.direction(), step);
            if (isHeroExitingThroughGate(stepped, move.direction(), heroExit)) {
                return true;
            }
            for (Position cell : stepped.occupiedPositions()) {
                if (!state.isInside(cell)) {
                    return false;
                }
                if (occupiedByOthers.contains(cell)) {
                    return false;
                }
            }
        }
        return true;
    }


    public boolean isHeroExitingThroughGate(
            Vehicle movedVehicle,
            Direction direction,
            ExitPoint exitPoint
    ) {
        if (exitPoint == null || exitPoint.exitDirection() != direction) {
            return false;
        }
        Position gate = exitPoint.position();
        return switch (direction) {
            case RIGHT -> movedVehicle.start().row() == gate.row()
                    && movedVehicle.start().col() + movedVehicle.length() - 1 > gate.col();
            case LEFT -> movedVehicle.start().row() == gate.row()
                    && movedVehicle.start().col() < gate.col();
            case DOWN -> movedVehicle.start().col() == gate.col()
                    && movedVehicle.start().row() + movedVehicle.length() - 1 > gate.row();
            case UP -> movedVehicle.start().col() == gate.col()
                    && movedVehicle.start().row() < gate.row();
        };
    }


    public ExitPoint getHeroExitForVehicle(BoardState state, Vehicle vehicle) {
        if (vehicle.role() == VehicleRole.HUMAN) {
            return state.humanExit();
        }
        if (vehicle.role() == VehicleRole.AI) {
            return state.aiExit();
        }
        return null;
    }

    private Set<Position> collectOtherVehicleCells(BoardState state, String movingVehicleId) {
        Set<Position> occupied = new HashSet<>();
        for (Vehicle other : state.vehicles()) {
            if (!other.id().equals(movingVehicleId)) {
                occupied.addAll(other.occupiedPositions());
            }
        }
        return occupied;
    }
}
