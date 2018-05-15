package facegallery.utils;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Parallelized<I, R> {
    protected List<I> inputs;
    protected ConcurrentLinkedQueue<Integer> processed = new ConcurrentLinkedQueue<>();

    protected Parallelized(List<I> inputs) {
        if (inputs != null) {
            this.inputs = inputs;
        }
    }

    public ConcurrentLinkedQueue<Integer> getProcessed() {
        return processed;
    }

    public abstract List<R> runSequential();

    public abstract boolean runParallel(List<R> results);

    public abstract List<R> createResultsContainer();
}
