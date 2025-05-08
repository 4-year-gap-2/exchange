package com.exchange.order_completed.infrastructure.mongodb.repository;

import com.exchange.order_completed.domain.mongodb.repository.MongoUnmatchedOrderStore;
import com.exchange.order_completed.domain.mongodb.entity.MongoUnmatchedOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MongoMongoUnmatchedOrderStoreImpl implements MongoUnmatchedOrderStore {

    private final MongoUnmatchedOrderStoreRepository mongoUnmatchedOrderStoreRepository;

    @Override
    public void save(MongoUnmatchedOrder mongoUnmatchedOrder) {
        mongoUnmatchedOrderStoreRepository.save(mongoUnmatchedOrder);
    }
}
