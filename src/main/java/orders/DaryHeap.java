package orders;

import java.util.Arrays;
import java.util.Comparator;

public class DaryHeap<T extends OrderActor> implements Heap<T> {

    private final int           d;
    private final Object[]      heap;
    private       int           heapSize;
    private final Comparator<T> cmp;

    public DaryHeap(int capacity, int numChild, final Comparator<T> cmp) {
        this.cmp = cmp;
        this.d = numChild;
        this.heap = new Object[capacity + 1];
        Arrays.fill(heap, -1);
    }

    @Override public boolean isEmpty() {
        return heapSize == 0;
    }

    private int parent(int i) {
        return (i - 1) / d;
    }

    private int kthChild(int i, int k) {
        return d * i + k;
    }

    @Override public void add(T x) {
        heap[heapSize++] = x;
        heapifyUp(heapSize - 1);
    }

    @Override public T first() {
        if (isEmpty())
            return null;
        return (T) heap[0];
    }

    @Override public boolean remove(final T el) {
        throw new UnsupportedOperationException();
    }

    @Override public int size() {
        return heapSize;
    }

    @Override public T removeFirst() {
        return delete(0);
    }

    @Override public T removeById(final int id) {
        return null;
    }

    public T delete(int ind) {
        if (isEmpty()) {
            return null;
        }
        T keyItem = (T) heap[ind];
        heap[ind] = heap[heapSize - 1];
        heapSize--;
        heapifyDown(ind);
        return keyItem;
    }

    private void heapifyUp(int childInd) {
        Object[] heap     = this.heap;
        int      childIdx = childInd;
        T        tmp      = (T) heap[childIdx];
        while (childIdx > 0 && cmp.compare(tmp, (T) heap[parent(childIdx)]) < 0) {
            heap[childIdx] = heap[parent(childIdx)];
            childIdx = parent(childIdx);
        }
        heap[childIdx] = tmp;
    }

    private void heapifyDown(int ind) {
        int      child;
        int      idx  = ind;
        Object[] heap = this.heap;
        T        tmp  = (T) heap[idx];
        while (kthChild(idx, 1) < heapSize) {
            child = minChild(idx);
            if (cmp.compare((T) heap[child], tmp) < 0) {
                heap[idx] = heap[child];
            } else {
                break;
            }
            idx = child;
        }
        heap[idx] = tmp;
    }

    private int minChild(int ind) {
        int bestChild = kthChild(ind, 1);
        int k         = 2;
        int pos       = kthChild(ind, k);
        while ((k <= d) && (pos < heapSize)) {
            if (cmp.compare((T) heap[pos], (T) heap[bestChild]) < 0) {
                bestChild = pos;
            }
            pos = kthChild(ind, k++);
        }
        return bestChild;
    }

    public void printHeap() {
        System.out.print("\nHeap = ");
        for (int i = 0; i < heapSize; i++)
            System.out.print(heap[i] + " ");
        System.out.println();
    }
}