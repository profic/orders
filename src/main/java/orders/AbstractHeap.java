package orders;

public abstract class AbstractHeap<E extends OrderActor> implements Heap<E> {

    protected final int[]    queue;
    protected final Object[] values;
    protected final int[]    indices;
    protected       int      size = 0;

    protected AbstractHeap(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException();
        }
        this.queue = new int[initialCapacity];
        this.indices = new int[initialCapacity];
        this.values = new Object[initialCapacity];
    }

    @Override public E removeById(int id) {
        int idx = indices[id];
        if (idx != -1) {
            return removeAt(idx, id);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected E removeAt(int idx, int id) {
        int size = --this.size;
        E   res  = (E) values[idx];
        if (size == idx) {
            remove(idx, id);
            return res;
        } else {
            int moved = queue[size];
            E   el    = (E) values[size];
            remove(size, id);
            siftDown(idx, moved, el);
            if (queue[idx] == moved) {
                siftUp(idx, moved, false, el);
            }
            return res;
        }
    }

    @Override public void add(E el) {
        int idx = size;
        size = idx + 1;
        if (idx == 0) {
            set(0, el.price(), el);
        } else {
            siftUp(idx, el.price(), true, el);
        }
    }

    protected void set(int idx, int el, E val) {
        queue[idx] = el;
        indices[val.id()] = idx;
        values[idx] = val;
    }

    protected abstract void siftUp(final int idx, final int moved, final boolean b, final E el);

    protected abstract void siftDown(final int idx, final int moved, final E el);

    protected void remove(int idx, int id) {
        queue[idx] = 0; // todo: may be superfluous
        indices[id] = -1; // todo: may be superfluous
        values[idx] = null; // todo: may be superfluous
    }

    @Override public boolean isEmpty() {
        return size == 0;
    }

    @Override @SuppressWarnings("unchecked")
    public E first() {
//        return (size == 0) ? null : (E) queue[0];
        return (size == 0) ? null : (E) values[0];
    }

    private int indexOf(E el) {
        return indices[el.id()];
    }

    @Override public boolean remove(E el) {
        int idx = indexOf(el);
        if (idx == -1)
            return false;
        else {
            removeAt(idx, el.id());
            return true;
        }
    }

    @Override public int size() {
        return size;
    }

    @Override @SuppressWarnings("unchecked")
    public E removeFirst() {
        if (size == 0) {
            return null;
        }
        int size = --this.size;

        E   result = (E) values[0];
        int elem   = queue[size];
        E   val    = (E) values[size];
        remove(size, result.id());
        if (size != 0) {
            siftDown(0, elem, val);
        }
        return result;
    }

}
