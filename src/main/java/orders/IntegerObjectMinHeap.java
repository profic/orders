/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 *
 * Copyright (C) 2017
 * ELKI Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package orders;

import java.util.Arrays;

/**
 * Binary heap for int keys and Object values.
 * <p>
 * This class is generated from a template.
 *
 * @param <V> Value type
 * @author Erich Schubert
 * @apiviz.has UnsortedIter
 */
public class IntegerObjectMinHeap<V extends OrderActor> implements IntegerObjectHeap<V>, Heap<V> {
    /**
     * Base heap.
     */
    protected int[] queue;

    /**
     * Base heap values.
     */
    protected Object[] values;
    protected int[]    indices;

    /**
     * Current size of heap.
     */
    protected int size;

    /**
     * Initial size of the 2-ary heap.
     */
    private final static int TWO_HEAP_INITIAL_SIZE = (1 << 5) - 1;

    /**
     * Constructor, with default size.
     */
    public IntegerObjectMinHeap() {
        super();
        int[]    twoheap = new int[TWO_HEAP_INITIAL_SIZE];
        Object[] twovals = new Object[TWO_HEAP_INITIAL_SIZE];

        this.queue = twoheap;
        this.values = twovals;
    }

    /**
     * Constructor, with given minimum size.
     *
     * @param minsize Minimum size
     */
    public IntegerObjectMinHeap(int minsize) {
        super();
        int size = HeapUtil.nextPow2Int(minsize + 1) - 1;
        this.queue = new int[size];
        this.values = new Object[size];
        this.indices = new int[size];
    }

    @Override
    public void clear() {
        size = 0;
        Arrays.fill(queue, 0);
        Arrays.fill(values, null);
        Arrays.fill(indices, 0);
    }

    @Override public void add(final V el) {
        this.add(el.price(), el);
    }

    @Override public V first() {
        return this.peekValue();
    }

