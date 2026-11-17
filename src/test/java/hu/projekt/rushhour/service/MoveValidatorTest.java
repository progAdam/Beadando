package hu.projekt.rushhour.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.Direction;
import hu.projekt.rushhour.model.ExitPoint;
import hu.projekt.rushhour.model.Move;
import hu.projekt.rushhour.model.Orientation;
import hu.projekt.rushhour.model.Position;
import hu.projekt.rushhour.model.Vehicle;
import hu.projekt.rushhour.model.VehicleRole;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoveValidatorTest {

    private MoveValidator validator;
    private BoardState sampleBoard;

    @BeforeEach
    void setUp() {
        validator = new MoveValidator();
        List<Vehicle> vehicles = List.of(
                new Vehicle("A", new Position(2, 1), 2, Orientation.HORIZONTAL, VehicleRole.HUMAN),
                new Vehicle("B", new Position(3, 4), 2, Orientation.HORIZONTAL, VehicleRole.AI),
                new Vehicle("C", new Position(1, 3), 2, Orientation.VERTICAL, VehicleRole.NEUTRAL),
                new Vehicle("D", new Position(4, 1), 2, Orientation.HORIZONTAL, VehicleRole.NEUTRAL)
        );
        sampleBoard = new BoardState(
                6,
                6,
                vehicles,
                new ExitPoint(new Position(2, 5), Direction.RIGHT),
                new ExitPoint(new Position(3, 0), Direction.LEFT)
        );
    }

    @Test
    void testValidShortAndLongSlideOnClearPath() {
        assertTrue(validator.isValidMove(sampleBoard, new Move("D", Direction.RIGHT, 1)));
        assertTrue(validator.isValidMove(sampleBoard, new Move("D", Direction.RIGHT, 3)));
    }

    @Test
    void testRejectsNullOrUnknownVehicle() {
        assertFalse(validator.isValidMove(null, new Move("A", Direction.RIGHT, 1)));
        assertFalse(validator.isValidMove(sampleBoard, null));
        assertFalse(validator.isValidMove(sampleBoard, new Move("Z", Direction.RIGHT, 1)));
    }

    @Test
    void testRejectsPerpendicularMovement() {
        assertFalse(validator.isValidMove(sampleBoard, new Move("A", Direction.UP, 1)));
        assertFalse(validator.isValidMove(sampleBoard, new Move("C", Direction.LEFT, 1)));
    }

    @Test
    void testRejectsCollisionWithOtherVehicle() {
        assertFalse(validator.isValidMove(sampleBoard, new Move("A", Direction.RIGHT, 1)));
        assertFalse(validator.isValidMove(sampleBoard, new Move("A", Direction.RIGHT, 2)));
    }

    @Test
    void testRejectsLeavingBoardForNonExitDirectionOrNeutralVehicle() {
        assertFalse(validator.isValidMove(sampleBoard, new Move("A", Direction.LEFT, 2)));
        assertFalse(validator.isValidMove(sampleBoard, new Move("D", Direction.RIGHT, 4)));
        assertFalse(validator.isValidMove(sampleBoard, new Move("C", Direction.UP, 2)));
    }

    @Test
    void testHeroCarCanExitThroughDesignatedGate() {
        BoardState clearExitBoard = new BoardState(
                6,
                6,
                List.of(
                        new Vehicle("A", new Position(2, 3), 2, Orientation.HORIZONTAL, VehicleRole.HUMAN),
                        new Vehicle("B", new Position(3, 1), 2, Orientation.HORIZONTAL, VehicleRole.AI)
                ),
                new ExitPoint(new Position(2, 5), Direction.RIGHT),
                new ExitPoint(new Position(3, 0), Direction.LEFT)
        );
        assertTrue(validator.isValidMove(clearExitBoard, new Move("A", Direction.RIGHT, 1)));
        assertTrue(validator.isValidMove(clearExitBoard, new Move("A", Direction.RIGHT, 2)));
        assertTrue(validator.isValidMove(clearExitBoard, new Move("B", Direction.LEFT, 2)));
    }

    @Test
    void testVerticalHeroExits() {
        Vehicle vUp = new Vehicle("A", new Position(0, 2), 2, Orientation.VERTICAL, VehicleRole.HUMAN);
        ExitPoint topExit = new ExitPoint(new Position(0, 2), Direction.UP);
        assertTrue(validator.isHeroExitingThroughGate(vUp.move(Direction.UP, 1), Direction.UP, topExit));

        Vehicle vDown = new Vehicle("B", new Position(4, 2), 2, Orientation.VERTICAL, VehicleRole.AI);
        ExitPoint bottomExit = new ExitPoint(new Position(5, 2), Direction.DOWN);
        assertTrue(validator.isHeroExitingThroughGate(vDown.move(Direction.DOWN, 1), Direction.DOWN, bottomExit));
    }
}
