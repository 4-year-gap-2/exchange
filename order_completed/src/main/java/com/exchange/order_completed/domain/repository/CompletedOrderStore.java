package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.entiry.CompletedOrder;

public interface CompletedOrderStore {

    void save(CompletedOrder completedOrder);
}
