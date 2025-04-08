package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.TransactionV2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepositoryStoreV2 extends JpaRepository<TransactionV2, UUID> {
}
