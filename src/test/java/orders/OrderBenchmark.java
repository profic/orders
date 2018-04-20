package orders;


import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class OrderBenchmark {
    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(OrderBenchmark.class.getSimpleName())
                .forks(1)
                .threads(1)
                .warmupIterations(20)
                .measurementIterations(5)
//                .warmupIterations(5)
//                .measurementIterations(5)
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void test() throws Exception {
        Orders.doWork();
    }
}
