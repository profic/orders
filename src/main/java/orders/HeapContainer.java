package orders;

import java.util.Comparator;

public class HeapContainer<T extends OrderActor> implements OrdersContainer<T> {
    private final OrdersHeap<T> heap;

    public static long addTime    = 0;
    public static long addCounter = 0;

    public static long removeTime    = 0;
    public static long removeCounter = 0;

    public static long removeFirstTime    = 0;
    public static long removeFirstCounter = 0;

    public HeapContainer(Comparator<T> cmp, int capacity) {
        this.heap = new OrdersHeap<>(capacity + 1, cmp);
    }

    @Override public void add(T el) {
        long start = System.nanoTime();
        heap.add(el);
        long total = System.nanoTime() - start;
        addCounter++;
        addTime += total;
    }

    @Override public void removeFirst() {
        if (!isEmpty()) {
            long start = System.nanoTime();
            heap.removeFirst();
            long total = System.nanoTime() - start;
            removeFirstCounter++;
            removeFirstTime += total;
        }
    }

    @Override public T first() {
        return isEmpty() ? null : heap.first();
    }

    @Override public T removeById(int id) {
        if (!isEmpty()) {
            long start = System.nanoTime();
            T    res     = heap.removeById(id);
            long total = System.nanoTime() - start;
            removeCounter++;
            removeTime += total;
            return res;
        }
        return null;
    }

    @Override public void remove(T el) {
        if (!isEmpty()) {
            heap.remove(el);
        }
    }

    public static void reset() {
        addTime = 0;
        addCounter = 0;

        removeTime = 0;
        removeCounter = 0;

        removeFirstTime = 0;
        removeFirstCounter = 0;
    }

    public static void showStats() {
        System.out.println("addTime = " + addTime / addCounter);
        System.out.println("removeTime = " + removeTime / removeCounter);
        System.out.println("removeFirstTime = " + removeFirstTime / removeFirstCounter);
    }

    @Override public boolean isEmpty() {
        return heap.isEmpty();
    }

    @Override public int size() {
        return heap.size();
    }
}
