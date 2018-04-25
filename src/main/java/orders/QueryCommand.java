package orders;

public class QueryCommand implements Runnable {

    private final OrdersContainer<Buyer>  buyers;
    private final OrdersContainer<Seller> sellers;
    private final String                  query;
    private final Prices                  prices;

    public static long showPriceForOrder        = 0;
    public static long showPriceForSize         = 0;
    public static long showPriceForOrderCounter = 0;
    public static long showPriceForSizeCounter  = 0;

    public QueryCommand(
            final OrdersContainer<Buyer> buyers,
            final OrdersContainer<Seller> sellers,
            final String query,
            final Prices prices
    ) {
        this.buyers = buyers;
        this.sellers = sellers;
        this.query = query;
        this.prices = prices;
    }

    private void showPrice(final String s) {
        long start         = System.nanoTime();
        int  priceBeginIdx = s.lastIndexOf(',') + 1;

        int price = Utils.parseInt(s, priceBeginIdx, s.length());
        print(prices.getPrice(price));
        long totalTime = System.nanoTime() - start;
        showPriceForOrder += totalTime;
        showPriceForOrderCounter++;
    }

    public static void reset() {
        showPriceForOrder = 0;
        showPriceForSize = 0;
        showPriceForOrderCounter = 0;
        showPriceForSizeCounter  = 0;
    }

    private void showPrice(OrderActor entry) {
        long start = System.nanoTime();
        if (entry == null) {
            print("empty");
        } else {
            int price = entry.price();
            print(price + "," + prices.getPrice(price));
        }
        long totalTime        = System.nanoTime() - start;
        showPriceForSize += totalTime;
        showPriceForSizeCounter++;
    }


    private boolean isSellerQuery(final String s) {
        return s.charAt(3) == 'e';
    }

    private boolean isBuyerQuery(final String s) {
        return s.charAt(2) == 'b';
    }

    @Override
    public void run() {
        if (isBuyerQuery(query)) {
            showPrice(buyers.first());
        } else if (isSellerQuery(query)) {
            showPrice(sellers.first());
        } else {
            showPrice(query);
        }
    }

    // todo: cleanup
    private void print(Object o) {
//        SeparateStepsBenchmark.l.add(o.toString());
        if (false == true) { // todo: remove
//        if (true) {
            System.out.println(o);
        }
    }
}
