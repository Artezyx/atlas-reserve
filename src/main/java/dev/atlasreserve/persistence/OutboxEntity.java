package dev.atlasreserve.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="outbox_events")
public class OutboxEntity {
    @Id private UUID id=UUID.randomUUID();
    @Column(name="tenant_id",nullable=false) private String tenantId;
    @Column(name="event_type",nullable=false) private String eventType;
    @Column(name="payload",nullable=false,columnDefinition="text") private String payload;
    @Column(name="published_at") private Instant publishedAt;
    protected OutboxEntity() {}
    public OutboxEntity(String tenantId,String eventType,String payload){this.tenantId=tenantId;this.eventType=eventType;this.payload=payload;}
    public UUID id(){return id;}
    public String tenantId(){return tenantId;}
    public String eventType(){return eventType;}
    public String payload(){return payload;}
    public void markPublished(){this.publishedAt=Instant.now();}
}
