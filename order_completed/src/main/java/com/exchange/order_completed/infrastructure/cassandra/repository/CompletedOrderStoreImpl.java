package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.entity.CompletedOrder;
import com.exchange.order_completed.domain.repository.CompletedOrderStore;
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
