package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entiry.TransactionV1;

public interface TransactionStoreV1 {
    TransactionV1 saveWithConsistencyLevel(TransactionV1 transactionV1);
}
