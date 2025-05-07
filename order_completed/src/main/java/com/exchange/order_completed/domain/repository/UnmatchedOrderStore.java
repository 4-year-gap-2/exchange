package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;

public interface UnmatchedOrderStore {

    void save(UnmatchedOrder unmatchedOrder);

    void save(com.exchange.order_completed.domain.mongodb.entity.UnmatchedOrder unmatchedOrder);
}
