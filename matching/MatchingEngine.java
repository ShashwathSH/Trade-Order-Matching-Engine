package matching;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * High-level engine that wraps OrderBook and logging.
 */
public class MatchingEngine implements AutoCloseable {
    private final OrderBook orderBook;
    private final FileLogger logger;
    private final List<Trade> trades = new ArrayList<>();

    public MatchingEngine(FileLogger logger, BiFunction<Order, Order, BigDecimal> execPriceStrategy) {
        this.logger = logger;
        this.orderBook = new OrderBook(execPriceStrategy);
    }

    public synchronized List<Trade> submit(Order order) {
        logger.log("SUBMIT: " + order);
        List<Trade> newTrades = orderBook.submitOrder(order);
        if (!newTrades.isEmpty()) {
            for (Trade t : newTrades) {
                logger.log("TRADE: " + t);
                trades.add(t);
            }
        }
        // Log snapshot for visibility
        logger.log(orderBook.snapshot());
        return newTrades;
    }

    public List<Trade> getTrades() { return List.copyOf(trades); }

    @Override
    public void close() throws Exception {
        if (logger != null) logger.close();
    }
}
