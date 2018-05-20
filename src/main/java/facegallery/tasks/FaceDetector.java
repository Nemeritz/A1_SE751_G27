package facegallery.tasks;

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

    public Boolean[] run(boolean batch) {
        if (!batch) {
            for (int i = 0; i < imageBytes.length; i++) {
                if (imageBytes[i] != null) {
                    detections[i] = detect(imageBytes[i]);
                }
                else {
                    detections[i] = null;
                }
            }
        } else {
            detections = detect(imageBytes);
        }

        return detections;
    }

    public Boolean[] run(boolean batch, BlockingQueue<Integer> readyQueue) {
        if (!batch) {
            for (int i = 0; i < imageBytes.length; i++) {
                if (imageBytes[i] != null) {
                    detections[i] = detect(imageBytes[i]);
                }
                else {
                    detections[i] = null;
                }
                readyQueue.offer(i);
            }
        } else {
            detections = detect(imageBytes);
            for (int i = 0; i < imageBytes.length; i++) {
                readyQueue.offer(i);
            }
        }

        return detections;
    }

    public BlockingQueue<Integer> runAsync() {
        BlockingQueue<Integer> readyQueue = new LinkedBlockingQueue<>();

        for (int i = 0; i < imageBytes.length; i++) {
            @Future(taskType = TaskInfoType.INTERACTIVE)
            Boolean task = asyncWorker(i, readyQueue);
        }

        return readyQueue;
    }

    public Boolean runAsync(Void wait) {
        @Future(taskType = TaskInfoType.INTERACTIVE)
        Boolean[] taskGroup = new Boolean[detections.length];

        for (int i = 0; i < imageBytes.length; i++) {
            @Future(taskType = TaskInfoType.INTERACTIVE)
            Boolean task = asyncWorker(i);
            taskGroup[i] = task;
        }

        return taskGroup == null;
    }

    public void runAsyncPipeline(BlockingQueue<Integer> inputReady, BlockingQueue<Integer> readyQueue) {
        @Future(taskType = TaskInfoType.INTERACTIVE)
        Boolean sync = asyncPipelineWorkerOuter(inputReady, readyQueue);
    }

    @Task
    private Boolean asyncWorker(Integer index) {
        try {
            detections[index] = CloudVisionFaceDetector.imageHasFace(imageBytes[index]);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            detections[index] = false;
        }

        return true;
    }

    @Task
    private Boolean asyncWorker(Integer index, BlockingQueue<Integer> readyQueue) {
        try {
            detections[index] = CloudVisionFaceDetector.imageHasFace(imageBytes[index]);
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
                e.printStackTrace(System.err);
            }
        }

        return true;
    }

    @Task
    private Boolean asyncPipelineWorkerInner(Integer index, BlockingQueue<Integer> readyQueue) {
        try {
            detections[index] = CloudVisionFaceDetector.imageHasFace(imageBytes[index]);
            readyQueue.offer(index);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return true;
    }

    private Boolean detect(ByteArray byteArray) {
        try {
            return CloudVisionFaceDetector.imageHasFace(byteArray);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    private Boolean[] detect(ByteArray[] byteArrays) {
        try {
            return CloudVisionFaceDetector.imageHasFace(byteArrays);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
}
