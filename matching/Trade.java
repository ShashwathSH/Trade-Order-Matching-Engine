package matching;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Trade {

    private static final AtomicLong ID_GEN = new AtomicLong(1);
    private final long tradeId;
    private final long buyOrderId;
    private final long sellOrderId;
    private final BigDecimal price;
    private final Instant timestamp;

    public Trade( long buyOrderId, long sellOrderId, BigDecimal price) {
        this.tradeId = ID_GEN.getAndIncrement();
        if(buyOrderId <= 0 || sellOrderId <= 0){
            throw new IllegalArgumentException("Id must be positive");
        }
        if(buyOrderId == sellOrderId){
            throw new IllegalArgumentException("Buy and Sell order must be different..");
        }
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        if(price == null || price.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Enter a valid price...");
        }
        this.price = price;
        this.timestamp = Instant.now();
    }

    public long getTradeId() {
        return tradeId;
    }

    public long getBuyOrderId() {
        return buyOrderId;
    }

    public long getSellOrderId() {
        return sellOrderId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "tradeId=" + tradeId +
                ", buyOrderId=" + buyOrderId +
                ", sellOrderId=" + sellOrderId +
                ", price=" + price +
                ", timestamp=" + timestamp +
                '}';
    }
}
