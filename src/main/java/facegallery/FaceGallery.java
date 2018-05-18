package facegallery;

import apt.annotations.InitParaTask;
import apt.annotations.TaskScheduingPolicy;
import facegallery.gui.FaceGalleryGui;
import facegallery.tasks.FaceDetector;
import facegallery.tasks.ImageBytesReader;
import facegallery.utils.ByteArray;
import javafx.application.Application;

import java.io.File;
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
                    "\t3. GUI",
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

	public static void runSequential() {
        ImageBytesReader imageBytesReader = new ImageBytesReader(TEST_DATASET_DIR);
        File[] fileList = imageBytesReader.getFileList();

        long imageReadStartTime = System.currentTimeMillis();
        ByteArray[] imageBytes = imageBytesReader.run();
        long imageReadEndTime = System.currentTimeMillis();

        FaceDetector faceDetector = new FaceDetector(imageBytes);

        long faceDetectStartTime = System.currentTimeMillis();
        Boolean[] detections = faceDetector.run(true);
        long faceDetectEndTime = System.currentTimeMillis();

        System.out.println("Sequential as follows:");
        for (int i = 0; i < detections.length; i++) {
            System.out.println(fileList[i].getName() + ": " + detections[i].toString());
        }
        System.out.println("Image read took " + Double.toString((double)(imageReadEndTime - imageReadStartTime) / 1000) + " seconds");
        System.out.println("Face detection took " + Double.toString((double)(faceDetectEndTime - faceDetectStartTime) / 1000) + " seconds");
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
        Application.launch(FaceGalleryGui.class);
    }
}
