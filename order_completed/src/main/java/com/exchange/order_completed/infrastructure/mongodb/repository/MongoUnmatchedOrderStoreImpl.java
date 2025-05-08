//package com.exchange.order_completed.infrastructure.mongodb.repository;
//
//import com.exchange.order_completed.domain.repository.UnmatchedOrderStore;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Repository;
//
//@Repository
//@Primary
////@Repository("mongodbUnmatchedOrderStoreImpl")
//@RequiredArgsConstructor
//public class MongoUnmatchedOrderStoreImpl implements UnmatchedOrderStore {
//
//    private final MongoUnmatchedOrderStoreRepository mongoUnmatchedOrderStoreRepository;
//
//    @Override
//    public void save(com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder unmatchedOrder) {
//    }
//
//    @Override
//    public void save(com.exchange.order_completed.domain.mongodb.entity.UnmatchedOrder unmatchedOrder) {
//        mongoUnmatchedOrderStoreRepository.save(unmatchedOrder);
//    }
//}
