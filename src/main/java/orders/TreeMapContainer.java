package orders;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Set;

public class TreeMapContainer<T extends OrderActor> implements OrdersContainer<T> {
    private final NavigableMap<Integer, Set<T>> elByPrice;
    private final Object[]                      elById;

    public static long addTime    = 0;
    public static long addCounter = 0;

    public static long removeTime    = 0;
    public static long removeCounter = 0;

    public static long removeFirstTime    = 0;
    public static long removeFirstCounter = 0;

    private int size;

    public TreeMapContainer(final NavigableMap<Integer, Set<T>> elByPrice, int capacity) {
        this.elByPrice = elByPrice;
        elById = new Object[capacity];
    }

    @Override public void add(T el) {
        long   start = System.nanoTime();
        int    price = el.price();
        Set<T> s     = elByPrice.computeIfAbsent(price, k -> new HashSet<>());
        s.add(el);
        elById[el.id()] = el;
        size++;
        long total = System.nanoTime() - start;
//        addCounter++;
//        addTime += total;
    }

    @Override public void removeFirst() {
        if (!isEmpty()) {
            long start = System.nanoTime();

            Iterator<T> it = elByPrice.firstEntry().getValue().iterator();
            T           el = it.next();
            elById[el.id()] = null;
            elByPrice.compute(el.price(), (price, s) -> {
                it.remove();
                return it.hasNext() ? s : null;
            });
            size--;

            long total = System.nanoTime() - start;
//            removeFirstCounter++;
//            removeFirstTime += total;
        }
    }

    @Override public T first() {
        if (isEmpty()) {
            return null;
        } else {
            Iterator<T> it = elByPrice.firstEntry().getValue().iterator();
            return it.hasNext() ? it.next() : null;
        }
    }

    @Override public T removeById(final int id) {
        T el = (T) elById[id];
        if (el != null) {
            remove(el);
        }
        return el;
    }


    @Override public void remove(T el) {

        long start = System.nanoTime();

        Set<T> s = elByPrice.get(el.price());
        if (s != null) {
            s.remove(el);
            if (s.isEmpty()) {
                elByPrice.remove(el.price());
            }
            elById[el.id()] = null;
            size--;
        }

        long total = System.nanoTime() - start;
//        removeCounter++;
//        removeTime += total;
    }

    public static void showStats() { // todo: remove
        System.out.println("addTime = " + addTime / addCounter);
        System.out.println("removeTime = " + removeTime / removeCounter);
        System.out.println("removeFirstTime = " + removeFirstTime / removeFirstCounter);
    }

    @Override public boolean isEmpty() {
        return size == 0;
    }

    @Override public int size() {
        return size;
    }

    @Override public String toString() {
        return elByPrice.toString();
    }

    public static void reset() {
        addTime = 0;
        addCounter = 0;

        removeTime = 0;
        removeCounter = 0;

        removeFirstTime = 0;
        removeFirstCounter = 0;
    }

}
