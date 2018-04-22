package orders;

import com.google.common.collect.ImmutableMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;

@State(Scope.Benchmark)
public class BuyerParseBenchmark {

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(BuyerParseBenchmark.class.getSimpleName())
                .forks(1)
                .threads(1)
                .warmupIterations(20)
                .measurementIterations(5)
//                .warmupIterations(5)
//                .measurementIterations(5)
                .build();
        new Runner(opt).run();
    }

//        @Param({"2", "3", "4", "5", "6"})
    @Param({"6"})
    int idLen;

//        @Param({"2", "3", "4", "5", "6"})
    @Param({"6"})
    int priceLen;


    String s;
    int    idxSndComma;
    Random r = new Random();

    ImmutableMap<Integer, Integer> m = ImmutableMap.of(
            2, 99,
            3, 999,
            4, 9999,
            5, 99999,
            6, 999999
    );

//    @Setup(Level.Invocation)
    @Setup(Level.Iteration)
    public void setup() {
        s = String.format("o,%s,b,%s,40", r.nextInt(m.get(idLen)), r.nextInt(m.get(priceLen)));
        idxSndComma = s.indexOf(',', 2);
    }

    @Benchmark
    public void plain() {
        Orders o = new Orders();
        o.parse(s.toCharArray(), idxSndComma, Orders.Ctor.BUYER);
    }

}
