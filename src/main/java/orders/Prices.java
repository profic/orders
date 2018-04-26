package orders;

public class Prices {
    private final int[] prices;

    public Prices(final int size) {
        prices = new int[size];
    }

    public void increase(int price, int count) {
//        System.out.println("increase: price = " + price + ", count = " + count);
        put(price, count);
    }

    public void decrease(int price, int count) {
//        System.out.println("decrease: price = " + price + ", count = " + count);
        put(price, -count);
    }

    public int getPrice(int price) {
        return prices[idx(price)];
    }

    private void put(final int price, final int count) {
//        if (price == 649) {
//            System.out.println();
//        }
        prices[idx(price)] += count;
    }

    private int idx(final int price) {
        return price % prices.length;
    }
}
