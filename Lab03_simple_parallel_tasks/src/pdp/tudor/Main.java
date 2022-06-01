package pdp.tudor;

import pdp.tudor.computation.ThreadManager;
import pdp.tudor.domain.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int aRowCount = 2000;
    private static final int aColCount = 2000;
    private static final int bRowCount = 2000;
    private static final int bColCount = 2000;

    private static final int numberOfThreads = 4;
    private static final boolean useThreadPoolApproach = false;
    private static final String computationType = "Kth";

    public static void runThreadListApproach(Matrix a, Matrix b, Matrix c) throws Exception {
        List<Thread> threadsList = new ArrayList<>();

        switch (computationType) {
            case "Row":
                for (int i = 0; i < numberOfThreads; i++)
                    threadsList.add(ThreadManager.initRowThread(i, a, b, c, numberOfThreads));
                break;
            case "Column":
                for (int i = 0; i < numberOfThreads; i++)
                    threadsList.add(ThreadManager.initColThread(i, a, b, c, numberOfThreads));
                break;
            case "Kth":
                for (int i = 0; i < numberOfThreads; i++)
                    threadsList.add(ThreadManager.initKThread(i, a, b, c, numberOfThreads));
                break;
            default:
                throw new Exception("Invalid FUNCTION");
        }

        for (Thread thread : threadsList) {
            thread.start();
        }

        for (Thread thread : threadsList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Final Matrix\n" + c.toString());
    }

    public static void runThreadPoolApproach(Matrix a, Matrix b, Matrix c) throws Exception {
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        switch (computationType) {
            case "Row":
                for (int i=0;i<numberOfThreads;i++)
                    service.submit(ThreadManager.initRowThread(i, a, b, c, numberOfThreads));
                break;
            case "Column":
                for (int i=0;i<numberOfThreads;i++)
                    service.submit(ThreadManager.initColThread(i, a, b, c, numberOfThreads));
                break;
            case "Kth":
                for (int i=0;i<numberOfThreads;i++)
                    service.submit(ThreadManager.initKThread(i, a, b, c, numberOfThreads));
                break;
            default:
                throw new Exception("Invalid strategy");
        }

        service.shutdown();
        try {
            if (!service.awaitTermination(300, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
            System.out.println("result:\n" + c.toString());
        } catch (InterruptedException ex) {
            service.shutdownNow();
            ex.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {

        Matrix a = new Matrix(aRowCount, aColCount);
        Matrix b = new Matrix(bRowCount, bColCount);

        System.out.println(a);
        System.out.println(b);

        boolean isMultiplicationPossible = a.numberOfColumns == b.numberOfRows;
        if (isMultiplicationPossible) {
            float start = System.nanoTime();
            Matrix result = new Matrix(a.numberOfRows, b.numberOfColumns);
            if (useThreadPoolApproach) {
                try {
                    runThreadPoolApproach(a, b, result);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            } else {
                try {
                    runThreadListApproach(a, b, result);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }

            System.out.println("Time elapsed: " + (System.nanoTime() - start)/1_000_000_000.0 + " seconds");
        } else {
            System.err.println("Matrix multiplication impossible");
        }
    }
}
