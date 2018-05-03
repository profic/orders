package orders;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class ParseJob {

    public static final Runnable PARSE_END = () -> {};

    private final ExecutorService         executor;
    private final BlockingQueue<Runnable> parsedArr;
    private final OrdersContainer         bids;
    private final OrdersContainer         asks;

    public ParseJob(
            final ExecutorService executor,
            final int size,
            final OrdersContainer bids,
            final OrdersContainer asks
    ) {
        this.executor = executor;
        this.parsedArr = new LinkedBlockingQueue<>(size);
        this.bids = bids;
        this.asks = asks;
    }


    public Future<?> parse(final BlockingQueue<String> readArr) {
        return executor.submit(() -> {
            boolean run = true;

            BlockingQueue<Runnable> parsedArr = this.parsedArr;
            while (run) {
                String s;
                while ((s = readArr.poll()) != null) {
                    if (ReadJob.END.equals(s)) {
                        run = false;
                        break;
                    }
                    parsedArr.add(doParse(s));
                }
                Thread.yield();
            }
            parsedArr.add(PARSE_END);
        });
    }

    private Runnable doParse(final String s) {
        Runnable res   = null;
        char     sType = s.charAt(0);
        if (sType == 'o') {
            res = processOrder(s);
        } else if (sType == 'u') {
            res = update(s);
        } else if (sType == 'q') {
            res = query(s);
        }
        return res;
    }

    private QueryCommand query(final String s) {
        return new QueryCommand(bids, asks, s);
    }

    private Runnable update(final String s) {
        int len = s.length();

        OrdersContainer orders = s.charAt(len - 1) == 'd' ? bids : asks;

        int priceStartIdx = 2;
        int priceEndIdx   = s.indexOf(',', priceStartIdx + 1);
        int sizeStartIdx  = priceEndIdx + 1;
        int sizeEndIdx    = len - 4;

        Integer price = Utils.parseInt(s, priceStartIdx, priceEndIdx);
        Integer size  = Utils.parseInt(s, sizeStartIdx, sizeEndIdx);
        return () -> orders.setSize(price, size);
    }

    private Runnable processOrder(final String s) {
        char orderType = s.charAt(2);
        if (orderType == 's') {
            return parse(s, bids);
        } else {
            return parse(s, asks);
        }
    }

    private Runnable parse(
            final String s,
            final OrdersContainer orders
    ) {
        int len         = s.length();
        int endPriceIdx = s.lastIndexOf(',', len - 2);

        int endSizeIdx   = len;
        int beginSizeIdx = endPriceIdx + 1;

        int size = Utils.parseInt(s, beginSizeIdx, endSizeIdx);

        return () -> {
            int decreased;
            for (int rest = size; rest != 0; rest -= decreased) {
                decreased = orders.decreaseSizeAtFirstPrice(size);
            }
        };
    }

    public BlockingQueue<Runnable> getParsedQueue() {
        return parsedArr;
    }
}
