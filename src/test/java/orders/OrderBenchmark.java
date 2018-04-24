package orders;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@State(Scope.Benchmark)
public class OrderBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(OrderBenchmark.class.getSimpleName())
                .warmupIterations(20)
                .measurementIterations(20)
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    private ExecutorService e;

    @Setup
    public void setup() {
        e = Executors.newFixedThreadPool(3);
    }

    @TearDown
    public void shutdown() {
        e.shutdown();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void multipleThread() throws Exception {
        new OrdersProcessor(e, "c:\\Users\\Uladzislau_Malchanau\\Desktop\\data2.txt").run();
    }
}