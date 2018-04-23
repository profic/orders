package orders;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;
import java.util.function.IntFunction;

public class Action<In, Out> {
    private final AtomicReferenceArray<In> inArr;

    public AtomicReferenceArray<Out> getOutArr() {
        return outArr;
    }

    private final AtomicReferenceArray<Out> outArr;
    private final In                        inPoisonPill;
    private final Out                       outPoisonPill;
    private final ExecutorService           e;

    public Action(final AtomicReferenceArray<In> inArr, final int size, In inPoisonPill, Out outPoisonPill, ExecutorService e) {
        this.inArr = inArr;
        this.outArr = new AtomicReferenceArray<>(size);
        this.inPoisonPill = inPoisonPill;
        this.outPoisonPill = outPoisonPill;
        this.e = e;
    }

    public Future<?> run(final Function<In, Out> function, Callable<Void> beforeRun) {
        return e.submit(() -> {
//            System.out.println("ACTION RUN");
            beforeRun.call();
            int pos = 0;
            while (!inPoisonPill.equals(inArr.get(pos))) {
                In s;
                while ((s = inArr.get(pos)) != null) {
//                    System.out.println("READING...");
                    if (inPoisonPill.equals(s)) {
//                        System.out.println("GOT POISON PILL: " + s);
                        break;
                    }
                    outArr.set(pos, function.apply(s));
                    pos++;
                }
            }
//            System.out.println("ACTION COMPLETED");
            outArr.set(pos, outPoisonPill);
            return null;
        });
    }
}


class InAction<In> {
    private final In inPoisonPill;
    private ExecutorService e;

    public InAction(In inPoisonPill, ExecutorService e) {
        this.inPoisonPill = inPoisonPill;
        this.e = e;
    }

    public Future<?> run(IntFunction<In> inExtractor, IntF2<In> outConsumer, Callable<Void> beforeRun) {
        return e.submit(() -> {
//            System.out.println("IN CATION RUN");
            beforeRun.call();
            int pos = 0;
            while (!inPoisonPill.equals(inExtractor.apply(pos))) {
                In in;
                while ((in = inExtractor.apply(pos)) != null) {
                    if (inPoisonPill.equals(in)) {
                        break;
                    }
                    outConsumer.apply(pos, in);
                    pos++;
                }
            }
            return null;
        });
    }
}


@FunctionalInterface
interface IntF2<In> {
    void apply(int in1, In in2);
}
