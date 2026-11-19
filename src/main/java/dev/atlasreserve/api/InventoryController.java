package dev.atlasreserve.api;

import dev.atlasreserve.persistence.*;
import dev.atlasreserve.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/v1")
public class InventoryController {
    private final InventoryService service;
    public InventoryController(InventoryService service){this.service=service;}
    public record CreateStock(@NotBlank String sku,@Min(0) long onHand) {}
    public record CreateReservation(@NotBlank String sku,@NotBlank String orderRef,@Min(1) long quantity) {}
    private String tenant(Jwt jwt){
        String id=jwt.getClaimAsString("tenant_id");
        if(id==null || id.isBlank())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"missing_tenant");
        return id;
    }
    private Object stock(StockEntity s){return Map.of("id",s.id(),"sku",s.sku(),"on_hand",s.onHand(),"reserved",s.reserved(),"available",s.available());}
    private Object reservation(ReservationEntity r){return Map.of("id",r.id(),"sku",r.sku(),"order_ref",r.orderRef(),"quantity",r.quantity(),"status",r.status());}
    @PostMapping("/stock") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('SCOPE_inventory.admin')")
    public Object create(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody CreateStock body){return stock(service.createStock(tenant(jwt),body.sku(),body.onHand()));}
    @GetMapping("/stock/{sku}") @PreAuthorize("hasAnyAuthority('SCOPE_inventory.read','SCOPE_inventory.write','SCOPE_inventory.admin')")
    public Object get(@AuthenticationPrincipal Jwt jwt,@PathVariable String sku){return stock(service.getStock(tenant(jwt),sku));}
    @PostMapping("/reservations") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('SCOPE_inventory.write')")
    public Object reserve(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody CreateReservation body){return reservation(service.reserve(tenant(jwt),body.sku(),body.orderRef(),body.quantity()));}
    @PostMapping("/reservations/{id}/confirm") @PreAuthorize("hasAuthority('SCOPE_inventory.write')")
    public Object confirm(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return reservation(service.confirm(tenant(jwt),id));}
    @PostMapping("/reservations/{id}/cancel") @PreAuthorize("hasAuthority('SCOPE_inventory.write')")
    public Object cancel(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return reservation(service.cancel(tenant(jwt),id));}
}
