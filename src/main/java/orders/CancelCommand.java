package orders;

public class CancelCommand implements Runnable {

    private final OrdersContainer<Buyer>  buyers;
    private final OrdersContainer<Seller> sellers;
    private final Prices                  prices;
    private final int                     id;

    public static StopWatch sw = new StopWatch();


    public CancelCommand(final OrdersContainer<Buyer> buyers, final OrdersContainer<Seller> sellers, final int id, final Prices prices) {
        this.buyers = buyers;
        this.sellers = sellers;
        this.id = id;
        this.prices = prices;
    }

    public static void reset() {
        sw = new StopWatch();
    }

    @Override
    public void run() {
        sw.start();

        OrderActor res = sellers.removeById(id);
        if (res == null) {
            res = buyers.removeById(id);
        }
        if (res != null) {
            prices.decrease(res.price(), res.size());
        }

        sw.stop();
    }
}
