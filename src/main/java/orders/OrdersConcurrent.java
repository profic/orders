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
import java.util.stream.Stream;

public class OrdersConcurrent {

    private final int PRICES_COUNT = 10_000;
    private final int IDS_COUNT    = 1_000_000 + 1;

    private final Comparator<Buyer>  BUYERS_COMPARATOR = Comparator.comparingInt(Buyer::price).reversed();
    private final Prices             prices            = new Prices(PRICES_COUNT);
    private final IContainer<Buyer>  buyers            = new HeapContainer<>(BUYERS_COMPARATOR, IDS_COUNT);
    private final IContainer<Seller> sellers           = new HeapContainer<>(Comparator.comparingInt(Seller::price), IDS_COUNT);

    static Path path = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data2.txt");

    public static void main(String[] args) throws Exception {
        OrdersConcurrent o = new OrdersConcurrent();
        o.doWorkConcurrent(Executors.newFixedThreadPool(2));
    }

    public void doWork() throws IOException {
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(this::processLine);
        }
    }

    private void processLine(final String s) {
        char   sType = s.charAt(0);
        if (sType == 'o') processOrder(s);
        else if (sType == 'c') cancelOrder(s);
        else if (sType == 'q') processQuery(s);
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

    private void cancelOrder(final String s) {
        int id = Utils.parseInt(s, 2, s.length());
        cancelOrder(id);
    }

    private void cancelOrder(final int id) {
        sellers.removeById(id);
        buyers.removeById(id);
    }

    private void processOrder(final String s) {
        int  endIdIdx  = s.indexOf(',', 2);
        char orderType = s.charAt(endIdIdx + 1);

        if (orderType == 's') {
            sell(s, endIdIdx);
        } else {
            buy(s, endIdIdx);
        }
    }

    public void buy(final String s, final int endIdIdx) {
        Buyer buyer = parse(s, endIdIdx, Ctor.BUYER);
        buy(buyer);
    }

    public void buy(final Buyer buyer) {
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

    public <T extends OrderEntry> T parse(
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
    public void sell(final String s, final int endIdIdx) {
        Seller seller = parse(s, endIdIdx, Ctor.SELLER);
        sell(seller);
    }

    public void sell(final Seller seller) {
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

    private void print(Object o) {
        if (false == true) { // todo: remove
//        if (true) {
            System.out.println(o);
        }
    }


    private static final String END       = "END";
    private static final Object PARSE_END = new Object();

    private final AtomicReferenceArray<String> readArr   = new AtomicReferenceArray<>(IDS_COUNT + 1);
    private final AtomicReferenceArray<Object> parsedArr = new AtomicReferenceArray<>(IDS_COUNT + 1);

    ExecutorService e = null;

    public void doWorkConcurrent(ExecutorService e) throws Exception {
        this.e = e;
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
        int pos = 0;
        while (PARSE_END != parsedArr.get(pos)) {
            Object e;
            while ((e = parsedArr.get(pos)) != null) {
                if (PARSE_END == e) {
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
                pos++;
            }
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
        return e.submit(() -> {
            readLatch.await();
            parseLatch.countDown();
            int pos = 0;
            while (!END.equals(readArr.get(pos))) {
                String s;
                while ((s = readArr.get(pos)) != null) {
                    if (END.equals(s)) {
                        break;
                    }
                    parsedArr.set(pos, doParse(s));
                    pos++;
                }
            }
            parsedArr.set(pos, PARSE_END);
            return null;
        });
    }

    private <T extends OrderEntry> T processOrder2(final String s) {
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
            res = processOrder2(s);
        } else if (sType == 'c') {
            res = Utils.parseInt(s, 2, s.length());
        } else if (sType == 'q') {
            res = s;
        }

        return res;
    }

    private Future<?> read(final CountDownLatch readLatch) {
        return e.submit(() -> {
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
