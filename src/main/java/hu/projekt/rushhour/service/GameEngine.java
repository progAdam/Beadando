package hu.projekt.rushhour.service;

import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.Direction;
import hu.projekt.rushhour.model.ExitPoint;
import hu.projekt.rushhour.model.Move;
import hu.projekt.rushhour.model.MoveOutcome;
import hu.projekt.rushhour.model.PlayerTurn;
import hu.projekt.rushhour.model.Vehicle;
import hu.projekt.rushhour.model.VehicleRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GameEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameEngine.class);

    private final MoveValidator moveValidator;

    public GameEngine(MoveValidator moveValidator) {
        this.moveValidator = Objects.requireNonNull(moveValidator, "MoveValidator must not be null");
    }


    public MoveOutcome applyMove(BoardState state, Move move) {
        if (!moveValidator.isValidMove(state, move)) {
            throw new IllegalArgumentException("Invalid move: " + move);
        }

        Vehicle vehicle = state.findVehicle(move.vehicleId()).orElseThrow();
        ExitPoint heroExit = moveValidator.getHeroExitForVehicle(state, vehicle);

        int appliedSteps = move.steps();
        boolean exited = false;
        for (int step = 1; step <= move.steps(); step++) {
            Vehicle candidate = vehicle.move(move.direction(), step);
            if (moveValidator.isHeroExitingThroughGate(candidate, move.direction(), heroExit)) {
                appliedSteps = step;
                exited = true;
                break;
            }
        }

        Vehicle movedVehicle = vehicle.move(move.direction(), appliedSteps);
        BoardState nextState = state.withUpdatedVehicle(movedVehicle);

        Optional<PlayerTurn> winner = Optional.empty();
        if (exited) {
            winner = vehicle.role() == VehicleRole.HUMAN
                    ? Optional.of(PlayerTurn.HUMAN)
                    : Optional.of(PlayerTurn.AI);
            LOGGER.info("Hero vehicle {} exited the board! Winner: {}", vehicle.id(), winner.get());
        } else {
            LOGGER.info("Applied move: {} {} {}", move.vehicleId(), move.direction(), appliedSteps);
        }

        return new MoveOutcome(nextState, winner);
    }


    public List<Move> getValidMovesForVehicle(BoardState state, String vehicleId) {
        Optional<Vehicle> vehicleOpt = state.findVehicle(vehicleId);
        if (vehicleOpt.isEmpty()) {
            return List.of();
        }
        Vehicle vehicle = vehicleOpt.get();
        int maxDistance = Math.max(state.rows(), state.cols());
        List<Move> validMoves = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            if (dir.getOrientation() != vehicle.orientation()) {
                continue;
            }
            for (int steps = 1; steps <= maxDistance; steps++) {
                Move candidate = new Move(vehicle.id(), dir, steps);
                if (moveValidator.isValidMove(state, candidate)) {
                    validMoves.add(candidate);
                    Vehicle stepped = vehicle.move(dir, steps);
                    ExitPoint exit = moveValidator.getHeroExitForVehicle(state, vehicle);
                    if (moveValidator.isHeroExitingThroughGate(stepped, dir, exit)) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return List.copyOf(validMoves);
    }


    public List<Move> getAllValidMoves(BoardState state) {
        List<Move> allMoves = new ArrayList<>();
        for (Vehicle vehicle : state.vehicles()) {
            allMoves.addAll(getValidMovesForVehicle(state, vehicle.id()));
        }
        return List.copyOf(allMoves);
    }
}
