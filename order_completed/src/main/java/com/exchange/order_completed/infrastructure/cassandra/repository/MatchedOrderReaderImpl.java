package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;
import com.exchange.order_completed.domain.repository.MatchedOrderReader;
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
    public MatchedOrder findMatchedOrder(UUID userId, int shard, LocalDate yearMonthDate, UUID idempotencyId, Integer attempt) {
        return (attempt == 1
                ? matchedOrderReaderRepository.findByUserIdAndIdempotencyIdWithLocalOne(userId, shard, yearMonthDate, idempotencyId)
                : matchedOrderReaderRepository.findByUserIdAndIdempotencyIdWithLocalQuorum(userId, shard, yearMonthDate, idempotencyId)
        ).orElse(null);
    }

    @Override
    public Optional<MatchedOrder> findByOrderId(UUID orderId) {
        return matchedOrderReaderRepository.findByOrderId(orderId);
    }

    @Override
    public List<MatchedOrder> findByUserIdAndYearMonthDate(UUID userId, LocalDate yearMonthDate) {
        return matchedOrderReaderRepository.findByUserIdAndYearMonthDate(userId,yearMonthDate);
    }
}