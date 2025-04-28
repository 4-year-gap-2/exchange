package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.entity.MatchedOrder;
import com.exchange.order_completed.domain.repository.MatchedOrderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchedOrderStoreImpl implements MatchedOrderStore {

    private final MatchedOrderStoreRepository matchedOrderStoreRepository;

    @Override
    public void save(MatchedOrder matchedOrder) {
        matchedOrderStoreRepository.save(matchedOrder);
    }

    @Override
    public void deleteAll() {
        matchedOrderStoreRepository.deleteAll();
    }
}
