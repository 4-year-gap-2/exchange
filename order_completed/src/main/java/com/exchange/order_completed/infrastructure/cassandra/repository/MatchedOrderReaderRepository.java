package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchedOrderReaderRepository extends CassandraRepository<MatchedOrder, UUID> {

    @Query("SELECT * FROM matched_order WHERE user_id = :userId AND shard = :shard AND year_month_date = :yearMonthDate")
    @Consistency(DefaultConsistencyLevel.LOCAL_ONE)
    Optional<MatchedOrder> findByUserIdAndIdempotencyIdWithLocalOne(
            @Param("userId") UUID userId,
            @Param("shard") int shard,
            @Param("yearMonthDate") LocalDate yearMonthDate
    );

    @Query("SELECT * FROM matched_order WHERE user_id = :userId AND shard = :shard AND year_month_date = :yearMonthDate")
    @Consistency(DefaultConsistencyLevel.LOCAL_QUORUM)
    Optional<MatchedOrder> findByUserIdAndIdempotencyIdWithLocalQuorum(
            @Param("userId") UUID userId,
            @Param("shard") int shard,
            @Param("yearMonthDate") LocalDate yearMonthDate
    );

    @Query("SELECT * FROM matched_order WHERE user_id = :userId AND shard IN (:shard1, :shard2, :shard3) AND year_month_date >= :fromDate AND year_month_date <= :toDate")
    List<MatchedOrder> findByUserIdAndShardInAndYearMonthDateRange(
            UUID userId,
            int shard1, int shard2, int shard3,
            LocalDate fromDate,
            LocalDate toDate
    );
}
