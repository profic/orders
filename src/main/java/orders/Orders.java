package orders;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

public class Orders {

    private final int PRICES_COUNT = 10_000;
    private final int IDS_COUNT    = 1_000_000 + 1;

    private final Comparator<Buyer>  BUYERS_COMPARATOR = Comparator.comparingInt(Buyer::price).reversed();
    private final Prices             prices            = new Prices(PRICES_COUNT);
    private final IContainer<Buyer>  buyers            = new HeapContainer<>(BUYERS_COMPARATOR, IDS_COUNT);
    private final IContainer<Seller> sellers           = new HeapContainer<>(Comparator.comparingInt(Seller::price), IDS_COUNT);

    public static void main(String[] args) throws Exception {
//        new Orders().doWorkSingleThread();
//        new Orders().doWorkConcurrent();

//        Path     path    = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");
//        String[] strings = Files.lines(path).toArray(String[]::new);
//        new Orders().doWorkSingleThreadPrepared(strings);

        new Orders().doWorkConcurrent2(10);
    }

    private final    BlockingQueue<String> q    = new ArrayBlockingQueue<>(IDS_COUNT);
    private volatile boolean               done = false;

    public void doWorkSingleThread() throws IOException {
        Path path = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(this::processLine);
        }
    }


    public void doWorkSingleThreadPrepared() throws IOException {
        Path path = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");
        try (Stream<String> lines = Files.lines(path)) {
            doWorkSingleThreadPrepared(lines.toArray(String[]::new));
        }
    }

    public void doWorkConcurrent() throws Exception {
        Path path = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");

        new Thread(() -> {
            try (Stream<String> lines = Files.lines(path)) {
                lines.forEach(q::add);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                done = true;
            }
        }).start();
        while (!done) {
            while (!q.isEmpty()) {
                processLine(q.take());
            }
        }
    }

    public void doWorkConcurrent2(final int chunks) throws Exception {
        Path path = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");

        int arrSize = (int) Math.ceil((double) IDS_COUNT / chunks);

        BlockingQueue<String[]> q = new ArrayBlockingQueue<>(chunks);

        new Thread(() -> {
            try (BufferedReader r = Files.newBufferedReader(path)) {
                boolean _continue = true;
                for (int i = 0; i < chunks && _continue; i++) {
                    String[] buf = new String[arrSize];

                    for (int j = 0; j < arrSize && _continue; j++) {
                        String line = r.readLine();
                        if (line != null) {
                            buf[j] = line;
                        } else {
                            _continue = false;
                        }
                    }
                    q.add(buf);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                done = true;
            }
        }).start();

        while (!q.isEmpty() || !done) {
            doWorkSingleThreadPrepared(q.take());
        }
    }


    public void doWorkSingleThreadPrepared(String[] strings) {
        for (String string : strings) {
            if (string == null) {
                break;
            } else {
                processLine(string);
            }
        }
    }


    private void processLine(final String s) {
        if (s.startsWith("o")) processOrder(s);
        else if (s.startsWith("c")) cancelOrder(s);
        else if (s.startsWith("q")) processQuery(s);
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
        int priceEndIdx   = s.length();
        int price         = Utils.parseInt(s.substring(priceBeginIdx, priceEndIdx));
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
        int id = Utils.parseInt(s.substring(2));
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
        if (buyer.hasItems()) {
            buyers.add(buyer);
            prices.increase(buyer.price(), buyer.size());
        }
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
    }

    public <T extends OrderEntry> T parse(
            final String s,
            final int endIdIdx,
            Ctor ctor
    ) {

        char[] arr = s.toCharArray();

        int len           = arr.length;
        int beginIdIdx    = 2;
        int endPriceIdx   = -1;
        int beginPriceIdx = -1;
        int endSizeIdx    = len;
        int beginSizeIdx  = endPriceIdx + 1;

        for (int i = endIdIdx + 1; i < len; i++) {
            if (arr[i] == ',') {
                beginPriceIdx = i + 1;
                break;
            }
        }

        for (int i = len - 2; i > 0; i--) {
            if (arr[i] == ',') {
                endPriceIdx = i;
                break;
            }
        }

        int id    = Utils.parseInt(arr, beginIdIdx, endIdIdx);
        int price = Utils.parseInt(arr, beginPriceIdx, endPriceIdx);
        int size  = Utils.parseInt(arr, beginSizeIdx, endSizeIdx);

        return (T) ctor.create(id, size, price);
    }

//    public <T extends OrderEntry> T parse(
//            final String s,
//            final int endIdIdx,
//            Ctor ctor
//    ) {
//        int beginIdIdx    = 2;
//        int beginPriceIdx = s.indexOf(',', endIdIdx + 1) + 1;
//        int endPriceIdx   = s.lastIndexOf(',', s.length() - 2); // todo: s.length() - 2 maybe superfluous
//        int endSizeIdx    = s.length();
//        int beginSizeIdx  = endPriceIdx + 1;
//
//        int id    = Utils.parseInt(s.substring(beginIdIdx, endIdIdx));
//        int price = Utils.parseInt(s.substring(beginPriceIdx, endPriceIdx));
//        int size  = Utils.parseInt(s.substring(beginSizeIdx, endSizeIdx));
//
//        return (T) ctor.create(id, size, price);
//    }

    public enum Ctor {

        BUYER {
            @Override Buyer create(final int id, final int size, final int price) {
                return new Buyer(id, size, price);
            }
        }, SELLER {
            @Override Seller create(final int id, final int size, final int price) {
                return new Seller(id, size, price);
            }
        };

        abstract <T extends OrderEntry> T create(int id, int size, int price);
    }

    public void sell(final String s, final int endIdIdx) {
        Seller seller = parse(s, endIdIdx, Ctor.SELLER);
        sell(seller);

        if (seller.hasItems()) {
            prices.increase(seller.price(), seller.size());
            sellers.add(seller);
        }
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
    }

    private void buy(Buyer buyer, Seller seller, int decreasePrice) {
        prices.decrease(decreasePrice, Math.min(seller.size(), buyer.size()));
        int oldSize = buyer.size();
        buyer.decreaseSize(seller.size());
        seller.decreaseSize(oldSize);
    }

    private void print(Object o) {
        if (false == true) { // todo: remove
//        if (true) {
            System.out.println(o);
        }
    }
}
