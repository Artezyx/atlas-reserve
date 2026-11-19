package dev.atlasreserve.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<ReservationEntity,UUID> {
    Optional<ReservationEntity> findByTenantIdAndOrderRef(String tenantId,String orderRef);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReservationEntity r where r.tenantId=:tenant and r.id=:id")
    Optional<ReservationEntity> locked(@Param("tenant") String tenant,@Param("id") UUID id);
}
