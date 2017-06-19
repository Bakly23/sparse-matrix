import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SparseMatrixSupportImpl implements SparseMatrixSupport<SparseMatrix> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SparseMatrixSupportImpl.class);

    //number of threads used for multiplying matrixes
    private final int defaultNumberOfThreads;

    public SparseMatrixSupportImpl() {
        this(8);
    }

    public SparseMatrixSupportImpl(int numberOfThreads) {
        this.defaultNumberOfThreads = numberOfThreads;
    }


    @Override
    //returned stream is parallel
    public Stream<Integer> toStream(SparseMatrix matrix) {
        return Stream.concat(Stream.of(matrix.getNumberOfRows(), matrix.getNumberOfColumns()),
                StreamSupport.stream(matrix.spliterator(), true));
    }

    @Override
    //generation of matrix from stream is sequential
    public SparseMatrix fromStream(Stream<Integer> stream) {
        return new SparseMatrix(stream);
    }

    @Override
    //multipling of matrix is parallel
    public SparseMatrix multiply(SparseMatrix first, SparseMatrix second) {
        if (first.getNumberOfColumns() != second.getNumberOfRows()) {
            throw new RuntimeException("Number of columns of the first matrix must be equal to number of rows of the second.");
        }

        LOGGER.info("Calculation of resulting multiply matrix has been started.");
        SparseMatrix resultMatrix = new SparseMatrix(first.getNumberOfRows(), second.getNumberOfColumns(),
                concurrentlyMultiply(first, second));
        LOGGER.info("Calculation of resulting multiply matrix has been finished.");


        return resultMatrix;
    }

    private Stream<MatrixElement> concurrentlyMultiply(SparseMatrix first, SparseMatrix second) {
        int numberOfThreads = defaultNumberOfThreads > first.getNumberOfRows() ? first.getNumberOfRows()
                : defaultNumberOfThreads;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        return IntStream.range(0, numberOfThreads)
                .mapToObj(i -> submitTask(executorService, i, first, second, numberOfThreads))
                .collect(Collectors.toList())
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException("Error occurred during calculation of matrix rows.", e);
                    }
                })
                .flatMap(List::stream);
    }

    private Future<List<MatrixElement>> submitTask(ExecutorService executorService, int i, SparseMatrix first,
                                                   SparseMatrix second, int numberOfThreads) {
        return executorService.submit(() -> calculateRows(calcStartRow(i, first.getNumberOfRows(), numberOfThreads),
                calcEndRow(i, first.getNumberOfRows(), numberOfThreads), first, second));
    }

    private int calcEndRow(int i, int numberOfRows, int numberOfThreads) {
        return i == numberOfThreads - 1 ? numberOfRows : calcStartRow(i + 1, numberOfRows, numberOfThreads);
    }

    private int calcStartRow(int i, int numberOfRows, int numberOfThreads) {
        return numberOfRows / numberOfThreads * i;
    }

    private List<MatrixElement> calculateRows(int startRowInclusive, int endRowExclusive, SparseMatrix first,
                                              SparseMatrix second) {
        List<MatrixElement> result = new ArrayList<>((endRowExclusive - startRowInclusive) * second.getNumberOfRows());
        LOGGER.info("Started to calculate rows from " + startRowInclusive + " to " + endRowExclusive);
        for (int i = startRowInclusive; i < endRowExclusive; i++) {
            if (first.containsRow(i)) {
                result.addAll(first.calculateMultipliedRow(second, i));
            }
        }
        LOGGER.info("Finished to calculate rows from " + startRowInclusive + " to " + endRowExclusive);
        return result;
    }
}
