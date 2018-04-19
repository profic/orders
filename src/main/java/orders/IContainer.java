package orders;

public interface IContainer<T extends OrderEntry> {
    void add(T el);

    void removeFirst();

    T first();

    void removeById(Integer id);

    void remove(T el);

    boolean isEmpty();

    int size();
}
