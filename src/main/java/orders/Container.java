package orders;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Set;

public class Container<T extends OrderEntry> implements IContainer<T> {
    private final NavigableMap<Integer, Set<T>> elByPrice;
    private final Object[]                      elById;

    private int size;

    public Container(final NavigableMap<Integer, Set<T>> elByPrice, int capacity) {
        this.elByPrice = elByPrice;
        elById = new Object[capacity];
    }

    @Override public void add(T el) {
        int    price = el.price();
        Set<T> s     = elByPrice.computeIfAbsent(price, k -> new HashSet<>());
        s.add(el);
        elById[el.id()] = el;
        size++;
    }

    @Override public void removeFirst() {
        if (!isEmpty()) {
            Iterator<T> it = elByPrice.firstEntry().getValue().iterator();
            T           el = it.next();
            elById[el.id()] = null;
            elByPrice.compute(el.price(), (price, s) -> {
                it.remove();
                return it.hasNext() ? s : null;
            });
            size--;
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

    @Override public void removeById(Integer id) {
        T el = (T) elById[id];
        if (el != null) {
            remove(el);
        }
    }

    @Override public void remove(T el) {
        Set<T> s = elByPrice.get(el.price());
        if (s != null) {
            s.remove(el);
            if (s.isEmpty()) {
                elByPrice.remove(el.price());
            }
            elById[el.id()] = null;
            size--;
        }
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
}
