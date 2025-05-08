package com.exchange.order_completed.domain.mongodb.repository;

import com.exchange.order_completed.domain.mongodb.entity.MongoMatchedOrder;

import java.util.UUID;

public interface MongoMatchedOrderReader {

    MongoMatchedOrder findMatchedOrder(UUID idempotencyId);
}
