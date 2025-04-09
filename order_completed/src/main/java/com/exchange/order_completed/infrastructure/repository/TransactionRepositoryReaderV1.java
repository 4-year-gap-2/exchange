package com.exchange.order_completed.infrastructure.repository;

import com.exchange.order_completed.domain.entiry.TransactionV1;
import com.exchange.order_completed.domain.repository.TransactionReaderV1;
import com.exchange.order_completed.domain.repository.TransactionStoreV1;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepositoryReaderV1 extends CassandraRepository<TransactionV1, String>{

}
