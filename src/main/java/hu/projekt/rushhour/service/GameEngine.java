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
        return applyMove(state, move, PlayerTurn.HUMAN);
    }

    public MoveOutcome applyMove(BoardState state, Move move, PlayerTurn currentTurn) {
        Objects.requireNonNull(currentTurn, "PlayerTurn must not be null");
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

        if (exited && !isOwnTargetVehicle(vehicle, currentTurn)) {
            throw new IllegalArgumentException(
                    "Opponent's target vehicle cannot be moved out of the board: " + move);
        }

        Vehicle movedVehicle = vehicle.move(move.direction(), appliedSteps);
        BoardState nextState = state.withUpdatedVehicle(movedVehicle);

        Optional<PlayerTurn> winner = Optional.empty();
        if (exited) {
            winner = Optional.of(currentTurn);
            LOGGER.info("Hero vehicle {} exited the board! Winner: {}", vehicle.id(), winner.get());
        } else {
            LOGGER.info("Applied move: {} {} {}", move.vehicleId(), move.direction(), appliedSteps);
        }

        return new MoveOutcome(nextState, winner);
    }

    public List<Move> getValidMovesForVehicle(BoardState state, String vehicleId) {
        return getValidMovesForVehicle(state, vehicleId, null);
    }

    public List<Move> getValidMovesForVehicle(BoardState state, String vehicleId, PlayerTurn currentTurn) {
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
                    Vehicle stepped = vehicle.move(dir, steps);
                    ExitPoint exit = moveValidator.getHeroExitForVehicle(state, vehicle);
                    if (moveValidator.isHeroExitingThroughGate(stepped, dir, exit)) {
                        if (currentTurn == null || isOwnTargetVehicle(vehicle, currentTurn)) {
                            validMoves.add(candidate);
                        }
                        break;
                    }
                    validMoves.add(candidate);
                } else {
                    break;
                }
            }
        }
        return List.copyOf(validMoves);
    }

    public List<Move> getAllValidMoves(BoardState state) {
        return getAllValidMoves(state, null);
    }

    public List<Move> getAllValidMoves(BoardState state, PlayerTurn currentTurn) {
        List<Move> allMoves = new ArrayList<>();
        for (Vehicle vehicle : state.vehicles()) {
            allMoves.addAll(getValidMovesForVehicle(state, vehicle.id(), currentTurn));
        }
        return List.copyOf(allMoves);
    }

    private boolean isOwnTargetVehicle(Vehicle vehicle, PlayerTurn currentTurn) {
        return (currentTurn == PlayerTurn.HUMAN && vehicle.role() == VehicleRole.HUMAN)
                || (currentTurn == PlayerTurn.AI && vehicle.role() == VehicleRole.AI);
    }
}