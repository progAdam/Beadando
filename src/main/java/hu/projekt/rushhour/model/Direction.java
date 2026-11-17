package hu.projekt.rushhour.model;


public enum Direction {
    UP(-1, 0, Orientation.VERTICAL),
    DOWN(1, 0, Orientation.VERTICAL),
    LEFT(0, -1, Orientation.HORIZONTAL),
    RIGHT(0, 1, Orientation.HORIZONTAL);

    private final int rowDelta;
    private final int colDelta;
    private final Orientation orientation;

    Direction(int rowDelta, int colDelta, Orientation orientation) {
        this.rowDelta = rowDelta;
        this.colDelta = colDelta;
        this.orientation = orientation;
    }

    public int getRowDelta() {
        return rowDelta;
    }

    public int getColDelta() {
        return colDelta;
    }

    public Orientation getOrientation() {
        return orientation;
    }
}
