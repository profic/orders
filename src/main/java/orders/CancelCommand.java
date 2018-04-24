package orders;

public class CancelCommand implements Runnable {

    private final OrdersContainer<Buyer>  buyers;
    private final OrdersContainer<Seller> sellers;
    private final int                     id;

    public CancelCommand(final OrdersContainer<Buyer> buyers, final OrdersContainer<Seller> sellers, final int id) {
        this.buyers = buyers;
        this.sellers = sellers;
        this.id = id;
    }

    @Override
    public void run() {
        sellers.removeById(id);
        buyers.removeById(id);
    }
}
