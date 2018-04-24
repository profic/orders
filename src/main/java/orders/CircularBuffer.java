package orders;

import sun.misc.Contended;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class CircularBuffer<T> {

    @Contended
    private final AtomicReferenceArray<T> delegate;

    @Contended
    private int posAdd;
    @Contended
    private int posPoll;

    public CircularBuffer(int size) {
        delegate = new AtomicReferenceArray<>(size);
    }

    public void add(T el) {
        delegate.set(posAdd++, el);
    }

    public T poll() {
        return delegate.get(posPoll++);
    }

    public void increaseGetPos() {
        posPoll++;
    }

    public T peek() {
        return delegate.get(posPoll);
    }
}
