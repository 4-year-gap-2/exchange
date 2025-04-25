package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.entity.CompletedOrder;

public interface CompletedOrderStore {

    void save(CompletedOrder completedOrder);

    void deleteAll();
}
