package pdp.tudor.computation;

import pdp.tudor.domain.Matrix;
import pdp.tudor.domain.Pair;

public class KThread extends MatrixThread{
    public KThread(int iStart, int jStart, int count, int K, Matrix a, Matrix b, Matrix result) {
        super(iStart, jStart, count, a, b, result, K);
    }

    public void computeElements() {
        int i = iStart, j = jStart;
        int size = sizeOfTask;
        while (size > 0 && i < result.numberOfRows) {
            pairs.add(new Pair<>(i, j));
            size--;
            i += (j + k) / result.numberOfColumns;
            j = (j + k) % result.numberOfRows;
        }
    }
}
