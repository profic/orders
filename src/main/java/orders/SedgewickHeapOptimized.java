package orders;

import java.util.*;

public class SedgewickHeapOptimized<Key extends OrderEntry> {
    private Object[]        arr;
    private Comparator<Key> cmp;
    private int             size;
    private int[]           indices; // todo: think how to replace with HashMap

    public SedgewickHeapOptimized(int initCapacity, Comparator<Key> cmp) {
        arr = new Object[initCapacity + 1];
        this.cmp = cmp;
        size = 0;
        indices = new int[initCapacity];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = -1;
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    public Key first() {
        if (isEmpty()) {
            throw new NoSuchElementException("Priority queue underflow");
        }
        return (Key) arr[1];
    }

    public void test() {
        Optional<Key> opt = Arrays.stream(arr).filter(Objects::nonNull).map(s -> (Key) s).min(cmp);
        if (opt.isPresent()) {
            int expected = opt.get().price();
            if (expected != first().price()) {
                throw new RuntimeException();
            }
        }
    }

    private void resize(int capacity) {
        assert capacity > size;
        Object[] tmpArr = new Object[capacity];
        System.arraycopy(arr, 1, tmpArr, 1, size);
        arr = tmpArr;
    }

    public void insert(Key x) {
        // double size of array if necessary
        if (size == arr.length - 1) {
            resize(2 * arr.length);
        }

        // add x, and percolate it up to maintain heap invariant
        arr[++size] = x;
        indices[x.id()] = size;
        swim(size);
        test();
    }

    @SuppressWarnings("unchecked")
    public Key delFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("Priority queue underflow");
        }
        Key first = (Key) arr[1];
        doRemove(first, 1);
        test();
        return first;
    }

    public void remove(Key el) {
        removeById(el.id());
        test();
    }

    public void removeById(int id) {
        int idx = indices[id];
        if (idx != -1) {
            remove(id, idx);
        }
        test();
    }

    private void doRemove(final Key el, final int idx) {
        int id = el.id();
        remove(id, idx);
    }

    private void remove(final int id, final int idx) {
        exch(idx, size--);
        sink(idx);
        arr[size + 1] = null;     // to avoid loiterig and help with garbage collection
        indices[id] = -1;
        if ((size > 0) && (size == (arr.length - 1) / 4)) {
            resize(arr.length / 2);
        }
    }

    private void swim(int k) {
        int idx = k;
        while (idx > 1 && greater(idx / 2, idx)) {
            exch(idx, idx / 2);
            idx /= 2;
        }
    }

    private void sink(int k) {
        int idx = k;
        while (2 * idx <= size) {
            int j = 2 * idx;
            if (j < size && greater(j, j + 1)) j++;
            if (!greater(idx, j)) break;
            exch(idx, j);
            idx = j;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean greater(int i, int j) {
//        return ((Key) arr[i]).compareTo(((Key) arr[j])) > 0;
        return cmp.compare(((Key) arr[i]), ((Key) arr[j])) > 0;
    }

    @SuppressWarnings("unchecked")
    private void exch(int i, int j) {
        Key swap1 = (Key) arr[i];
        Key swap2 = (Key) arr[j];
        arr[i] = swap2;
        arr[j] = swap1;

        int id1 = swap1.id();
        int id2 = swap2.id();
        indices[id1] = j;
        indices[id2] = i;
    }
}
