package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.entity.MatchedOrder;

public interface MatchedOrderStore {

    void save(MatchedOrder matchedOrder);

    void deleteAll();
}
