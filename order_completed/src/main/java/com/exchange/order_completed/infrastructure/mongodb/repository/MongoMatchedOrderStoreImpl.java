package com.exchange.order_completed.infrastructure.mongodb.repository;

import com.exchange.order_completed.domain.mongodb.entity.MongoMatchedOrder;
import com.exchange.order_completed.domain.mongodb.entity.MongoUnmatchedOrder;
import com.exchange.order_completed.domain.mongodb.repository.MongoMatchedOrderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MongoMatchedOrderStoreImpl implements MongoMatchedOrderStore {

    private final MongoMatchedOrderStoreRepository mongoMatchedOrderStoreRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public void save(MongoMatchedOrder newMatchedOrder) {
        mongoMatchedOrderStoreRepository.save(newMatchedOrder);
    }

    @Override
    public void saveMatchedOrderAndUpdateUnmatchedOrder(MongoMatchedOrder newMatchedOrder, MongoUnmatchedOrder persistentUnmatchedOrder) {
        mongoMatchedOrderStoreRepository.save(newMatchedOrder);

        Query query = new Query(Criteria.where("orderId").is(persistentUnmatchedOrder.getOrderId()));
        Update update = new Update().set("quantity", persistentUnmatchedOrder.getQuantity());
        mongoTemplate.updateFirst(query, update, MongoUnmatchedOrder.class);
    }
}
