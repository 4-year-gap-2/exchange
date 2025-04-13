package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.TransactionB;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionBStoreRepository extends JpaRepository<TransactionB, Long> {
}
