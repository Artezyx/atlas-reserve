package dev.atlasreserve.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<StockEntity,UUID> {
    Optional<StockEntity> findByTenantIdAndSku(String tenantId,String sku);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockEntity s where s.tenantId=:tenant and s.sku=:sku")
    Optional<StockEntity> locked(@Param("tenant") String tenant,@Param("sku") String sku);
}
