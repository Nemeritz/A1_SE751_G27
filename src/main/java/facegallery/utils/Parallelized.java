package facegallery.utils;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Parallelized<I, R> {
    protected List<I> inputs;
    protected ConcurrentLinkedQueue<Integer> processed = new ConcurrentLinkedQueue<>();

    @Nullable
    protected Parallelized(List<I> inputs) {
        if (inputs != null) {
            this.inputs = inputs;
        }
    }

    public ConcurrentLinkedQueue<Integer> getProcessed() {
        return processed;
    }

    public abstract List<R> runSequential();

    public abstract boolean runParallel(List<R> results, List<AtomicBoolean> ready);

    public abstract List<R> createResultsContainer();

    public abstract List<AtomicBoolean> createReadyContainer();
}
