package orders;

public class OrdersMaxHeapIntKey<E extends OrderActor> extends AbstractHeap<E> implements Heap<E> {

    public OrdersMaxHeapIntKey(int initialCapacity) {
        super(initialCapacity);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void siftUp(final int k, final int el, boolean add, final E val) {
//        if (add && indices[val.id()] != -1) {
//            throw new IllegalStateException("Duplicates are not allowed");
//        }
        int idx = k;
        while (idx > 0) {
            final int parentIdx = (idx - 1) / 2;
            final int parent    = queue[parentIdx];
            if (el <= parent) {
                break;
            }

            set(idx, parent, (E) values[parentIdx]);

            idx = parentIdx;
        }
        set(idx, el, val); // todo: maybe superfluous
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void siftDown(final int index, final int el, final E val) {
        int idx  = index;
        int half = size / 2;
        while (idx < half) {
            int childIdx = (idx * 2) + 1;
            int child    = queue[childIdx];

            int rightIdx = childIdx + 1;
            if (rightIdx < size && child < queue[rightIdx]) {
                childIdx = rightIdx;
                child = queue[childIdx];
            }
            if (el >= child) {
                break;
            }
            set(idx, child, (E) values[childIdx]);
            idx = childIdx;
        }
        set(idx, el, val);
    }
}