package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entity.MatchedOrder;

public interface CompletedOrderStore {

    void save(MatchedOrder matchedOrder);

    void deleteAll();
}
