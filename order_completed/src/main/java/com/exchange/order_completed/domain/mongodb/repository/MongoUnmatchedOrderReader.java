package com.exchange.order_completed.domain.mongodb.repository;

import com.exchange.order_completed.domain.mongodb.entity.MongoUnmatchedOrder;

import java.util.UUID;

public interface MongoUnmatchedOrderReader {

    MongoUnmatchedOrder findUnmatchedOrder(UUID orderId);
}
