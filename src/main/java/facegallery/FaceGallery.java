package facegallery;

import apt.annotations.InitParaTask;
import apt.annotations.TaskScheduingPolicy;
import facegallery.gui.FaceGalleryGui;
import facegallery.tasks.FaceDetector;
import facegallery.tasks.ImageBytesReader;
import facegallery.tasks.Tasker;
import facegallery.tasks.TaskerStats;
import facegallery.utils.ByteArray;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class FaceGallery {
    public static final String DATASET_DIR = System.getenv("FACEGALLERY_DATASET");
    public static final String TEST_DATASET_DIR = DATASET_DIR + "/Test";
    public static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    @InitParaTask(numberOfThreads = 32, schedulingPolicy = TaskScheduingPolicy.MixedSchedule)
	public static void main(String[] args) {
	    try (Scanner scanner = new Scanner(System.in)) {
            String[] prompt = {
                    "Select operating mode:",
                    "\t1. CLI-Sequential",
                    "\t2. CLI-Parallel",
                    "\t3. CLI-Parallel",
                    "\t4. CLI-ParallelPipeline",
                    "\t5. GUI",
                    "\tAny other key to exit"
            };

            for (String line : prompt) {
                System.out.println(line);
            }

            int input = -1;
            try {
                input = scanner.nextInt();
            } catch (RuntimeException ignore) {
            }

            switch(input) {
                case 1:
                    runSequential();
                    break;
                case 2:
                    runParallel();
                    break;
                case 3:
                    runGui();
                    break;
                default:
                    break;
            }
            System.exit(0);
        }
	}

	private static Void statsPrinter(TaskerStats stats) {
        System.out.printf("=====================================%n" +
                "|  Last Action : %17s  |%n" +
                "|  Total Time  : %17.2f  |%n" +
                "=====================================%n" +
                "|  Task  |  Time  |  Done  |  Tota  |%n" +
                "|  LOAD  |  %3.2f  |  %4d  |  %4d  |%n" +
                "|  THUM  |  %3.2f  |  %4d  |  %4d  |%n" +
                "|  FACE  |  %3.2f  |  %4d  |  %4d  |%n" +
                "|  RESC  |  %3.2f  |  %4d  |  %4d  |%n" +
                "=====================================%n",
                stats.lastAction.toDisplayString(),
                stats.totalRuntime,
                stats.fileReadStats.runtime, stats.fileReadStats.taskProgress, stats.fileReadStats.taskTotal,
                stats.thumbnailGenerateStats.runtime, stats.thumbnailGenerateStats.taskProgress, stats.thumbnailGenerateStats.taskTotal,
                stats.faceDetectionStats.runtime, stats.faceDetectionStats.taskProgress, stats.faceDetectionStats.taskTotal,
                stats.imageRescaleStats.runtime, stats.imageRescaleStats.taskProgress, stats.imageRescaleStats.taskProgress
        );
        return null;
    };

    private static Void imagePrinter(List<BufferedImage> images) {

        return null;
    }

	public static void runSequential() {
        Tasker tasker = new Tasker();
        tasker.performSequential(FaceGallery::statsPrinter, FaceGallery::imagePrinter, true);
    }

    public static void runConcurrent() {
        Tasker tasker = new Tasker();
        tasker.performConcurrent(FaceGallery::statsPrinter, FaceGallery::imagePrinter, false);
    }

    public static void runParallel() {
        // Image reading
        ImageBytesReader imageBytesReader = new ImageBytesReader(TEST_DATASET_DIR);
        File[] fileList = imageBytesReader.getFileList();
        BlockingQueue<Integer> imageBytesReady = imageBytesReader.runAsync();
        ByteArray[] imageBytes = imageBytesReader.getImageBytes();


        // Cloud Vision Detection
        FaceDetector faceDetector = new FaceDetector(imageBytes);
        BlockingQueue<Integer> faceDetectionReady = new LinkedBlockingQueue<>();
        faceDetector.runAsyncPipeline(imageBytesReady, faceDetectionReady);
        Boolean[] detections = faceDetector.getDetections();

        System.out.println("Async check for null at first... should be all true...");
        for (int i = 0; i < detections.length; i++) {
            System.out.println(fileList[i].getName() + ": " + Boolean.toString(detections[i] == null));
        }

        System.out.println("Waiting... should be all false...");
        for (int i = 0; i < imageBytes.length; i++) {
            try {
                System.out.println(fileList[i].getName() + ": " + Boolean.toString(detections[faceDetectionReady.take()] == null));
            } catch (InterruptedException ignore) {

            }
        }

        System.out.println("And now results...");
        for (int i = 0; i < imageBytes.length; i++) {
            System.out.println(fileList[i].getName() + ": " + Boolean.toString(detections[i]));
        }
    }

    public static void runGui() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                FaceGalleryGui app = new FaceGalleryGui();
                app.setVisible(true);
            }
        });
        while(true);
    }
}
