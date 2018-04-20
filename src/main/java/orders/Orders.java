package orders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Stream;

public class Orders {

    private static final Buyer  buyer = new Buyer();
    private static final Seller seler = new Seller();

    private static final Comparator<Integer> BUYER_PRICE_COMPARATOR = (o1, o2) -> Integer.compare(o2, o1);

    public static void main(String[] args) throws Exception {
//        test();
        doWork();
    }

    private static void test() {
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
        ).forEach(Orders::processLine);
    }

    public static void doWork() throws IOException {
        Path path = Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop", "data2.txt");
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(Orders::processLine);
        }
    }

    private static void processLine(final String s) {
        if (s.startsWith("o")) processOrder(s);
        else if (s.startsWith("c")) cancelOrder(s);
        else if (s.startsWith("q")) processQuery(s);
    }

    private static final int    PRICES_COUNT = 10_000;
    private static final int    IDS_COUNT    = 1_000_000;
    private static final Prices prices       = new Prices(PRICES_COUNT);

    private static final IContainer<Buyer>  buyers  = new Container<>(new TreeMap<>(BUYER_PRICE_COMPARATOR), IDS_COUNT);
    private static final IContainer<Seller> sellers = new Container<>(new TreeMap<>(), IDS_COUNT);

    private static void processQuery(final String s) {
        if (isBuyerQuery(s)) {
            showBuyer();
        } else if (isSellerQuery(s)) {
            showSeller();
        } else {
            showPrice(s);
        }
    }

    private static boolean isSellerQuery(final String s) {
        return s.charAt(3) == 'e';
    }

    private static boolean isBuyerQuery(final String s) {
        return s.charAt(2) == 'b';
    }

    private static void showPrice(final String s) {
        int priceBeginIdx = s.lastIndexOf(',') + 1;
        int priceEndIdx   = s.length();
        int price         = Utils.parseInt(s.substring(priceBeginIdx, priceEndIdx));
        print(prices.getPrice(price));
    }

    private static void showBuyer() {
        Buyer buyer = buyers.first();
        if (buyer == null) {
            print("empty");
        } else {
            int price = buyer.price();
            print(price + "," + prices.getPrice(price));
        }
    }

    private static void showSeller() {
        Seller seller = sellers.first();
        if (seller == null) {
            print("empty");
        } else {
            int price = seller.price();
            print(price + "," + prices.getPrice(price));
        }
    }

    private static void cancelOrder(final String s) {
        int id = Utils.parseInt(s.substring(2));
        sellers.removeById(id);
        buyers.removeById(id);
    }

    private static void processOrder(final String s) {
        int  endIdIdx  = s.indexOf(',', 2);
        char orderType = s.charAt(endIdIdx + 1);

        if (orderType == 's') {
            sell(s, endIdIdx);
        } else {
            buy(s, endIdIdx);
        }
    }

    private static void buy(final String s, final int endIdIdx) {
        Buyer buyer = parse(s, endIdIdx, Orders.buyer);
        buy(buyer);
        if (buyer.hasItems()) {
            buyers.add(buyer);
            prices.increase(buyer.price(), buyer.size());
        }
    }

    private static void buy(final Buyer buyer) {
        Seller seller = sellers.first();
        while (seller != null && seller.price() <= buyer.price() && buyer.hasItems()) {
            buy(buyer, seller);
            if (!seller.hasItems()) {
                sellers.removeFirst();
                seller = sellers.first();
            }
        }
    }

    private static <T extends OrderEntry> T parse(
            final String s,
            final int endIdIdx,
            final T entry
    ) {
        int beginIdIdx    = 2;
        int beginPriceIdx = s.indexOf(',', endIdIdx + 1) + 1;
        int endPriceIdx   = s.lastIndexOf(',', s.length() - 2); // todo: s.length() - 2 maybe superfluous
        int endSizeIdx    = s.length();
        int beginSizeIdx  = endPriceIdx + 1;

        int id    = Utils.parseInt(s.substring(beginIdIdx, endIdIdx));
        int price = Utils.parseInt(s.substring(beginPriceIdx, endPriceIdx));
        int size  = Utils.parseInt(s.substring(beginSizeIdx, endSizeIdx));
        entry.id = id;
        entry.size = size;
        entry.price = price;
        return entry;
    }

    private static void sell(final String s, final int endIdIdx) {
        Seller seller = parse(s, endIdIdx, seler);
        sell(seller);

        if (seller.hasItems()) {
            prices.increase(seller.price(), seller.size());
            sellers.add(seller);
        }
    }

    private static void sell(final Seller seller) {
        Buyer buyer = buyers.first();
        while (buyer != null && seller.hasItems() && buyer.price() >= seller.price()) {
            buy(buyer, seller);
            if (!buyer.hasItems()) {
                buyers.removeFirst();
                buyer = buyers.first();
            }
        }
    }

    private static void buy(Buyer buyer, Seller seller) {
        prices.decrease(buyer.price(), Math.min(seller.size(), buyer.size()));
        int oldSize = buyer.size();
        buyer.decreaesSize(seller.size());
        seller.decreaesSize(oldSize);
    }

    private static void print(Object o) {
        if (false == true) {
            System.out.println(o);
        }
    }
}
