package com.exchange.order_completed.infrastructure.mongodb.repository;

import com.exchange.order_completed.domain.mongodb.entity.MongoMatchedOrder;
import com.exchange.order_completed.domain.mongodb.repository.MongoMatchedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MongoMongoMatchedOrderReaderImpl implements MongoMatchedOrderReader {

    private final MongoMatchedOrderReaderRepository mongoMatchedOrderReaderRepository;

    @Override
    public MongoMatchedOrder findMatchedOrder(UUID idempotencyId) {
        return mongoMatchedOrderReaderRepository.findByIdempotencyId(idempotencyId);
    }
}
