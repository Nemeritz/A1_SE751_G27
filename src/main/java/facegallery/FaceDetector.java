package facegallery;

import apt.annotations.Future;
import apt.annotations.Task;
import apt.annotations.TaskInfoType;
import facegallery.utils.CloudVisionFaceDetector;
import pt.runtime.TaskIDGroup;
import facegallery.utils.ByteArray;
import pu.loopScheduler.*;

import java.lang.management.ThreadInfo;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.System;

public class FaceDetector {    
    private static boolean detect(ByteArray byteArray) {
    	try {
            return CloudVisionFaceDetector.imageHasFace(byteArray);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            return false;
        }
    }

    @Task
    private static Void detectWorker(List<ByteArray> fileBytes, List<AtomicBoolean> detections, LoopScheduler scheduler) {
        LoopRange range = scheduler.getChunk(ThreadID.getStaticID());
        for (int i = range.loopStart; i < range.loopEnd; i += range.localStride) {
            try {
                detections.get(i).set(CloudVisionFaceDetector.imageHasFace(fileBytes.get(i)));
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
                detections.get(i).set(false);
            }
        }
        return null;
    }

    public static List<Boolean> detectSequential(List<ByteArray> fileBytes) {
    	List<Boolean> detections = new ArrayList<>();
        for (int i = 0; i < fileBytes.size(); i++) {
        	detections.add(detect(fileBytes.get(i)));
        }
        return detections;
    }

    public static List<AtomicBoolean> createParallelDetectionContainer(int fileBytesSize) {
    	List<AtomicBoolean> detections = new ArrayList<>(fileBytesSize);
    	for (int i = 0; i < fileBytesSize; i++) {
    		detections.add(new AtomicBoolean(true));
    	}
    	return detections;
    }
    
    public static Void detectParallel(List<ByteArray> fileBytes, List<AtomicBoolean> detections) {
        LoopScheduler scheduler = LoopSchedulerFactory
                .createLoopScheduler(
                        0,
                        fileBytes.size(),
                        1,
                        10,
                        AbstractLoopScheduler.LoopCondition.LessThan,
                        LoopSchedulerFactory.LoopSchedulingType.Static
                );

        @Future(taskType = TaskInfoType.MULTI_IO, taskCount = 10)
        Void v = detectWorker(fileBytes, detections, scheduler);

        return null;
    }
}
