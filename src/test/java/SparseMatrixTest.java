import org.junit.Test;

import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;

public class SparseMatrixTest {
    @Test
    public void testNumberOfColumns() {
        SparseMatrix denseMatrix = SparseMatrixTestUtil.generateDenseMatrix(6, 3);
        assertEquals(3, denseMatrix.getNumberOfColumns());
    }

    @Test
    public void testNumberOfRows() {
        SparseMatrix denseMatrix = SparseMatrixTestUtil.generateDenseMatrix(4, 2);
        assertEquals(4, denseMatrix.getNumberOfRows());
    }

    @Test
    public void testMatrixFilling() {
        SparseMatrix denseMatrix = SparseMatrixTestUtil.generateDenseMatrix(4, 4);
        assertEquals(8, denseMatrix.getValue(2, 0));
        assertEquals(14, denseMatrix.getValue(3, 2));
    }

    @Test
    public void testTranspose() {
        SparseMatrix denseMatrixTransposed = SparseMatrixTestUtil.generateDenseMatrix(10000, 4)
                .transposedMatrix();
        assertEquals(8, denseMatrixTransposed.getValue(0, 2));
        assertEquals(8, denseMatrixTransposed.transposedMatrix().getValue(2, 0));
    }

    @Test
    public void testSpliterator() {
        SparseMatrix sparseMatrix = SparseMatrixTestUtil
                .generateSparseMatrixWithSkippedRowsAndCells(1001, 566, 2, 3);
        int numberOfNotNullElements = StreamSupport
                .stream(sparseMatrix.spliterator(), true)
                .filter(i -> i != 0)
                .mapToInt(i -> i)
                .toArray()
                .length;
        assertEquals(93889, numberOfNotNullElements);

        int sum = StreamSupport
                .stream(sparseMatrix.spliterator(), true)
                .filter(i -> i != 0)
                .mapToInt(i -> i > 0 ? 1 : 0)
                .reduce(0, (i1, i2) -> i1 + i2);
        assertEquals(93889, sum);
    }

    @Test
    public void testSpliteratorSplit() {
        SparseMatrix sparseMatrix = SparseMatrixTestUtil
                .generateSparseMatrixWithSkippedRowsAndCells(1001, 566, 2, 3);
        Spliterator<Integer> spliterator = sparseMatrix.spliterator();
        Spliterator<Integer> spliterator2 = spliterator.trySplit();
        Spliterator<Integer> spliterator3 = spliterator2.trySplit();
        Spliterator<Integer> spliterator4 = spliterator3.trySplit();
        int numberOfNotNullElements = Stream.concat(
                Stream.concat(StreamSupport.stream(spliterator, true), StreamSupport.stream(spliterator2, true)),
                Stream.concat(StreamSupport.stream(spliterator3, true), StreamSupport.stream(spliterator4, true)))
                .filter(i -> i != 0)
                .mapToInt(i -> i > 0 ? 1 : 0)
                .reduce(0, (i1, i2) -> i1 + i2);
        assertEquals(93889, numberOfNotNullElements);
    }

    @Test
    public void testMatrixWithSkippedCells() {
        SparseMatrix matrixWithSkippedCells = SparseMatrixTestUtil
                .generateSparseMatrixWithSkippedCells(10, 10, 4);
        assertEquals(0, matrixWithSkippedCells.getValue(4, 3));
        assertEquals(0, matrixWithSkippedCells.transposedMatrix().getValue(6, 0));
    }

    @Test
    public void testMatrixWithSkippedRows() {
        SparseMatrix matrixWithSkippedRows = SparseMatrixTestUtil
                .generateSparseMatrixWithSkippedRows(10, 10, 3);
        assertEquals(1, matrixWithSkippedRows.getValue(0, 1));
        assertEquals(0, matrixWithSkippedRows.getValue(5, 0));
        int cellEightToSix = MultiplyRowToColumnUtil
                .generateRowWithSkippedRowsAndCells(8, 10, 3, 0)[6];
        assertEquals(cellEightToSix, matrixWithSkippedRows.getValue(8, 6));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWrongRowIndex() {
        SparseMatrix denseMatrix = SparseMatrixTestUtil.generateDenseMatrix(10, 10);
        denseMatrix.getValue(11, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWrongColumnIndex() {
        SparseMatrix denseMatrixTransposed = SparseMatrixTestUtil.generateDenseMatrix(10, 10);
        denseMatrixTransposed.getValue(0, 11);
    }
}
