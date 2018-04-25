package orders;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static java.util.stream.Collectors.joining;

@State(Scope.Benchmark)
public class SeparateStepsBenchmark {
    public static final Comparator<Seller>           SELLER_COMPARATOR = Comparator.comparingInt(Seller::price);
    private             AtomicReferenceArray<String> readArr;
    private             AtomicReferenceArray<Object> parsedArr;

    public static void main(String[] args) throws RunnerException {
        List<String> methodsToInclude = Arrays.asList(
//                "readAndParse"
                "parseAndProcess"
        );
        String include = methodsToInclude
                .stream()
                .map(s -> ".*SeparateStepsBenchmark." + s)
                .collect(joining("|"));

        Options opt = new OptionsBuilder()
                .include(include)
                .warmupIterations(20)
                .measurementIterations(20)
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    private ExecutorService executor;

    @Setup
    public void setup() throws Exception {
        executor = Executors.newFixedThreadPool(2);

        CountDownLatch readLatch = new CountDownLatch(1);

        ReadJob   readJob    = new ReadJob(size, path, executor);
        Future<?> readFuture = readJob.read(readLatch::countDown);
        readFuture.get();
        this.readArr = readJob.getReadArr();
    }

    @Setup(value = Level.Invocation)
    public void before() throws Exception {
        l = new ArrayList<>(1_000_000);
//        if (!LogExecutionTime.methodExecutions.isEmpty()) {
//            LogExecutionTime.methodExecutions.forEach((k, v) -> System.out.println(k + ": " + v));
//            LogExecutionTime.methodExecutions = new HashMap<>();
//        }
        ParseJob parseJob = new ParseJob(executor, size);
        this.parsedArr = parseJob.getParsedArr();
        Future<Object> parseFuture = parseJob.parse(() -> { }, readArr);
        parseFuture.get();
    }

    @TearDown
    public void shutdown() {
        executor.shutdown();
    }

    int  size = 1_000_000 + 1;
    Path path = Paths.get(Utils.ORDERS_PATH);

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void readAndParse() throws Exception {

        CountDownLatch readLatch  = new CountDownLatch(1);
        CountDownLatch parseLatch = new CountDownLatch(1);

        ReadJob  readJob  = new ReadJob(size, path, executor);
        ParseJob parseJob = new ParseJob(executor, size);

        Future<?> readFuture = readJob.read(readLatch::countDown);
        Future<Object> parseFuture = parseJob.parse(() -> {
            readLatch.await();
            parseLatch.countDown();
        }, readJob.getReadArr());

        readFuture.get();
        parseFuture.get();
    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void read() throws Exception {

        ReadJob   readJob    = new ReadJob(size, path, executor);
        Future<?> readFuture = readJob.read(() -> {});
        readFuture.get();
    }

    private static void checkEquality() {
        if (!l.equals(eq)) {
            System.out.println("l.size() = " + l.size());
            System.out.println("eq.size() = " + eq.size());
//            throw new RuntimeException();
        }
    }

    static List<String> eq;

    static {
        try {
            eq = Files.readAllLines(Paths.get("C:\\Projects\\orders\\tree.txt"));
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void parse() throws Exception {
        Future<Object> parseFuture = doParse();
        parseFuture.get();
    }

    private Future<Object> doParse() {
        ParseJob parseJob = new ParseJob(executor, size);

        return parseJob.parse(() -> { }, readArr);
    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void process() throws Exception {
        AtomicReferenceArray<Object> parsedArr = this.parsedArr;
        process(parsedArr);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void parseAndProcess() throws Exception {
        ParseJob parseJob = new ParseJob(executor, size);

        Future<Object> parseFuture = parseJob.parse(() -> { }, readArr);

        process(parseJob.getParsedArr());

        parseFuture.get();

        checkEquality();
    }

    static class T {
        public static void main(String[] args) throws Exception {
            SeparateStepsBenchmark b = new SeparateStepsBenchmark();

            b.setup();
            for (int i = 0; i < 10; i++) {
                b.before();
                b.parseAndProcessFromDifferentSources();
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void parseAndProcessFromDifferentSources() throws Exception {
        AtomicReferenceArray<Object> parsedArr     = this.parsedArr;
        Future<?>                    processFuture = executor.submit(() -> process(parsedArr));

        Future<Object> parseFuture = doParse();
        parseFuture.get();
        processFuture.get();
        checkEquality();
    }

    private void process(final AtomicReferenceArray<Object> parsedArr) {
        CommandFactory factory = new CommandFactory(
                new Prices(10_001),
                new HeapContainer<>(BUYERS_COMPARATOR, size),
                new HeapContainer<>(SELLER_COMPARATOR, size)
        );

        int     position = 0;
        boolean run      = true;
        while (run) {
            Object e;
            while ((e = parsedArr.get(position)) != null) {
                if (ParseJob.PARSE_END == e) {
                    run = false;
                    break;
                }
                factory.createCommand(e).run();
                position++;
            }
            Thread.yield();
        }
    }

    public static List<String> l = new ArrayList<>(1_000_000);

    private static final Comparator<Buyer> BUYERS_COMPARATOR = Comparator.comparingInt(Buyer::price).reversed();


}
