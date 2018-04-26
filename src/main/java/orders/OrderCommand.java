package orders;

public class OrderCommand implements Runnable {

    private final Heap<Buyer>  buyers;
    private final Heap<Seller> sellers;
    private final OrderActor              order;
    private final Prices                  prices;

    public static StopWatch sw = new StopWatch();

    public OrderCommand(
            final Heap<Buyer> buyers,
            final Heap<Seller> sellers,
            final OrderActor order,
            final Prices prices) {
        this.buyers = buyers;
        this.sellers = sellers;
        this.order = order;
        this.prices = prices;
    }

    @Override
    public void run() {
//        sw.start();

        OrderActor order = this.order;
        if (order instanceof Buyer) {
            buy((Buyer) order);
        } else {
            sell((Seller) order);
        }

//        sw.stop();
    }

    public static void reset() {
        sw = new StopWatch();
    }

    private void sell(final Seller seller) {
        Heap<Buyer> buyers = this.buyers;

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

        int decreaseSize = Math.min(seller.size(), buyer.size());

//        System.out.println("decreaseSize = " + decreaseSize + ", buyer = " + buyer + ", seller = " + seller);

        prices.decrease(decreasePrice, decreaseSize);
        int oldBuyerSize = buyer.size();
        buyer.decreaseSize(seller.size());
        seller.decreaseSize(oldBuyerSize);
    }

    private void buy(final Buyer buyer) {
        Heap<Seller> sellers = this.sellers;

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
}
