package facegallery.tasks;

import apt.annotations.Future;
import apt.annotations.Task;
import apt.annotations.TaskInfoType;
import facegallery.utils.AsyncLoopRange;
import facegallery.utils.AsyncLoopScheduler;
import facegallery.utils.ByteArray;
import pu.loopScheduler.ThreadID;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThumbnailGenerator {
    private ByteArray[] imageBytes;
    private BufferedImage[] resized;
    private int targetWidth;

    public ThumbnailGenerator(ByteArray[] imageBytes, int targetWidth) {
        resized = new BufferedImage[imageBytes.length];
        this.imageBytes = imageBytes;
        this.targetWidth = targetWidth;
    }

    public ByteArray[] getImageBytes() {
        return imageBytes;
    }

    public BufferedImage[] getResized() {
        return resized;
    }

    public BufferedImage[] run() {
        for (int i = 0; i < imageBytes.length; i++) {
            if (imageBytes[i] != null) {
                resized[i] = resize(imageBytes[i]);
            }
            else {
                resized[i] = null;
            }
        }

        return resized;
    }

    public BufferedImage[] run(BlockingQueue<Integer> readyQueue) {
        for (int i = 0; i < imageBytes.length; i++) {
            if (imageBytes[i] != null) {
                resized[i] = resize(imageBytes[i]);
            }
            else {
                resized[i] = null;
            }
            readyQueue.offer(i);
        }

        return resized;
    }

    public BlockingQueue<Integer> runAsync() {
        BlockingQueue<Integer> readyQueue = new LinkedBlockingQueue<>();

        AsyncLoopScheduler scheduler = new AsyncLoopScheduler(0, imageBytes.length, 8);

        @Future(taskType = TaskInfoType.MULTI, taskCount = 8, reduction = "AND")
        Boolean sync = asyncWorker(scheduler, readyQueue);

        return readyQueue;
    }

    public Boolean runAsync(Void wait) {
        AsyncLoopScheduler scheduler = new AsyncLoopScheduler(0, imageBytes.length, 8);

        @Future(taskType = TaskInfoType.MULTI, taskCount = 8, reduction = "AND")
        Boolean sync = asyncWorker(scheduler);

        return sync;
    }

    public void runAsyncPipeline(BlockingQueue<Integer> inputReady, BlockingQueue<Integer> readyQueue) {
        AsyncLoopScheduler scheduler = new AsyncLoopScheduler(0, imageBytes.length, 8);

        @Future(taskType = TaskInfoType.MULTI, taskCount = 8, reduction = "AND")
        Boolean sync = asyncPipelineWorker(scheduler, inputReady, readyQueue);
    }

    @Task
    private Boolean asyncWorker(AsyncLoopScheduler scheduler) {
        AsyncLoopRange range = scheduler.requestLoopRange();

        for (int i = range.loopStart; i < range.loopEnd; i += 1) {
            resized[i] = resize(imageBytes[i]);
        }

        return true;
    }

    @Task
    private Boolean asyncWorker(AsyncLoopScheduler scheduler, BlockingQueue<Integer> readyQueue) {
        AsyncLoopRange range = scheduler.requestLoopRange();

        System.out.printf("Thread %d: %d to %d%n", ThreadID.getStaticID(), range.loopStart, range.loopEnd);
        for (int i = range.loopStart; i < range.loopEnd; i += 1) {
            System.out.println("working " + i);
            resized[i] = resize(imageBytes[i]);
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
                resized[nextIndex] = resize(imageBytes[nextIndex]);
                outputReady.offer(nextIndex);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        return true;
    }

    private BufferedImage resize(ByteArray byteArray) {
        BufferedImage inputImage;
        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray.getBytes());
        try {
            inputImage = ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            inputImage = null;
        }

        if (inputImage != null) {
            double aspectRatio = (double)inputImage.getWidth(null)/(double) inputImage.getHeight(null);
            int targetHeight = (int)(targetWidth / aspectRatio);

            BufferedImage bufferedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = bufferedImage.createGraphics();
            graphics2D.setComposite(AlphaComposite.Src);

            //below three lines are for RenderingHints for better image quality at cost of higher processing time
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

            graphics2D.drawImage(inputImage, 0, 0, targetWidth, targetHeight, null);
            graphics2D.dispose();

            return bufferedImage;
        }

        return null;
    }
}
