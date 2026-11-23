package hu.projekt.rushhour.io;

import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.Direction;
import hu.projekt.rushhour.model.ExitPoint;
import hu.projekt.rushhour.model.Orientation;
import hu.projekt.rushhour.model.Position;
import hu.projekt.rushhour.model.Vehicle;
import hu.projekt.rushhour.model.VehicleRole;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoardFileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardFileManager.class);
    private static final String DEFAULT_RESOURCE = "/default-board.txt";

    public BoardState loadBoardOrDefault(Path filePath) {
        if (filePath != null && Files.exists(filePath) && Files.isRegularFile(filePath)) {
            try {
                BoardState loaded = loadBoardFromFile(filePath);
                LOGGER.info("Loaded board from external file: {}", filePath);
                return loaded;
            } catch (Exception e) {
                LOGGER.warn("Failed to parse board file '{}', falling back to default: {}", filePath, e.getMessage());
            }
        } else {
            LOGGER.info("No external board file supplied or found; using built-in default board.");
        }
        return loadDefaultBoard();
    }

    public BoardState loadBoardFromFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        return parseLines(lines);
    }

    public BoardState loadDefaultBoard() {
        try (InputStream is = getClass().getResourceAsStream(DEFAULT_RESOURCE)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    return parseLines(reader.lines().toList());
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read default-board.txt resource, using hardcoded fallback.", e);
        }
        return createHardcodedDefaultBoard();
    }

    public void saveBoardToFile(BoardState state, Path filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# Rush Hour NxM Board File");
        lines.add("ROWS " + state.rows());
        lines.add("COLS " + state.cols());
        lines.add(String.format("EXIT_A %d %d %s",
                state.humanExit().position().row(),
                state.humanExit().position().col(),
                state.humanExit().exitDirection()));
        lines.add(String.format("EXIT_B %d %d %s",
                state.aiExit().position().row(),
                state.aiExit().position().col(),
                state.aiExit().exitDirection()));
        for (Vehicle v : state.vehicles()) {
            lines.add(String.format("VEHICLE %s %d %d %d %s %s",
                    v.id(),
                    v.start().row(),
                    v.start().col(),
                    v.length(),
                    v.orientation(),
                    v.role()));
        }
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }
        Files.write(filePath, lines, StandardCharsets.UTF_8);
        LOGGER.info("Saved board state to {}", filePath);
    }

    private BoardState parseLines(List<String> lines) throws IOException {
        Integer rows = null;
        Integer cols = null;
        ExitPoint exitA = null;
        ExitPoint exitB = null;
        List<Vehicle> vehicles = new ArrayList<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\s+");
            try {
                switch (parts[0].toUpperCase()) {
                    case "ROWS" -> rows = Integer.parseInt(parts[1]);
                    case "COLS" -> cols = Integer.parseInt(parts[1]);
                    case "EXIT_A" -> exitA = new ExitPoint(
                            new Position(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])),
                            Direction.valueOf(parts[3].toUpperCase())
                    );
                    case "EXIT_B" -> exitB = new ExitPoint(
                            new Position(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])),
                            Direction.valueOf(parts[3].toUpperCase())
                    );
                    case "VEHICLE" -> vehicles.add(new Vehicle(
                            parts[1],
                            new Position(Integer.parseInt(parts[2]), Integer.parseInt(parts[3])),
                            Integer.parseInt(parts[4]),
                            Orientation.valueOf(parts[5].toUpperCase()),
                            VehicleRole.valueOf(parts[6].toUpperCase())
                    ));
                    default -> throw new IOException("Unknown directive in board file: " + parts[0]);
                }
            } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                throw new IOException("Hibás formátum a pályafájlban ('" + line + "'): " + e.getMessage(), e);
            }
        }

        if (rows == null || cols == null || exitA == null || exitB == null || vehicles.isEmpty()) {
            throw new IOException("Incomplete board definition in file");
        }

        BoardState state = new BoardState(rows, cols, vehicles, exitA, exitB);
        validateBoardConsistency(state);
        return state;
    }

    private void validateBoardConsistency(BoardState state) throws IOException {
        Set<Position> occupied = new HashSet<>();
        Set<String> ids = new HashSet<>();
        for (Vehicle v : state.vehicles()) {
            if (!ids.add(v.id())) {
                throw new IOException("Duplicate vehicle ID: " + v.id());
            }
            for (Position pos : v.occupiedPositions()) {
                if (!state.isInside(pos)) {
                    throw new IOException("Vehicle " + v.id() + " is out of board bounds at " + pos);
                }
                if (!occupied.add(pos)) {
                    throw new IOException("Overlapping vehicles at position " + pos);
                }
            }
        }
    }

    private BoardState createHardcodedDefaultBoard() {
        List<Vehicle> vehicles = List.of(
                new Vehicle("A", new Position(2, 2), 2, Orientation.HORIZONTAL, VehicleRole.HUMAN),
                new Vehicle("B", new Position(3, 8), 2, Orientation.HORIZONTAL, VehicleRole.AI),
                new Vehicle("C", new Position(0, 4), 3, Orientation.VERTICAL, VehicleRole.NEUTRAL),
                new Vehicle("D", new Position(2, 6), 3, Orientation.VERTICAL, VehicleRole.NEUTRAL),
                new Vehicle("E", new Position(0, 7), 2, Orientation.HORIZONTAL, VehicleRole.NEUTRAL),
                new Vehicle("F", new Position(0, 10), 3, Orientation.VERTICAL, VehicleRole.NEUTRAL),
                new Vehicle("G", new Position(4, 1), 2, Orientation.VERTICAL, VehicleRole.NEUTRAL),
                new Vehicle("H", new Position(5, 3), 3, Orientation.HORIZONTAL, VehicleRole.NEUTRAL),
                new Vehicle("I", new Position(1, 1), 2, Orientation.HORIZONTAL, VehicleRole.NEUTRAL),
                new Vehicle("J", new Position(4, 8), 2, Orientation.HORIZONTAL, VehicleRole.NEUTRAL)
        );
        return new BoardState(
                6,
                12,
                vehicles,
                new ExitPoint(new Position(2, 11), Direction.RIGHT),
                new ExitPoint(new Position(3, 0), Direction.LEFT)
        );
    }
}