package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.TransactionV1;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface TransactionRepositoryStoreV1 extends CassandraRepository<TransactionV1, String> {
}
