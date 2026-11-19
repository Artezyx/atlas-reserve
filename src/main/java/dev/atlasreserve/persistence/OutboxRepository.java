package dev.atlasreserve.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEntity,java.util.UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OutboxEntity o where o.publishedAt is null order by o.id")
    List<OutboxEntity> pending(Pageable pageable);
}
