package orders;

public interface IContainer<T extends Priceable & IntIdentifiable> {
    void add(T el);

    void removeFirst();

    T first();

    void removeById(Integer id);

    void remove(T el);

    boolean isEmpty();

    int size();
}
