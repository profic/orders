package orders;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Stream;

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

    @Setup(value = Level.Iteration)
    public void setup() {
        l = new ArrayList<>(1_000_000);
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
//        Path path = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data2.txt");
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
    public void readFile() throws Exception {
//        Path path = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");
        Path path = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data2.txt");
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> {});
        }
    }

    //        @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void readAndParse() throws Exception {
//        Path path = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");
        Path path = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data2.txt");
        try (Stream<String> lines = Files.lines(path)) {
            Orders o = new Orders();
            lines.forEach(s -> {
                if (s.startsWith("o")) {
                    doParse(o, s);
                }
            });
        }
    }

    static ExecutorService e = Executors.newFixedThreadPool(3);

    static String     END       = "END";
    static OrderEntry ORDER_END = new OrderEntry(-1, -1, -1) {};

    //    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void readFileAndParseConcurrent() throws Exception {
//        Path path = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");
        Path                        path = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data2.txt");
        LinkedBlockingQueue<String> q    = new LinkedBlockingQueue<>(1_000_000);

        Orders o = new Orders();

        AtomicBoolean done = new AtomicBoolean(false);

        Future<?> f = e.submit(() -> {
            try (Stream<String> lines = Files.lines(path)) {
                lines.forEach(s -> {
                    if (s.startsWith("o")) {
                        q.add(s);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                done.set(true);
            }
        });
        while (!done.get()) {
            while (!q.isEmpty()) {
                doParse(o, q.take());
            }
            Thread.yield();
//            Thread.sleep(1);
        }
        f.get();
    }

    //    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void readFileAndParseAndProcessConcurrent() throws Exception {
        Path                             path      = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data2.txt");
        AtomicReferenceArray<String>     readArr   = new AtomicReferenceArray<>(1_000_000);
        AtomicReferenceArray<OrderEntry> parsedArr = new AtomicReferenceArray<>(1_000_000);

        Orders o = new Orders();

        CountDownLatch latch = new CountDownLatch(1);
        Future<?> readFuture = e.submit(() -> {
            latch.countDown();
            int pos = 0;
            try (BufferedReader b = Files.newBufferedReader(path)) {
                String line;
                while ((line = b.readLine()) != null) {
                    if (line.startsWith("o")) {
                        readArr.set(pos++, line);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                readArr.set(pos, END);
            }
        });

        Future<Object> parseFuture = e.submit(() -> {
            latch.await();
            int pos = 0;
            while (!END.equals(readArr.get(pos))) {
                String s;
                while ((s = readArr.get(pos)) != null) {
                    if (END.equals(s)) {
                        break;
                    }
                    parsedArr.set(pos, doParse(o, s));
                    pos++;
                }
            }
            parsedArr.set(pos, ORDER_END);
            return null;
        });

        int pos = 0;
        while (ORDER_END != parsedArr.get(pos)) {
            OrderEntry e;
            while ((e = parsedArr.get(pos)) != null) {
                if (ORDER_END == e) {
                    break;
                }
                process(e, o);
                pos++;
            }
        }

        readFuture.get();
        parseFuture.get();
    }

//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void readFileAndParseConcurrentArrayBased() throws Exception {
//        Path path = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");
        doWork();
    }

    static class Test {
        public static void main(String[] args) throws Exception {
            try {
//                new OrderBenchmark().doWork();
                OrderBenchmark.doExtract();
            } finally {
                e.shutdown();
            }
        }
    }

    private void doWork() throws Exception {
        Path                         path = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data2.txt");
        AtomicReferenceArray<String> arr  = new AtomicReferenceArray<>(1_000_000);

        Orders o = new Orders();

        CountDownLatch latch = new CountDownLatch(1);
        Future<?> f = e.submit(() -> {
            latch.countDown();
            int pos = 0;
            try (BufferedReader b = Files.newBufferedReader(path)) {
                String line;
                while ((line = b.readLine()) != null) {
                    if (line.startsWith("o")) {
                        arr.set(pos++, line);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                arr.set(pos, END);
//                System.out.println("STOP READING FILE");
//                System.out.println("pos = " + pos);
            }
        });
        latch.await();
        int pos = 0;
//        System.out.println("START LOOP");
        while (!END.equals(arr.get(pos))) {
            String s;
            while ((s = arr.get(pos)) != null) {
                if (END.equals(s)) {
                    break;
                }
                doParse(o, s);
                pos++;
//                if (pos % 1000 == 0) {
//                    System.out.println("pos = " + pos);
//                }
            }
//            System.out.println("At " + pos + " is null");
//            Thread.yield();
//            Thread.sleep(1);
//            System.out.println("End is not at " + pos);
        }
//        System.out.println("DONE1");
        f.get();
//        System.out.println("DONE2");
    }

    //        @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void readAndParseAndProcess() throws Exception {
        Path path = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data2.txt");
        try (Stream<String> lines = Files.lines(path)) {
            Orders o = new Orders();
            lines.forEach(s -> {
                if (s.startsWith("o")) {
                    process(doParse(o, s), o);
                }
            });
        }
    }

    //        @Benchmark
//        @BenchmarkMode(Mode.AverageTime)
    public void parse() {
        Orders o = new Orders();
        for (String s : orderStrings) {
            OrderEntry order = doParse(o, s);
//            process(order);
        }
    }

    //        @Benchmark
//        @BenchmarkMode(Mode.AverageTime)
    public void processParsed() {
        for (OrderEntry order : orders) {
            process(order);
        }
    }

    private void process(final OrderEntry order) {
        process(order, o);
    }

    private static void process(final OrderEntry order, Orders o) {
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
            return o.parse(s.toCharArray(), endIdIdx, Ctor.SELLER);
        } else {
            return o.parse(s.toCharArray(), endIdIdx, Ctor.BUYER);
        }
    }


//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void extract() throws Exception {
        doExtract();
    }

    private static void doExtract() throws IOException, InterruptedException, ExecutionException {
        Path path = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data2.txt");

        int oneMillion = 1_000_000;

        AtomicReferenceArray<String> readArr = new AtomicReferenceArray<>(oneMillion);

        Orders o = new Orders();

        CountDownLatch readLatch  = new CountDownLatch(1);
        CountDownLatch parseLatch = new CountDownLatch(1);

        InAction<String> readAction = new InAction<>(END, e);

        BufferedReader b = Files.newBufferedReader(path);
        Future<?> readFuture = readAction.run(
                pos -> {
                    try {
                        String s = b.readLine();
                        if (s == null) {
                            readArr.set(pos, END);
                            return END;
                        } else {
                            return s;
                        }
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }
                }, readArr::set,
                Utils.toCallable(readLatch::countDown)
        );

        Action<String, OrderEntry> parseAction = new Action<>(readArr, oneMillion, END, ORDER_END, e);

        Future<?> parseFuture = parseAction.run(
                s -> doParse(o, s),
                Utils.toCallable(() -> {
                    readLatch.await();
                    parseLatch.countDown();
                })
        );

        AtomicReferenceArray<OrderEntry> parsedArr = parseAction.getOutArr();

        InAction<OrderEntry> processAction = new InAction<>(ORDER_END, e);
        Future<?> processFuture = processAction.run(
                parsedArr::get,
                (pos, in2) -> process(in2, o),
                Utils.toCallable(parseLatch::await)
        );

        readFuture.get();
//        System.out.println("READ FUTURE COMPLETED");
        parseFuture.get();
//        System.out.println("PARSE FUTURE COMPLETED");
        processFuture.get();
    }

    //    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void singleThread() throws Exception {
        new OrdersConcurrent().doWork();
    }


//        @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void multipleThread() throws Exception {
        new OrdersConcurrent().doWorkConcurrent(e);
    }


//        @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
    public void multipleThreadRefactored() throws Exception {
        OrdersConcurrentAbstractTransfer o = new OrdersConcurrentAbstractTransfer();
        o.doWorkConcurrent(e);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void jcTools() throws Exception {
        OrdersConcurrentJcTools o = new OrdersConcurrentJcTools();
        o.doWorkConcurrent(e);
    }

    public static List<String> l = new ArrayList<>(1_000_000);

}