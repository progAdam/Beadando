package hu.projekt.rushhour.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.Direction;
import hu.projekt.rushhour.model.ExitPoint;
import hu.projekt.rushhour.model.Move;
import hu.projekt.rushhour.model.Orientation;
import hu.projekt.rushhour.model.Position;
import hu.projekt.rushhour.model.Vehicle;
import hu.projekt.rushhour.model.VehicleRole;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class RandomAiPlayerTest {

    @Test
    void testChoosesValidMoveWhenAvailable() {
        GameEngine engine = mock(GameEngine.class);
        Vehicle vA = new Vehicle("A", new Position(2, 0), 2, Orientation.HORIZONTAL, VehicleRole.HUMAN);
        Vehicle vB = new Vehicle("B", new Position(3, 2), 2, Orientation.HORIZONTAL, VehicleRole.AI);
        BoardState state = new BoardState(
                6,
                6,
                List.of(vA, vB),
                new ExitPoint(new Position(2, 5), Direction.RIGHT),
                new ExitPoint(new Position(3, 0), Direction.LEFT)
        );

        Move expectedMove = new Move("B", Direction.LEFT, 1);
        when(engine.getValidMovesForVehicle(state, "A")).thenReturn(List.of());
        when(engine.getValidMovesForVehicle(state, "B")).thenReturn(List.of(expectedMove));

        RandomAiPlayer aiPlayer = new RandomAiPlayer(engine, new Random(42));
        Optional<Move> chosen = aiPlayer.chooseMove(state);

        assertTrue(chosen.isPresent());
        assertEquals(expectedMove, chosen.get());
    }

    @Test
    void testReturnsEmptyWhenNoVehicleCanMove() {
        GameEngine engine = mock(GameEngine.class);
        Vehicle vA = new Vehicle("A", new Position(2, 0), 2, Orientation.HORIZONTAL, VehicleRole.HUMAN);
        BoardState state = new BoardState(
                6,
                6,
                List.of(vA),
                new ExitPoint(new Position(2, 5), Direction.RIGHT),
                new ExitPoint(new Position(3, 0), Direction.LEFT)
        );
        when(engine.getValidMovesForVehicle(state, "A")).thenReturn(List.of());

        RandomAiPlayer aiPlayer = new RandomAiPlayer(engine, new Random(1));
        assertTrue(aiPlayer.chooseMove(state).isEmpty());
    }
}
