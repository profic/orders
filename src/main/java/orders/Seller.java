package orders;

public class Seller implements Comparable<Seller>, IntIdentifiable, Priceable {
    // todo: incapsulate and make final
    public int id;
    public int size;
    public int price;

    @Override public String toString() {
        return "{" +
                "id=" + id +
                ", size=" + size +
                ", price=" + price +
                '}';
    }

    @Override public int compareTo(final Seller o) {
        return Integer.compare(price, o.price);
    }

    public Seller(final int id, final int size, final int price) {
        this.id = id;
        this.size = size;
        this.price = price;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seller seller = (Seller) o;
        return id == seller.id;
    }

    @Override public int hashCode() {
        return id;
    }

    @Override public int id() {
        return id;
    }

    public boolean hasItems() {
        return size > 0;
    }

    @Override public int price() {
        return price;
    }
}
