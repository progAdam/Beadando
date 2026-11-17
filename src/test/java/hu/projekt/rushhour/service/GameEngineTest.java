package hu.projekt.rushhour.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.Direction;
import hu.projekt.rushhour.model.ExitPoint;
import hu.projekt.rushhour.model.Move;
import hu.projekt.rushhour.model.MoveOutcome;
import hu.projekt.rushhour.model.Orientation;
import hu.projekt.rushhour.model.PlayerTurn;
import hu.projekt.rushhour.model.Position;
import hu.projekt.rushhour.model.Vehicle;
import hu.projekt.rushhour.model.VehicleRole;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameEngineTest {

    private GameEngine gameEngine;
    private BoardState board;

    @BeforeEach
    void setUp() {
        gameEngine = new GameEngine(new MoveValidator());
        board = new BoardState(
                6,
                6,
                List.of(
                        new Vehicle("A", new Position(2, 2), 2, Orientation.HORIZONTAL, VehicleRole.HUMAN),
                        new Vehicle("B", new Position(3, 2), 2, Orientation.HORIZONTAL, VehicleRole.AI),
                        new Vehicle("C", new Position(0, 0), 2, Orientation.VERTICAL, VehicleRole.NEUTRAL)
                ),
                new ExitPoint(new Position(2, 5), Direction.RIGHT),
                new ExitPoint(new Position(3, 0), Direction.LEFT)
        );
    }

    @Test
    void testApplyNormalMoveUpdatesPositionWithoutWinner() {
        MoveOutcome outcome = gameEngine.applyMove(board, new Move("C", Direction.DOWN, 3));
        assertTrue(outcome.winner().isEmpty());
        Vehicle updatedC = outcome.nextState().findVehicle("C").orElseThrow();
        assertEquals(new Position(3, 0), updatedC.start());
    }

    @Test
    void testApplyWinningMoveForHumanAndAi() {
        MoveOutcome humanWin = gameEngine.applyMove(board, new Move("A", Direction.RIGHT, 3));
        assertEquals(PlayerTurn.HUMAN, humanWin.winner().orElseThrow());

        MoveOutcome aiWin = gameEngine.applyMove(board, new Move("B", Direction.LEFT, 3));
        assertEquals(PlayerTurn.AI, aiWin.winner().orElseThrow());
    }

    @Test
    void testApplyInvalidMoveThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> gameEngine.applyMove(board, new Move("A", Direction.UP, 1)));
    }

    @Test
    void testGetAllValidMovesFindsMovesForAllVehicles() {
        List<Move> moves = gameEngine.getAllValidMoves(board);
        assertFalse(moves.isEmpty());
        assertTrue(moves.contains(new Move("A", Direction.RIGHT, 3)));
        assertTrue(moves.contains(new Move("B", Direction.LEFT, 3)));
        assertTrue(moves.contains(new Move("C", Direction.DOWN, 4)));
        assertTrue(gameEngine.getValidMovesForVehicle(board, "UNKNOWN").isEmpty());
    }
}
