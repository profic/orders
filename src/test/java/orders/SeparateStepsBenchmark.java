package orders;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

@State(Scope.Benchmark)
public class SeparateStepsBenchmark {
    public static final Comparator<Seller>           SELLER_COMPARATOR = Comparator.comparingInt(Seller::price);
    private             AtomicReferenceArray<String> readArr;
    private             AtomicReferenceArray<Object> parsedArr;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SeparateStepsBenchmark.class.getSimpleName())
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

        this.readArr = readJob.getReadArr();
        this.parsedArr = parseJob.getParsedArr();
    }

    @TearDown
    public void shutdown() {
        executor.shutdown();
    }

    int  size = 1_000_000 + 1;
    Path path = Paths.get("c:\\Users\\Влад\\Desktop\\data2.txt");

//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
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


//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void read() throws Exception {

        ReadJob   readJob    = new ReadJob(size, path, executor);
        Future<?> readFuture = readJob.read(() -> {});
        readFuture.get();
    }


//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void parse() throws Exception {
        Future<Object> parseFuture = doParse();
        parseFuture.get();
    }

    private Future<Object> doParse() {
        ParseJob parseJob = new ParseJob(executor, size);

        return parseJob.parse(() -> { }, readArr);
    }


//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void process() throws Exception {
        AtomicReferenceArray<Object> parsedArr = this.parsedArr;
        process(parsedArr);
    }

//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void parseAndProcess() throws Exception {
        ParseJob parseJob = new ParseJob(executor, size);

        Future<Object> parseFuture = parseJob.parse(() -> { }, readArr);

        process(parseJob.getParsedArr());

        parseFuture.get();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void parseAndProcessWaitABit() throws Exception {
        ParseJob parseJob = new ParseJob(executor, size);

        CountDownLatch latch = new CountDownLatch(1);

        Future<Object> parseFuture = parseJob.parse(latch::countDown, readArr);

        latch.await();
        process(parseJob.getParsedArr());

        parseFuture.get();
    }


//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void parseAndProcessFromDifferentSources() throws Exception {
        AtomicReferenceArray<Object> parsedArr     = this.parsedArr;
        Future<?>                    processFuture = executor.submit(() -> process(parsedArr));

        Future<Object> parseFuture = doParse();
        parseFuture.get();
        processFuture.get();
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

    private static final Comparator<Buyer> BUYERS_COMPARATOR = Comparator.comparingInt(Buyer::price).reversed();

    /*
    Future<?> readFuture = readJob.read(readLatch::countDown);
        Future<Object> parseFuture = parseJob.parse(() -> {
            readLatch.await();
            parseLatch.countDown();
        }, readJob.getReadArr());
     */

}
