package pdp.tudor.computation;

import pdp.tudor.domain.Matrix;

public class ThreadManager {
    /**
     * Computes the element which will be in the ith row and the jth column in the result
     * @param i - current row
     * @param j - current column
     */
    public static int buildElement(Matrix a, Matrix b, int i, int j) throws Exception {
        if (i < a.numberOfRows && j < b.numberOfColumns) {
            int element = 0;
            for (int k = 0; k < a.numberOfColumns; k++) {
                element += a.getElement(i, k) * b.getElement(k, j);
            }
            return element;
        } else
            throw new Exception("Row/column out of bounds!");
    }

    public static MatrixThread initRowThread(int index, Matrix a, Matrix b, Matrix c, int noThreads) {
        int resultSize = c.numberOfRows * c.numberOfColumns;
        int count = resultSize / noThreads;

        int iStart = count * index / c.numberOfColumns;
        int jStart = count * index % c.numberOfColumns;

        if (index == noThreads - 1)
            count += resultSize % noThreads;

        return new RowThread(iStart, jStart, count, a, b, c);
    }

    public static MatrixThread initColThread(int index, Matrix a, Matrix b, Matrix c, int noThreads) {
        int resultSize = c.numberOfRows * c.numberOfColumns;
        int count = resultSize / noThreads;

        int iStart = count * index % c.numberOfRows;
        int jStart = count * index / c.numberOfRows;

        if (index == noThreads - 1)
            count += resultSize % noThreads;

        return new ColumnThread(iStart, jStart, count, a, b, c);
    }

    public static MatrixThread initKThread(int index, Matrix a, Matrix b, Matrix c, int noThreads) {
        int resultSize = c.numberOfRows * c.numberOfColumns;
        int count = resultSize / noThreads;

        if (index < resultSize % noThreads)
            count++;

        int iStart = index / c.numberOfColumns;
        int jStart = index % c.numberOfColumns;
        return new KThread(iStart, jStart, count, noThreads, a, b, c);
    }
}
