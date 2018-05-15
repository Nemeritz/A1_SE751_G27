package facegallery;

import apt.annotations.InitParaTask;
import facegallery.utils.ByteArray;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class FaceGallery {
    public static String DATASET_DIR = "C:\\Users\\lichk\\Documents\\Git\\facedataset";

    @InitParaTask(numberOfThreads = 8)
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
        ImageBytesReader imageBytesReader = new ImageBytesReader(DATASET_DIR + File.separator + "Test");
        List<File> fileList = List.of(imageBytesReader.getFileList());
        List<ByteArray> fileBytes = imageBytesReader.runSequential();

        FaceDetector faceDetector = new FaceDetector(fileBytes);
        List<AtomicBoolean> detections = faceDetector.runSequential();

        System.out.println("Sequential as follows:");
        for (int i = 0; i < detections.size(); i++) {
            System.out.println(fileList.get(i).getName() + ": " + detections.get(i).toString());
        }
    }

    public static void runParallel() {
        ImageBytesReader imageBytesReader = new ImageBytesReader(DATASET_DIR + File.separator + "Test");
        List<File> fileList = List.of(imageBytesReader.getFileList());
        List<ByteArray> fileBytes = imageBytesReader.createResultsContainer();
        List<AtomicBoolean> bytesReady = imageBytesReader.createReadyContainer();

        FaceDetector faceDetector = new FaceDetector(fileBytes);
        List<AtomicBoolean> detections = faceDetector.createResultsContainer();
        List<AtomicBoolean> detectionsReady = faceDetector.createReadyContainer();

        boolean bytesRead = imageBytesReader.runParallel(fileBytes, bytesReady);

        if (bytesRead) {
            boolean detectionsDone = faceDetector.runParallel(detections, detectionsReady);
            if (detectionsDone) {
                System.out.println("Parallel as follows:");
                for (int i = 0; i < detections.size(); i++) {
                    System.out.println(fileList.get(i).getName() + ": " + detections.get(i).toString());
                }
            }
        }
    }

    public static void runParallelPipeline() {
        ImageBytesReader imageBytesReader = new ImageBytesReader(DATASET_DIR + File.separator + "Test");
        List<ByteArray> fileBytes = imageBytesReader.createResultsContainer();
        List<AtomicBoolean> bytesReady = imageBytesReader.createReadyContainer();

        FaceDetector faceDetector = new FaceDetector(fileBytes);
        List<AtomicBoolean> detections = faceDetector.createResultsContainer();
        List<AtomicBoolean> detectionsReady = faceDetector.createReadyContainer();
    }

    public static void runGui() {

    }
}
