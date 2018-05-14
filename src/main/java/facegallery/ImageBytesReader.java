package facegallery;

import apt.annotations.Future;
import apt.annotations.Task;
import apt.annotations.TaskInfoType;
import facegallery.utils.ByteArray;
import pu.loopScheduler.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageBytesReader {
    public static List<ByteArray> getImagesFilesSequential(File[] files) {
        List<ByteArray> fileBytes = new ArrayList<>(files.length);

        for (File file : files) {
            try {
                fileBytes.add(new ByteArray(Files.readAllBytes(file.toPath())));
            }
            catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
                fileBytes.add(new ByteArray(null));
            }
        }

        return fileBytes;
    }

    @Task
    private static Void readFileWorker(File[] fileList, List<ByteArray> fileBytes, List<AtomicBoolean> readyFlags, LoopScheduler scheduler) {
        LoopRange range = scheduler.getChunk(ThreadID.getStaticID());

        for (int i = range.loopStart; i < range.loopEnd; i += range.localStride) {
            try {
                fileBytes.get(i).setBytes(Files.readAllBytes(fileList[i].toPath()));
                readyFlags.get(i).set(true);
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
                readyFlags.get(i).set(true);
            }
        }

        return null;
    }

    public static List<ByteArray> createImageBytesContainer(int fileListSize) {
        ArrayList<ByteArray> containers = new ArrayList<>(fileListSize);
        for (int i = 0; i < fileListSize; i++) {
            containers.add(new ByteArray(null));
        }
        return containers;
    }

    public static List<AtomicBoolean> createBytesReadyContainer(int fileListSize) {
        ArrayList<AtomicBoolean> containers = new ArrayList<>(fileListSize);
        for (int i = 0; i < fileListSize; i++) {
            containers.add(new AtomicBoolean(false));
        }
        return containers;
    }

    public static File[] getFileListFromDir(String filePath) {
        return new File(filePath).listFiles((File dir, String name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".gif");
        });
    }

    public static Void getImagesFilesParallel(File[] fileList, List<ByteArray> fileBytes, List<AtomicBoolean> readyFlags) {
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
        Void v = readFileWorker(fileList, fileBytes, readyFlags, scheduler);

        return null;
    }
}
