package orders;

public class CancelCommand implements Runnable {

    private final OrdersContainer<Buyer>  buyers;
    private final OrdersContainer<Seller> sellers;
    private final int                     id;

    public static long time = 0;
    public static long counter = 0;


    public CancelCommand(final OrdersContainer<Buyer> buyers, final OrdersContainer<Seller> sellers, final int id) {
        this.buyers = buyers;
        this.sellers = sellers;
        this.id = id;
    }

    public static void reset() {
        time = 0;
        counter = 0;
    }

    @Override
    public void run() {
        counter++;

        long start = System.nanoTime();

        sellers.removeById(id);
        buyers.removeById(id);

        long totalTime = System.nanoTime() - start;
        time += totalTime;
    }
}
