package orders;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
public class IntHeapAddBenchmark {

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(IntHeapAddBenchmark.class.getSimpleName())
                .forks(1)
                .threads(1)
                .warmupIterations(10)
                .measurementIterations(5)
                .build();
        new Runner(opt).run();
    }

//    public static final int CNT = 1_000_000;
    public static final int CNT = 10_000;
    Random r        = new Random();
    int[]  shuffled = IntStream.rangeClosed(0, CNT).map(__ -> r.nextInt(Integer.MAX_VALUE)).toArray();
    DHeap dHeap;
    IntegerMinHeap minHeap;

    @Setup(Level.Invocation)
    public void setup() {
        dHeap = new DHeap(4, CNT + 100);
        minHeap = new IntegerMinHeap(CNT);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void dHeapAdd() {
        for (int j = 0; j < 100; j++) {
            for (int i : shuffled) {
                dHeap.insert(i);
            }
            for (int i = 0; i < CNT; i++) {
                dHeap.deleteMin();
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void minHeapAdd() {
        for (int j = 0; j < 100; j++) {
            for (int i : shuffled) {
                minHeap.add(i);
            }
            for (int i = 0; i < CNT; i++) {
                minHeap.poll();
            }
        }
    }

}
