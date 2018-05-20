package facegallery.tasks;

import apt.annotations.Gui;

import java.awt.image.BufferedImage;
import java.util.function.Function;

public class Tasker {
    private TaskerStats sequentialStats;
    private TaskerStats parallelStats;
    private BufferedImage[] displayImages;

    public void performSequential(Function<TaskerStats, Void> statsUpdater, Function<BufferedImage[], Void> imagesUpdater) {
        @Gui
        Void sync = statsUpdater.apply(sequentialStats);
    }

    public void performParallel(Function<TaskerStats, Void> statsUpdater, Function<BufferedImage[], Void> imagesUpdater) {
        @Gui
        Void sync = statsUpdater.apply(parallelStats);
    }
}
