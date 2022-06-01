package pdp.tudor;


import java.util.*;
import java.util.stream.IntStream;

public class Main {
    private static final List<Integer> A = new LinkedList<>(Arrays.asList(1, 1, 1, 1, 1, 1, 1));
    private static final List<Integer> B = new LinkedList<>(Arrays.asList(1, 1, 1, 1, 1, 1, 1));

    private static final boolean DEBUG = true;
    private static final int SIZE_BIG = 1000;
    private static final int MAX = 100;
    private static final List<Integer> A_BIG = new LinkedList<>();
    private static final List<Integer> B_BIG = new LinkedList<>();

    private static void populateBIG() {
        Random random = new Random();
        IntStream.range(0, SIZE_BIG).forEach(
                index -> {
                    A_BIG.add(Math.abs(random.nextInt()) % MAX);
                    B_BIG.add(Math.abs(random.nextInt()) % MAX);
                }
        );
    }

    private static void testSMALL() {
        int sum = 0;
        for (int i = 0; i < A.size(); i++) {
            sum += A.get(i) * B.get(i);
        }
        System.out.println("Sum is -> " + sum);

        Buffer buffer = new Buffer(DEBUG);
        Producer producer = new Producer(buffer, A, B, DEBUG);
        Consumer consumer = new Consumer(buffer, A.size(), DEBUG);
        producer.start();
        consumer.start();
    }

    private static void testBIG() {
        populateBIG();
        int sum = 0;
        for (int i = 0; i < A_BIG.size(); i++) {
            sum += A_BIG.get(i) * B_BIG.get(i);
        }
        System.out.println("Sum is -> " + sum);

        Buffer buffer = new Buffer(DEBUG);
        Producer producer = new Producer(buffer, A_BIG, B_BIG, DEBUG);
        Consumer consumer = new Consumer(buffer, A_BIG.size(), DEBUG);
        producer.start();
        consumer.start();
    }

    public static void main(String[] args) {
        testSMALL();
//        testBIG();
    }
}
