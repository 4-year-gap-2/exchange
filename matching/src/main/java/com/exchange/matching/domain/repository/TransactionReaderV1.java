package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entiry.TransactionV1;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface TransactionReaderV1 {
    Slice<TransactionV1> findByUserIdWithConsistencyLevel(UUID userId, Pageable pageable);
}
