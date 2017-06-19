import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SparseMatrixSupportImplTest {

    @Test
    public void testFromStream() {
        SparseMatrix sparseMatrix = new SparseMatrixSupportImpl().fromStream(Stream.of(2, 3, 5, 6, 0, 0, 1, 2));
        assertEquals(2, sparseMatrix.getNumberOfRows());
        assertEquals(3, sparseMatrix.getNumberOfColumns());
        assertEquals(5, sparseMatrix.getValue(0, 0));
        assertEquals(0, sparseMatrix.getValue(1, 0));
        assertEquals(2, sparseMatrix.getValue(1, 2));
    }

    @Test
    public void testToStream() {
        SparseMatrixSupport<SparseMatrix> support = new SparseMatrixSupportImpl();
        SparseMatrix sparseMatrix = support.fromStream(Stream.of(2, 3, 5, 6, 0, 0, 1, 2));
        assertArrayEquals(new Integer[]{2, 3, 5, 6, 0, 0, 1, 2},
                support.toStream(sparseMatrix).toArray(Integer[]::new));
    }

    @Test
    public void testMultiplyWithOneRow() {
        SparseMatrix denseMatrix1 = SparseMatrixTestUtil.generateDenseMatrix(1, 1000);
        SparseMatrix denseMatrix2 = SparseMatrixTestUtil.generateDenseMatrix(1000, 1);
        SparseMatrix multipliedMatrix = new SparseMatrixSupportImpl().multiply(denseMatrix1, denseMatrix2);
        int[] zerothRowOfFirstMatrix = MultiplyRowToColumnUtil
                .generateRowWithSkippedRowsAndCells(0, 1000, 0, 0);
        int[] zerothColumnOfSecondMatrix = MultiplyRowToColumnUtil
                .generateColumnWithSkippedRowsAndCells(0, 1, 1000, 0, 0);
        int cellZeroToZero = MultiplyRowToColumnUtil.multiplyRowToColumn(zerothRowOfFirstMatrix, zerothColumnOfSecondMatrix);
        assertEquals(cellZeroToZero, multipliedMatrix.getValue(0, 0));
    }

    @Test
    public void testSimpleMultiply() {
        SparseMatrix denseMatrix1 = SparseMatrixTestUtil.generateDenseMatrix(2, 3);
        SparseMatrix denseMatrix2 = SparseMatrixTestUtil.generateDenseMatrix(3, 2);
        SparseMatrix multipliedMatrix = new SparseMatrixSupportImpl().multiply(denseMatrix1, denseMatrix2);
        assertEquals(10, multipliedMatrix.getValue(0, 0));
        assertEquals(13, multipliedMatrix.getValue(0, 1));
        assertEquals(28, multipliedMatrix.getValue(1, 0));
        assertEquals(40, multipliedMatrix.getValue(1, 1));
        assertEquals(2, multipliedMatrix.getNumberOfRows());
        assertEquals(2, multipliedMatrix.getNumberOfColumns());
    }

    @Test
    public void testMultiplyWithMultipleRows() {
        SparseMatrix denseMatrix1 = SparseMatrixTestUtil.generateDenseMatrix(10, 100);
        SparseMatrix denseMatrix2 = SparseMatrixTestUtil.generateDenseMatrix(100, 10);
        SparseMatrix multipliedMatrix = new SparseMatrixSupportImpl(5).multiply(denseMatrix1, denseMatrix2);
        int[] fifthRowOfFirstMatrix = MultiplyRowToColumnUtil
                .generateRowWithSkippedRowsAndCells(5, 100, 0, 0);
        int[] sixthColumnOfSecondMatrix = MultiplyRowToColumnUtil
                .generateColumnWithSkippedRowsAndCells(6, 10, 100, 0, 0);
        int cellFiveToSix = MultiplyRowToColumnUtil.multiplyRowToColumn(fifthRowOfFirstMatrix, sixthColumnOfSecondMatrix);
        assertEquals(cellFiveToSix, multipliedMatrix.getValue(5, 6));
    }

    @Test(expected = RuntimeException.class)
    public void testIncompatibleMatrixes() {
        SparseMatrix denseMatrix1 = SparseMatrixTestUtil.generateDenseMatrix(10, 1000);
        SparseMatrix denseMatrix2 = SparseMatrixTestUtil.generateDenseMatrix(100, 10);
        new SparseMatrixSupportImpl(5).multiply(denseMatrix1, denseMatrix2);
    }

    @Test
    public void testMultiplyBigSparseMatrixes() {
        int firstNumberOfRows = 10000;
        int firstNumberOfColumns = 4000;
        int firstSkipRows = 5;
        int firstSkipCells = 5;

        int secondNumberOfRows = 4000;
        int secondNumberOfColumns = 6500;
        int secondSkipRows = 7;
        int secondSkipCells = 11;

        SparseMatrix sparseMatrix1 = SparseMatrixTestUtil
                .generateSparseMatrixWithSkippedRowsAndCells(firstNumberOfRows, firstNumberOfColumns, firstSkipRows, firstSkipCells);
        SparseMatrix sparseMatrix2 = SparseMatrixTestUtil
                .generateSparseMatrixWithSkippedRowsAndCells(secondNumberOfRows, secondNumberOfColumns, secondSkipRows, secondSkipCells);
        SparseMatrix multipliedMatrix = new SparseMatrixSupportImpl().multiply(sparseMatrix1, sparseMatrix2);

        int[] zerothRowOfFirstMatrix = MultiplyRowToColumnUtil
                .generateRowWithSkippedRowsAndCells(0, firstNumberOfColumns, firstSkipRows, firstSkipCells);
        int[] eleventhColumnOfSecondMatrix = MultiplyRowToColumnUtil
                .generateColumnWithSkippedRowsAndCells(11, secondNumberOfColumns, secondNumberOfRows, secondSkipRows, secondSkipCells);
        int cellZeroToEleven = MultiplyRowToColumnUtil.multiplyRowToColumn(zerothRowOfFirstMatrix, eleventhColumnOfSecondMatrix);

        assertEquals(cellZeroToEleven, multipliedMatrix.getValue(0, 11));

        int[] zerothColumnOfSecondMatrix = MultiplyRowToColumnUtil
                .generateColumnWithSkippedRowsAndCells(0, secondNumberOfColumns, secondNumberOfRows, secondSkipRows, secondSkipCells);
        int cellZeroToZero = MultiplyRowToColumnUtil.multiplyRowToColumn(zerothRowOfFirstMatrix, zerothColumnOfSecondMatrix);

        assertEquals(cellZeroToZero, multipliedMatrix.getValue(0, 0));

        int lastNonEmptyRowIndex = firstNumberOfRows - firstSkipRows;
        int lastColumnIndex = secondNumberOfColumns - 1;
        int[] lastNonEmptyRowOfFirstMatrix = MultiplyRowToColumnUtil
                .generateRowWithSkippedRowsAndCells(lastNonEmptyRowIndex, firstNumberOfColumns, firstSkipRows, firstSkipCells);
        int[] lastColumnOfSecondMatrix = MultiplyRowToColumnUtil
                .generateColumnWithSkippedRowsAndCells(lastColumnIndex, secondNumberOfColumns, secondNumberOfRows, secondSkipRows, secondSkipCells);
        int lastNonEmptyValue = MultiplyRowToColumnUtil.multiplyRowToColumn(lastNonEmptyRowOfFirstMatrix, lastColumnOfSecondMatrix);

        assertEquals(lastNonEmptyValue, multipliedMatrix.getValue(lastNonEmptyRowIndex, lastColumnIndex));
    }
}
