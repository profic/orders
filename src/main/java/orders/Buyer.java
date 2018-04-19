package orders;

public class Buyer implements Comparable<Buyer>, IntIdentifiable, Priceable {
    final int id;
    int size;
    final int price;

    public Buyer(final int id, final int size, final int price) {
        this.id = id;
        this.size = size;
        this.price = price;
    }

    @Override public int compareTo(final Buyer o) {
        return Integer.compare(o.price, price);
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Buyer buyer = (Buyer) o;
        return id == buyer.id;
    }

    @Override public int hashCode() {
        return id;
    }

    public boolean hasItems() {
        return size > 0;
    }

    @Override public int id() {
        return id;
    }

    @Override public int price() {
        return price;
    }
}
