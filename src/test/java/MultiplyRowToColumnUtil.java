import java.util.stream.IntStream;

class MultiplyRowToColumnUtil {
    static int multiplyRowToColumn(int[] row, int[] column) {
        if (row.length != column.length) {
            throw new IllegalArgumentException("Number of cells in row must be equal to number of cells in column");
        }
        int result = 0;
        for (int i = 0; i < row.length; i++) {
            result += row[i] * column[i];
        }
        return result;
    }

    static int[] generateRowWithSkippedRowsAndCells(int rowIndex, int numberOfColumns, int skipRows, int skipCells) {
        if (skipRows == 0 || rowIndex % skipRows == 0) {
            return IntStream.range(0, numberOfColumns)
                    .map(i -> {
                        int indexOfElementInMatrix = (rowIndex * numberOfColumns + i);
                        if (skipCells == 0 || indexOfElementInMatrix % skipCells == 0) {
                            return indexOfElementInMatrix % 149;
                        } else {
                            return 0;
                        }
                    })
                    .toArray();
        } else {
            return IntStream.range(0, numberOfColumns)
                    .map(i -> 0)
                    .toArray();
        }
    }

    static int[] generateColumnWithSkippedRowsAndCells(int columnIndex, int numberOfColumns, int numberOfRows,
                                                       int skipRows, int skipCells) {
        return IntStream.range(0, numberOfRows)
                .map(i -> {
                    int indexOfElementInMatrix = (numberOfColumns * i + columnIndex);
                    if ((skipRows == 0 || i % skipRows == 0) && (skipCells == 0 || indexOfElementInMatrix % skipCells == 0)) {
                        return indexOfElementInMatrix % 149;
                    } else {
                        return 0;
                    }
                })
                .toArray();
    }


}