    @Override public boolean remove(final V el) {
        removeById(el.id());
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    @Override public V removeFirst() {
        V res = this.poll();
        checkIndices();
        return res;
    }

    @Override public V removeById(final int id) {
        V res = null;
        if (!isEmpty()) {
            int idx = indices[id];
            res = removeAt(idx, id);
        }

//        for (int i = 0; i < size; i++) {
//            Object value = values[i];
//            if (((V) value).id() == id) {
//                return removeAt(i, id);
//            }
//        }
        checkIndices();
        return res;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void add(final int key, final V v) {
        grow();
        final int idx = size;
//        queue[idx] = key;
//        values[idx] = v;
        set(idx, key, v);
        ++size;
        siftUp(idx, key, v);
        checkIndices();
    }

    private void grow() {
        if (size >= queue.length) {
            // Grow by one layer.
            queue = Arrays.copyOf(queue, queue.length + queue.length + 1);
            values = Arrays.copyOf(values, values.length + values.length + 1);
        }
    }

    @Override
    public void add(int key, V val, int max) {
        if (size < max) {
            add(key, val);
        } else if (queue[0] < key) {
            replaceTopElement(key, val);
        }
    }

    @Override
    public void replaceTopElement(int reinsert, V val) {
        siftDown(0, reinsert, val);
    }

    /**
     * Heapify-Up method for 2-ary heap.
     *
     * @param k   Position in 2-ary heap.
     * @param el  Current object
     * @param val Current value
     */
    private void siftUp(final int k, final int el, final V val) {
        int idx = k;
        while (idx > 0) {
            int parentIdx = (idx - 1) / 2;
            int parent    = queue[parentIdx];
            if (el >= parent) {
                break;
            }
//            queue[idx] = parent;
//            values[idx] = values[parentIdx];
            set(idx, parent, (V) values[parentIdx]);
            idx = parentIdx;
        }
//        queue[idx] = el;
//        values[idx] = val;
        set(idx, el, val);
    }

    private void checkIndices() {
//        for (int i = 0; i < size; i++) {
//            Object o = values[i];
//            if (indices[((V) o).id()] != i) {
//                throw new IllegalStateException();
//            }
//        }
    }

    @Override
    public V poll() {
        if (isEmpty()) {
            return null;
        } else {
            --size;
            // Replacement object:
            V res = peekValue();
            if (size > 0) {
                final int    reinsertKey = queue[size];
                final Object reinsertVal = values[size];
//                queue[size] = 0;
//                values[size] = null;
                remove(size, res.id());
                siftDown(0, reinsertKey, reinsertVal);
            } else {
                remove(0, res.id());
//                queue[0] = 0;
//                values[0] = null;
            }
            return res;
        }
    }

    private void remove(int idx, int id) {
        queue[idx] = 0;
        values[idx] = null;
        indices[id] = 0;
    }

    private void set(int idx, final int el, V value) { // todo: ???
        queue[idx] = el;
        values[idx] = value;
        indices[value.id()] = idx;
    }

    private V removeAt(int idx, int id) {
        int size = --this.size;
        V   res  = (V) values[idx];
        if (size == idx) {
            remove(idx, id);
            return res;
        } else {
            int moved = queue[size];
            V movedVal = (V) values[size];
            remove(size, id);
            siftDown(idx, moved, movedVal);
            if (queue[idx] == moved) {
                siftUp(idx, moved, movedVal);
            }
            return res;
        }
    }

    /**
     * Invoke heapify-down for the root object.
     *
     * @param el    Object to insert.
     * @param val   Value to reinsert.
     * @param index
     */
//    private void siftDown(int el, Object val) {
    private void siftDown(final int index, final int el, final Object val) {
        int       idx  = index;
        final int half = size / 2;
        while (idx < half) {
            int childIdx = (idx * 2) + 1;
            int child    = queue[childIdx];

            final int rightIdx = childIdx + 1;
            if (rightIdx < size && child > queue[rightIdx]) {
                childIdx = rightIdx;
//                child = queue[rightIdx];
                child = queue[childIdx];
            }
            if (child >= el) {
                break;
            }

//            queue[idx] = child;
//            values[idx] = values[childIdx];

            set(idx, child, (V) values[childIdx]);

            idx = childIdx;
        }
//        queue[idx] = el;
//        values[idx] = val;
        set(idx, el, (V) val);
    }

    @Override
    public int peekKey() {
        return queue[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public V peekValue() {
        return (V) values[0];
    }

    @Override
    public boolean containsKey(int q) {
        for (int pos = 0; pos < size; pos++) {
            if (queue[pos] == q) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(V q) {
        for (int pos = 0; pos < size; pos++) {
            if (q.equals(values[pos])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(IntegerObjectMinHeap.class.getSimpleName()).append(" [");
        for (UnsortedIter iter = new UnsortedIter(); iter.valid(); iter.advance()) {
            buf.append(iter.getKey()).append(':').append(iter.getValue()).append(',');
        }
        buf.append(']');
        return buf.toString();
    }

    @Override
    public UnsortedIter unsortedIter() {
        return new UnsortedIter();
    }

    /**
     * Unsorted iterator - in heap order. Does not poll the heap.
     * <p>
     * Use this class as follows:
     *
     * <pre>
     * {@code
     * for (IntegerObjectHeap.UnsortedIter<V> iter = heap.unsortedIter(); iter.valid(); iter.next()) {
     *   doSomething(iter.get());
     * }
     * }
     * </pre>
     *
     * @author Erich Schubert
     */
    private class UnsortedIter implements IntegerObjectHeap.UnsortedIter<V> {
        /**
         * Iterator position.
         */
        protected int pos = 0;

        @Override
        public boolean valid() {
            return pos < size;
        }

        @Override
        public UnsortedIter advance() {
            pos++;
            return this;
        }

        @Override
        public int getKey() {
            return queue[pos];
        }

        @Override
        @SuppressWarnings("unchecked")
        public V getValue() {
            return (V) values[pos];
        }
    }
}
