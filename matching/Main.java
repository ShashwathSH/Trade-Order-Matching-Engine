package matching;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.BiFunction;

public class Main {
    public static void main(String[] args) throws Exception {
        // Execution price strategy: midpoint of buy and sell price (scale chosen as 8)
        BiFunction<Order, Order, BigDecimal> midpoint = (buy, sell) ->
                buy.getPrice().add(sell.getPrice())
                        .divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_EVEN);

        try (FileLogger logger = new FileLogger("engine.log");
             MatchingEngine engine = new MatchingEngine(logger, midpoint)) {

            // Demo orders (use BigDecimal for price)
            Order o1 = new Order(Order.Side.BUY, BigDecimal.valueOf(10), 150L);
            engine.submit(o1);

            Order o2 = new Order(Order.Side.SELL, BigDecimal.valueOf(149.00), 100L);
            List<Trade> trades1 = engine.submit(o2);
            System.out.println("Trades after o2:");
            trades1.forEach(System.out::println);

            // Partial fills
            Order o3 = new Order(Order.Side.BUY, BigDecimal.valueOf(151.00), 200L);
            engine.submit(o3);

            Order o4 = new Order(Order.Side.SELL, BigDecimal.valueOf(150.50), 50L);
            engine.submit(o4);

            // Add more and show book
            Order o5 = new Order(Order.Side.SELL, BigDecimal.valueOf(152.00), 200L);
            engine.submit(o5);

            System.out.println("All trades so far:");
            engine.getTrades().forEach(System.out::println);

            System.out.println("Final orderbook snapshot in log (engine.log).");
        }
    }
}
