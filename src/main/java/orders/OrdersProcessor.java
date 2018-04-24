package orders;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class OrdersProcessor {

    private static final int    PRICES_COUNT = 10_000;
    private static final int    IDS_COUNT    = 1_000_000 + 1;
    private static final String END          = "END";
    private static final Object PARSE_END    = new Object();

    private final Comparator<Buyer>       BUYERS_COMPARATOR = Comparator.comparingInt(Buyer::price).reversed();
    private final Prices                  prices            = new Prices(PRICES_COUNT);
    private final OrdersContainer<Buyer>  buyers            = new HeapContainer<>(BUYERS_COMPARATOR, IDS_COUNT);
    private final OrdersContainer<Seller> sellers           = new HeapContainer<>(Comparator.comparingInt(Seller::price), IDS_COUNT);

    private final AtomicReferenceArray<String> readArr   = new AtomicReferenceArray<>(IDS_COUNT + 1);
    private final AtomicReferenceArray<Object> parsedArr = new AtomicReferenceArray<>(IDS_COUNT + 1);

    private final ExecutorService executor;
    private final Path            path;

    public OrdersProcessor(final ExecutorService executor, String file) {
        this.executor = executor;
        this.path = Paths.get(file);
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            OrdersProcessor o = new OrdersProcessor(executor, "c:\\Users\\Uladzislau_Malchanau\\Desktop\\data2.txt");
            o.run();
        } finally {
            executor.shutdown();
        }
    }

    private void processQuery(final String s) {
        if (isBuyerQuery(s)) {
            showPrice(buyers.first());
        } else if (isSellerQuery(s)) {
            showPrice(sellers.first());
        } else {
            showPrice(s);
        }
    }

    private boolean isSellerQuery(final String s) {
        return s.charAt(3) == 'e';
    }

    private boolean isBuyerQuery(final String s) {
        return s.charAt(2) == 'b';
    }

    private void showPrice(final String s) {
        int priceBeginIdx = s.lastIndexOf(',') + 1;

        int price = Utils.parseInt(s, priceBeginIdx, s.length());
        print(prices.getPrice(price));
    }

    private void showPrice(OrderEntry entry) {
        if (entry == null) {
            print("empty");
        } else {
            int price = entry.price();
            print(price + "," + prices.getPrice(price));
        }
    }

    private void cancelOrder(final int id) {
        sellers.removeById(id);
        buyers.removeById(id);
    }

    private void buy(final Buyer buyer) {
        OrdersContainer<Seller> sellers = this.sellers;

        Seller seller = sellers.first();
        while (seller != null && seller.price() <= buyer.price() && buyer.hasItems()) {
            buy(buyer, seller, seller.price());
            if (!seller.hasItems()) {
                sellers.removeFirst();
                seller = sellers.first();
            }
        }

        if (buyer.hasItems()) {
            buyers.add(buyer);
            prices.increase(buyer.price(), buyer.size());
        }
    }

    private <T extends OrderEntry> T parse(
            final String s,
            final int endIdIdx,
            Ctor ctor
    ) {
        int len           = s.length();
        int beginPriceIdx = s.indexOf(',', endIdIdx + 1) + 1;
        int endPriceIdx   = s.lastIndexOf(',', len - 2);

        int beginIdIdx   = 2;
        int endSizeIdx   = len;
        int beginSizeIdx = endPriceIdx + 1;

        int id    = Utils.parseInt(s, beginIdIdx, endIdIdx);
        int price = Utils.parseInt(s, beginPriceIdx, endPriceIdx);
        int size  = Utils.parseInt(s, beginSizeIdx, endSizeIdx);

        return (T) ctor.create(id, size, price);
    }

    private void sell(final Seller seller) {
        OrdersContainer<Buyer> buyers = this.buyers;

        Buyer buyer = buyers.first();
        while (buyer != null && seller.hasItems() && buyer.price() >= seller.price()) {
            buy(buyer, seller, buyer.price());
            if (!buyer.hasItems()) {
                buyers.removeFirst();
                buyer = buyers.first();
            }
        }

        if (seller.hasItems()) {
            prices.increase(seller.price(), seller.size());
            sellers.add(seller);
        }
    }

    private void buy(Buyer buyer, Seller seller, int decreasePrice) {
        prices.decrease(decreasePrice, Math.min(seller.size(), buyer.size()));
        int oldBuyerSize = buyer.size();
        buyer.decreaseSize(seller.size());
        seller.decreaseSize(oldBuyerSize);
    }

    // todo: cleanup
    private void print(Object o) {
        if (false == true) { // todo: remove
//        if (true) {
            System.out.println(o);
        }
    }

    public void run() throws Exception {
        CountDownLatch readLatch  = new CountDownLatch(1);
        CountDownLatch parseLatch = new CountDownLatch(1);

        Future<?>      readFuture  = read(readLatch);
        Future<Object> parseFuture = parse(readLatch, parseLatch);
        process(parseLatch);

        readFuture.get();
        parseFuture.get();
    }

    private void process(final CountDownLatch parseLatch) throws Exception {
        parseLatch.await();
        int                          position  = 0;
        boolean                      run       = true;
        AtomicReferenceArray<Object> parsedArr = this.parsedArr;
        while (run) {
            Object e;
            while ((e = parsedArr.get(position)) != null) {
                if (PARSE_END == e) {
                    run = false;
                    break;
                }
                if (isCancelOrder(e)) {
                    cancelOrder((Integer) e);
                } else if (isQuery(e)) {
                    processQuery((String) e);
                } else if (e instanceof Buyer) {
                    buy((Buyer) e);
                } else {
                    sell((Seller) e);
                }
                position++;
            }
            Thread.yield();
        }
    }

    private boolean isQuery(final Object e) {
        return e.getClass() == String.class;
    }

    private boolean isCancelOrder(final Object e) {
        return e.getClass() == Integer.class;
    }

    private Future<Object> parse(
            final CountDownLatch readLatch,
            final CountDownLatch parseLatch) {
        return executor.submit(() -> {
            readLatch.await();
            parseLatch.countDown();
            int                          position  = 0;
            boolean                      run       = true;
            AtomicReferenceArray<String> readArr   = this.readArr;
            AtomicReferenceArray<Object> parsedArr = this.parsedArr;
            while (run) {
                String s;
                while ((s = readArr.get(position)) != null) {
                    if (END.equals(s)) {
                        run = false;
                        break;
                    }
                    parsedArr.set(position, doParse(s));
                    position++;
                }
                Thread.yield();
            }
            parsedArr.set(position, PARSE_END);
            return null;
        });
    }

    private <T extends OrderEntry> T processOrder(final String s) {
        int  endIdIdx  = s.indexOf(',', 2);
        char orderType = s.charAt(endIdIdx + 1);
        if (orderType == 's') {
            return parse(s, endIdIdx, Ctor.SELLER);
        } else {
            return parse(s, endIdIdx, Ctor.BUYER);
        }
    }

    private Object doParse(final String s) {
        Object res   = null;
        char   sType = s.charAt(0);
        if (sType == 'o') {
            res = processOrder(s);
        } else if (sType == 'c') {
            res = Utils.parseInt(s, 2, s.length());
        } else if (sType == 'q') {
            res = s;
        }

        return res;
    }

    private Future<?> read(final CountDownLatch readLatch) {
        return executor.submit(() -> {
            readLatch.countDown();
            int pos = 0;
            try (BufferedReader b = Files.newBufferedReader(path)) {
                String line;
                while ((line = b.readLine()) != null) {
                    readArr.set(pos++, line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                readArr.set(pos, END);
            }
        });
    }
}
