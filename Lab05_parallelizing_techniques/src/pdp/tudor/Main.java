package pdp.tudor;

import pdp.tudor.domain.Operation;
import pdp.tudor.domain.Polynomial;

import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException{
        Polynomial first = new Polynomial(2);
        Polynomial second = new Polynomial(2);

        System.out.println("First:" + first);
        System.out.println("Second:" + second);
        System.out.println("\n");

        System.out.println(simpleSequential(first, second).toString() + "\n");
        System.out.println(simpleThreaded(first, second).toString() + "\n");
        System.out.println(karatsubaSequential(first, second).toString() + "\n");
        System.out.println(karatsubaThreaded(first, second).toString() + "\n");
    }

    private static Polynomial simpleSequential(Polynomial p, Polynomial q) {
        long startTime = System.currentTimeMillis();
        Polynomial result1 = Operation.simpleSequential(p, q);
        long endTime = System.currentTimeMillis();
        System.out.println("Simple sequential multiplication");
        System.out.println("time: " + (endTime - startTime) + " ms");
        return result1;
    }

    private static Polynomial simpleThreaded(Polynomial p, Polynomial q) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        Polynomial result2 = Operation.simpleThreaded(p, q);
        long endTime = System.currentTimeMillis();
        System.out.println("Simple parallel multiplication");
        System.out.println("time: " + (endTime - startTime) + " ms");
        return result2;
    }

    private static Polynomial karatsubaSequential(Polynomial p, Polynomial q) {
        long startTime = System.currentTimeMillis();
        Polynomial result3 = Operation.karatsubaSequential(p, q);
        long endTime = System.currentTimeMillis();
        System.out.println("Karatsuba sequential multiplication");
        System.out.println("time: " + (endTime - startTime) + " ms");
        return result3;
    }

    private static Polynomial karatsubaThreaded(Polynomial p, Polynomial q) throws ExecutionException,
            InterruptedException {
        long startTime = System.currentTimeMillis();
        Polynomial result4 = Operation.karatsubaThreaded(p, q, 1);
        long endTime = System.currentTimeMillis();
        System.out.println("Karatsuba parallel multiplication");
        System.out.println("time: " + (endTime - startTime) + " ms");
        return result4;
    }

}
