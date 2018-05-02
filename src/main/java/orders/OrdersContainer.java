package orders;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class OrdersContainer {
    public final TreeMap<Integer, Integer> orders;

    public OrdersContainer() {
        orders = new TreeMap<>();
    }

    public OrdersContainer(Comparator<Integer> cmp) {
        orders = new TreeMap<>(cmp);
    }

    public Map.Entry<Integer, Integer> getFirst() {
        return orders.firstEntry();
    }


    public Integer getFirstPrice() {
        return orders.firstKey();
    }

    public void increaseSize(Integer price, Integer size) {
        orders.compute(price, (__, oldSize) -> oldSize == null ? size : oldSize + size);
    }

    public int decreaseSize(Integer price, Integer size) {

        int oldSize = getSize(price);
        int newSize = oldSize - size;
        if (newSize <= 0) {
            orders.remove(price);
            return oldSize;
        } else {
            orders.put(price, newSize);
            return size;
        }
    }

    public Integer getSize(Integer price) {
        Integer size = orders.get(price);
        return size == null ? 0 : size;
    }

    public void setSize(final Integer price, final Integer size) {
        orders.put(price, size);
    }

    public int decreaseSizeAtFirstPrice(final int size) {
        return decreaseSize(getFirstPrice(), size);
    }
}
