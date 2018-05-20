package facegallery.tasks;

public class TaskerStats {
    public LastAction lastAction;
    public Stats fileReadStats = new Stats();
    public Stats thumbnailGenerateStats = new Stats();
    public Stats faceDetectionStats = new Stats();
    public Stats imageRescaleStats = new Stats();
    public double totalRuntime;

    public TaskerStats(
            LastAction lastAction,
            Stats fileReadStats,
            Stats thumbnailGenerateStats,
            Stats faceDetectionStats,
            Stats imageRescaleStats,
            double totalRuntime
    ) {
        this.lastAction = lastAction;
        this.fileReadStats = fileReadStats;
        this.thumbnailGenerateStats = thumbnailGenerateStats;
        this.faceDetectionStats = faceDetectionStats;
        this.imageRescaleStats = imageRescaleStats;
        this.totalRuntime = totalRuntime;
    }

    public TaskerStats() {
    }

    public static class Stats {
        public int taskProgress;
        public int taskTotal;
        public double runtime;

        public Stats(int taskProgress, int taskTotal, double runtime) {
            this.taskProgress = taskProgress;
            this.taskTotal = taskTotal;
            this.runtime = runtime;
        }

        public Stats() {

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
