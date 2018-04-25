package orders;

public class CommandFactory {

    private final Prices                  prices;
    private final OrdersContainer<Buyer>  buyers;
    private final OrdersContainer<Seller> sellers;

    public CommandFactory(
            final Prices prices,
            final OrdersContainer<Buyer> buyers,
            final OrdersContainer<Seller> sellers) {

        this.prices = prices;
        this.buyers = buyers;
        this.sellers = sellers;
    }

    public Runnable createCommand(Object e) {
        Runnable command;
        if (isCancelOrder(e)) {
            command = new CancelCommand(buyers, sellers, (Integer) e, prices);
        } else if (isQuery(e)) {
            command = new QueryCommand(buyers, sellers, (String) e, prices);
        } else {
            command = new OrderCommand(buyers, sellers, (OrderActor) e, prices);
        }
        return command;
    }

    private boolean isQuery(final Object e) {
        return e.getClass() == String.class;
    }

    private boolean isCancelOrder(final Object e) {
        return e.getClass() == Integer.class;
    }
}
