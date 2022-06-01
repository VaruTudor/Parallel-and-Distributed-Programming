package pdp.tudor;

import java.util.List;

public class Producer extends Thread {
    public Buffer buffer;
    public List<Integer> A, B;
    private final boolean debug;

    public Producer(Buffer buffer, List<Integer> A, List<Integer> B, boolean debug) {
        this.buffer = buffer;
        this.A = A;
        this.B = B;
        this.debug = debug;
    }

    @Override
    public void run() {
        for (int i = 0; i < A.size(); i++) {
            try {
                if (debug)
                    System.out.printf("Producer - Computed Product - %d * %d = %d\n", A.get(i), B.get(i), A.get(i) * B.get(i));

                buffer.put(A.get(i) * B.get(i));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
