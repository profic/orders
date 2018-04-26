package orders;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class OrdersProcessor {

    private static final int PRICES_COUNT = 10_000;
    private static final int IDS_COUNT    = 1_000_000 + 1;

    private final Prices       prices  = new Prices(PRICES_COUNT);
    private final Heap<Buyer>  buyers  = HeapContainer.forBuyer(IDS_COUNT);
    private final Heap<Seller> sellers = HeapContainer.forSeller(IDS_COUNT);

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
//            OrdersProcessor o = new OrdersProcessor(executor, args[0]);
            OrdersProcessor o = new OrdersProcessor(executor, Utils.ORDERS_PATH);
//            OrdersProcessor o = new OrdersProcessor(executor, "c:\\Users\\Uladzislau_Malchanau\\Desktop\\test_data.txt");
            o.run();
        } finally {
            executor.shutdown();
        }
    }

    public void run() throws Exception {
        CountDownLatch readLatch  = new CountDownLatch(1);
        CountDownLatch parseLatch = new CountDownLatch(1);

        ReadJob  readJob  = new ReadJob(IDS_COUNT + 1, path, executor);
        ParseJob parseJob = new ParseJob(executor, IDS_COUNT + 1);

        Future<?> readFuture = readJob.read(readLatch::countDown);
        Future<Object> parseFuture = parseJob.parse(() -> {
            readLatch.await();
            parseLatch.countDown();
        }, readJob.getReadArr());

        process(parseLatch, parseJob.getParsedArr());

        readFuture.get();
        parseFuture.get();
    }

    private void process(final CountDownLatch parseLatch, final AtomicReferenceArray<Object> parsedArr) throws Exception {
        parseLatch.await();
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
