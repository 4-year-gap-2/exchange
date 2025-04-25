package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.entity.CompletedOrder;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompletedOrderReaderRepository extends CassandraRepository<CompletedOrder, UUID> {

    CompletedOrder findByUserIdAndOrderId(UUID userId, UUID orderId);

    Optional<CompletedOrder> findByOrderId(UUID orderId);
}
