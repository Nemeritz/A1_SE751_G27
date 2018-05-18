package facegallery;

import apt.annotations.AsyncCatch;
import apt.annotations.Future;
import apt.annotations.Task;
import apt.annotations.TaskInfoType;
import facegallery.utils.ByteArray;
import facegallery.utils.CloudVisionFaceDetector;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FaceDetector {
    private ByteArray[] imageBytes;
    private Boolean[] detections;

    public FaceDetector(ByteArray[] imageBytes) {
        this.imageBytes = imageBytes;
        detections = new Boolean[imageBytes.length];
    }

    public ByteArray[] getImageBytes() {
        return imageBytes;
    }

    public Boolean[] getDetections() {
        return detections;
    }

    public Boolean[] run() {
        Boolean[] results = new Boolean[imageBytes.length];

        for (int i = 0; i < imageBytes.length; i++) {
            if (imageBytes[i] != null) {
                results[i] = detect(imageBytes[i]);
            }
            else {
                results[i] = null;
            }
        }

        return results;
    }

    public BlockingQueue<Integer> runAsync() {
        BlockingQueue<Integer> readyQueue = new LinkedBlockingQueue<>();

        for (int i = 0; i < imageBytes.length; i++) {
            @Future(taskType = TaskInfoType.INTERACTIVE)
            @AsyncCatch(throwables={Exception.class}, handlers={"asyncExceptionHandler()"})
            Boolean task = asyncWorker(i, readyQueue);
        }

        return readyQueue;
    }

    public Boolean runAsync(Void wait) {
        @Future(taskType = TaskInfoType.INTERACTIVE)
        Boolean[] taskGroup = new Boolean[detections.length];

        for (int i = 0; i < imageBytes.length; i++) {
            @Future(taskType = TaskInfoType.INTERACTIVE)
            @AsyncCatch(throwables={Exception.class}, handlers={"asyncExceptionHandler()"})
            Boolean task = asyncWorker(i);
            taskGroup[i] = task;
        }

        return taskGroup == null;
    }

    public void runAsyncPipeline(BlockingQueue<Integer> inputReady, BlockingQueue<Integer> readyQueue) {
        @Future(taskType = TaskInfoType.INTERACTIVE)
        @AsyncCatch(throwables={Exception.class}, handlers={"asyncExceptionHandler()"})
        Boolean sync = asyncPipelineWorkerOuter(inputReady, readyQueue);
    }

    @Task
    private Boolean asyncWorker(Integer index) {
        try {
            detections[index] = CloudVisionFaceDetector.imageHasFace(imageBytes[index].getBytes());
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            detections[index] = false;
        }

        return true;
    }

    @Task
    private Boolean asyncWorker(Integer index, BlockingQueue<Integer> readyQueue) {
        try {
            detections[index] = CloudVisionFaceDetector.imageHasFace(imageBytes[index].getBytes());
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            detections[index] = false;
        } finally {
            readyQueue.offer(index);
        }

        return true;
    }

    @Task
    private Boolean asyncPipelineWorkerOuter(BlockingQueue<Integer> inputReady, BlockingQueue<Integer> outputReady) {
        for (int i = 0; i < imageBytes.length; i++) {
            try {
                Integer nextIndex = inputReady.take();
                @Future(taskType = TaskInfoType.INTERACTIVE)
                Boolean sync = asyncPipelineWorkerInner(nextIndex, outputReady);
            } catch (InterruptedException e) {
                System.out.println(e.getLocalizedMessage());
            }
        }

        return true;
    }

    @Task
    private Boolean asyncPipelineWorkerInner(Integer index, BlockingQueue<Integer> readyQueue) {
        try {
            detections[index] = CloudVisionFaceDetector.imageHasFace(imageBytes[index].getBytes());
            readyQueue.offer(index);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
        return true;
    }

    private void asyncExceptionHandler() {
        System.out.println("Exception");
    }

    private boolean detect(ByteArray byteArray) {
        try {
            return CloudVisionFaceDetector.imageHasFace(byteArray);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            return false;
        }
    }
}
