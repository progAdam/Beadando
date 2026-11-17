package hu.projekt.rushhour.service;

import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.Move;
import hu.projekt.rushhour.model.Vehicle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RandomAiPlayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomAiPlayer.class);

    private final GameEngine gameEngine;
    private final Random random;

    public RandomAiPlayer(GameEngine gameEngine, Random random) {
        this.gameEngine = Objects.requireNonNull(gameEngine, "GameEngine must not be null");
        this.random = Objects.requireNonNull(random, "Random must not be null");
    }


    public Optional<Move> chooseMove(BoardState state) {
        List<Vehicle> candidates = new ArrayList<>(state.vehicles());
        Collections.shuffle(candidates, random);

        for (Vehicle vehicle : candidates) {
            List<Move> validMoves = gameEngine.getValidMovesForVehicle(state, vehicle.id());
            if (!validMoves.isEmpty()) {
                Move selected = validMoves.get(random.nextInt(validMoves.size()));
                LOGGER.info("AI selected move: {}", selected);
                return Optional.of(selected);
            }
        }

        LOGGER.warn("AI found no valid moves on the current board");
        return Optional.empty();
    }
}
