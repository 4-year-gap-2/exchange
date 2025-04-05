package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.TransactionV1;
import com.exchange.matching.domain.repository.TransactionReader;
import com.exchange.matching.domain.repository.TransactionStore;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;


import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends CassandraRepository<TransactionV1, String>, TransactionReader, TransactionStore {
    Slice<TransactionV1> findByUserId(String userId, Pageable pageable);

    @Query("SELECT * FROM exchange.transactions WHERE user_id = ?0 AND transaction_date >= ?1 AND transaction_date <= ?2 ALLOW FILTERING")
    List<TransactionV1> findByUserIdAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);
}
