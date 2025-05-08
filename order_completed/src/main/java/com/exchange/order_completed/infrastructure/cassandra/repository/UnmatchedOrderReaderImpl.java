package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;
import com.exchange.order_completed.domain.repository.UnmatchedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UnmatchedOrderReaderImpl implements UnmatchedOrderReader {

    private final UnmatchedOrderReaderRepository unmatchedOrderReaderRepository;

    @Override
    public UnmatchedOrder findUnmatchedOrder(UUID userId, int shard, LocalDate yearMonthDate, UUID orderId, Integer attempt) {
        return (attempt == 1
                ? unmatchedOrderReaderRepository.findByUserAndOrderWithLocalOne(userId, shard, yearMonthDate, orderId)
                : unmatchedOrderReaderRepository.findByUserAndOrderWithLocalQuorum(userId, shard, yearMonthDate, orderId)
        ).orElse(null);
    }

    @Override
    public List<UnmatchedOrder> findByUserIdAndShardInAndYearMonthDateRange(UUID userId, int shard1, int shard2, int shard3, LocalDate fromDate, LocalDate toDate) {
        return unmatchedOrderReaderRepository.findByUserIdAndShardInAndYearMonthDateRange(userId, shard1, shard2, shard3, fromDate, toDate);
    }
}
