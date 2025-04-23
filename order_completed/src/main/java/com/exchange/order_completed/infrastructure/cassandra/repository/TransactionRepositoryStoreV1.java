package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.exchange.order_completed.domain.entiry.TransactionV1;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface TransactionRepositoryStoreV1 extends CassandraRepository<TransactionV1, String> {
}
