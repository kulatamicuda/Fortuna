package se.grunka.fortuna.accumulator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import se.grunka.fortuna.Pool;

public class Accumulator implements AutoCloseable {
    private final Map<Integer, Context> eventContexts = new ConcurrentHashMap<Integer, Context>();
    private final AtomicInteger sourceCount = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        private final ThreadFactory delegate = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = delegate.newThread(r);
            thread.setDaemon(true);
            return thread;
        }
    });
    private final Pool[] pools;

    public Accumulator(Pool[] pools) {
        this.pools = pools;
    }

    public void addSource(EntropySource entropySource) {
        int sourceId = sourceCount.getAndIncrement();
        EventAdder eventAdder = new EventAdderImpl(sourceId, pools);
        EventScheduler eventScheduler = new EventSchedulerImpl(sourceId, eventContexts, scheduler);
        Context context = new Context(entropySource, eventAdder, eventScheduler);
        eventContexts.put(sourceId, context);
        eventScheduler.schedule(0, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void close() {
        shutdownAndAwaitTermination(scheduler);
    }
    
        private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                pool.awaitTermination(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }


}
