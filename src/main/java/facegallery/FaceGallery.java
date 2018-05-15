package facegallery;

import apt.annotations.Future;
import facegallery.utils.ByteArray;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class FaceGallery {
    public static String DATASET_DIR = System.getenv("FACEGALLRY_DATASET");

	public static void main(String[] args) {
	    try (Scanner scanner = new Scanner(System.in)) {
            String[] prompt = {
                    "Select operating mode:",
                    "\t1. CLI-Sequential",
                    "\t2. CLI-Parallel",
                    "\t3. CLI-Parallel-Pipeline",
                    "\t4. GUI",
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
                    runParallelPipeline();
                    break;
                case 4:
                    runGui();
                    break;
                default:
                    break;
            }
        }
	}

	public static void runSequential() {
        ImageBytesReader imageBytesReader =  new ImageBytesReader(DATASET_DIR + File.pathSeparator + "Test");
        List<ByteArray> fileBytes = imageBytesReader.runSequential();

        List<Boolean> sequentialDetections = FaceDetector.detectSequential(fileBytes);

        System.out.println("Sequential as follows:");
        for (Boolean detection : sequentialDetections) {
            System.out.println(detection.toString());
        }
    }

    public static void runParallel() {
        File[] fileList = ImageBytesReader.getImageListFromDir(DATASET_DIR + File.pathSeparator + "Test");
        List<ByteArray> fileBytes = ImageBytesReader.createImageBytesContainer(fileList.length);
        List<AtomicBoolean> bytesReady = ImageBytesReader.createBytesReadyContainer(fileList.length);
        List<AtomicBoolean> parallelDetections = FaceDetector.createDetectionContainer(fileList.length);

        @Future
        Void v = FaceDetector.detectParallel(fileBytes, parallelDetections);

        System.out.println("Parallel as follows:");
        for (AtomicBoolean detection : parallelDetections) {
            System.out.println(detection.toString());
        }
    }

    public static void runParallelPipeline() {
        File[] fileList = ImageBytesReader.getImageListFromDir(DATASET_DIR + File.pathSeparator + "Test");

    }

    public static void runGui() {

    }
}
