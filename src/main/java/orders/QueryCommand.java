package orders;

import java.util.Map;

public class QueryCommand implements Runnable {

    private final OrdersContainer bids;
    private final OrdersContainer asks;
    private final String          query;

    public QueryCommand(
            final OrdersContainer bids,
            final OrdersContainer asks,
            final String query
    ) {
        this.bids = bids;
        this.asks = asks;
        this.query = query;
    }

    @Override
    public void run() {
        /*
        q,best_bid
        q,best_ask
        q,size,<price>
         */
        if (isBidQuery(query)) {
            showPrice(bids.getFirst());
        } else if (isAskQuery(query)) {
            showPrice(asks.getFirst());
        } else {
            showPrice(query);
        }
    }

    private void showPrice(Map.Entry<Integer, Integer> e) {
        if (e == null) {
            System.out.println("empty");
        } else {
            System.out.println(e.getKey() + "," + e.getValue());
        }
    }

    private void showPrice(final String s) {
        int priceBeginIdx = s.lastIndexOf(',') + 1;
        int price         = Utils.parseInt(s, priceBeginIdx, s.length());

        int bidSize  = bids.getSize(price);
        int askSize = asks.getSize(price);

        System.out.println(bidSize + askSize);
    }


    private boolean isAskQuery(final String s) {
        return s.charAt(7) == 'a';
    }

    private boolean isBidQuery(final String s) {
        return s.charAt(7) == 'b';
    }
}
