package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.entity.MatchedOrder;
import com.exchange.order_completed.domain.repository.MatchedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
    public MatchedOrder findByUserIdAndOrderId(UUID userId, UUID orderId, Integer attempt) {
        return (attempt == 1
                ? matchedOrderReaderRepository.findByUserAndOrderWithLocalOne(userId, orderId)
                : matchedOrderReaderRepository.findByUserAndOrderWithLocalQuorum(userId, orderId)
        ).orElse(null);
    }

    @Override
    public Optional<MatchedOrder> findByOrderId(UUID orderId) {
        return matchedOrderReaderRepository.findByOrderId(orderId);
    }
}
