package pdp.tudor.domain;

public class Task implements Runnable {
    private final int start;
    private final int end;
    private final Polynomial first;
    private final Polynomial second;
    private final Polynomial result;

    public Task(int start, int end, Polynomial first, Polynomial second, Polynomial result) {
        this.start = start;
        this.end = end;
        this.first = first;
        this.second = second;
        this.result = result;
    }

    @Override
    public void run() {
        for (int index = start; index < end; index++) {
            // no more elements to calculate
            if (index > result.getLength()) {
                return;
            }
            // find all the pairs that we add to obtain the value of a result coefficient
            for (int j = 0; j <= index; j++) {
                if (j < first.getLength() && (index - j) < second.getLength()) {
                    int value = first.coefficients.get(j) * second.coefficients.get(index - j);
                    result.coefficients.set(index, result.coefficients.get(index) + value);
                }
            }
        }
    }
}
