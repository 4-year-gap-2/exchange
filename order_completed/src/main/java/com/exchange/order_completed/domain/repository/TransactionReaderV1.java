package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.application.query.FindTransactionQuery;
import com.exchange.order_completed.domain.entity.TransactionV1;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TransactionReaderV1 {
    Slice<TransactionV1> findByUserIdWithConsistencyLevel(FindTransactionQuery query, Pageable pageable);
}
