package facegallery.utils;

public class Timer {
    private long startTime = 0;
    private long endTime = 0;

    public void startTiming() {
        startTime = System.currentTimeMillis();
    }

    public void stopTiming() {
        endTime = System.currentTimeMillis();
    }

    public double getTime() {
        return (double)(endTime - startTime) / 1000;
    }
}
