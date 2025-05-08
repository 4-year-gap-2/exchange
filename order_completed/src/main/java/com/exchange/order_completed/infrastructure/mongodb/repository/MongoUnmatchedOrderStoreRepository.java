package com.exchange.order_completed.infrastructure.mongodb.repository;

import com.exchange.order_completed.domain.mongodb.entity.MongoUnmatchedOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface MongoUnmatchedOrderStoreRepository extends MongoRepository<MongoUnmatchedOrder, UUID> {
}
