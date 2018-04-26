package orders;

import java.util.HashMap;
import java.util.Map;

public class Prices {
    private final Map<Integer, Integer> prices;

    public Prices(final int size) {
        prices = new HashMap<>(size * 2, 2f);
    }

    public void increase(int price, int count) {
        put(price, count);
    }

    public void decrease(int price, int count) {
        put(price, -count);
    }

    public int getPrice(int price) {
        return prices.getOrDefault(price, 0);
    }

    private void put(final int price, final int count) {
        prices.compute(price, (k, v) -> v == null ? count : v + count);
    }
}
