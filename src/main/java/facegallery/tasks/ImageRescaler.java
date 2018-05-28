package facegallery.tasks;

import apt.annotations.Future;
import apt.annotations.Task;
import apt.annotations.TaskInfoType;
import com.jhlabs.image.LensBlurFilter;
import facegallery.utils.AsyncLoopRange;
import facegallery.utils.AsyncLoopScheduler;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageRescaler {
    private BufferedImage[] images;
    private Boolean[] hasFace;
    private BufferedImage[] rescaled;
    private RescaleOp rescaleOp = new RescaleOp(0.3f, 0.0f, null);
    private LensBlurFilter blurFilter = new LensBlurFilter();

    public ImageRescaler(BufferedImage[] images, Boolean[] hasFace) {
        rescaled = new BufferedImage[images.length];
        this.images = images;
        this.hasFace = hasFace;
    }

    public BufferedImage[] getImages() {
        return images;
    }

    public BufferedImage[] getRescaled() {
        return rescaled;
    }

    public BufferedImage[] run() {
        for (int i = 0; i < images.length; i++) {
            if (images[i] != null) {
                rescaled[i] = rescale(images[i], hasFace[i]);
            }
            else {
                rescaled[i] = null;
            }
        }

        return rescaled;
    }

    public BufferedImage[] run(BlockingQueue<Integer> readyQueue) {
        for (int i = 0; i < images.length; i++) {
            if (images[i] != null) {
                rescaled[i] = rescale(images[i], hasFace[i]);
            }
            else {
                rescaled[i] = null;
            }

            readyQueue.offer(i);
        }

        return rescaled;
    }

    public BlockingQueue<Integer> runAsync() {
        BlockingQueue<Integer> readyQueue = new LinkedBlockingQueue<>();

        AsyncLoopScheduler scheduler = new AsyncLoopScheduler(0, images.length, 8);

        @Future(taskType = TaskInfoType.MULTI, taskCount = 8, reduction = "AND")
        Boolean sync = asyncWorker(scheduler, readyQueue);

        return readyQueue;
    }

    public Boolean runAsync(Void wait) {
        AsyncLoopScheduler scheduler = new AsyncLoopScheduler(0, images.length, 8);

        @Future(taskType = TaskInfoType.MULTI, taskCount = 8, reduction = "AND")
        Boolean sync = asyncWorker(scheduler);

        return sync;
    }

    public Void runAsyncPipeline(BlockingQueue<Integer> thumbnailReady, BlockingQueue<Integer> faceDetectReady, BlockingQueue<Integer> readyQueue) {
        AtomicBoolean[] sync = new AtomicBoolean[images.length];
        for (int i = 0; i < images.length; i++) {
            sync[i] = new AtomicBoolean(false);
        }

        AsyncLoopScheduler scheduler = new AsyncLoopScheduler(0, images.length, 8);

        BlockingQueue<Integer> inputReady = new LinkedBlockingQueue<>();
        @Future
        Boolean s1 = asyncPipelineSynch(thumbnailReady, sync, inputReady);

        @Future
        Boolean s2 = asyncPipelineSynch(faceDetectReady, sync, inputReady);

        @Future(taskType = TaskInfoType.MULTI, taskCount = 8, reduction = "AND")
        Boolean s3 = asyncPipelineWorker(scheduler, inputReady, readyQueue);

        return null;
    }

    @Task
    private Boolean asyncWorker(AsyncLoopScheduler scheduler) {
        AsyncLoopRange range = scheduler.requestLoopRange();
        for (int i = range.loopStart; i < range.loopEnd; i += 1) {
            rescaled[i] = rescale(images[i], hasFace[i]);
        }

        return true;
    }

    @Task
    private Boolean asyncWorker(AsyncLoopScheduler scheduler, BlockingQueue<Integer> readyQueue) {
        AsyncLoopRange range = scheduler.requestLoopRange();

        for (int i = range.loopStart; i < range.loopEnd; i += 1) {
            rescaled[i] = rescale(images[i], hasFace[i]);
            readyQueue.offer(i);
        }

        return true;
    }

    @Task
    private Boolean asyncPipelineWorker(AsyncLoopScheduler scheduler, BlockingQueue<Integer> inputReady, BlockingQueue<Integer> outputReady) {
        AsyncLoopRange range = scheduler.requestLoopRange();
        for (int i = range.loopStart; i < range.loopEnd; i += 1) {
            try {
                Integer nextIndex = inputReady.take();
                rescaled[nextIndex] = rescale(images[nextIndex], hasFace[nextIndex]);
                outputReady.offer(nextIndex);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        return true;
    }

    @Task
    private Boolean asyncPipelineSynch(BlockingQueue<Integer> inputQueue, AtomicBoolean[] sync, BlockingQueue<Integer> readyQueue) {
        for (int i = 0; i < images.length; i++) {
            try {
                Integer iDone = inputQueue.take();
                if (!sync[iDone].compareAndSet(false, true)) {
                    readyQueue.offer(iDone);
                }
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        return true;
    }

    private BufferedImage rescale(BufferedImage image, Boolean hasFace) {
        if (image != null) {
            if (!hasFace) {
                return blurFilter.filter(rescaleOp.filter(image, null), null);
            }
            return image;
        }

        return null;
    }
}
