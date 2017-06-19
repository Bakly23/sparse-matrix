class MatrixElement implements Comparable<MatrixElement> {
    private final int row;
    private final int column;
    private volatile int value;

    MatrixElement(int row, int column, int value) {
        this.row = row;
        this.column = column;
        this.value = value;
    }

    int getRow() {
        return row;
    }

    int getColumn() {
        return column;
    }

    int getValue() {
        return value;
    }

    void changeValue(int diff) {
        value += diff;
    }

    @Override
    public int compareTo(MatrixElement o) {
        if (row != o.row) {
            return row - o.row;
        }
        return column - o.column;
    }
}
