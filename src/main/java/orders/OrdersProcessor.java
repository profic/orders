package orders;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class OrdersProcessor {

    private static final int IDS_COUNT = 1_000_000 + 1;

    private final OrdersContainer bids = new OrdersContainer(((Comparator<Integer>) Integer::compare).reversed());
    private final OrdersContainer asks = new OrdersContainer();

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
            OrdersProcessor o = new OrdersProcessor(executor, "C:\\Users\\Uladzislau_Malchanau\\Desktop\\data3.txt");
            o.run();
        } finally {
            executor.shutdown();
        }
    }

    public void run() throws Exception {

        ReadJob  readJob  = new ReadJob(IDS_COUNT + 1, path, executor);
        ParseJob parseJob = new ParseJob(executor, IDS_COUNT + 1, bids, asks);

        Future<?> readFuture  = readJob.read();
        Future<?> parseFuture = parseJob.parse(readJob.getReadArr());

        process(parseJob.getParsedArr());

        readFuture.get();
        parseFuture.get();
    }

    private void process(final AtomicReferenceArray<Runnable> parsedArr) {
        int     position = 0;
        boolean run      = true;
        while (run) {
            Runnable e;
            while ((e = parsedArr.get(position)) != null) {
                if (ParseJob.PARSE_END == e) {
                    run = false;
                    break;
                }
                e.run();
                position++;
            }
            Thread.yield();
        }
    }
}
