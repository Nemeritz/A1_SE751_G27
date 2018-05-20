package facegallery.tasks;

import apt.annotations.Gui;
import facegallery.FaceGallery;

import java.awt.image.BufferedImage;
import java.util.function.Function;

public class Tasker {
    private TaskerStats sequentialStats;
    private TaskerStats parallelStats;
    private BufferedImage[] displayImages;

    public void performSequential(Function<TaskerStats, Void> statsUpdater, Function<BufferedImage[], Void> imagesUpdater) {
        ImageBytesReader imageBytesReader = new ImageBytesReader(FaceGallery.TEST_DATASET_DIR);
        @Gui
        Void sync = statsUpdater.apply(sequentialStats);
    }

    public void performParallel(Function<TaskerStats, Void> statsUpdater, Function<BufferedImage[], Void> imagesUpdater) {
        @Gui
        Void sync = statsUpdater.apply(parallelStats);
    }
}
