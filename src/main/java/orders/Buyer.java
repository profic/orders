package orders;

public class Buyer extends OrderEntry<Buyer> {

    public Buyer(final int id, final int size, final int price) {
        super(id, size, price);
    }

    @Override public int compareTo(final Buyer o) {
        return Integer.compare(o.price, price);
    }
}
