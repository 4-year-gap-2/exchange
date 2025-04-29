package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.exchange.order_completed.domain.entity.MatchedOrder;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MatchedOrderReaderRepository extends CassandraRepository<MatchedOrder, UUID> {

    @Query("SELECT * FROM matched_order WHERE user_id = :userId AND idempotency_id = :idempotencyId")
    @Consistency(DefaultConsistencyLevel.LOCAL_ONE)
    Optional<MatchedOrder> findByUserIdAndIdempotencyIdWithLocalOne(
            @Param("userId") UUID userId,
            @Param("idempotencyId") UUID idempotencyId
    );

    @Query("SELECT * FROM matched_order WHERE user_id = :userId AND idempotency_id = :idempotencyId")
    @Consistency(DefaultConsistencyLevel.LOCAL_QUORUM)
    Optional<MatchedOrder> findByUserIdAndIdempotencyIdWithLocalQuorum(
            @Param("userId") UUID userId,
            @Param("idempotencyId") UUID idempotencyId
    );

    MatchedOrder findByUserIdAndOrderId(UUID userId, UUID orderId);

    Optional<MatchedOrder> findByOrderId(UUID orderId);
}
