package orders;

public interface OrdersContainer<T extends OrderEntry> {
    void add(T el);

    void removeFirst();

    T first();

    void removeById(int id);

    void remove(T el);

    boolean isEmpty();

    int size();
}
