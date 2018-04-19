package orders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Orders {

    private static final Pattern p = Pattern.compile("o,([0-9]+),b,([0-9]+),([0-9]+)");


    public static void main(String[] args) {
        test();
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

    private static void doWork() throws IOException {
        Files.lines(Paths.get("")).forEach(Orders::processLine);
    }

    private static void processLine(final String s) {
        if (s.startsWith("o")) processOrder(s);
        else if (s.startsWith("c")) cancelOrder(s);
        else if (s.startsWith("q")) processQuery(s);
    }


    static final int                PRICES_COUNT = 10_000;
    static final int                IDS_COUNT    = 1_000_000;
    static       IContainer<Buyer>  buyers       = new Container<>(new TreeMap<>((o1, o2) -> Integer.compare(o2, o1)), IDS_COUNT);
    static       IContainer<Seller> sellers      = new Container<>(new TreeMap<>(), IDS_COUNT);
    static       Prices             prices       = new Prices(PRICES_COUNT);

    public static void processQuery(final String s) {
        if (s.charAt(2) == 'b') {
            showBuyer();
        } else if (s.charAt(3) == 'e') {
            showSeller();
        } else {
            showPrice(s);
        }

    }

    private static void showPrice(final String s) {
        int price = Integer.parseInt(s.substring(s.lastIndexOf(',') + 1, s.length()));
        System.out.println(prices.getPrice(price));
    }

    private static void showBuyer() {
        Buyer buyer = buyers.first();
        if (buyer == null) {
            System.out.println("empty");
        } else {
            int price = buyer.price();
            System.out.println(price + "," + prices.getPrice(price));
        }
    }

    private static void showSeller() {
        Seller seller = sellers.first();
        if (seller == null) {
            System.out.println("empty");
        } else {
            int price = seller.price();
            System.out.println(price + "," + prices.getPrice(price));
        }
    }

    private static void cancelOrder(final String s) {
//        buyers.removeById(Integer.parseInt(s));
        int id = Integer.parseInt(s.substring(s.indexOf(",") + 1));
        // todo: optimize: create arrays with size 1M and check whether such id is present
        sellers.removeById(id);
        buyers.removeById(id);
    }

    private static void processOrder(final String s) {
        int endIdIdx  = s.indexOf(',', 2);
        int orderType = endIdIdx + 1;

        if (s.charAt(orderType) == 's') {
            sell(s, endIdIdx);
        } else {
            buy(s, endIdIdx);
        }
    }

    private static void buy(final String s, final int endIdIdx) {
//        Buyer buyer = parseBuyerRegex(s); // todo:
        Buyer buyer = parseBuyer(s, endIdIdx);
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


    public static Buyer parseBuyerRegex(final String s) {
        Matcher m = p.matcher(s);
        if (m.matches()) {
            int id    = Integer.parseInt(m.group(1));
            int price = Integer.parseInt(m.group(2));
            int size  = Integer.parseInt(m.group(3));
            return new Buyer(id, size, price);
        } else {
            throw new RuntimeException();
        }
    }

    interface Function3<In1, In2, In3, Out> {
        Out apply(In1 a1, In2 a2, In3 a3);
    }

    public static <T extends OrderEntry> T parse(
            final String s,
            final int endIdIdx,
            final Function3<Integer, Integer, Integer, T> f
    ) {
        int beginIdIdx    = 2;
        int beginPriceIdx = s.indexOf(',', endIdIdx + 1) + 1;
        int endPriceIdx   = s.lastIndexOf(',', s.length() - 2); // todo: s.length() - 2 maybe superfluous
        int endSizeIdx    = s.length();
        int beginSizeIdx  = endPriceIdx + 1;

        int id    = Integer.parseInt(s.substring(beginIdIdx, endIdIdx));
        int price = Integer.parseInt(s.substring(beginPriceIdx, endPriceIdx));
        int size  = Integer.parseInt(s.substring(beginSizeIdx, endSizeIdx));

        return f.apply(id, size, price);
    }

    public static Buyer parseBuyer(final String s, final int endIdIdx) {
        return parse(s, endIdIdx, Buyer::new);
    }

    public static Seller parseSeller(final String s, final int endIdIdx) {
        return parse(s, endIdIdx, Seller::new);
    }

    private static void sell(final String s, final int endIdIdx) {
        Seller seller = parseSeller(s, endIdIdx);
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

}
