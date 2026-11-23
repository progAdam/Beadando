package hu.projekt.rushhour.service;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameEngineTest {

    private GameEngine gameEngine;
    private BoardState board;

    @BeforeEach
    void setUp() {
        this.gameEngine = new GameEngine(new MoveValidator());
        this.board = new BoardState(
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
        MoveOutcome outcome = gameEngine.applyMove(board, new Move("C", Direction.DOWN, 3), PlayerTurn.HUMAN);
        Assertions.assertTrue(outcome.winner().isEmpty());
        Vehicle updatedC = outcome.nextState().findVehicle("C").orElseThrow();
        Assertions.assertEquals(new Position(3, 0), updatedC.start());
    }

    @Test
    void testApplyWinningMoveForHumanAndAi() {
        MoveOutcome humanWin = gameEngine.applyMove(board, new Move("A", Direction.RIGHT, 3), PlayerTurn.HUMAN);
        Assertions.assertEquals(PlayerTurn.HUMAN, humanWin.winner().orElseThrow());

        MoveOutcome aiWin = gameEngine.applyMove(board, new Move("B", Direction.LEFT, 3), PlayerTurn.AI);
        Assertions.assertEquals(PlayerTurn.AI, aiWin.winner().orElseThrow());
    }

    @Test
    void applyMoveShouldNotDeclareWinnerWhenAiMovesHumanTargetCarOut() {
        Move humanExitMove = new Move("A", Direction.RIGHT, 3);
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> gameEngine.applyMove(board, humanExitMove, PlayerTurn.AI)
        );
    }

    @Test
    void applyMoveShouldNotDeclareWinnerWhenHumanMovesAiTargetCarOut() {
        Move aiExitMove = new Move("B", Direction.LEFT, 3);
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> gameEngine.applyMove(board, aiExitMove, PlayerTurn.HUMAN)
        );
    }

    @Test
    void testApplyInvalidMoveThrowsException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> gameEngine.applyMove(board, new Move("A", Direction.UP, 1), PlayerTurn.HUMAN)
        );
    }

    @Test
    void testGetAllValidMovesFindsMovesForAllVehicles() {
        List<Move> allMoves = gameEngine.getAllValidMoves(board);
        Assertions.assertFalse(allMoves.isEmpty());
        Assertions.assertTrue(allMoves.contains(new Move("A", Direction.RIGHT, 3)));
        Assertions.assertTrue(allMoves.contains(new Move("B", Direction.LEFT, 3)));
        Assertions.assertTrue(allMoves.contains(new Move("C", Direction.DOWN, 4)));
        Assertions.assertTrue(gameEngine.getValidMovesForVehicle(board, "UNKNOWN").isEmpty());

        List<Move> aiMoves = gameEngine.getAllValidMoves(board, PlayerTurn.AI);
        Assertions.assertFalse(aiMoves.contains(new Move("A", Direction.RIGHT, 3)));
        Assertions.assertTrue(aiMoves.contains(new Move("B", Direction.LEFT, 3)));
    }
}