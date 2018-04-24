package orders;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.IntFunction;

public class InAction<In> {
    private final In              inPoisonPill;
    private       ExecutorService e;

    public InAction(In inPoisonPill, ExecutorService e) {
        this.inPoisonPill = inPoisonPill;
        this.e = e;
    }

    public Future<?> run(IntFunction<In> inExtractor, IntF2<In> outConsumer, Callable<Void> beforeRun) {
        return e.submit(() -> {
//            System.out.println("IN CATION RUN");
            beforeRun.call();
            int pos = 0;

            boolean poisonPillReceived = false;
            while (!poisonPillReceived) {
                In in;
                while ((in = inExtractor.apply(pos)) != null) {
                    if (inPoisonPill.equals(in)) {
                        poisonPillReceived = true;
                        break;
                    }
                    outConsumer.apply(pos, in);
                    pos++;
                }
                Thread.yield();
            }
            return null;
        });
    }
}
