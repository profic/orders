package orders;

class Prices {
    private int[] prices = new int[Orders.PRICES_COUNT];

    public void increase(int price, int count) {
        put(price, count);
    }

    public void decrease(int price, int count) {
        put(price, -count);
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
