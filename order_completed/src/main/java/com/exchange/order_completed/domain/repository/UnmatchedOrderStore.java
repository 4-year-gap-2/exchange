package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.entity.UnmatchedOrder;

public interface UnmatchedOrderStore {

    void save(UnmatchedOrder unmatchedOrder);
}
