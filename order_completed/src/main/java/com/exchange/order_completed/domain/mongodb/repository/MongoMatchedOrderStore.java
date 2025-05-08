package com.exchange.order_completed.domain.mongodb.repository;

import com.exchange.order_completed.domain.mongodb.entity.MongoMatchedOrder;
import com.exchange.order_completed.domain.mongodb.entity.MongoUnmatchedOrder;

public interface MongoMatchedOrderStore {

    void save(MongoMatchedOrder newMatchedOrder);

    void saveMatchedOrderAndUpdateUnmatchedOrder(MongoMatchedOrder newMatchedOrder, MongoUnmatchedOrder persistentUnmatchedOrder);
}
