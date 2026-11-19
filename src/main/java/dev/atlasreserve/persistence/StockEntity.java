package dev.atlasreserve.persistence;

import dev.atlasreserve.domain.InventoryMath;
import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="stocks", uniqueConstraints=@UniqueConstraint(columnNames={"tenant_id","sku"}))
public class StockEntity {
    @Id private UUID id = UUID.randomUUID();
    @Column(name="tenant_id",nullable=false) private String tenantId;
    @Column(nullable=false) private String sku;
    @Column(name="on_hand",nullable=false) private long onHand;
    @Column(nullable=false) private long reserved;
    @Version private long version;
    protected StockEntity() {}
    public StockEntity(String tenantId,String sku,long initial) {
        if (initial < 0) throw new IllegalArgumentException("negative_stock");
        this.tenantId=tenantId; this.sku=sku; this.onHand=initial;
    }
    public UUID id(){return id;}
    public String sku(){return sku;}
    public long onHand(){return onHand;}
    public long reserved(){return reserved;}
    public long available(){return onHand-reserved;}
    public void reserve(long qty){ apply(InventoryMath.reserve(new InventoryMath.Amounts(onHand,reserved),qty)); }
    public void confirm(long qty){ apply(InventoryMath.confirm(new InventoryMath.Amounts(onHand,reserved),qty)); }
    public void cancel(long qty){ apply(InventoryMath.cancel(new InventoryMath.Amounts(onHand,reserved),qty)); }
    private void apply(InventoryMath.Amounts x){this.onHand=x.onHand();this.reserved=x.reserved();}
}
