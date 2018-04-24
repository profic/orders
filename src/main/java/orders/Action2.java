package orders;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Action2<In, Out> {
    private final CircularBuffer<In> inArr;

    public CircularBuffer<Out> getOutArr() {
        return outArr;
    }

    private final CircularBuffer<Out> outArr;
    private final In                  inPoisonPill;
    private final Out                 outPoisonPill;
    private final ExecutorService     e;

    public Action2(final CircularBuffer<In> inArr, final int size, In inPoisonPill, Out outPoisonPill, ExecutorService e) {
        this.inArr = inArr;
        this.outArr = new CircularBuffer<>(size);
        this.inPoisonPill = inPoisonPill;
        this.outPoisonPill = outPoisonPill;
        this.e = e;
    }

    public Future<?> run(final Function<In, Out> function, Callable<Void> beforeRun) {
        return e.submit(() -> {
//            System.out.println("ACTION RUN");
            beforeRun.call();
            boolean poisonPillReceived = false;
            while (!poisonPillReceived) {
                In s;
                while ((s = inArr.peek()) != null) {
                    inArr.increaseGetPos();
//                    System.out.println("READING...");
                    if (inPoisonPill.equals(s)) {
                        poisonPillReceived = true;
                        break;
                    }
                    outArr.add(function.apply(s));
                }
                Thread.yield();
            }
//            System.out.println("ACTION COMPLETED");
            outArr.add(outPoisonPill);
            return null;
        });
    }
}