package orders;

public enum Ctor {

    BUYER {
        @Override Buyer create(final int id, final int size, final int price) {
            return new Buyer(id, size, price);
        }
    }, SELLER {
        @Override Seller create(final int id, final int size, final int price) {
            return new Seller(id, size, price);
        }
    };

    abstract <T extends OrderEntry> T create(int id, int size, int price);
}
