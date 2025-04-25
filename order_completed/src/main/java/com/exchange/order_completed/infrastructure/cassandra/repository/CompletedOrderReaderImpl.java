package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.entiry.CompletedOrder;
import com.exchange.order_completed.domain.repository.CompletedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CompletedOrderReaderImpl implements CompletedOrderReader {

    private final CompletedOrderReaderRepository completedOrderReaderRepository;

    @Override
    public CompletedOrder findByUserIdAndOrderId(UUID userId, UUID orderId) {
        return completedOrderReaderRepository.findByUserIdAndOrderId(userId, orderId);
    }

    @Override
    public Optional<Object> findByOrderId(UUID orderId) {
        return completedOrderReaderRepository.findByOrderId(orderId);
    }
}
