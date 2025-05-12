package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entity.MatchedOrder;
import com.exchange.matching.domain.repository.CompletedOrderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchedOrderStoreImpl implements CompletedOrderStore {

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
