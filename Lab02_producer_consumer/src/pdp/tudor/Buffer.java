package pdp.tudor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer {
    private final boolean debug;

    public Buffer(boolean debug) {
        this.debug = debug;
    }

    private static final int CAPACITY = 1;
    private final Queue<Integer> queue = new LinkedList<>();

    private final Lock lock = new ReentrantLock();
    private final Condition conditionalVariable = lock.newCondition();

    public void put(int product) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == CAPACITY) {
                if (debug)
                    System.out.println("Producer - waiting...");
                conditionalVariable.await();
            }

            queue.add(product);
            if (debug)
                System.out.println("Producer - sent product " + product);
            conditionalVariable.signal();

        } finally {
            lock.unlock();
        }
    }

    public int get() throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == 0) {
                if (debug)
                    System.out.println("Consumer - waiting...");
                conditionalVariable.await();
            }

            Integer value = queue.poll();
            if (value != null) {
                if (debug)
                    System.out.println("Consumer - received " + value);
                conditionalVariable.signal();
            }
            return value;
        } finally {
            lock.unlock();
        }
    }
}
