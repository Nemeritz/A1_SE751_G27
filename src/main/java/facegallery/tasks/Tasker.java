package facegallery.tasks;

import apt.annotations.Future;
import apt.annotations.Gui;
import facegallery.FaceGallery;
import facegallery.utils.ByteArray;
import facegallery.utils.Timer;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public class Tasker {
    private TaskerStats sequentialStats;
    private TaskerStats concurrentStats;
    private TaskerStats parallelStats;
    private TaskerStats parallelPipelineStats;

    public Void performSequential(Function<TaskerStats, Void> statsUpdater, Function<List<BufferedImage>, Void> imagesUpdater, boolean batchFaceDetect) {
        Thread t = Thread.currentThread();
        String name = t.getName();
        System.out.println("name=" + name);
        // Preparing
        Timer timer = new Timer();
        Timer totalTimer = new Timer();
        sequentialStats = new TaskerStats();
        TaskerStats stats = sequentialStats;
        ImageBytesReader imageBytesReader = new ImageBytesReader(FaceGallery.TEST_DATASET_DIR);
        ByteArray[] imageBytes = imageBytesReader.getImageBytes();
        ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator(imageBytes, 150);
        BufferedImage[] thumbnails = thumbnailGenerator.getResized();
        FaceDetector faceDetector = new FaceDetector(imageBytes);
        Boolean[] hasFace = faceDetector.getDetections();
        ImageRescaler imageRescaler = new ImageRescaler(thumbnails, hasFace);
        BufferedImage[] rescaled = imageRescaler.getRescaled();

        totalTimer.startTiming();

        // Set stats to file read
        stats.lastAction = TaskerStats.LastAction.FILE_READ;
        stats.fileReadStats.taskTotal = imageBytes.length;

        // Start the timer and start the 'File Read' task
        timer.startTiming();
        imageBytesReader.run();

        // Finalise stats and prepare for next task
        stats.fileReadStats.taskProgress = stats.fileReadStats.taskTotal;
        timer.stopTiming();

        stats.fileReadStats.runtime = timer.getTime();
        stats.thumbnailGenerateStats.taskTotal = imageBytes.length;
        stats.lastAction = TaskerStats.LastAction.THUMBNAIL;

        Void gs0 = statsUpdater.apply(stats);

        // Start the timer and start the 'Thumbnail Generate' task
        timer.startTiming();
        thumbnailGenerator.run();

        Void gs1 = imagesUpdater.apply(new CopyOnWriteArrayList<>(rescaled));

        // Finalise stats and prepare for next task
        stats.thumbnailGenerateStats.taskProgress = stats.thumbnailGenerateStats.taskTotal;
        timer.stopTiming();

        stats.thumbnailGenerateStats.runtime = timer.getTime();
        stats.faceDetectionStats.taskTotal = imageBytes.length;
        stats.lastAction = TaskerStats.LastAction.FACE_DETECT;

        Void gs2 = statsUpdater.apply(stats);

        // Start the timer and start the 'Face Detection' task
        timer.startTiming();
        faceDetector.run(batchFaceDetect);

        // Finalise stats and prepare for next task
        stats.faceDetectionStats.taskProgress = stats.faceDetectionStats.taskTotal;
        timer.stopTiming();

        stats.faceDetectionStats.runtime = timer.getTime();
        stats.imageRescaleStats.taskTotal = hasFace.length;
        stats.lastAction = TaskerStats.LastAction.RESCALE;

        Void gs3 = statsUpdater.apply(stats);

        // Start the timer and start the 'Image Rescale' task
        timer.startTiming();
        imageRescaler.run();

        // Finalise stats and end
        stats.imageRescaleStats.taskProgress = stats.imageRescaleStats.taskTotal;
        timer.stopTiming();

        stats.imageRescaleStats.runtime = timer.getTime();
        stats.lastAction = TaskerStats.LastAction.IDLE;

        Void gs4 = statsUpdater.apply(stats);

        Void gs5 = imagesUpdater.apply(new CopyOnWriteArrayList<>(rescaled));

        // All done, check time
        totalTimer.stopTiming();
        stats.totalRuntime = totalTimer.getTime();

        Void gs6 = statsUpdater.apply(stats);

        return null;
    }

    public Void performConcurrent(Function<TaskerStats, Void> statsUpdater, Function<List<BufferedImage>, Void> imagesUpdater, boolean batchFaceDetect) {
        // Preparing
        Timer timer = new Timer();
        Timer totalTimer = new Timer();
        concurrentStats = new TaskerStats();
        TaskerStats stats = concurrentStats;
        BlockingQueue<Integer> readyQueue = new LinkedBlockingQueue<>();
        ImageBytesReader imageBytesReader = new ImageBytesReader(FaceGallery.TEST_DATASET_DIR);
        ByteArray[] imageBytes = imageBytesReader.getImageBytes();
        ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator(imageBytes, 150);
        BufferedImage[] thumbnails = thumbnailGenerator.getResized();
        FaceDetector faceDetector = new FaceDetector(imageBytes);
        Boolean[] hasFace = faceDetector.getDetections();
        ImageRescaler imageRescaler = new ImageRescaler(thumbnails, hasFace);
        BufferedImage[] rescaled = imageRescaler.getRescaled();

        totalTimer.startTiming();

        // Set stats to file read
        stats.lastAction = TaskerStats.LastAction.FILE_READ;
        stats.fileReadStats.taskTotal = imageBytes.length;
        @Gui
        Void gs0 = statsUpdater.apply(stats);

        // Start the timer and start the 'File Read' task
        timer.startTiming();
        @Future
        Object s0 = imageBytesReader.run(readyQueue);

        // Update the gui with each read for stats
        for (int i = 0; i < imageBytes.length; i++) {
            stats.fileReadStats.taskProgress++;
            @Gui
            Void gs1 = statsUpdater.apply(stats);

            try {
                readyQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        // Finalise stats and prepare for next task
        timer.stopTiming();
        stats.fileReadStats.runtime = timer.getTime();
        stats.thumbnailGenerateStats.taskTotal = imageBytes.length;
        stats.lastAction = TaskerStats.LastAction.THUMBNAIL;
        @Gui
        Void gs2 = statsUpdater.apply(stats);

        // Start the timer and start the 'Thumbnail Generate' task
        readyQueue.clear();
        timer.startTiming();
        @Future
        Object s1 = thumbnailGenerator.run(readyQueue);

        for (int i = 0; i < imageBytes.length; i++) {
            stats.thumbnailGenerateStats.taskProgress++;
            @Gui
            Void gs3 = statsUpdater.apply(stats);

            try {
                readyQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }

            @Gui
            Void gs4 = imagesUpdater.apply(new CopyOnWriteArrayList<>(thumbnails));
        }

        // Finalise stats and prepare for next task
        timer.stopTiming();
        stats.thumbnailGenerateStats.runtime = timer.getTime();
        stats.faceDetectionStats.taskTotal = imageBytes.length;
        stats.lastAction = TaskerStats.LastAction.FACE_DETECT;
        @Gui
        Void gs5 = statsUpdater.apply(stats);

        // Start the timer and start the 'Face Detection' task
        readyQueue.clear();
        timer.startTiming();
        @Future
        Object s3 = faceDetector.run(batchFaceDetect, readyQueue);

        for (int i = 0; i < imageBytes.length; i++) {
            stats.faceDetectionStats.taskProgress++;
            @Gui
            Void gs6 = statsUpdater.apply(stats);

            try {
                readyQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        // Finalise stats and prepare for next task
        timer.stopTiming();
        stats.faceDetectionStats.runtime = timer.getTime();
        stats.imageRescaleStats.taskTotal = hasFace.length;
        stats.lastAction = TaskerStats.LastAction.RESCALE;
        @Gui
        Void gs7 = statsUpdater.apply(stats);

        // Start the timer and start the 'Image Rescale' task
        readyQueue.clear();
        timer.startTiming();
        @Future
        Object s4 = imageRescaler.run(readyQueue);

        for (int i = 0; i < imageBytes.length; i++) {
            stats.imageRescaleStats.taskProgress++;
            @Gui
            Void gs8 = statsUpdater.apply(stats);

            try {
                readyQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }

            @Gui
            Void gs9 = imagesUpdater.apply(new CopyOnWriteArrayList<>(rescaled));
        }

        // Finalise stats and end
        timer.stopTiming();
        stats.imageRescaleStats.runtime = timer.getTime();
        stats.lastAction = TaskerStats.LastAction.IDLE;

        @Gui
        Void gs10 = statsUpdater.apply(stats);

        // All done, check time
        totalTimer.stopTiming();
        stats.totalRuntime = totalTimer.getTime();

        @Gui
        Void gs11 = statsUpdater.apply(stats);

        return null;
    }
}