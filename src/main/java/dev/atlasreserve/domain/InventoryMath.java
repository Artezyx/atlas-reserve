package dev.atlasreserve.domain;

/** Pure domain rules: available = onHand - reserved. Uses checked integer operations. */
public final class InventoryMath {
    private InventoryMath() {}
    public record Amounts(long onHand, long reserved) {
        public Amounts {
            if (onHand < 0 || reserved < 0 || reserved > onHand) throw new IllegalArgumentException("stock invariants violated");
        }
        public long available() { return onHand - reserved; }
    }
    public static Amounts reserve(Amounts a, long quantity) {
        requirePositive(quantity);
        if (quantity > a.available()) throw new IllegalStateException("insufficient_available_stock");
        return new Amounts(a.onHand(), Math.addExact(a.reserved(),quantity));
    }
    public static Amounts confirm(Amounts a, long quantity) {
        requirePositive(quantity);
        if (quantity > a.reserved()) throw new IllegalStateException("insufficient_reserved_stock");
        return new Amounts(Math.subtractExact(a.onHand(),quantity), Math.subtractExact(a.reserved(),quantity));
    }
    public static Amounts cancel(Amounts a, long quantity) {
        requirePositive(quantity);
        if (quantity > a.reserved()) throw new IllegalStateException("insufficient_reserved_stock");
        return new Amounts(a.onHand(), Math.subtractExact(a.reserved(),quantity));
    }
    private static void requirePositive(long quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity_must_be_positive");
    }
}
