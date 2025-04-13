package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionStoreRepository extends JpaRepository<Transaction, UUID> {

}
