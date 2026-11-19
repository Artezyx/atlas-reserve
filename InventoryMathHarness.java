import dev.atlasreserve.domain.InventoryMath;

/** Dependency-free smoke test of the pure Java 21 domain logic. */
public class InventoryMathHarness {
    public static void main(String[] args) {
        var s = new InventoryMath.Amounts(10,0);
        s = InventoryMath.reserve(s,7);
        if(s.available()!=3)throw new AssertionError("available stock");
        try {InventoryMath.reserve(s,4);throw new AssertionError("oversell allowed");} catch(IllegalStateException expected){}
        s=InventoryMath.confirm(s,5);
        if(s.onHand()!=5 || s.reserved()!=2)throw new AssertionError("confirm");
        s=InventoryMath.cancel(s,2);
        if(s.available()!=5)throw new AssertionError("cancel");
        System.out.println("InventoryMath smoke checks: PASS");
    }
}
