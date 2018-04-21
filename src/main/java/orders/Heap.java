package orders;

public interface Heap<Key extends OrderEntry> {
    boolean isEmpty();

    int size();

    Key first();

    void insert(Key x);

     Key delFirst();

    void remove(Key el);

    void removeById(int id);
}
