package orders;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class OrdersProcessor {

    private static final int PRICES_COUNT = 10_000;
    private static final int IDS_COUNT    = 1_000_000 + 1;

    private final Prices       prices  = new Prices(PRICES_COUNT);
    private final Heap<Buyer>  buyers  = new OrdersMaxHeapIntKey<>(IDS_COUNT + 1);
    private final Heap<Seller> sellers = new OrdersMinHeapIntKey<>(IDS_COUNT + 1);

    private final CommandFactory factory = new CommandFactory(
            prices, buyers, sellers
    );

    private final ExecutorService executor;
    private final Path            path;

    public OrdersProcessor(final ExecutorService executor, String file) {
        this.executor = executor;
        this.path = Paths.get(file);
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            OrdersProcessor o = new OrdersProcessor(executor, args[0]);
            o.run();
        } finally {
            executor.shutdown();
        }
    }

    public void run() throws Exception {

        ReadJob  readJob  = new ReadJob(IDS_COUNT + 1, path, executor);
        ParseJob parseJob = new ParseJob(executor, IDS_COUNT + 1);

        Future<?> readFuture  = readJob.read();
        Future<?> parseFuture = parseJob.parse(readJob.getReadArr());

        process(parseJob.getParsedArr());

        readFuture.get();
        parseFuture.get();
    }

    private void process(final AtomicReferenceArray<Object> parsedArr) {
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
}
