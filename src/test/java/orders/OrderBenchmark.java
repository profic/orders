package orders;


import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@State(Scope.Benchmark)
public class OrderBenchmark {

    public static final int warmUpIterations      = 10;
    public static final int measurementIterations = 10;

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

    private static void runWithProfiler() throws RunnerException {
        String userDir = System.getProperty("user.dir");
        final String destinationFolder = System.getProperty("basedir",
                                                            userDir) + "/target";
        final String profile = System.getProperty("basedir", userDir) + "/src/main/jfc/profile.jfc";

        Options opt = new OptionsBuilder()
                .include(OrderBenchmark.class.getSimpleName())
                .addProfiler(JmhFlightRecorderProfiler.class)
                .jvmArgs("-Xmx256m", "-Xms256m", "-XX:+UnlockCommercialFeatures",
                         "-Djmh.stack.profiles=" + destinationFolder,
                         "-Djmh.executor=FJP",
                         "-Djmh.fr.options=defaultrecording=true,settings=" + profile)
                .result(destinationFolder + "/" + "benchmarkResults.csv")
                .resultFormat(ResultFormatType.CSV)
                .warmupIterations(warmUpIterations)
                .measurementIterations(measurementIterations)
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    //    @Benchmark
    public void test0() throws Exception {
        new Orders().doWorkSingleThread();
    }

    //    @Benchmark
    public void test() throws Exception {
        new Orders().doWorkConcurrent();
    }

    private static String[]         strings;
    private static Orders           o            = new Orders();
    private static List<OrderEntry> orders       = new ArrayList<>();
    private static List<String>     orderStrings = new ArrayList<>();

    static {
        Path path = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");
        try (Stream<String> lines = Files.lines(path)) {
            strings = lines.toArray(String[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String s : strings) {
            if (s.startsWith("o")) {
                OrderEntry e = doParse(o, s);
                orders.add(e);
                orderStrings.add(s);
            }
        }
    }


    //    @Benchmark
    public void test2() throws Exception {
        new Orders().doWorkSingleThreadPrepared(strings);
    }

    //        @Benchmark
    public void test4() throws Exception {
        new Orders().doWorkSingleThreadPrepared();
    }

//    @Param({"2", "4", "6", "8", "10", "20", "50"})
//    int chunks;

    //    @Benchmark
    public void test5() throws Exception {
        new Orders().doWorkConcurrent2(10);
    }


    //    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void test3() throws Exception {
        Path path = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> {});
        }
    }

    @Benchmark
    public void testParse() {
        Orders o = new Orders();
        for (String s : orderStrings) {
            OrderEntry order = doParse(o, s);
//            process(order);
        }
    }

//    @Benchmark
    public void processParsed() {
        for (OrderEntry order : orders) {
            process(order);
        }
    }

    private void process(final OrderEntry order) {
        if (order instanceof Buyer) {
            o.buy((Buyer) order);
        } else {
            o.sell((Seller) order);
        }
    }

    private static OrderEntry doParse(final Orders o, final String s) {
        int  endIdIdx  = s.indexOf(',', 2);
        char orderType = s.charAt(endIdIdx + 1);
        if (orderType == 's') {
            return o.parse(s, endIdIdx, false);
        } else {
            return o.parse(s, endIdIdx, true);
        }
    }


    private static OrderEntry doParse2(final Orders o, final String s) {
        int  endIdIdx  = s.indexOf(',', 2);
        char orderType = s.charAt(endIdIdx + 1);
        if (orderType == 's') {
            return o.parse2(s, endIdIdx, Orders.Ctor.SELLER);
        } else {
            return o.parse2(s, endIdIdx, Orders.Ctor.BUYER);
        }
    }
}
