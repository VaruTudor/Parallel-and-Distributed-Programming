package pdp.tudor.computation;

import pdp.tudor.domain.Matrix;
import pdp.tudor.domain.Pair;

public class RowThread extends MatrixThread{
    public RowThread(int iStart, int jStart, int count, Matrix a, Matrix b, Matrix result) {
        super(iStart, jStart, count, a, b, result);

    }

    public void computeElements() {
        int i = iStart, j = jStart;
        int size = sizeOfTask;
        while (size > 0 && i < result.numberOfRows && j<result.numberOfColumns) {
            pairs.add(new Pair<>(i, j));
            j++;
            size--;
            if (j == result.numberOfRows) {
                j = 0;
                i++;
            }
        }
    }
}
