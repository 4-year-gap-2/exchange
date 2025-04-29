package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.entity.UnmatchedOrder;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface UnmatchedOrderStoreRepository extends CassandraRepository<UnmatchedOrder, UUID> {

}
