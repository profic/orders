package orders;

public class QueryCommand implements Runnable {

    private final OrdersContainer<Buyer>  buyers;
    private final OrdersContainer<Seller> sellers;
    private final String                  query;
    private final Prices                  prices;

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
        int priceBeginIdx = s.lastIndexOf(',') + 1;

        int price = Utils.parseInt(s, priceBeginIdx, s.length());
        int res   = prices.getPrice(price);
        System.out.println((Object) res);
    }

    private void showPrice(OrderActor entry) {
        if (entry == null) {
            System.out.println((Object) "empty");
        } else {
            int price = entry.price();
            System.out.println((Object) (price + "," + prices.getPrice(price)));
        }
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

}
