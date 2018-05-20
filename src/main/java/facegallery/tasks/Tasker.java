package facegallery.tasks;

import apt.annotations.Future;
import apt.annotations.Gui;
import facegallery.FaceGallery;
import facegallery.utils.ByteArray;
import facegallery.utils.Timer;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public class Tasker {
    private TaskerStats sequentialStats;
    private TaskerStats parallelStats;

    public void performSequential(Function<TaskerStats, Void> statsUpdater, Function<List<BufferedImage>, Void> imagesUpdater) {
        // Preparing
        Timer timer = new Timer();
        Timer totalTimer = new Timer();

        totalTimer.startTiming();

        sequentialStats = new TaskerStats();
        BlockingQueue<Integer> readyQueue = new LinkedBlockingQueue<>();
        ImageBytesReader imageBytesReader = new ImageBytesReader(FaceGallery.TEST_DATASET_DIR);
        ByteArray[] imageBytes = imageBytesReader.getImageBytes();

        // Set stats to file read
        sequentialStats.lastAction = TaskerStats.LastAction.FILE_READ;
        sequentialStats.fileReadStats.taskTotal = imageBytes.length;
        @Gui
        Void gs0 = statsUpdater.apply(sequentialStats);

        // Start the timer and start the 'File Read' task
        timer.startTiming();
        @Future
        Object s0 = imageBytesReader.run(readyQueue);

        // Update the gui with each read for stats
        for (int i = 0; i < imageBytes.length; i++) {
            sequentialStats.fileReadStats.taskProgress++;
            @Gui
            Void gs1 = statsUpdater.apply(sequentialStats);

            try {
                readyQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
        timer.stopTiming();

        // Finalise stats and prepare for next task
        sequentialStats.fileReadStats.taskProgress++;
        sequentialStats.fileReadStats.runtime = timer.getTime();
        sequentialStats.thumbnailGenerateStats.taskTotal = imageBytes.length;
        sequentialStats.lastAction = TaskerStats.LastAction.THUMBNAIL;
        @Gui
        Void gs2 = statsUpdater.apply(sequentialStats);
        ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator(imageBytes, 150);
        BufferedImage[] thumbnails = thumbnailGenerator.getResized();

        // Start the timer and start the 'Thumbnail Generate' task
        readyQueue.clear();
        timer.startTiming();
        @Future
        Object s1 = thumbnailGenerator.run(readyQueue);

        for (int i = 0; i < imageBytes.length; i++) {
            sequentialStats.thumbnailGenerateStats.taskProgress++;
            @Gui
            Void gs3 = statsUpdater.apply(sequentialStats);

            try {
                readyQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }

            @Gui
            Void gs4 = imagesUpdater.apply(new CopyOnWriteArrayList<>(thumbnails));
        }
        timer.stopTiming();

        // Finalise stats and prepare for next task
        sequentialStats.thumbnailGenerateStats.taskProgress++;
        sequentialStats.thumbnailGenerateStats.runtime = timer.getTime();
        sequentialStats.faceDetectionStats.taskTotal = imageBytes.length;
        sequentialStats.lastAction = TaskerStats.LastAction.FACE_DETECT;
        @Gui
        Void gs5 = statsUpdater.apply(sequentialStats);
        FaceDetector faceDetector = new FaceDetector(imageBytes);
        Boolean[] hasFace = faceDetector.getDetections();

        // Start the timer and start the 'Face Detection' task
        readyQueue.clear();
        timer.startTiming();
        @Future
        Object s3 = faceDetector.run(false, readyQueue);

        for (int i = 0; i < imageBytes.length; i++) {
            sequentialStats.faceDetectionStats.taskProgress++;
            @Gui
            Void gs6 = statsUpdater.apply(sequentialStats);

            try {
                readyQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
        timer.stopTiming();

        // Finalise stats and prepare for next task
        sequentialStats.faceDetectionStats.taskProgress++;
        sequentialStats.faceDetectionStats.runtime = timer.getTime();
        sequentialStats.imageRescaleStats.taskTotal = hasFace.length;
        sequentialStats.lastAction = TaskerStats.LastAction.RESCALE;
        @Gui
        Void gs7 = statsUpdater.apply(sequentialStats);
        ImageRescaler imageRescaler = new ImageRescaler(thumbnails, hasFace, new RescaleOp(0.3f, 0.0f, null));
        BufferedImage[] rescaled = imageRescaler.getRescaled();

        // Start the timer and start the 'Image Rescale' task
        readyQueue.clear();
        timer.startTiming();
        @Future
        Object s4 = imageRescaler.run(readyQueue);

        for (int i = 0; i < imageBytes.length; i++) {
            sequentialStats.imageRescaleStats.taskProgress++;
            @Gui
            Void gs8 = statsUpdater.apply(sequentialStats);

            try {
                readyQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }

            @Gui
            Void gs9 = imagesUpdater.apply(new CopyOnWriteArrayList<>(rescaled));
        }
        timer.stopTiming();

        // Finalise stats and end
        sequentialStats.imageRescaleStats.taskProgress++;
        sequentialStats.imageRescaleStats.runtime = timer.getTime();
        sequentialStats.lastAction = TaskerStats.LastAction.IDLE;

        totalTimer.startTiming();
        sequentialStats.totalRuntime = totalTimer.getTime();

        @Gui
        Void gs10 = statsUpdater.apply(sequentialStats);
    }

    public void performParallel(Function<TaskerStats, Void> statsUpdater, Function<BufferedImage[], Void> imagesUpdater) {
        @Gui
        Void sync = statsUpdater.apply(parallelStats);
    }
}
