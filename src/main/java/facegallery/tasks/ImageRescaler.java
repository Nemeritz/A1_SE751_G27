package facegallery.tasks;

import apt.annotations.Future;
import apt.annotations.Task;
import apt.annotations.TaskInfoType;
import pu.loopScheduler.*;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ImageRescaler {
    private BufferedImage[] images;
    private Boolean[] hasFace;
    private BufferedImage[] rescaled;
    private RescaleOp rescaleOp;

    public ImageRescaler(BufferedImage[] images, Boolean[] hasFace, RescaleOp rescaleOp) {
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

        LoopScheduler scheduler = LoopSchedulerFactory
                .createLoopScheduler(
                        0,
                        images.length,
                        1,
                        2,
                        AbstractLoopScheduler.LoopCondition.LessThan,
                        LoopSchedulerFactory.LoopSchedulingType.Static
                );

        @Future(taskType = TaskInfoType.MULTI, taskCount = 2, reduction = "AND")
        Boolean sync = asyncWorker(scheduler, readyQueue);

        return readyQueue;
    }

    public Boolean runAsync(Void wait) {
        LoopScheduler scheduler = LoopSchedulerFactory
                .createLoopScheduler(
                        0,
                        images.length,
                        1,
                        2,
                        AbstractLoopScheduler.LoopCondition.LessThan,
                        LoopSchedulerFactory.LoopSchedulingType.Static
                );

        @Future(taskType = TaskInfoType.MULTI, taskCount = 2, reduction = "AND")
        Boolean sync = asyncWorker(scheduler);

        return sync;
    }

    public void runAsyncPipeline(BlockingQueue<Integer> inputReady, BlockingQueue<Integer> readyQueue) {
        LoopScheduler scheduler = LoopSchedulerFactory
                .createLoopScheduler(
                        0,
                        images.length,
                        1,
                        2,
                        AbstractLoopScheduler.LoopCondition.LessThan,
                        LoopSchedulerFactory.LoopSchedulingType.Static
                );

        @Future(taskType = TaskInfoType.MULTI, taskCount = 2, reduction = "AND")
        Boolean sync = asyncPipelineWorker(scheduler, inputReady, readyQueue);
    }

    @Task
    private Boolean asyncWorker(LoopScheduler scheduler) {
        LoopRange range = scheduler.getChunk(ThreadID.getStaticID());

        for (int i = range.loopStart; i < range.loopEnd; i += range.localStride) {
            rescaled[i] = rescale(images[i], hasFace[i]);
        }

        return true;
    }

    @Task
    private Boolean asyncWorker(LoopScheduler scheduler, BlockingQueue<Integer> readyQueue) {
        LoopRange range = scheduler.getChunk(ThreadID.getStaticID());

        for (int i = range.loopStart; i < range.loopEnd; i += range.localStride) {
            rescaled[i] = rescale(images[i], hasFace[i]);
            readyQueue.offer(i);
        }

        return true;
    }

    @Task
    private Boolean asyncPipelineWorker(LoopScheduler scheduler, BlockingQueue<Integer> inputReady, BlockingQueue<Integer> outputReady) {
        LoopRange range = scheduler.getChunk(ThreadID.getStaticID());

        for (int i = range.loopStart; i < range.loopEnd; i += range.localStride) {
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

    private BufferedImage rescale(BufferedImage image, Boolean hasFace) {
        if (image != null) {
            if (!hasFace) {
                return rescaleOp.filter(image, null);
            }
            return image;
        }

        return null;
    }
}
