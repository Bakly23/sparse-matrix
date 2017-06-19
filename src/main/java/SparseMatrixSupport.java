import java.util.stream.Stream;

public interface SparseMatrixSupport<M> {

	/**
	 * Converts a given matrix into a stream of integers.
	 * @param matrix matrix representation
	 * @return a stream of integers, where the first element is a number of rows, the second is a number of columns. 
	 */
	Stream<Integer> toStream(M matrix);

	/**
	 * Converts a given stream of integers into a matrix.
	 * @param stream a stream of integers like in the corresponding {@link SparseMatrixSupport#toStream} method
	 * @return matrix representation
	 */
	M fromStream(Stream<Integer> stream);

	/**
	 * Multiplies matrices. Does not modify source matrices.
	 * @param first the first matrix
	 * @param second the second matrix
	 */
	M multiply(M first, M second);
}