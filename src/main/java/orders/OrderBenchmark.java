package orders;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@State(Scope.Benchmark)
public class OrderBenchmark {

    public static final int warmUpIterations      = 20;
    public static final int measurementIterations = 20;

    public static void main(String[] args) throws RunnerException {
//        runWithProfiler();
        runPlain();
    }

    private static void runPlain() throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(OrderBenchmark.class.getSimpleName())
                .warmupIterations(warmUpIterations)
                .measurementIterations(measurementIterations)
                .forks(1)
                .build();
        new Runner(opt).run();
    }


    @Setup(value = Level.Invocation)
    public void setup() {
        l = new ArrayList<>(1_000_000);
    }

    static List<String> eq;

    static {
        try {

            eq = Files.readAllLines(Paths.get("C:\\Projects\\orders\\tree.txt"));
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    static ExecutorService e = Executors.newFixedThreadPool(3);

    static String     END       = "END";
    static OrderEntry ORDER_END = new OrderEntry(-1, -1, -1) {};


//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void singleThread() throws Exception {
        new OrdersConcurrent().doWork();
//        checkEquality();
    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void multipleThread() throws Exception {
        new OrdersConcurrent().doWorkConcurrent(e);
//        checkEquality();
    }

//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void multipleThreadReturnString() throws Exception {
        new OrdersConcurrent().doWorkConcurrent(e);
//        checkEquality();
    }

    private static void checkEquality() {
        if (!l.equals(eq)) {
            throw new RuntimeException();
        }
    }

    static class Test {
        public static void main(String[] args) throws Exception {
            for (int i = 0; i < 10; i++) {
                new OrdersConcurrent().doWork();
                checkEquality();
                l = new ArrayList<>(1_000_000);
            }
        }
    }

    public static List<String> l = new ArrayList<>(1_000_000);

}