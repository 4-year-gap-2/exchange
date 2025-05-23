package com.exchange.order_completed.domain.cassandra.repository;

import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchedOrderReader {

    List<MatchedOrder> findByUserIdAndShardInAndYearMonthDateRange(UUID userId, int shard1, int shard2, int shard3, LocalDate fromDate, LocalDate toDate);
}
