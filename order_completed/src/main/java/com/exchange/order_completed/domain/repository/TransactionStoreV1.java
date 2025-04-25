package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.domain.entity.TransactionV1;

public interface TransactionStoreV1 {
    TransactionV1 saveWithConsistencyLevel(TransactionV1 transactionV1);
}
