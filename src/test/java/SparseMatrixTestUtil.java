import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class SparseMatrixTestUtil {

    private static SparseMatrix generateMatrix(int numberOfRows, int numberOfColumns, Stream<Integer> elementStream) {
        return new SparseMatrix(Stream.concat(Stream.of(numberOfRows, numberOfColumns), elementStream));
    }

    static SparseMatrix generateDenseMatrix(int numberOfRows, int numberOfColumns) {
        return generateSparseMatrixWithFilter(numberOfRows, numberOfColumns, i -> true);
    }

    private static SparseMatrix generateSparseMatrixWithFilter(int numberOfRows, int numberOfColumns,
                                                               IntPredicate predicate) {
        Stream<Integer> elementStream = IntStream.range(0, numberOfRows * numberOfColumns)
                .mapToObj(i -> predicate.test(i) ? generateElement(i) : 0);
        return generateMatrix(numberOfRows, numberOfColumns, elementStream);
    }

    static SparseMatrix generateSparseMatrixWithSkippedCells(int numberOfRows, int numberOfColumns, int skipCells) {
        return generateSparseMatrixWithFilter(numberOfRows, numberOfColumns, i -> i % skipCells == 0);
    }

    static SparseMatrix generateSparseMatrixWithSkippedRows(int numberOfRows, int numberOfColumns, int skipRows) {
        return generateSparseMatrixWithFilter(numberOfRows, numberOfColumns,
                i -> (i / numberOfColumns) % skipRows == 0);
    }

    static SparseMatrix generateSparseMatrixWithSkippedRowsAndCells(int numberOfRows, int numberOfColumns,
                                                                    int skipRows, int skipCells) {
        return generateSparseMatrixWithFilter(numberOfRows, numberOfColumns,
                i -> (i / numberOfColumns) % skipRows == 0 && i % skipCells == 0);
    }

    private static Integer generateElement(int elem) {
        return elem % 149;
    }
}
