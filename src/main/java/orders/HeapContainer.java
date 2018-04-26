package orders;

import java.util.Comparator;

public class HeapContainer<T extends OrderActor> implements OrdersContainer<T> {
    private final OrdersHeap<T> heap;

    public HeapContainer(Comparator<T> cmp, int capacity) {
        this.heap = new OrdersHeap<>(capacity + 1, cmp);
    }

    @Override public void add(T el) {
        heap.add(el);
    }

    @Override public void removeFirst() {
        if (!isEmpty()) {
            heap.removeFirst();
        }
    }

    @Override public T first() {
        return heap.first();
    }

    @Override public T removeById(int id) {
        if (!isEmpty()) {
            return heap.removeById(id);
        }
        return null;
    }

    @Override public void remove(T el) {
        if (!isEmpty()) {
            heap.remove(el);
        }
    }

    @Override public boolean isEmpty() {
        return heap.isEmpty();
    }

    @Override public int size() {
        return heap.size();
    }
}
