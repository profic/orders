package orders;

public class CancelCommand implements Runnable {

    private final OrdersContainer<Buyer>  buyers;
    private final OrdersContainer<Seller> sellers;
    private final Prices                  prices;
    private final int                     id;

    public static long time    = 0;
    public static long counter = 0;


    public CancelCommand(final OrdersContainer<Buyer> buyers, final OrdersContainer<Seller> sellers, final int id, final Prices prices) {
        this.buyers = buyers;
        this.sellers = sellers;
        this.id = id;
        this.prices = prices;
    }

    public static void reset() {
        time = 0;
        counter = 0;
    }

    @Override
    public void run() {
        counter++;

        long start = System.nanoTime();

        OrderActor res = sellers.removeById(id);
        if (res == null) {
            res = buyers.removeById(id);
        }
        if (res != null) {
            prices.decrease(res.price(), res.size());
        }

        long totalTime = System.nanoTime() - start;
        time += totalTime;
    }
}
