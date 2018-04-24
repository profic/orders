package orders;

public class QueryCommand implements Runnable{

    private final OrdersContainer<Buyer>  buyers;
    private final OrdersContainer<Seller> sellers;
    private final String                  query;
    private final Prices                  prices;

    public QueryCommand(final OrdersContainer<Buyer> buyers, final OrdersContainer<Seller> sellers, final String query, final Prices prices) {
        this.buyers = buyers;
        this.sellers = sellers;
        this.query = query;
        this.prices = prices;
    }

    private void showPrice(final String s) {
        int priceBeginIdx = s.lastIndexOf(',') + 1;

        int price = Utils.parseInt(s, priceBeginIdx, s.length());
        print(prices.getPrice(price));
    }

    private void showPrice(OrderActor entry) {
        if (entry == null) {
            print("empty");
        } else {
            int price = entry.price();
            print(price + "," + prices.getPrice(price));
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

    // todo: cleanup
    private void print(Object o) {
        if (false == true) { // todo: remove
//        if (true) {
            System.out.println(o);
        }
    }
}
