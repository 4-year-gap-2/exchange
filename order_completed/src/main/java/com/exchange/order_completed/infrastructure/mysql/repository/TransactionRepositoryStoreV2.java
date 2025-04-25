package com.exchange.order_completed.infrastructure.mysql.repository;

import com.exchange.order_completed.domain.entiry.TransactionV2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepositoryStoreV2 extends JpaRepository<TransactionV2, UUID> {
}
