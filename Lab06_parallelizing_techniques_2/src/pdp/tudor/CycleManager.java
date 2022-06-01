package pdp.tudor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CycleManager implements Runnable{
    private final Graph graph;
    private final int startingNode;
    private final List<Integer> path;
    private final Lock lock;
    private final List<Integer> result;
    private final AtomicBoolean foundCycle;

    CycleManager(Graph graph, int node, List<Integer> result, AtomicBoolean foundCycle) {
        this.graph = graph;
        this.startingNode = node;
        this.path = new ArrayList<>();
        this.lock = new ReentrantLock();
        this.foundCycle = foundCycle;
        this.result = result;
    }

    @Override
    public void run() {
        visit(startingNode);
    }

    private void visit(int node) {
        // add the starting node to the path
        path.add(node);
        if (!foundCycle.get()){
            // as long as no cycle was found
            if (path.size() == graph.size()) {
                // stop condition
                if (graph.neighboursOf(node).contains(startingNode)){
                    // cycle found
                    foundCycle.set(true);
                    this.lock.lock();
                    result.clear();
                    result.addAll(this.path);
                    if(!result.isEmpty()){
                        System.out.println(result);
//                        System.out.println("Found hamiltonian cycle");
                    }
                    this.lock.unlock();
                }
                return;
            }

            graph.neighboursOf(node).forEach(neighbour->{
                if (!this.path.contains(neighbour)){
                    visit(neighbour);
                }
            });
        }
    }
}
