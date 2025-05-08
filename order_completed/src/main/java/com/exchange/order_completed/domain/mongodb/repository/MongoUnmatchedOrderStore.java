package com.exchange.order_completed.domain.mongodb.repository;

import com.exchange.order_completed.domain.mongodb.entity.MongoUnmatchedOrder;

public interface MongoUnmatchedOrderStore {

    void save(MongoUnmatchedOrder mongoUnmatchedOrder);
}
