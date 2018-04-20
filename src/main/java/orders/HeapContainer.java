package orders;

import java.util.Comparator;

public class HeapContainer<T extends OrderEntry> implements IContainer<T> {
    private final SedgewickHeapOptimized<T> heap;
    Comparator<T> cmp;

    public HeapContainer(Comparator<T> cmp, int capacity) {
        heap = new SedgewickHeapOptimized<>(capacity, cmp);
        this.cmp = cmp;
    }

    @Override public void add(T el) {
        // todo: cleanup
        if (first() != null) {
            T firstBeforeInsert = first();

            int res = cmp.compare(firstBeforeInsert, el);
            T firstAfterInsert = first();
            boolean assert_;
            if (res < 0) { // new el less than curr
                assert_ = cmp.compare(firstAfterInsert, firstBeforeInsert) == 0;
            } else if (res > 0) { // new el bigger than curr
                assert_ = cmp.compare(firstAfterInsert, firstBeforeInsert) == 0;
            } else { // new and curr first elements are equal
                assert_ = cmp.compare(firstAfterInsert, el) == 0;
            }

            if (!assert_) {
                throw new RuntimeException();
            }
        }
        heap.insert(el);
    }

    @Override public void removeFirst() {
        if (!isEmpty()) {
            heap.delFirst();
        }
    }

    @Override public T first() {
        return isEmpty() ? null : heap.first();
    }

    @Override public void removeById(int id) {
        if (!isEmpty()) {
            // todo: cleanup
            T firstBefore = first();
            heap.removeById(id);
            if (firstBefore.id() != id) {
                if (firstBefore != first()) {
                    throw new RuntimeException();
                }
            }
        }
    }

    @Override public void remove(T el) {
        if (isEmpty()) {
            T firstBefore = first();
            heap.remove(el);
            if (firstBefore.id() != el.id()) {
                if (firstBefore != first()) {
                    throw new RuntimeException();
                }
            }
        }
    }

    @Override public boolean isEmpty() {
        return heap.isEmpty();
    }

    @Override public int size() {
        return heap.size();
    }
}
