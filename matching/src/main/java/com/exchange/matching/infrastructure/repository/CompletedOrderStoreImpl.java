package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.CompletedOrder;
import com.exchange.matching.domain.repository.CompletedOrderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompletedOrderStoreImpl implements CompletedOrderStore {

    private final CompletedOrderStoreRepository completedOrderStoreRepository;

    @Override
    public void save(CompletedOrder completedOrder) {
        completedOrderStoreRepository.save(completedOrder);
    }

    @Override
    public void deleteAll() {
        completedOrderStoreRepository.deleteAll();
    }
}
