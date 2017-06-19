Sparse Matrix was implemented. It uses "Compressed sparse row" format to keep data.
It has 2 constructors: first for ordered stream of matrix values from top left corner to bottom right corner, 
other one for ordered stream of not-empty matrix elements(it is used as a way to create matrix resulting from multiplication).
SparseMatrixSupport interface was implemented. Matrix creation from stream is non-concurrent and matrix transforming to stream is concurrent.
Matrix multiplication is also concurrent and you can define number of the threads used for multiplication.
