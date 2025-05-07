package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UnmatchedOrderReaderRepository extends CassandraRepository<UnmatchedOrder, UUID> {

    @Query("SELECT * FROM unmatched_order WHERE user_id = :userId AND order_id = :orderId")
    @Consistency(DefaultConsistencyLevel.LOCAL_ONE)
    Optional<UnmatchedOrder> findByUserAndOrderWithLocalOne(
            @Param("userId") UUID userId,
            @Param("orderId") UUID orderId
    );

    @Query("SELECT * FROM unmatched_order WHERE user_id = :userId AND order_id = :orderId")
    @Consistency(DefaultConsistencyLevel.LOCAL_QUORUM)
    Optional<UnmatchedOrder> findByUserAndOrderWithLocalQuorum(
            @Param("userId") UUID userId,
            @Param("orderId") UUID orderId
    );
}
