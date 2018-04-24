package orders;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class InAction2<In> {
    private final In              inPoisonPill;
    private       ExecutorService e;

    public InAction2(In inPoisonPill, ExecutorService e) {
        this.inPoisonPill = inPoisonPill;
        this.e = e;
    }

    public Future<?> run(Supplier<In> inExtractor, Consumer<In> outConsumer, Callable<Void> beforeRun) {
        return e.submit(() -> {
//            System.out.println("IN CATION RUN");
            beforeRun.call();
            boolean poisonPillReceived = false;
            while (!poisonPillReceived) {
                In in;
                while ((in = inExtractor.get()) != null) {
                    if (inPoisonPill.equals(in)) {
                        poisonPillReceived = true;
                        break;
                    }
                    outConsumer.accept(in);
                }
                Thread.yield();
            }
            return null;
        });
    }
}
