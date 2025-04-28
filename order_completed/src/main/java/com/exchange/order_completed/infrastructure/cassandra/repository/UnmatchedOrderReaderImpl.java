package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.entity.UnmatchedOrder;
import com.exchange.order_completed.domain.repository.UnmatchedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UnmatchedOrderReaderImpl implements UnmatchedOrderReader {

    private final UnmatchedOrderReaderRepository unmatchedOrderReaderRepository;

    @Override
    public UnmatchedOrder findUnmatchedOrder(UUID userId, UUID orderId, Integer attempt) {
        return (attempt == 1
                ? unmatchedOrderReaderRepository.findByUserAndOrderWithLocalOne(userId, orderId)
                : unmatchedOrderReaderRepository.findByUserAndOrderWithLocalQuorum(userId, orderId)
        ).orElse(null);
    }
}
