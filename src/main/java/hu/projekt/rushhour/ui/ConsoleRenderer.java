package hu.projekt.rushhour.ui;

import hu.projekt.rushhour.model.BoardState;
import hu.projekt.rushhour.model.Direction;
import hu.projekt.rushhour.model.ExitPoint;
import hu.projekt.rushhour.model.PlayerScore;
import hu.projekt.rushhour.model.Position;
import hu.projekt.rushhour.model.Vehicle;
import java.io.PrintStream;
import java.util.List;


public class ConsoleRenderer {

    private final PrintStream out;

    public ConsoleRenderer(PrintStream out) {
        this.out = out;
    }


    public void renderBoard(BoardState state) {
        String[][] grid = new String[state.rows()][state.cols()];
        for (int r = 0; r < state.rows(); r++) {
            for (int c = 0; c < state.cols(); c++) {
                grid[r][c] = ".";
            }
        }
        for (Vehicle v : state.vehicles()) {
            for (Position pos : v.occupiedPositions()) {
                if (state.isInside(pos)) {
                    grid[pos.row()][pos.col()] = v.id();
                }
            }
        }

        out.println();
        printTopOrBottomExitRow(state, Direction.UP);

        StringBuilder header = new StringBuilder("     ");
        for (int c = 0; c < state.cols(); c++) {
            header.append(String.format(" %-2s", columnLabel(c)));
        }
        out.println(header);

        String border = "    +" + "---".repeat(state.cols()) + "+";
        out.println(border);

        for (int r = 0; r < state.rows(); r++) {
            String leftMarker = isSideExit(state.humanExit(), r, Direction.LEFT) ? "A<"
                    : (isSideExit(state.aiExit(), r, Direction.LEFT) ? "B<" : "  ");
            String rightMarker = isSideExit(state.humanExit(), r, Direction.RIGHT) ? ">A"
                    : (isSideExit(state.aiExit(), r, Direction.RIGHT) ? ">B" : "");

            StringBuilder rowSb = new StringBuilder();
            rowSb.append(String.format("%2d%s|", r + 1, leftMarker));
            for (int c = 0; c < state.cols(); c++) {
                rowSb.append(String.format(" %-2s", grid[r][c]));
            }
            rowSb.append("|").append(rightMarker);
            out.println(rowSb);
        }

        out.println(border);
        printTopOrBottomExitRow(state, Direction.DOWN);

        out.printf("Céljárművek: [A] Humán kijárat: sor %d, oszlop %s (%s) | [B] AI kijárat: sor %d, oszlop %s (%s)%n",
                state.humanExit().position().row() + 1,
                columnLabel(state.humanExit().position().col()),
                state.humanExit().exitDirection(),
                state.aiExit().position().row() + 1,
                columnLabel(state.aiExit().position().col()),
                state.aiExit().exitDirection());
        out.println();
    }


    public void renderHighScores(List<PlayerScore> scores) {
        out.println("\n=== HIGH SCORE RANGLISTA ===");
        if (scores.isEmpty()) {
            out.println("Még nincs rögzített győzelem az adatbázisban.");
        } else {
            out.printf("%-4s %-25s %s%n", "#", "Játékos", "Győzelmek");
            out.println("---------------------------------------");
            for (int i = 0; i < scores.size(); i++) {
                PlayerScore score = scores.get(i);
                out.printf("%-4d %-25s %d%n", i + 1, score.playerName(), score.wins());
            }
        }
        out.println("=======================================\n");
    }


    public void renderHelp() {
        out.println("""
                
                === ELÉRHETŐ PARANCSOK ===
                  <JÁRMŰ> <IRÁNY> <LÉPÉS>  - Jármű csúsztatása (pl. A RIGHT 2, C UP 1, D BALRA 3)
                                             Irányok: UP/FEL, DOWN/LE, LEFT/BALRA, RIGHT/JOBBRA
                  LOAD <fajlnev.txt>       - Pálya betöltése szövegfájlból
                  SAVE <fajlnev.txt>       - Aktuális pálya mentése szövegfájlba
                  SAVE_XML <fajlnev.xml>   - Folyamatban lévő játék mentése XML fájlba
                  LOAD_XML <fajlnev.xml>   - Mentett játék visszatöltése XML fájlból
                  SCORES                   - High score táblázat megjelenítése
                  HELP                     - Súgó megjelenítése
                  QUIT                     - Kilépés a játékból
                ==========================
                """);
    }

    public void printMessage(String message) {
        out.println(message);
    }

    private void printTopOrBottomExitRow(BoardState state, Direction dir) {
        boolean hasExit = state.humanExit().exitDirection() == dir || state.aiExit().exitDirection() == dir;
        if (!hasExit) {
            return;
        }
        StringBuilder sb = new StringBuilder("     ");
        for (int c = 0; c < state.cols(); c++) {
            if (state.humanExit().exitDirection() == dir && state.humanExit().position().col() == c) {
                sb.append(" A ");
            } else if (state.aiExit().exitDirection() == dir && state.aiExit().position().col() == c) {
                sb.append(" B ");
            } else {
                sb.append("   ");
            }
        }
        out.println(sb);
    }

    private boolean isSideExit(ExitPoint exit, int row, Direction dir) {
        return exit.exitDirection() == dir && exit.position().row() == row;
    }

    private String columnLabel(int colIndex) {
        return String.valueOf((char) ('a' + colIndex));
    }
}
