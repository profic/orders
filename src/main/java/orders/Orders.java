package orders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

public class Orders {

    private final int PRICES_COUNT = 10_000;
    private final int IDS_COUNT    = 1_000_000 + 1;

    private final Comparator<Buyer>  BUYERS_COMPARATOR = Comparator.comparingInt(Buyer::price).reversed();
    private final Prices             prices            = new Prices(PRICES_COUNT);
    private final IContainer<Buyer>  buyers            = new HeapContainer<>(BUYERS_COMPARATOR, IDS_COUNT);
    private final IContainer<Seller> sellers           = new HeapContainer<>(Comparator.comparingInt(Seller::price), IDS_COUNT);

    public static void main(String[] args) throws Exception {
//        test();
        new Orders().doWork();
    }

    private void test() {
        Arrays.asList(
                "o,0,b,95,40",
                "o,1,b,96,20",
                "q,buyers",
                "o,2,b,96,10",
                "o,3,s,101,300",
                "o,4,s,99,50",
                "o,5,s,99,10",
                "q,sellers",
                "o,6,s,99,15",
                "c,1",
                "o,7,s,91,30",
                "q,size,95"
        ).forEach(this::processLine);
    }

    public void doWork() throws IOException {
        Path path = Paths.get("C:\\Users\\Влад\\Desktop", "data2.txt");
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(this::processLine);
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

    private void buy(final String s, final int endIdIdx) {
        Buyer buyer = parseBuyer(s, endIdIdx);
        buy(buyer);
        if (buyer.hasItems()) {
            buyers.add(buyer);
            prices.increase(buyer.price(), buyer.size());
        }
    }

    private void buy(final Buyer buyer) {
        Seller seller = sellers.first();
        while (seller != null && seller.price() <= buyer.price() && buyer.hasItems()) {
            buy(buyer, seller, seller.price());
            if (!seller.hasItems()) {
                sellers.removeFirst();
                seller = sellers.first();
            }
        }
    }

    private <T extends OrderEntry> T parse(
            final String s,
            final int endIdIdx,
            final Function3<Integer, Integer, Integer, T> f
    ) {
        int beginIdIdx    = 2;
        int beginPriceIdx = s.indexOf(',', endIdIdx + 1) + 1;
        int endPriceIdx   = s.lastIndexOf(',', s.length() - 2); // todo: s.length() - 2 maybe superfluous
        int endSizeIdx    = s.length();
        int beginSizeIdx  = endPriceIdx + 1;

        int id    = Utils.parseInt(s.substring(beginIdIdx, endIdIdx));
        int price = Utils.parseInt(s.substring(beginPriceIdx, endPriceIdx));
        int size  = Utils.parseInt(s.substring(beginSizeIdx, endSizeIdx));

        return f.apply(id, size, price);
    }

    private Buyer parseBuyer(final String s, final int endIdIdx) {
        return parse(s, endIdIdx, Buyer::new);
    }

    private Seller parseSeller(final String s, final int endIdIdx) {
        return parse(s, endIdIdx, Seller::new);
    }

    private void sell(final String s, final int endIdIdx) {
        Seller seller = parseSeller(s, endIdIdx);
        sell(seller);

        if (seller.hasItems()) {
            prices.increase(seller.price(), seller.size());
            sellers.add(seller);
        }
    }

    private void sell(final Seller seller) {
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
