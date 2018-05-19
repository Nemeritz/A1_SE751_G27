package facegallery.tasks;

import apt.annotations.Gui;

import java.util.function.Function;

public class Tasker {
    private TaskerModel taskerModel;

    public void performSequential(Function<TaskerModel, Void> guiUpdater) {
        @Gui
        Void sync = guiUpdater.apply(taskerModel);
    }

    public void performParallel(Function<TaskerModel, Void> guiUpdater) {
        @Gui
        Void sync = guiUpdater.apply(taskerModel);
    }
}
