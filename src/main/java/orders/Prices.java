package orders;

public class Prices {
    private int[] prices;
    private int   range;

    public Prices(final int range) {
        this.range = range;
    }

    public void increase(int price, int count) {
        put(price, count);
    }

    public void decrease(int price, int count) {
        put(price, -count);
    }

    public int getPrice(int price) {
        return prices == null ? 0 : prices[idx(price)];
    }

    private void put(final int price, final int count) {
        if (prices == null) {
            int maxArraySize = (int) Long.min(Integer.MAX_VALUE, (long) price + range);
            int minArraySize = Integer.max(0, price - range);
            prices = new int[maxArraySize - minArraySize + 1];
        }
        prices[idx(price)] += count;
    }

    private int idx(final int price) {
        return price % prices.length;
    }
}
