package matching;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiFunction;

public class OrderBook {
    // buy: higher price first, then earlier timestamp
    private final PriorityBlockingQueue<Order> buys =
            new PriorityBlockingQueue<>(11, (a, b) -> {
                int cmp = b.getPrice().compareTo(a.getPrice()); // reverse: highest price first
                if (cmp != 0) return cmp;
                return a.getTimestamp().compareTo(b.getTimestamp());
                // earlier timestamp first
            });

    // sell: lower price first, then earlier timestamp
    private final PriorityBlockingQueue<Order> sells =
            new PriorityBlockingQueue<>(11, (a, b) -> {
                int cmp = a.getPrice().compareTo(b.getPrice()); // lowest price first
                if (cmp != 0) return cmp;
                return a.getTimestamp().compareTo(b.getTimestamp());
            });

    // execution price strategy must return BigDecimal now
    private final BiFunction<Order, Order, BigDecimal> executionPriceStrategy;

    public OrderBook(BiFunction<Order, Order, BigDecimal> executionPriceStrategy) {
        this.executionPriceStrategy = executionPriceStrategy;
    }

    public PriorityBlockingQueue<Order> getBuys() { return buys; }
    public PriorityBlockingQueue<Order> getSells() { return sells; }


    public synchronized List<Trade> submitOrder(Order newOrder) {
        List<Trade> trades = new ArrayList<>();
        if (newOrder.getSide() == Order.Side.BUY) {
            // try match with best sell(s)
            while (!newOrder.isFilled() && !sells.isEmpty()) {
                Order bestSell = sells.peek();
                if (bestSell == null) break;
                // if buy price < best sell price -> no match
                if (newOrder.getPrice().compareTo(bestSell.getPrice()) < 0) break;

                long qty = Math.min(newOrder.getRemainingQty(), bestSell.getRemainingQty());
                BigDecimal execPrice = executionPriceStrategy.apply(newOrder, bestSell);
                Trade t = new Trade(newOrder.getId(), bestSell.getId(), execPrice);
                trades.add(t);

                newOrder.reduce(qty);
                bestSell.reduce(qty);

                if (bestSell.isFilled()) sells.poll();
            }
            if (!newOrder.isFilled()) buys.add(newOrder);
        } else { // SELL
            while (!newOrder.isFilled() && !buys.isEmpty()) {
                Order bestBuy = buys.peek();
                if (bestBuy == null) break;
                // if best buy price < sell price -> no match
                if (bestBuy.getPrice().compareTo(newOrder.getPrice()) < 0) break;

                long qty = Math.min(newOrder.getRemainingQty(), bestBuy.getRemainingQty());
                BigDecimal execPrice = executionPriceStrategy.apply(bestBuy, newOrder);
                Trade t = new Trade(bestBuy.getId(), newOrder.getId(), execPrice);
                trades.add(t);

                newOrder.reduce(qty);
                bestBuy.reduce(qty);

                if (bestBuy.isFilled()) buys.poll();
            }
            if (!newOrder.isFilled()) sells.add(newOrder);
        }
        return trades;
    }

    /**
     * Helper to produce a readable snapshot. Sorting uses BigDecimal.compareTo.
     */
    public synchronized String snapshot() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== OrderBook Snapshot ===\n");
        sb.append("BUYS (top first):\n");
        buys.stream()
                .sorted((a, b) -> {
                    int cmp = b.getPrice().compareTo(a.getPrice());
                    if (cmp != 0) return cmp;
                    return a.getTimestamp().compareTo(b.getTimestamp());
                })
                .forEach(o -> sb.append(o).append("\n"));

        sb.append("SELLS (top first):\n");
        sells.stream()
                .sorted((a, b) -> {
                    int cmp = a.getPrice().compareTo(b.getPrice());
                    if (cmp != 0) return cmp;
                    return a.getTimestamp().compareTo(b.getTimestamp());
                })
                .forEach(o -> sb.append(o).append("\n"));

        return sb.toString();
    }
}
