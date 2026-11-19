package dev.atlasreserve.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.atlasreserve.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.UUID;

@Service
public class InventoryService {
    private final StockRepository stocks;
    private final ReservationRepository reservations;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    public InventoryService(StockRepository stocks,ReservationRepository reservations,OutboxRepository outbox,ObjectMapper mapper){
        this.stocks=stocks;this.reservations=reservations;this.outbox=outbox;this.mapper=mapper;
    }
    @Transactional
    public StockEntity createStock(String tenant,String sku,long onHand){
        return stocks.save(new StockEntity(tenant,sku,onHand));
    }
    @Transactional(readOnly=true)
    public StockEntity getStock(String tenant,String sku){
        return stocks.findByTenantIdAndSku(tenant,sku).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"sku_not_found"));
    }
    @Transactional
    public ReservationEntity reserve(String tenant,String sku,String orderRef,long quantity){
        // Serializes requests for same tenant + SKU, avoiding concurrent over-allocation.
        var stock=stocks.locked(tenant,sku).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"sku_not_found"));
        var existing=reservations.findByTenantIdAndOrderRef(tenant,orderRef);
        if(existing.isPresent()){
            var prior=existing.get();
            if(!prior.sku().equals(sku) || prior.quantity()!=quantity)throw new ResponseStatusException(HttpStatus.CONFLICT,"order_ref_conflict");
            return prior;
        }
        stock.reserve(quantity);
        var r=reservations.save(new ReservationEntity(tenant,orderRef,sku,quantity));
        enqueue(tenant,"inventory.reserved",r);
        return r;
    }
    @Transactional
    public ReservationEntity confirm(String tenant,UUID id){
        var r=reservations.locked(tenant,id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"reservation_not_found"));
        if(r.status()==dev.atlasreserve.domain.ReservationState.CONFIRMED)return r;
        if(r.status()!=dev.atlasreserve.domain.ReservationState.RESERVED)throw new ResponseStatusException(HttpStatus.CONFLICT,"invalid_transition");
        var stock=stocks.locked(tenant,r.sku()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        stock.confirm(r.quantity());r.confirm();enqueue(tenant,"inventory.confirmed",r);return r;
    }
    @Transactional
    public ReservationEntity cancel(String tenant,UUID id){
        var r=reservations.locked(tenant,id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"reservation_not_found"));
        if(r.status()==dev.atlasreserve.domain.ReservationState.CANCELLED)return r;
        if(r.status()!=dev.atlasreserve.domain.ReservationState.RESERVED)throw new ResponseStatusException(HttpStatus.CONFLICT,"invalid_transition");
        var stock=stocks.locked(tenant,r.sku()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        stock.cancel(r.quantity());r.cancel();enqueue(tenant,"inventory.cancelled",r);return r;
    }
    private void enqueue(String tenant,String type,ReservationEntity r){
        try {
            outbox.save(new OutboxEntity(tenant,type,mapper.writeValueAsString(Map.of("reservation_id",r.id(),"tenant_id",tenant,"sku",r.sku(),"quantity",r.quantity(),"status",r.status().name()))));
        } catch(JsonProcessingException e) {throw new IllegalStateException("serialization_failure",e);}
    }
}
