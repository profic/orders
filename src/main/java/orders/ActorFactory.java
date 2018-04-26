package orders;

public enum ActorFactory {

    BUYER {
        @Override public Buyer create(final int id, final int size, final int price) {
            return new Buyer(id, size, price);
        }
    }, SELLER {
        @Override public Seller create(final int id, final int size, final int price) {
            return new Seller(id, size, price);
        }
    };

    public abstract <T extends OrderActor> T create(int id, int size, int price);
}
