package com.exchange.order_completed.infrastructure.repository;

import com.exchange.order_completed.domain.entiry.CompletedOrder;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface CompletedOrderStoreRepository extends CassandraRepository<CompletedOrder, UUID> {

}
