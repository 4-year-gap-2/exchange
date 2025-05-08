package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;
import com.exchange.order_completed.domain.repository.UnmatchedOrderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UnmatchedOrderStoreImpl implements UnmatchedOrderStore {

    private final UnmatchedOrderStoreRepository unmatchedOrderStoreRepository;

    @Override
    public void save(UnmatchedOrder unmatchedOrder) {
        unmatchedOrderStoreRepository.save(unmatchedOrder);
    }

//    @Override
//    public void save(com.exchange.order_completed.domain.mongodb.entity.UnmatchedOrder unmatchedOrder) {
//    }
}
