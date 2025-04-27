package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.exchange.order_completed.domain.entity.CompletedOrder;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CompletedOrderReaderRepository extends CassandraRepository<CompletedOrder, UUID> {

    @Query("SELECT * FROM completed_order WHERE user_id = :userId AND order_id = :orderId")
    @Consistency(DefaultConsistencyLevel.LOCAL_ONE)
    Optional<CompletedOrder> findByUserAndOrderWithLocalOne(
            @Param("userId") UUID userId,
            @Param("orderId") UUID orderId
    );

    @Query("SELECT * FROM completed_order WHERE user_id = :userId AND order_id = :orderId")
    @Consistency(DefaultConsistencyLevel.LOCAL_QUORUM)
    Optional<CompletedOrder> findByUserAndOrderWithLocalQuorum(
            @Param("userId") UUID userId,
            @Param("orderId") UUID orderId
    );

    CompletedOrder findByUserIdAndOrderId(UUID userId, UUID orderId);

    Optional<CompletedOrder> findByOrderId(UUID orderId);
}
