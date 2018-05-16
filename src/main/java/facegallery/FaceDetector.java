package facegallery;

import apt.annotations.Future;
import apt.annotations.Task;
import apt.annotations.TaskInfoType;
import apt.annotations.AsyncCatch;
import apt.annotations.ReductionMethod;
import facegallery.utils.ByteArray;
import facegallery.utils.CloudVisionFaceDetector;
import facegallery.utils.Parallelized;
import pu.loopScheduler.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FaceDetector extends Parallelized<ByteArray, AtomicBoolean> {
    public FaceDetector(List<ByteArray> imageBytes) {
        super(imageBytes);
    }

    private static boolean detect(ByteArray byteArray) {
    	try {
            return CloudVisionFaceDetector.imageHasFace(byteArray);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            return false;
        }
    }

    @Task
    private Void detectWorker(List<AtomicBoolean> results, List<AtomicBoolean> ready, LoopScheduler scheduler) {
        System.out.println("Report thread2: " + Integer.toString(ThreadID.getStaticID()));
        LoopRange range = scheduler.getChunk(ThreadID.getStaticID());
        System.out.println(" ls: " + range.loopStart + " le: " + range.loopEnd + " lstride: " + range.localStride + "i ");
        for (int i = range.loopStart; i < range.loopEnd; i += range.localStride) {

            try {
                //results.get(i).set(CloudVisionFaceDetector.imageHasFace(inputs.get(i)));
                System.out.println("FINSISHED");
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
                results.get(i).set(false);
            } finally {
                ready.get(i).set(true);
            }
        }
        return null;
    }

    private Void detectWorker(List<AtomicBoolean> results, LoopScheduler scheduler) {
        System.out.println("Report thread2: " + Integer.toString(ThreadID.getStaticID()));
        LoopRange range = scheduler.getChunk(ThreadID.getStaticID());
        System.out.println(" ls: " + range.loopStart + " le: " + range.loopEnd + " lstride: " + range.localStride + "i ");
        for (int i = range.loopStart; i < range.loopEnd; i += range.localStride) {
            System.out.println("ls: " + range.loopStart + "le: " + range.loopEnd + "lstride: " + range.localStride + "i " + i);
            try {
                //results.get(i).set(CloudVisionFaceDetector.imageHasFace(inputs.get(i)));
                System.out.println("FINSISHED");
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
                results.get(i).set(false);
            } finally {
                processed.add(i);
            }
        }
        return null;
    }

    @Override
    public List<AtomicBoolean> runSequential() {
        List<AtomicBoolean> detections = new ArrayList<>();
        for (ByteArray input : inputs) {
            detections.add(new AtomicBoolean(detect(input)));
        }
        return detections;
    }

    @Override
    public boolean runParallel(List<AtomicBoolean> results) {
        LoopScheduler scheduler = LoopSchedulerFactory
                .createLoopScheduler(
                        0,
                        results.size(),
                        1,
                        10,
                        AbstractLoopScheduler.LoopCondition.LessThan,
                        LoopSchedulerFactory.LoopSchedulingType.Static
                );

        @Future(taskType = TaskInfoType.MULTI_IO, taskCount = 10/*, reduction="union"*/)
        @AsyncCatch(throwables={RuntimeException.class}, handlers={"handleRuntimeEx()"})
        Void v = detectWorker(results, scheduler);

        System.out.println(v);

        return true;
    }

    public boolean runParallel(List<AtomicBoolean> results, List<AtomicBoolean> ready) {
        LoopScheduler scheduler = LoopSchedulerFactory
                .createLoopScheduler(
                        0,
                        results.size(),
                        1,
                        10,
                        AbstractLoopScheduler.LoopCondition.LessThan,
                        LoopSchedulerFactory.LoopSchedulingType.Static
                );

        @Future(taskType = TaskInfoType.MULTI_IO, taskCount = 10/*, reduction="union"*/)
        @AsyncCatch(throwables={RuntimeException.class}, handlers={"handleRuntimeEx()"})
        Void v = detectWorker(results, ready, scheduler);

        System.out.println(v);

        return true;
    }

    @Override
    public List<AtomicBoolean> createResultsContainer() {
        List<AtomicBoolean> detections = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            detections.add(new AtomicBoolean(true));
        }
        return detections;
    }

    public List<AtomicBoolean> createReadyContainer() {
        List<AtomicBoolean> ready = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            ready.add(new AtomicBoolean(true));
        }
        return ready;
    }

    private static void handleRuntimeEx() {

    }
}
