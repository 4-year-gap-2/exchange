package com.exchange.order_completed.infrastructure.mongodb.repository;

import com.exchange.order_completed.domain.mongodb.entity.MongoMatchedOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface MongoMatchedOrderReaderRepository extends MongoRepository<MongoMatchedOrder, UUID> {

    MongoMatchedOrder findByIdempotencyId(UUID idempotencyId);
}
