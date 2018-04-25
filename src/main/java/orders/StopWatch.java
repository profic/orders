package orders;

public class StopWatch {

    private int  counter = 0;
    private long time    = 0;
    private long start   = 0;

    public long getAvg() {
        return time / counter;
    }

    public void start() {
        start = System.nanoTime();
    }

    public void stop() {
        counter++;
        time += System.nanoTime() - start;
    }
}
