package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;
import com.exchange.order_completed.domain.cassandra.repository.MatchedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MatchedOrderReaderImpl implements MatchedOrderReader {

    private final MatchedOrderReaderRepository matchedOrderReaderRepository;

    /**
     * attempt == 1 : LOCAL_ONE
     * attempt > 1  : LOCAL_QUORUM
     */
    @Override
    public MatchedOrder findMatchedOrder(UUID userId, int shard, LocalDate yearMonthDate, Integer attempt) {
        return (attempt == 1
                ? matchedOrderReaderRepository.findByUserIdAndIdempotencyIdWithLocalOne(userId, shard, yearMonthDate)
                : matchedOrderReaderRepository.findByUserIdAndIdempotencyIdWithLocalQuorum(userId, shard, yearMonthDate)
        ).orElse(null);
    }

    @Override
    public Optional<MatchedOrder> findByOrderId(UUID orderId) {
        return matchedOrderReaderRepository.findByOrderId(orderId);
    }

    @Override
    public List<MatchedOrder> findByUserIdAndShardInAndYearMonthDateRange(UUID userId, int shard1, int shard2, int shard3, LocalDate fromDate, LocalDate toDate) {
        return matchedOrderReaderRepository.findByUserIdAndShardInAndYearMonthDateRange(userId, shard1, shard2, shard3, fromDate, toDate);
    }

}