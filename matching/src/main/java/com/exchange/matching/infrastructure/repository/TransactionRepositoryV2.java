package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.TransactionV2;
import com.exchange.matching.domain.repository.TransactionReader;
import com.exchange.matching.domain.repository.TransactionStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepositoryV2 extends JpaRepository<TransactionV2, UUID>, TransactionReader, TransactionStore {
    Page<TransactionV2> findByUserId(String userId, Pageable pageable);
}
