package facegallery;

import apt.annotations.Future;
import apt.annotations.Task;
import apt.annotations.TaskInfoType;
import facegallery.utils.ByteArray;
import facegallery.utils.Parallelized;
import pu.loopScheduler.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageBytesReader extends Parallelized<Void, ByteArray> {
    private File[] fileList;

    public ImageBytesReader(String folderPath) {
        super(null);
        fileList = getImageListFromDir(folderPath);
    }

    @Override
    public List<ByteArray> runSequential() {
        List<ByteArray> fileBytes = new ArrayList<>(fileList.length);

        for (File file : fileList) {
            try {
                fileBytes.add(new ByteArray(Files.readAllBytes(file.toPath())));
            }
            catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
                fileBytes.add(new ByteArray(null));
            }
        }

        return fileBytes;
    }

    @Override
    public boolean runParallel(List<ByteArray> results) {
        LoopScheduler scheduler = LoopSchedulerFactory
                .createLoopScheduler(
                        0,
                        fileList.length,
                        1,
                        10,
                        AbstractLoopScheduler.LoopCondition.LessThan,
                        LoopSchedulerFactory.LoopSchedulingType.Static
                );

        @Future(taskType = TaskInfoType.MULTI_IO, taskCount = 10)
        Void v = readFileWorker(results, scheduler);

        return true;
    }

    public boolean runParallel(List<ByteArray> results, List<AtomicBoolean> ready) {
        LoopScheduler scheduler = LoopSchedulerFactory
                .createLoopScheduler(
                        0,
                        fileList.length,
                        1,
                        10,
                        AbstractLoopScheduler.LoopCondition.LessThan,
                        LoopSchedulerFactory.LoopSchedulingType.Static
                );

        @Future(taskType = TaskInfoType.MULTI_IO, taskCount = 10)
        Void v = readFileWorker(results, ready, scheduler);

        return true;
    }

    @Override
    public List<ByteArray> createResultsContainer() {
        ArrayList<ByteArray> containers = new ArrayList<>(fileList.length);
        for (int i = 0; i < fileList.length; i++) {
            containers.add(new ByteArray(null));
        }
        return containers;
    }

    public List<AtomicBoolean> createReadyContainer() {
        ArrayList<AtomicBoolean> containers = new ArrayList<>(fileList.length);
        for (int i = 0; i < fileList.length; i++) {
            containers.add(new AtomicBoolean(false));
        }
        return containers;
    }

    public File[] getFileList() {
        return fileList;
    }

    private File[] getImageListFromDir(String filePath) {
        return new File(filePath).listFiles((File dir, String name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".gif");
        });
    }

    private Void readFileWorker(List<ByteArray> results, List<AtomicBoolean> ready, LoopScheduler scheduler) {
        LoopRange range = scheduler.getChunk(ThreadID.getStaticID());
        System.out.println("Report thread: " + Integer.toString(ThreadID.getStaticID()));

        for (int i = range.loopStart; i < range.loopEnd; i += range.localStride) {
            try {
                results.get(i).setBytes(Files.readAllBytes(fileList[i].toPath()));
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
            } finally {
                ready.get(i).set(true);
            }
        }

        return null;
    }

    @Task
    private Void readFileWorker(List<ByteArray> results, LoopScheduler scheduler) {
        LoopRange range = scheduler.getChunk(ThreadID.getStaticID());

        for (int i = range.loopStart; i < range.loopEnd; i += range.localStride) {
            try {
                results.get(i).setBytes(Files.readAllBytes(fileList[i].toPath()));
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
            } finally {
                processed.add(i);
            }
        }

        return null;
    }
}
