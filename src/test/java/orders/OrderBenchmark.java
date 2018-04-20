package orders;


import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class OrderBenchmark {
    public static void main(String[] args) throws RunnerException {
//        runWithProfiler();
        runPlain();
    }

    private static void runPlain() throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(OrderBenchmark.class.getSimpleName())
                .warmupIterations(10)
                .measurementIterations(10)
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
                .warmupIterations(10)
                .measurementIterations(10)
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void test() throws Exception {
        Orders.doWork();
    }
}
