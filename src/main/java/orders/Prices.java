package orders;

public class Prices {
    private final int[] prices;

    public Prices(final int size) {
        prices = new int[size];
    }

    public void increase(int price, int count) {
        put(price, count);
    }

    public void decrease(int price, int count) {
        put(price, -count);
        if (getPrice(price) < 0) {
            System.out.println(price);
        }
    }

    public int getPrice(int price) {
        return prices[idx(price)];
    }

    private void put(final int price, final int count) {
        prices[idx(price)] += count;
    }

    private int idx(final int price) {
        return price % prices.length;
    }
}
