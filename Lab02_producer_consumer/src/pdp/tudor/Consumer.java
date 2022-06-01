package pdp.tudor;

public class Consumer extends Thread {
    private final boolean debug;
    private int sum = 0;
    private final int length;
    public Buffer buffer;

    public Consumer(Buffer buffer, int length, boolean debug) {
        this.buffer = buffer;
        this.length = length;
        this.debug = debug;
    }

    @Override
    public void run() {
        for (int i = 0; i < this.length; i++) {
            try {
                sum += buffer.get();
                if (debug)
                    System.out.println("Consumer - Sum " + sum);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Consumer - Sum is -> " + sum);
    }
}
