Implemetation of sparse matrix. It uses "Compressed sparse row"(https://en.wikipedia.org/wiki/Sparse_matrix#Compressed_sparse_row_(CSR,_CRS_or_Yale_format)) format to keep data.
<br>
Code is stored in default package because of demands from test system that was testing this code.
It has 2 constructors: first for ordered stream of matrix values from top left corner to bottom right corner, 
other one for ordered stream of not-empty matrix elements(it is used as a way to create matrix resulting from multiplication).
SparseMatrixSupport interface was implemented. Matrix creation from stream is non-concurrent and matrix transforming to stream is concurrent.
Matrix multiplication is also concurrent and you can define number of the threads used for multiplication.
