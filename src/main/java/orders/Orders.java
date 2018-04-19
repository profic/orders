package orders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Orders {

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


    public static final int                PRICES_COUNT = 10_000;
    public static final int                IDS_COUNT    = 1_000_000;
    static              IContainer<Buyer>  buyers       = new Container<>(new TreeMap<>((o1, o2) -> Integer.compare(o2, o1)), IDS_COUNT);
    static              IContainer<Seller> sellers      = new Container<>(new TreeMap<>(), IDS_COUNT);
    static              Prices             prices       = new Prices();

    public static void processQuery(final String s) {
        if (s.charAt(2) == 'b') {
            // Print the highest price of the order book that contains buy order(s)
            showBuyer();
        } else if (s.charAt(3) == 'e') {
            // Print the lowest price of the order book that contains sell order(s),
            showSeller();
        } else {
            // Print total size at specified price.
            // q,size,95
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
//        if (s.charAt(4) == 's') { // charAt will not always work
        // "o,0,b,95,40"

        int idxSndComma = s.indexOf(',', 2);

        if (s.charAt(idxSndComma + 1) == 's') { // charAt will not always work
            sell(s, idxSndComma);
        } else {
            buy(s, idxSndComma);
        }
    }

    private static void buy(final String s, final int idxSndComma) {
        Buyer buyer = parseBuyerRegex(s);
        buy(buyer);
        if (buyer.hasItems()) {
            buyers.add(buyer);
            prices.increase(buyer.price(), buyer.size);
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

    // "o,0,b,95,40"
    private static final Pattern p = Pattern.compile("o,([0-9]+),b,([0-9]+),([0-9]+)");

    public static Buyer parseBuyerRegex(final String s) {
        // todo: compare with regex

        Matcher m = p.matcher(s);
        if (m.matches()) {
            int id = Integer.parseInt(m.group(1));
            int price = Integer.parseInt(m.group(2));
            int size = Integer.parseInt(m.group(3));
            return new Buyer(id, size, price);
        } else {
            throw new RuntimeException();
        }
    }

    public static Buyer parseBuyer(final String s, final int idxSndComma) {
        // todo: compare with regex
        int idxThrdComma   = s.indexOf(',', idxSndComma + 1);
        int idxFourthComma = s.lastIndexOf(',', s.length() - 1); // todo: s.length() - 2 maybe superfluous

        int id    = Integer.parseInt(s.substring(2, idxSndComma));
        int price = Integer.parseInt(s.substring(idxThrdComma + 1, idxFourthComma));
        int size  = Integer.parseInt(s.substring(idxFourthComma + 1, s.length()));

        return new Buyer(id, size, price);
    }

    public static Seller parseSeller(final String s, final int idxSndComma) {
        // todo: compare with regex
        int idxThrdComma   = s.indexOf(',', idxSndComma + 1);
        int idxFourthComma = s.lastIndexOf(',', s.length() - 2); // todo: s.length() - 2 maybe superfluous

        int id    = Integer.parseInt(s.substring(2, idxSndComma));
        int price = Integer.parseInt(s.substring(idxThrdComma + 1, idxFourthComma));
        int size  = Integer.parseInt(s.substring(idxFourthComma + 1, s.length()));

        return new Seller(id, size, price);
    }

    private static void sell(final String s, final int idxSndComma) {
        Seller seller = parseSeller(s, idxSndComma);
        sell(seller);

        if (seller.hasItems()) {
            prices.increase(seller.price(), seller.size);
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
        prices.decrease(buyer.price(), Math.min(seller.size, buyer.size));
        int oldSize = buyer.size;
        buyer.size -= seller.size;
        seller.size -= oldSize;
    }

}
