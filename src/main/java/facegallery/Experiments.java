package facegallery;

import apt.annotations.Future;
import apt.annotations.TaskInfoType;
import facegallery.utils.ByteArray;
import pu.loopScheduler.LoopRange;
import pu.loopScheduler.LoopScheduler;
import pu.loopScheduler.ThreadID;

import java.io.File;
import java.nio.file.Files;

public class Experiments {
    private File[] getImageListFromDir(String filePath) {
        return new File(filePath).listFiles((File dir, String name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".gif");
        });
    }

    public ByteArray[] loadImagesAsync(String filePath) {
        File[] fileList = getImageListFromDir(filePath);

        @Future(taskType = TaskInfoType.INTERACTIVE)
        ByteArray[] imageBytes = new ByteArray[fileList.length];
        for (int i = 0; i < imageBytes.length; i++) {
            imageBytes[i] = worker(fileList[i]);
        }
        for (int i = 0; i < imageBytes.length; i++) {
            System.out.println(Integer.toString(i) + ". " + fileList[i].toString() + ": " + Boolean.toString(imageBytes[i] != null));
        }
        return imageBytes;
    }

    public ByteArray worker(File file) {
        try {
            return new ByteArray(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            return new ByteArray(null);
        }
    }

    public Boolean loadImagesWorker(File[] fileList, ByteArray[] imageBytes, LoopScheduler scheduler) {

        LoopRange range = scheduler.getChunk(ThreadID.getStaticID());
        System.out.println(String.format("Thread %d: %d, %d, %d", ThreadID.getStaticID(), range.loopStart, range.loopEnd, range.localStride));

        for (int i = range.loopStart; i < range.loopEnd; i += range.localStride) {
            try {
                imageBytes[i] = new ByteArray(Files.readAllBytes(fileList[i].toPath()));
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
            }
        }
        return true;
    }
}
