package orders;

import java.util.Comparator;

public class HeapContainer<T extends OrderEntry> implements OrdersContainer<T> {
//    private final OrdersHeap<T> heap;
    private final OrdersComparableHeap<T> heap;

    public HeapContainer(Comparator<T> cmp, int capacity) {
//        this.heap = new OrdersHeap<>(capacity + 1, cmp);
        this.heap = new OrdersComparableHeap<>(capacity + 1);
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
        return isEmpty() ? null : heap.first();
    }

    @Override public void removeById(int id) {
        if (!isEmpty()) {
            heap.removeById(id);
        }
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
