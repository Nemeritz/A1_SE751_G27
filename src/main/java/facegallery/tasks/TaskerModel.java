package facegallery.tasks;

import java.awt.image.BufferedImage;

public class TaskerModel {
    public LastAction lastAction;
    public Stats fileReadStats;
    public Stats thumbnailGenerateStats;
    public Stats faceDetectionStats;
    public Stats imageRescaleStats;
    public BufferedImage[] displayImages;
    public double totalRuntime;

    public TaskerModel(
            LastAction lastAction,
            Stats fileReadStats,
            Stats thumbnailGenerateStats,
            Stats faceDetectionStats,
            Stats imageRescaleStats,
            BufferedImage[] displayImages,
            double totalRuntime
    ) {
        this.lastAction = lastAction;
        this.fileReadStats = fileReadStats;
        this.thumbnailGenerateStats = thumbnailGenerateStats;
        this.faceDetectionStats = faceDetectionStats;
        this.imageRescaleStats = imageRescaleStats;
        this.displayImages = displayImages;
        this.totalRuntime = totalRuntime;
    }

    public class Stats {
        public double runtime;
        public int taskDone;
        public int taskTotal;

        public Stats(int taskDone, int taskTotal, double runtime) {
            this.taskDone = taskDone;
            this.taskTotal = taskTotal;
            this.runtime = runtime;
        }
    }

    public enum LastAction {
        IDLE,
        FILE_READ,
        THUMBNAIL,
        FACE_DETECT,
        RESCALE;

        public String toDisplayString() {
            switch (this.ordinal()) {
                case 0:
                    return "Idle";
                case 1:
                    return "Files read";
                case 2:
                    return "Thumbnails generated";
                case 3:
                    return "Faces detected";
                case 4:
                    return "Non-faces dimmed";
                default:
                    return "";
            }
        }
    }
}
