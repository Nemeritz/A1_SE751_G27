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
    private BufferedImage[] rescaled;
    private RescaleOp rescaleOp;

    public ImageRescaler(BufferedImage[] images, RescaleOp rescaleOp) {
        rescaled = new BufferedImage[images.length];
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
                images[i] = rescale(images[i]);
            }
            else {
                images[i] = null;
            }
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
            rescaled[i] = rescale(images[i]);
        }

        return true;
    }

    @Task
    private Boolean asyncWorker(LoopScheduler scheduler, BlockingQueue<Integer> readyQueue) {
        LoopRange range = scheduler.getChunk(ThreadID.getStaticID());

        for (int i = range.loopStart; i < range.loopEnd; i += range.localStride) {
            rescaled[i] = rescale(images[i]);
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
                rescaled[nextIndex] = rescale(images[nextIndex]);
                outputReady.offer(nextIndex);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        return true;
    }

    private BufferedImage rescale(BufferedImage image) {
        if (image != null) {
            BufferedImage dstImage = null;
            dstImage = rescaleOp.filter(image, null);

            return dstImage;
        }

        return null;
    }
}
