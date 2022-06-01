package pdp.tudor.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@SuppressWarnings("DuplicatedCode")
public class Operation {
    public static final int NR_THREADS = 5;
    private static final int MAX_DEPTH = 4;

    /**
     * Polynomial multiplication without using threads or any algorithm.
     */
    public static Polynomial simpleSequential(Polynomial first, Polynomial second) {
        int sizeOfResultCoefficientList = first.degree + second.degree + 1;
        List<Integer> coefficients = new ArrayList<>();
        IntStream.range(0, sizeOfResultCoefficientList).forEach(i -> coefficients.add(0));

        for (int i = 0; i < first.getLength(); i++) {
            for (int j = 0; j < second.getLength(); j++) {
                int index = i + j;
                int value = first.coefficients.get(i) * second.coefficients.get(j);
                coefficients.set(index, coefficients.get(index) + value);
            }
        }
        return new Polynomial(coefficients);
    }

    /**
     * Polynomial multiplication without using threads or any algorithm.
     */
    public static Polynomial simpleThreaded(Polynomial first, Polynomial second) throws InterruptedException {
        int sizeOfResultCoefficientList = first.degree + second.degree + 1;
        List<Integer> coefficients = new ArrayList<>();
        IntStream.range(0, sizeOfResultCoefficientList).forEach(i -> coefficients.add(0));

        Polynomial result = new Polynomial(coefficients);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NR_THREADS);

        // find how many computations for each thread
        int step = result.getLength() / NR_THREADS;
        if (step == 0) step = 1;

        for (int i = 0; i < result.getLength(); i += step) {
            Task task = new Task(i, i + step, first, second, result);
            executor.execute(task);
        }

        executor.shutdown();
        executor.awaitTermination(50, TimeUnit.SECONDS);

        return result;
    }

    public static Polynomial karatsubaSequential(Polynomial first, Polynomial second) {
        if (first.degree < 2 || second.degree < 2) {
            return simpleSequential(first, second);
        }

        int len = Math.max(first.degree, second.degree) / 2;
        Polynomial lowP1 = new Polynomial(first.coefficients.subList(0, len));
        Polynomial highP1 = new Polynomial(first.coefficients.subList(len, first.getLength()));
        Polynomial lowP2 = new Polynomial(second.coefficients.subList(0, len));
        Polynomial highP2 = new Polynomial(second.coefficients.subList(len, second.getLength()));

        Polynomial z1 = karatsubaSequential(lowP1, lowP2);
        Polynomial z2 = karatsubaSequential(Polynomial.add(lowP1, highP1), Polynomial.add(lowP2, highP2));
        Polynomial z3 = karatsubaSequential(highP1, highP2);

        //calculate the final result
        Polynomial r1 = Polynomial.addZeros(z3, 2 * len);
        Polynomial r2 = Polynomial.addZeros(Polynomial.subtract(Polynomial.subtract(z2, z3), z1), len);
        return Polynomial.add(Polynomial.add(r1, r2), z1);
    }

    public static Polynomial karatsubaThreaded(Polynomial first, Polynomial second, int currentDepth) throws ExecutionException, InterruptedException {
        if (currentDepth > MAX_DEPTH) {
            return karatsubaSequential(first, second);
        }

        if (first.degree < 2 || second.degree < 2) {
            return karatsubaSequential(first, second);
        }

        int len = Math.max(first.degree, second.degree) / 2;
        Polynomial lowP1 = new Polynomial(first.coefficients.subList(0, len));
        Polynomial highP1 = new Polynomial(first.coefficients.subList(len, first.getLength()));
        Polynomial lowP2 = new Polynomial(second.coefficients.subList(0, len));
        Polynomial highP2 = new Polynomial(second.coefficients.subList(len, second.getLength()));

        ExecutorService executor = Executors.newFixedThreadPool(NR_THREADS);
        Future<Polynomial> f1 = executor.submit(() -> karatsubaThreaded(lowP1, lowP2, currentDepth + 1));
        Future<Polynomial> f2 = executor.submit(() -> karatsubaThreaded(Polynomial.add(lowP1, highP1), Polynomial
                .add(lowP2, highP2), currentDepth + 1));
        Future<Polynomial> f3 = executor.submit(() -> karatsubaThreaded(highP1, highP2, currentDepth + 1));

        executor.shutdown();

        Polynomial z1 = f1.get();
        Polynomial z2 = f2.get();
        Polynomial z3 = f3.get();

        executor.awaitTermination(60, TimeUnit.SECONDS);

        //calculate the final result
        Polynomial r1 = Polynomial.addZeros(z3, 2 * len);
        Polynomial r2 = Polynomial.addZeros(Polynomial.subtract(Polynomial.subtract(z2, z3), z1), len);
        return Polynomial.add(Polynomial.add(r1, r2), z1);
    }

}
