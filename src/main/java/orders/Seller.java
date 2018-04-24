package orders;

public class Seller extends OrderEntry<Seller> {
    public Seller(final int id, final int size, final int price) {
        super(id, size, price);
    }

    @Override public int compareTo(final Seller o) {
        return Integer.compare(price, o.price);
    }
}
