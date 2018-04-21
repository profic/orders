package orders;

import java.util.Arrays;
import java.util.Comparator;

public class HeapOptimized<E extends OrderEntry> {

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private Object[]      queue;
    private int           size = 0;
    private int[]         indices;
    private Comparator<E> cmp;


    public HeapOptimized(int initialCapacity, Comparator<E> cmp) {
        this.cmp = cmp;
        if (initialCapacity < 1) {
            throw new IllegalArgumentException();
        }
        this.queue = new Object[initialCapacity];
        indices = new int[initialCapacity];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = -1;
        }
    }

    private void grow(int minCapacity) {
        int oldCapacity = queue.length;
        int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                (oldCapacity + 2) :
                (oldCapacity / 2));
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }
        queue = Arrays.copyOf(queue, newCapacity);
        indices = Arrays.copyOf(indices, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    public boolean add(E el) {
        int idx = size;
        if (idx >= queue.length) {
            grow(idx + 1);
        }
        size = idx + 1;
        if (idx == 0) {
            set(0, el);
        } else {
            siftUp(idx, el, true);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public E first() {
        return (size == 0) ? null : (E) queue[0];
    }

    private int indexOf(E el) {
        return indices[el.id()];
    }

    public boolean remove(E el) {
        int idx = indexOf(el);
        if (idx == -1)
            return false;
        else {
            removeAt(idx, el.id());
            return true;
        }
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    public E removeFirst() {
        if (size == 0) {
            return null;
        }
        int size = --this.size;

        E result = (E) queue[0];
        E elem   = (E) queue[size];
        remove(size, result.id());
        if (size != 0) {
            siftDown(0, elem);
        }
        return result;
    }

    private void remove(int idx, int id) {
        queue[idx] = null;
        indices[id] = -1;
    }

    private void set(int idx, E el) {
        queue[idx] = el;
        indices[el.id()] = idx;
    }

    @SuppressWarnings("unchecked")
    private E removeAt(int idx, int id) {
        int size = --this.size;
        if (size == idx) {
            remove(idx, id);
        } else {
            E moved = (E) queue[size];
            remove(size, id);
            siftDown(idx, moved);
            if (queue[idx] == moved) {
                siftUp(idx, moved, false);
                if (queue[idx] != moved) {
                    return moved;
                }
            }
        }
        return null;
    }

    public void removeById(int id) {
        int idx = indices[id];
        if (idx != -1) {
            removeAt(idx, id);
        }
    }

    @SuppressWarnings("unchecked")
    private void siftUp(int k, E el, boolean add) {
        if (add && indices[el.id()] != -1) {
            throw new IllegalStateException("Duplicates are not allowed");
        }
        int idx = k;
        while (idx > 0) {
            int parentIdx = (idx - 1) / 2;
            E   parent    = (E) queue[parentIdx];
            if (cmp.compare(el, parent) >= 0) {
                break;
            }
            set(idx, parent);

            idx = parentIdx;
        }
        set(idx, el);
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int index, E el) {
        int idx  = index;
        int half = size / 2;
        while (idx < half) {
            int childIdx = (idx * 2) + 1;
            E   child    = (E) queue[childIdx];

            int rightIdx = childIdx + 1;
            if (rightIdx < size && cmp.compare(child, (E) queue[rightIdx]) > 0) {
                childIdx = rightIdx;
                child = (E) queue[childIdx];
            }
            if (cmp.compare(el, child) <= 0) {
                break;
            }
            set(idx, child);
            idx = childIdx;
        }
        set(idx, el);
    }

    public boolean isEmpty() {
        return size == 0;
    }
}