package hu.projekt.rushhour.model;


public record Position(int row, int col) {


    public Position shift(Direction direction, int steps) {
        return new Position(
                row + direction.getRowDelta() * steps,
                col + direction.getColDelta() * steps
        );
    }
}
