package com.exchange.order_completed.domain.cassandra.repository;

import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;

public interface UnmatchedOrderStore {

    void save(UnmatchedOrder unmatchedOrder);
}
