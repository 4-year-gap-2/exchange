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

    @Query("SELECT * FROM matched_order WHERE user_id = :userId AND order_id = :orderId")
    @Consistency(DefaultConsistencyLevel.LOCAL_ONE)
    Optional<MatchedOrder> findByUserAndOrderWithLocalOne(
            @Param("userId") UUID userId,
            @Param("orderId") UUID orderId
    );

    @Query("SELECT * FROM matched_order WHERE user_id = :userId AND order_id = :orderId")
    @Consistency(DefaultConsistencyLevel.LOCAL_QUORUM)
    Optional<MatchedOrder> findByUserAndOrderWithLocalQuorum(
            @Param("userId") UUID userId,
            @Param("orderId") UUID orderId
    );

    MatchedOrder findByUserIdAndOrderId(UUID userId, UUID orderId);

    Optional<MatchedOrder> findByOrderId(UUID orderId);
}
