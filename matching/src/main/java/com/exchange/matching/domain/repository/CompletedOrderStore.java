package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entiry.CompletedOrder;

public interface CompletedOrderStore {

    void save(CompletedOrder completedOrder);

    void deleteAll();
}
