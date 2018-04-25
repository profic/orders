package orders;

public interface OrdersContainer<T extends OrderActor> {
    void add(T el);

    void removeFirst();

    T first();

    T removeById(int id);

    void remove(T el);

    boolean isEmpty();

    int size();
}
