package facegallery;

import apt.annotations.InitParaTask;
import apt.annotations.TaskScheduingPolicy;
import facegallery.gui.FaceGalleryGui;
import facegallery.tasks.ParallelTasker;
import facegallery.tasks.Tasker;
import facegallery.tasks.TaskerStats;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Scanner;


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
                    "\t2. CLI-Concurrent",
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
                    runConcurrent();
                    break;
                case 3:
                    runParallel();
                    break;
                case 4:
                    runParallelPipeline();
                    break;
                case 5:
                    runGui();
                    break;
                default:
                    break;
            }
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
                stats.imageRescaleStats.runtime, stats.imageRescaleStats.taskProgress, stats.imageRescaleStats.taskTotal
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
        tasker.performConcurrent(FaceGallery::statsPrinter, FaceGallery::imagePrinter, true);
    }

    public static void runParallel() {
        ParallelTasker tasker = new ParallelTasker();
        tasker.performParallel(FaceGallery::statsPrinter, FaceGallery::imagePrinter);
    }

    public static void runParallelPipeline() {

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
