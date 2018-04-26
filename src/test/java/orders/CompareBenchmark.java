package orders;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Comparator;
import java.util.Random;

@State(Scope.Benchmark)
public class CompareBenchmark {

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(CompareBenchmark.class.getSimpleName())
                .forks(1)
                .threads(1)
                .warmupIterations(5)
                .measurementIterations(5)
                .build();
        new Runner(opt).run();

    }

    Comparator<Integer> reversed = ((Comparator<Integer>) Integer::compare).reversed();
    Comparator<Integer> cmp      = Integer::compare;

    Random r = new Random();

    @Benchmark
    public void manual(Blackhole bh) {
        bh.consume(r.nextInt() > r.nextInt());
    }

    @Benchmark
    public void integerParse(Blackhole bh) {
        bh.consume(Integer.compare(r.nextInt(), r.nextInt()) > 0);
    }

//    @Benchmark
    public void reversed(Blackhole bh) {
        bh.consume(reversed.compare(r.nextInt(), r.nextInt()) > 0);
    }

//    @Benchmark
    public void cmp(Blackhole bh) {
        bh.consume(cmp.compare(r.nextInt(), r.nextInt()) > 0);
    }

}
