package com.exchange.order_completed.infrastructure.mongodb.repository;

import com.exchange.order_completed.domain.mongodb.entity.MongoUnmatchedOrder;
import com.exchange.order_completed.domain.mongodb.repository.MongoUnmatchedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MongoMongoUnmatchedOrderReaderImpl implements MongoUnmatchedOrderReader {

    private final MongoUnmatchedOrderReaderRepository mongoUnmatchedOrderReaderRepository;

    @Override
    public MongoUnmatchedOrder findUnmatchedOrder(UUID orderId) {
        return mongoUnmatchedOrderReaderRepository.findByOrderId(orderId);
    }
}
