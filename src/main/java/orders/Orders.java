package orders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Orders {

    private static final Pattern pattern = Pattern.compile("o,([0-9]+),[b,s],([0-9]+),([0-9]+)");

    private final int PRICES_COUNT = 10_000;
    private final int IDS_COUNT    = 1_000_000;

    private final IContainer<Buyer>  buyers  = new Container<>(new TreeMap<>((o1, o2) -> Integer.compare(o2, o1)), IDS_COUNT);
    private final IContainer<Seller> sellers = new Container<>(new TreeMap<>(), IDS_COUNT);
    private final Prices             prices  = new Prices(PRICES_COUNT);

    public static void main(String[] args) throws Exception {
        new Orders().doWork();
    }

    private void doWork() throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get("c:\\Users\\Uladzislau_Malchanau\\Desktop\\data2.txt"))) {
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
        int price         = Integer.parseInt(s.substring(priceBeginIdx, priceEndIdx));
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
        int id = Integer.parseInt(s.substring(s.indexOf(",") + 1));
        sellers.removeById(id);
        buyers.removeById(id);
    }

    private void processOrder(final String s) {
        int  endIdIdx  = s.indexOf(',', 2);
        char orderType = s.charAt(endIdIdx + 1);

        if (orderType == 's') {
            sell(s);
        } else {
            buy(s);
        }
    }

    private void buy(final String s) {
        Buyer buyer = parse(s, Buyer::new);
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
            final Function3<Integer, Integer, Integer, T> f
    ) {
        Matcher m = pattern.matcher(s);
        if (m.matches()) {
            int id    = Integer.parseInt(m.group(1));
            int price = Integer.parseInt(m.group(2));
            int size  = Integer.parseInt(m.group(3));
            return f.apply(id, size, price);
        } else {
            throw new RuntimeException();
        }
    }

    private void sell(final String s) {
        Seller seller = parse(s, Seller::new);
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
        if (true == false) {
            System.out.println(o);
        }
    }

}
