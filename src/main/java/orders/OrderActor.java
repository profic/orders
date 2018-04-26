package orders;

public abstract class OrderActor {
    private final int id;
    private final int price;
    public boolean cancelled = false;

    private int size;

    public OrderActor(final int id, final int size, final int price) {
        this.id = id;
        this.size = size;
        this.price = price;
    }

    public int id() {
        return id;
    }

    public int size() {
        return size;
    }

    public int price() {
        return price;
    }

    public void increaseSize(int size) {
        this.size += size;
    }

    public void decreaseSize(int size) {
        this.size -= size;
    }

    public boolean hasItems() {
        return size > 0;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderActor orderActor = (OrderActor) o;
        return id == orderActor.id;
    }

    @Override public int hashCode() {
        return id;
    }

    @Override public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", price=" + price +
                ", cancelled=" + cancelled +
                ", size=" + size +
                '}';
    }
}
