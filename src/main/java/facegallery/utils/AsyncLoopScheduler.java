package facegallery.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class AsyncLoopScheduler {
    int globalStart;
    int globalEnd;
    int workItems;
    int threads;
    int chunkSize;
    int extra;
    AtomicInteger requestCount = new AtomicInteger(0);

    public AsyncLoopScheduler(int globalStart, int globalEnd, int threads) {
        this.globalStart = globalStart;
        this.globalEnd = globalEnd;
        this.workItems = Math.abs(globalEnd - globalStart);
        this.threads = threads;
        chunkSize = workItems / threads;
        extra = workItems % threads;
    }

    public AsyncLoopRange requestLoopRange() {
        int localStart;
        int localEnd;

        int requestCount = this.requestCount.getAndAdd(1);

        if (requestCount < threads) {
            localStart = globalStart;
            if (requestCount < extra) {
                localStart += (chunkSize + 1) * requestCount;
                localEnd = localStart + chunkSize + 1;
            } else {
                localStart += (chunkSize + 1) * extra + chunkSize * requestCount - extra;
                localEnd = localStart + chunkSize;
            }
        }
        else {
            localStart = 0;
            localEnd = 0;
        }

        return new AsyncLoopRange(localStart, localEnd);
    }
}
