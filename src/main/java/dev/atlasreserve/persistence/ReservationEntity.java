package dev.atlasreserve.persistence;

import dev.atlasreserve.domain.ReservationState;
import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="reservations",uniqueConstraints=@UniqueConstraint(columnNames={"tenant_id","order_ref"}))
public class ReservationEntity {
    @Id private UUID id = UUID.randomUUID();
    @Column(name="tenant_id",nullable=false) private String tenantId;
    @Column(name="order_ref",nullable=false) private String orderRef;
    @Column(nullable=false) private String sku;
    @Column(nullable=false) private long quantity;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private ReservationState status=ReservationState.RESERVED;
    @Version private long version;
    protected ReservationEntity() {}
    public ReservationEntity(String tenantId,String orderRef,String sku,long quantity){
        this.tenantId=tenantId;this.orderRef=orderRef;this.sku=sku;this.quantity=quantity;
    }
    public UUID id(){return id;}
    public String sku(){return sku;}
    public long quantity(){return quantity;}
    public String orderRef(){return orderRef;}
    public ReservationState status(){return status;}
    public void confirm(){if(status!=ReservationState.RESERVED)throw new IllegalStateException("invalid_transition"); status=ReservationState.CONFIRMED;}
    public void cancel(){if(status!=ReservationState.RESERVED)throw new IllegalStateException("invalid_transition"); status=ReservationState.CANCELLED;}
}
