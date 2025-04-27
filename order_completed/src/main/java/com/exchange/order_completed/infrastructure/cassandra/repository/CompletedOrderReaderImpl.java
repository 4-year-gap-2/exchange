package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.entity.CompletedOrder;
import com.exchange.order_completed.domain.repository.CompletedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CompletedOrderReaderImpl implements CompletedOrderReader {

    private final CompletedOrderReaderRepository completedOrderReaderRepository;

    /**
     * attempt == 1 : LOCAL_ONE
     * attempt > 1  : LOCAL_QUORUM
     */
    @Override
    public CompletedOrder findByUserIdAndOrderId(UUID userId, UUID orderId, Integer attempt) {
        return (attempt == 1
                ? completedOrderReaderRepository.findByUserAndOrderWithLocalOne(userId, orderId)
                : completedOrderReaderRepository.findByUserAndOrderWithLocalQuorum(userId, orderId)
        ).orElse(null);
    }

    @Override
    public Optional<CompletedOrder> findByOrderId(UUID orderId) {
        return completedOrderReaderRepository.findByOrderId(orderId);
    }
}
