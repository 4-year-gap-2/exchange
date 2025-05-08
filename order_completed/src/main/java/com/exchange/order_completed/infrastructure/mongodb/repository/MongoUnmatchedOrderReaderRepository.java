package com.exchange.order_completed.infrastructure.mongodb.repository;

import com.exchange.order_completed.domain.mongodb.entity.MongoUnmatchedOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface MongoUnmatchedOrderReaderRepository extends MongoRepository<MongoUnmatchedOrder, UUID> {

    MongoUnmatchedOrder findByOrderId(UUID orderId);
}
