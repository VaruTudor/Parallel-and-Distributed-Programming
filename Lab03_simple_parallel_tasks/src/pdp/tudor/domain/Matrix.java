package pdp.tudor.domain;

import java.util.Random;
import java.util.stream.IntStream;

public class Matrix {
    public final int numberOfRows, numberOfColumns;
    public int[][] elements;

    public Matrix(int numberOfRows, int numberOfColumns) {
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        this.elements = new int[numberOfRows][numberOfColumns];
        populateMatrix();
    }

    private void populateMatrix() {
        Random random = new Random();
        IntStream.range(0, numberOfRows).forEach(i ->
                IntStream.range(0, numberOfColumns).forEach(j -> elements[i][j] = random.nextInt(100) + 1));
    }

    public int getElement(int row, int column) {
        return elements[row][column];
    }

    public void setElement(int row, int column, int value) {
        elements[row][column] = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        IntStream.range(0, numberOfRows).forEach(i -> {
            IntStream.range(0, numberOfColumns).forEach(j -> sb.append(elements[i][j]).append(" "));
            sb.append("\n");
        });
        return sb.toString();
    }
}
