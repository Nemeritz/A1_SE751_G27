package facegallery.tasks;

import apt.annotations.Future;
import apt.annotations.Gui;
import facegallery.FaceGallery;
import facegallery.utils.ByteArray;
import facegallery.utils.Timer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public class ParallelTasker {
    private TaskerStats stats;
    private TaskerStats parallelPipelineStats;

    private Void fileRead(Timer timer, ImageBytesReader imageBytesReader, ByteArray[] imageBytes, Function<TaskerStats, Void> statsUpdater) {
        // Set stats to file read
        stats.lastAction = TaskerStats.LastAction.FILE_READ;
        stats.fileReadStats.taskTotal = imageBytes.length;
        @Gui
        Void gs0 = statsUpdater.apply(stats);

        // Start the timer and start the 'File Read' task
        timer.startTiming();
        BlockingQueue<Integer> readyQueue = imageBytesReader.runAsync();

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

        @Gui
        Void gs2 = statsUpdater.apply(stats);

        return null;
    }

    private Void thumbnailGenerate(Timer timer, ThumbnailGenerator thumbnailGenerator, ByteArray[] imageBytes, BufferedImage[] thumbnails, Function<TaskerStats, Void> statsUpdater, Function<List<BufferedImage>, Void> imagesUpdater) {
        stats.thumbnailGenerateStats.taskTotal = imageBytes.length;
        stats.lastAction = TaskerStats.LastAction.THUMBNAIL;

        @Gui
        Void gs2 = statsUpdater.apply(stats);

        // Start the timer and start the 'Thumbnail Generate' task
        timer.startTiming();

        BlockingQueue<Integer> readyQueue = thumbnailGenerator.runAsync();

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
        @Gui
        Void gs5 = statsUpdater.apply(stats);

        stats.faceDetectionStats.taskTotal = imageBytes.length;
        stats.lastAction = TaskerStats.LastAction.FACE_DETECT;

        return null;
    }

    public void performParallel(Function<TaskerStats, Void> statsUpdater, Function<List<BufferedImage>, Void> imagesUpdater) {
        // Preparing
        stats = new TaskerStats();
        List<BlockingQueue<Integer>> readyQueues = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            readyQueues.add(new LinkedBlockingQueue<>());
        }
        List<Timer> timers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            timers.add(new Timer());
        }
        Timer totalTimer = new Timer();
        ImageBytesReader imageBytesReader = new ImageBytesReader(FaceGallery.TEST_DATASET_DIR);
        ByteArray[] imageBytes = imageBytesReader.getImageBytes();
        ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator(imageBytes, 150);
        BufferedImage[] thumbnails = thumbnailGenerator.getResized();
        FaceDetector faceDetector = new FaceDetector(imageBytes);
        Boolean[] hasFace = faceDetector.getDetections();
        ImageRescaler imageRescaler = new ImageRescaler(thumbnails, hasFace);
        BufferedImage[] rescaled = imageRescaler.getRescaled();

        totalTimer.startTiming();

        @Future
        Void t0 = fileRead(timers.get(0), imageBytesReader, imageBytes, statsUpdater);

        @Future(depends = "t0")
        Void t1 = thumbnailGenerate(timers.get(1), thumbnailGenerator, imageBytes, thumbnails, statsUpdater, imagesUpdater);


//        // Start the timer and start the 'Face Detection' task
//        readyQueue.clear();
//        timer.startTiming();
//        @Future
//        Object s3 = faceDetector.run(false, readyQueue);
//
//        for (int i = 0; i < imageBytes.length; i++) {
//            stats.faceDetectionStats.taskProgress++;
//            @Gui
//            Void gs6 = statsUpdater.apply(stats);
//
//            try {
//                readyQueue.take();
//            } catch (InterruptedException e) {
//                e.printStackTrace(System.err);
//            }
//        }
//
//        // Finalise stats and prepare for next task
//        timer.stopTiming();
//        stats.faceDetectionStats.runtime = timer.getTime();
//        stats.imageRescaleStats.taskTotal = hasFace.length;
//        stats.lastAction = TaskerStats.LastAction.RESCALE;
//        @Gui
//        Void gs7 = statsUpdater.apply(stats);
//
//        // Start the timer and start the 'Image Rescale' task
//        readyQueue.clear();
//        timer.startTiming();
//        @Future
//        Object s4 = imageRescaler.run(readyQueue);
//
//        for (int i = 0; i < imageBytes.length; i++) {
//            stats.imageRescaleStats.taskProgress++;
//            @Gui
//            Void gs8 = statsUpdater.apply(stats);
//
//            try {
//                readyQueue.take();
//            } catch (InterruptedException e) {
//                e.printStackTrace(System.err);
//            }
//
//            @Gui
//            Void gs9 = imagesUpdater.apply(new CopyOnWriteArrayList<>(rescaled));
//        }
//
//        // Finalise stats and end
//        timer.stopTiming();
//        stats.imageRescaleStats.runtime = timer.getTime();
//        stats.lastAction = TaskerStats.LastAction.IDLE;
//
//        @Gui
//        Void gs10 = statsUpdater.apply(stats);
//
//        // All done, check time
//        totalTimer.stopTiming();
//        stats.totalRuntime = totalTimer.getTime();
//
//        @Gui
//        Void gs11 = statsUpdater.apply(stats);
    }
}