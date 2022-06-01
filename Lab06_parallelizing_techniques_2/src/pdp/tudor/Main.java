package pdp.tudor;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    private static final int NR_THREADS = 5;
    private static final int NR_VERTICES = 50;
    private static final Graph INPUT_GRAPH = Graph.generateGraph(NR_VERTICES, true);

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.nanoTime();
        ExecutorService threadPool = Executors.newFixedThreadPool(NR_THREADS);

        for (int currentNode = 0; currentNode < INPUT_GRAPH.size(); currentNode++) {
            threadPool.submit(new CycleManager(INPUT_GRAPH, currentNode, new ArrayList<>(INPUT_GRAPH.size()), new AtomicBoolean(false)));
        }

        threadPool.shutdown();
        threadPool.awaitTermination(10, TimeUnit.SECONDS);
        long duration = (System.nanoTime() - startTime) / 1000000;
        System.out.println(duration + " ms");
    }
}
