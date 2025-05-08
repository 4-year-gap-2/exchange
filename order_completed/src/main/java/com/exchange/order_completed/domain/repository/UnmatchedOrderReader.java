package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UnmatchedOrderReader {

    UnmatchedOrder findUnmatchedOrder(UUID userId, int shard, LocalDate yearMonthDate, UUID orderId, Integer attempt);

    List<UnmatchedOrder> findByUserIdAndShardInAndYearMonthDateRange(UUID userId, int shard1, int shard2, int shard3, LocalDate fromDate, LocalDate toDate);
}
