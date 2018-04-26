//package orders;
//
//import java.util.Comparator;
//
//public class OrdersHeapIntKey<E extends OrderActor> implements Heap<E> {
//
//    private final Comparator<Integer> cmp;
//    private final int[]               queue;
//    private final Object[]            values;
//    private final int[]               indices;
//
//    private int size = 0;
//
//
//    public OrdersHeapIntKey(int initialCapacity, Comparator<Integer> cmp) {
//        if (initialCapacity < 1) {
//            throw new IllegalArgumentException();
//        }
//
//        this.cmp = cmp;
//        this.queue = new int[initialCapacity];
//        this.indices = new int[initialCapacity];
//        this.values = new Object[initialCapacity];
//        for (int i = 0; i < indices.length; i++) {
//            indices[i] = -1;
//        }
//    }
//
//    @Override public void add(E el) {
//        int idx = size;
//        size = idx + 1;
//        if (idx == 0) {
//            set(0, el.price(), el);
//        } else {
//            siftUp(idx, el.price(), true, el);
//        }
//    }
//
//    @Override @SuppressWarnings("unchecked")
//    public E first() {
////        return (size == 0) ? null : (E) queue[0];
//        return (size == 0) ? null : (E) values[0];
//    }
//
//    private int indexOf(E el) {
//        return indices[el.id()];
//    }
//
//    @Override public boolean remove(E el) {
//        int idx = indexOf(el);
//        if (idx == -1)
//            return false;
//        else {
//            removeAt(idx, el.id());
//            return true;
//        }
//    }
//
//    @Override public int size() {
//        return size;
//    }
//
//    @Override @SuppressWarnings("unchecked")
//    public E removeFirst() {
//        if (size == 0) {
//            return null;
//        }
//        int size = --this.size;
//
//        E   result = (E) values[0];
//        int elem   = queue[size];
//        E val = (E) values[size];
//        remove(size, result.id());
//        if (size != 0) {
//            siftDown(0, elem, val);
//        }
//        return result;
//    }
//
//    private void remove(int idx, int id) {
//        queue[idx] = 0; // todo: may be superfluous
//        indices[id] = -1; // todo: may be superfluous
//        values[idx] = null; // todo: may be superfluous
//    }
//
//    private void set(int idx, int el, E val) {
//        queue[idx] = el;
//        indices[val.id()] = idx;
//        values[idx] = val;
//    }
//
//    @SuppressWarnings("unchecked")
//    private E removeAt(int idx, int id) {
//        int    size = --this.size;
//        E res  = (E) values[idx];
//        if (size == idx) {
//            remove(idx, id);
//            return res;
//        } else {
//            int moved = queue[size];
//            E el = (E) values[size];
//            remove(size, id);
//            siftDown(idx, moved, el);
//            if (queue[idx] == moved) {
//                siftUp(idx, moved, false, el);
//            }
//            return res;
//        }
//    }
//
//    @Override public E removeById(int id) {
//        int idx = indices[id];
//        if (idx != -1) {
//            return removeAt(idx, id);
////            E res = (E) queue[idx];
////            res.cancelled = true;
////            return res;
//        }
//        return null;
//    }
//
//    private void checkIndices() {
//        for (int i = 0; i < queue.length; i++) {
//            Object o = queue[i];
//            if (o != null) {
//                if (indices[((E) o).id()] != i) {
//                    throw new IllegalStateException();
//                }
//            }
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    private void siftUp(final int k, final int el, boolean add, final E val) {
//        if (add && indices[val.id()] != -1) {
//            throw new IllegalStateException("Duplicates are not allowed");
//        }
//        int idx = k;
//        while (idx > 0) {
//            final int parentIdx = (idx - 1) / 2;
//            final int parent    = queue[parentIdx];
//            if (cmp.compare(el, parent) >= 0) {
//                break;
//            }
//
//            set(idx, parent, (E) values[parentIdx]);
//
//            idx = parentIdx;
//        }
//        set(idx, el, val); // todo: maybe superfluous
//    }
//
//    @SuppressWarnings("unchecked")
//    private void siftDown(final int index, final int el, final E val) {
//        int idx  = index;
//        int half = size / 2;
//        while (idx < half) {
//            int childIdx = (idx * 2) + 1;
//            int child    = queue[childIdx];
//
//            int rightIdx = childIdx + 1;
//            if (rightIdx < size && cmp.compare(child, queue[rightIdx]) > 0) {
//                childIdx = rightIdx;
//                child = queue[childIdx];
//            }
//            if (cmp.compare(el, child) <= 0) {
//                break;
//            }
//            set(idx, child, (E) values[childIdx]);
//            idx = childIdx;
//        }
//        set(idx, el, val);
//    }
//
//    @Override public boolean isEmpty() {
//        return size == 0;
//    }
//}