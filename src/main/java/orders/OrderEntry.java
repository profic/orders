package orders;

public abstract class OrderEntry<T extends OrderEntry> implements Comparable<T> {
    private final int id;
    protected final int price;

    private int size;

    public OrderEntry(final int id, final int size, final int price) {
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
        OrderEntry orderEntry = (OrderEntry) o;
        return id == orderEntry.id;
    }

    @Override public int hashCode() {
        return id;
    }

}
