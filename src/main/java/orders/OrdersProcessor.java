package orders;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static orders.Utils.checkedRunnable;

public class OrdersProcessor {

    public static final  int CHUNK_CNT    = 10;
    private static final int PRICES_COUNT = 10_000;
    private static final int IDS_COUNT    = 1_000_000 + 1;
    public static final  int BUF_SIZE     = (int) Math.ceil(OrdersProcessor.IDS_COUNT / OrdersProcessor.CHUNK_CNT) + 1;

    private final Comparator<Buyer> BUYERS_COMPARATOR = Comparator.comparingInt(Buyer::price).reversed();

    private final Prices                  prices  = new Prices(PRICES_COUNT);
    private final OrdersContainer<Buyer>  buyers  = new HeapContainer<>(BUYERS_COMPARATOR, IDS_COUNT);
    private final OrdersContainer<Seller> sellers = new HeapContainer<>(Comparator.comparingInt(Seller::price), IDS_COUNT);

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
            OrdersProcessor o = new OrdersProcessor(executor, "c:\\Users\\Uladzislau_Malchanau\\Desktop\\data2.txt");
            o.run();
        } finally {
            executor.shutdown();
        }
    }

    public void run() throws Exception {
        CountDownLatch readLatch  = new CountDownLatch(1);
        CountDownLatch parseLatch = new CountDownLatch(1);

        ReadJob  readJob  = new ReadJob(IDS_COUNT + 1, path, executor);
        ParseJob parseJob = new ParseJob(executor);

        Future<?> readFuture = readJob.read(readLatch::countDown);
        Future<Object> parseFuture = parseJob.parse(checkedRunnable(() -> {
            readLatch.await();
            parseLatch.countDown();
        }), readJob.getReadArr());

        process(parseLatch, parseJob.getParsedArr());

        readFuture.get();
        parseFuture.get();
    }

    private void process(final CountDownLatch parseLatch, final AtomicReferenceArray<Object[]> parsedArr) throws Exception {
        parseLatch.await();
        int     position = 0;
        boolean run      = true;
        while (run) {
            Object[] buf;
            while ((buf = parsedArr.get(position)) != null) {
                if (ParseJob.PARSE_END == buf) {
                    run = false;
                    break;
                }
                for (Object o : buf) {
                    factory.createCommand(o).run();
                }
                position++;
            }
            Thread.yield();
        }
    }
}
