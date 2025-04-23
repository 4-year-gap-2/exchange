package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.entiry.CompletedOrder;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompletedOrderStoreRepository extends CassandraRepository<CompletedOrder, UUID> {

}
