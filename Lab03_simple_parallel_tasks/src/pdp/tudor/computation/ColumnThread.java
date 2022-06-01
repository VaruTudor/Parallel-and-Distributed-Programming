package pdp.tudor.computation;

import pdp.tudor.domain.Matrix;
import pdp.tudor.domain.Pair;

public class ColumnThread extends MatrixThread {
    public ColumnThread(int iStart, int jStart, int count, Matrix a, Matrix b, Matrix result) {
        super(iStart, jStart, count, a, b, result);

    }

    public void computeElements() {
        int i = iStart, j = jStart;
        int size = sizeOfTask;
        while (size > 0 && i < result.numberOfRows && j < result.numberOfColumns) {
            pairs.add(new Pair<>(i, j));
            i++;
            size--;
            if (i == result.numberOfColumns) {
                i = 0;
                j++;
            }
        }
    }

}
