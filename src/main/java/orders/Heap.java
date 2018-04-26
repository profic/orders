package orders;

public interface Heap<E> {
    void add(E el);

    @SuppressWarnings("unchecked") E first();

    boolean remove(E el);

    int size();

    E removeFirst();

    E removeById(int id);

    boolean isEmpty();
}
