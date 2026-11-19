package dev.atlasreserve.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryMathTest {
    @Test void reserveNeverOversells(){
        var s=new InventoryMath.Amounts(10,0);
        s=InventoryMath.reserve(s,8);
        assertEquals(2,s.available());
        var x=s;
        assertThrows(IllegalStateException.class,()->InventoryMath.reserve(x,3));
    }
    @Test void confirmMovesReservedToSold(){
        var s=InventoryMath.confirm(new InventoryMath.Amounts(10,7),3);
        assertEquals(7,s.onHand());assertEquals(4,s.reserved());assertEquals(3,s.available());
    }
    @Test void cancellationRestoresAvailable(){
        var s=InventoryMath.cancel(new InventoryMath.Amounts(5,4),4);
        assertEquals(5,s.available());
    }
    @Test void noNegativeOrOverflow(){
        assertThrows(IllegalArgumentException.class,()->new InventoryMath.Amounts(1,2));
        assertThrows(IllegalArgumentException.class,()->InventoryMath.reserve(new InventoryMath.Amounts(3,0),0));
    }
}
