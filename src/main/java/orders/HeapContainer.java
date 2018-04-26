package orders;


import java.util.Comparator;

public class HeapContainer<T extends OrderActor> implements OrdersContainer<T> {
    private Heap<T> heap;

    public static StopWatch addSw         = new StopWatch();
    public static StopWatch removeSw      = new StopWatch();
    public static StopWatch removeFirstSw = new StopWatch();

    private HeapContainer(Comparator<T> cmp, int capacity) {
//        try {
//            cmp(cmp, new Seller(-1, -1, -1), new Seller(-1, -1, -1));
//            this.heap = new OrdersHeapIntKey<>(capacity + 1, Integer::compare);
//        } catch (ClassCastException e) {
//            System.out.println("SELLER HEAP");
//            this.heap = new OrdersHeapIntKey<>(capacity + 1, ((Comparator<Integer>) Integer::compare).reversed());
//        }
//        this.heap = new DaryHeap<>(capacity + 1, 4, cmp);
    }

    public static <T extends OrderActor> Heap<T> forSeller(int capacity) {
        return new OrdersMinHeapIntKey<>(capacity + 1);
    }

    public static <T extends OrderActor> Heap<T> forBuyer(int capacity) {
        return new OrdersMaxHeapIntKey<>(capacity + 1);
    }


    public static void cmp(Comparator cmp, Object o1, Object o2) {
        cmp.compare(o1, o2);
    }

    @Override public void add(T el) {
//        addSw.start();
        heap.add(el);
//        addSw.stop();
    }

    @Override public void removeFirst() {
        if (!isEmpty()) {
//            removeFirstSw.start();
            heap.removeFirst();
//            removeFirstSw.stop();
        }
    }

    @Override public T first() {
        T first = heap.first();
//        while (first != null && first.cancelled) {
//            heap.removeFirst();
//            first = heap.first();
//        }
        return first;
    }

    @Override public T removeById(int id) {
        if (!isEmpty()) {
//            removeSw.start();
            T res = heap.removeById(id);
//            removeSw.stop();
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
//        addSw = new StopWatch();
        removeSw = new StopWatch();
//        removeFirstSw = new StopWatch();
    }

    public static void showStats() {
//        System.out.println("addTime = " + addSw.getAvg());
//        System.out.println("removeTime = " + removeSw.getAvg());
//        System.out.println("removeFirstTime = " + removeFirstSw.getAvg());

        System.out.println("addTime = " + addSw.getTime());
        System.out.println("removeTime = " + removeSw.getTime());
        System.out.println("removeFirstTime = " + removeFirstSw.getTime());
    }

    @Override public boolean isEmpty() {
        return heap.isEmpty();
    }

    @Override public int size() {
        return heap.size();
    }
}
