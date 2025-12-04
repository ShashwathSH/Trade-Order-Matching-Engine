package matching;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public final class Order {
    public enum Side {BUY, SELL}
    private static final AtomicLong ID_GEN = new AtomicLong(1);
    private final long seq = ID_GEN.getAndIncrement();
    private final UUID id;
    private final Side side;
    private final Instant timestamp;
    private final BigDecimal price;
    private final long originalQty;
    private long remainingQty;

    public Order(Side side, BigDecimal price, long originalQty) {
        this.id = UUID.randomUUID();
        this.side = side;
        this.timestamp = Instant.now();
        if(price == null || price.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Enter a valid price....");
        }
        this.price = price;

        if(originalQty<=0) throw new IllegalArgumentException("Original quantity must be > 0");
        this.originalQty = originalQty;
        this.remainingQty = originalQty;
    }

    public UUID getId() {
        return id;
    }

    public Side getSide() {
        return side;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public long getOriginalQty() {
        return originalQty;
    }

    public synchronized long getRemainingQty() {
        return remainingQty;
    }

    public long getSeq() {
        return seq;
    }

    public synchronized void reduce(long q) {
        if (q > 0 && q <= remainingQty) {
            remainingQty -= q;
        } else {
            throw new IllegalArgumentException("Not enough qty");
        }

    }
    @Override
    public String toString(){
        return "Order{" +
                "seq=" + seq +
                ", id=" + id +
                ", side=" + side +
                ".price=" + price +
                ", remainingQty=" + remainingQty +
                '}';
    }

}
