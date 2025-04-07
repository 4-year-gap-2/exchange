package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.TransactionV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepositoryReaderV2 extends JpaRepository<TransactionV2, UUID> {
    Page<TransactionV2> findByUserId(UUID userId, Pageable pageable);
}
