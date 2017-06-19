import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SparseMatrix {
    private final static int DEFAULT_SIZE = 8;

    private final int[] iArray;
    private final int[] jArray;
    private final int[] values;
    private final int numberOfRows;
    private final int numberOfColumns;

    public SparseMatrix(Stream<Integer> stream) {
        Iterator<Integer> iterator = stream.sequential().iterator();
        this.numberOfRows = iterator.next();
        this.numberOfColumns = iterator.next();
        Triplet<int[]> sparseMatrixCreationResult = new SparseMatrixCreator()
                .createSparseMatrix(iterator);
        iArray = sparseMatrixCreationResult.first;
        jArray = sparseMatrixCreationResult.second;
        values = sparseMatrixCreationResult.third;
    }

    public SparseMatrix(int numberOfRows, int numberOfColumns, Stream<MatrixElement> stream) {
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        Triplet<int[]> sparseMatrixCreationResult = new MultiplySparseMatrixCreator(numberOfRows)
                .createSparseMatrix(stream);
        iArray = sparseMatrixCreationResult.first;
        jArray = sparseMatrixCreationResult.second;
        values = sparseMatrixCreationResult.third;
    }

    public int getValue(int rowNumber, int columnNumber) {
        if (rowNumber > numberOfRows - 1) {
            throw new IllegalArgumentException("Matrix contains " + numberOfRows + " rows; " +
                    "you can't retrieve row with index " + rowNumber);
        }
        if (columnNumber > numberOfColumns - 1) {
            throw new IllegalArgumentException("Matrix contains " + numberOfColumns + " columns; " +
                    "you can't retrieve column with index " + columnNumber);
        }
        int rowStart = iArray[rowNumber];
        int rowFinish = iArray[rowNumber + 1];
        if (rowFinish > rowStart) {
            int valueIndex = Arrays.binarySearch(jArray, rowStart, rowFinish, columnNumber);
            if (valueIndex > -1) {
                return values[valueIndex];
            }
        }
        return 0;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public SparseMatrix transposedMatrix() {
        MatrixElement[] transposedElements = new MatrixElement[values.length];
        int currentRow = 0;
        for (int i = 0; i < values.length; i++) {
            if (iArray[currentRow + 1] == i) {
                currentRow++;
            }
            transposedElements[i] = new MatrixElement(jArray[i], currentRow, values[i]);
        }
        Arrays.parallelSort(transposedElements);
        return new SparseMatrix(numberOfColumns, numberOfRows, Arrays.stream(transposedElements));
    }

    public boolean containsRow(int i) {
        return iArray.length > i + 1 && iArray[i] < iArray[i + 1];
    }

    public Collection<MatrixElement> calculateMultipliedRow(SparseMatrix second, int i) {
        Map<Integer, MatrixElement> rowValues = new HashMap<>();
        for (int firstRowElementIndex = iArray[i]; firstRowElementIndex < iArray[i + 1]; firstRowElementIndex++) {
            int k = jArray[firstRowElementIndex];
            for (int secondRowElementIndex = second.iArray[k]; secondRowElementIndex < second.iArray[k + 1]; secondRowElementIndex++) {
                int j = second.jArray[secondRowElementIndex];
                int value = values[firstRowElementIndex] * second.values[secondRowElementIndex];
                if (rowValues.containsKey(j)) {
                    rowValues.get(j).changeValue(value);
                } else {
                    rowValues.put(j, new MatrixElement(i, j, value));
                }
            }
        }
        List<MatrixElement> result = new ArrayList<>(rowValues.values());
        Collections.sort(result);
        return result;
    }

    public Spliterator<Integer> spliterator() {
        return new SparseMatrixSpliterator();
    }

    private class SparseMatrixSpliterator implements Spliterator<Integer> {
        private final int endRowExclusive;
        private int currentRowIndex;
        private int currentColumnIndex;

        private SparseMatrixSpliterator() {
            this(0, numberOfRows, 0);
        }

        private SparseMatrixSpliterator(int startRowInclusive, int endRowExclusive, int currentColumnIndex) {
            this.currentRowIndex = startRowInclusive;
            this.currentColumnIndex = currentColumnIndex;
            this.endRowExclusive = endRowExclusive;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Integer> action) {
            boolean advance = currentRowIndex < endRowExclusive;
            if (advance) {
                action.accept(getValue(currentRowIndex, currentColumnIndex));
                currentColumnIndex++;
                if (currentColumnIndex == numberOfColumns) {
                    currentColumnIndex = 0;
                    currentRowIndex++;
                }
            }
            return advance;
        }

        @Override
        public Spliterator<Integer> trySplit() {
            if (endRowExclusive - currentRowIndex > 1) {
                int oldCurrentRowIndex = currentRowIndex;
                currentRowIndex = (currentRowIndex + endRowExclusive) / 2;
                currentColumnIndex = 0;
                return new SparseMatrixSpliterator(oldCurrentRowIndex, currentRowIndex, currentColumnIndex);
            } else {
                return null;
            }
        }

        @Override
        public long estimateSize() {
            return (endRowExclusive - currentRowIndex) * numberOfColumns - currentColumnIndex;
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SUBSIZED | Spliterator.SIZED | Spliterator.CONCURRENT
                    | Spliterator.IMMUTABLE;
        }
    }

    private class SparseMatrixCreator {
        private int[] tmpIArray;
        private int[] tmpJArray;
        private int[] tmpValues;
        private int currentRow = 0;
        private int currentColumn = 0;
        private int numberOfNotNullElementsInCurrentRow = 0;
        private int indexOfTriplet = 0;

        public SparseMatrixCreator() {
            tmpIArray = new int[numberOfRows + 1];
            tmpJArray = new int[DEFAULT_SIZE];
            tmpValues = new int[DEFAULT_SIZE];
        }

        private Triplet<int[]> createSparseMatrix(Iterator<Integer> iterator) {
            iterator.forEachRemaining(this::putMatrixElementIntoMatrix);
            fillLastRow();
            tmpJArray = Arrays.copyOf(tmpJArray, indexOfTriplet);
            tmpValues = Arrays.copyOf(tmpValues, indexOfTriplet);
            return new Triplet<>(tmpIArray, tmpJArray, tmpValues);
        }

        private void fillLastRow() {
            tmpIArray[currentRow + 1] = tmpIArray[currentRow] + numberOfNotNullElementsInCurrentRow;
        }

        private void putMatrixElementIntoMatrix(Integer value) {
            if (currentColumn == numberOfColumns) {
                if (numberOfNotNullElementsInCurrentRow == 0) {
                    tmpIArray[currentRow + 1] = tmpIArray[currentRow];
                } else {
                    tmpIArray[currentRow + 1] = tmpIArray[currentRow] + numberOfNotNullElementsInCurrentRow;
                    numberOfNotNullElementsInCurrentRow = 0;
                }
                currentRow++;
                currentColumn = 0;
            }

            if (value != 0) {
                assureCapacity();
                numberOfNotNullElementsInCurrentRow++;
                tmpJArray[indexOfTriplet] = currentColumn;
                tmpValues[indexOfTriplet++] = value;
            }
            currentColumn++;
        }

        private void assureCapacity() {
            if (indexOfTriplet + 1 == tmpValues.length) {
                int newSize = getNewSize(tmpValues.length);
                tmpValues = Arrays.copyOf(tmpValues, newSize);
                tmpJArray = Arrays.copyOf(tmpJArray, newSize);
            }
        }

        private int getNewSize(int length) {
            if (length == Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Current implementation of sparse matrix does not support keeping " +
                        "matrix with more non-zero elements then " + Integer.MAX_VALUE);
            } else if (length > Integer.MAX_VALUE / 2) {
                return Integer.MAX_VALUE;
            } else {
                return length * 2;
            }
        }
    }

    private class MultiplySparseMatrixCreator {
        private int[] tmpIArray;
        private int[] tmpJArray;
        private int[] tmpValues;
        private int currentRow = 0;
        private int numberOfElementsInCurrentRow = 0;
        private int indexOfTriplet = 0;

        public MultiplySparseMatrixCreator(int numberOfRows) {
            tmpIArray = new int[numberOfRows + 1];
            tmpJArray = new int[DEFAULT_SIZE];
            tmpValues = new int[DEFAULT_SIZE];
        }

        private Triplet<int[]> createSparseMatrix(Stream<MatrixElement> stream) {
            stream
                    .filter(matrixElement -> matrixElement.getValue() != 0)
                    .forEach(this::putMatrixElementIntoMatrix);
            fillLastElementsOfIArray();
            tmpJArray = Arrays.copyOf(tmpJArray, indexOfTriplet);
            tmpValues = Arrays.copyOf(tmpValues, indexOfTriplet);
            return new Triplet<>(tmpIArray, tmpJArray, tmpValues);
        }

        private void fillLastElementsOfIArray() {
            tmpIArray[currentRow + 1] = tmpIArray[currentRow] + numberOfElementsInCurrentRow;
            for (int i = currentRow + 2; i < tmpIArray.length; i++) {
                tmpIArray[i] = tmpIArray[currentRow + 1];
            }
        }

        private void putMatrixElementIntoMatrix(MatrixElement matrixElement) {
            if (matrixElement.getRow() > numberOfRows - 1) {
                throw new IllegalArgumentException("Stream contains matrix element which row index " +
                        "is equal or higher then matrix number of rows.");
            }
            if (matrixElement.getColumn() > numberOfColumns - 1) {
                throw new IllegalArgumentException("Stream contains matrix element which column index " +
                        "is equal or higher then matrix number of columns.");
            }
            if (matrixElement.getRow() == currentRow) {
                numberOfElementsInCurrentRow++;
            } else if (matrixElement.getRow() > currentRow) {
                tmpIArray[currentRow + 1] = tmpIArray[currentRow] + numberOfElementsInCurrentRow;
                for (int i = currentRow + 2; i <= matrixElement.getRow(); i++) {
                    tmpIArray[i] = tmpIArray[currentRow + 1];
                }
                numberOfElementsInCurrentRow = 1;
                currentRow = matrixElement.getRow();
            } else {
                throw new IllegalArgumentException("Row index of matrix elements in the stream must obey ascending order.");
            }
            assureCapacity();
            tmpJArray[indexOfTriplet] = matrixElement.getColumn();
            tmpValues[indexOfTriplet++] = matrixElement.getValue();
        }

        private void assureCapacity() {
            if (indexOfTriplet + 1 == tmpValues.length) {
                int newSize = getNewSize(tmpValues.length);
                tmpValues = Arrays.copyOf(tmpValues, newSize);
                tmpJArray = Arrays.copyOf(tmpJArray, newSize);
            }
        }

        private int getNewSize(int length) {
            if (length == Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Current implementation of sparse matrix does not support keeping " +
                        "matrix with more non-zero elements then " + Integer.MAX_VALUE);
            } else if (length > Integer.MAX_VALUE / 2) {
                return Integer.MAX_VALUE;
            } else {
                return length * 2;
            }
        }
    }

    private class Triplet<T> {
        private final T first;
        private final T second;
        private final T third;

        Triplet(T first, T second, T third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }
}
