package orders;

public class OrdersHeapIntKeys<V extends OrderActor> implements Heap<V> {

    private final int[]    queue;
    private final Object[] values;
    private final int[]    indices;
    private       int      size = 0;


    public OrdersHeapIntKeys(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException();
        }

        int size = HeapUtil.nextPow2Int(initialCapacity);
        this.queue = new int[size];
        this.indices = new int[size];
        this.values = new Object[size];
    }

    @Override public void add(V val) {
        int idx = size;
        size = idx + 1;
        int el = val.price();
        if (idx == 0) {
            set(0, el, val);
        } else {
            siftUp(idx, el, true, val);
        }
        checkIndices();
    }

    @Override @SuppressWarnings("unchecked")
    public V first() {
        return (size == 0) ? null : (V) values[0];
    }

    private int indexOf(V el) {
        return indices[el.id()];
    }

    @Override public boolean remove(V el) {
        if (isEmpty()) {
            return false;
        }
        int idx = indexOf(el);
        removeAt(idx, el.id());
        return true;
    }

    @Override public int size() {
        return size;
    }

    @Override @SuppressWarnings("unchecked")
    public V removeFirst() {
        if (isEmpty()) {
            return null;
        }
        int size = --this.size;

        V   result = (V) values[0];
        int elem   = queue[size];
        V val = (V) values[size];
        remove(size, result.id());
        if (size != 0) {
            siftDown(0, elem, val);
        }
        checkIndices();
        return result;
    }

    private void remove(int idx, int id) {
        queue[idx] = 0;
        values[idx] = null; // todo: removed for GC - OK, but maybe not needed here
        indices[id] = 0;
    }

    private void set(int idx, int el, V val) {
        queue[idx] = el;
        values[idx] = val;
        indices[val.id()] = idx;
    }

    @SuppressWarnings("unchecked")
    private V removeAt(int idx, int id) {
        int size = --this.size;
        V   res  = (V) values[idx];
        if (size == idx) {
            remove(idx, id);
            return res;
        } else {
            int moved = queue[size];
            V val = (V) values[size];
            remove(size, id);
            siftDown(idx, moved, val);
            if (queue[idx] == moved) {
                siftUp(idx, moved, false, val);
            }
            return res;
        }
    }

    @Override public V removeById(int id) {
        if (!isEmpty()) {
            int idx = indices[id];
            V   res   = removeAt(idx, id);
            checkIndices();
            return res;
        }
        return null;
    }

    private void checkIndices() {
        for (int i = 0; i < size; i++) {
            Object o = values[i];
            if (o != null) {
                if (indices[((V) o).id()] != i) {
                    throw new IllegalStateException();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void siftUp(int k, int el, boolean add, V val) {
//        if (add && indices[val.id()] != -1) {
//            throw new IllegalStateException("Duplicates are not allowed");
//        }
        int idx = k;
        while (idx > 0) {
            int parentIdx = (idx - 1) / 2;
            int parent    = queue[parentIdx];
            if (Integer.compare(el, parent) >= 0) {
                break;
            }
            set(idx, parent, (V) values[parentIdx]);
            idx = parentIdx;
        }
        set(idx, el, val); // todo: I think it's superfluous
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int index, int el, final V val) {
        int idx  = index;
        int half = size / 2;
        while (idx < half) {
            int childIdx = (idx * 2) + 1;
            int child    = queue[childIdx];

            int rightIdx = childIdx + 1;
            if (rightIdx < size && Integer.compare(child, queue[rightIdx]) > 0) {
                childIdx = rightIdx;
                child = queue[childIdx];
            }
            if (Integer.compare(el, child) <= 0) {
                break;
            }
            set(idx, child, (V) values[childIdx]);
            idx = childIdx;
        }
        set(idx, el, val); // todo: I think it's superfluous
    }

    @Override public boolean isEmpty() {
        return size == 0;
    }
}