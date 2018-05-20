package facegallery.tasks;

public class TaskerStats {
    public LastAction lastAction;
    public double fileReadRuntime;
    public double thumbnailGenerateRuntime;
    public double faceDetectionRuntime;
    public double imageRescaleRuntime;
    public double totalRuntime;

    public TaskerStats(
            LastAction lastAction,
            double fileReadRuntime,
            double thumbnailGenerateRuntime,
            double faceDetectionRuntime,
            double imageRescaleRuntime,
            double totalRuntime
    ) {
        this.lastAction = lastAction;
        this.fileReadRuntime = fileReadRuntime;
        this.thumbnailGenerateRuntime = thumbnailGenerateRuntime;
        this.faceDetectionRuntime = faceDetectionRuntime;
        this.imageRescaleRuntime = imageRescaleRuntime;
        this.totalRuntime = totalRuntime;
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
