package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.Transaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface TransactionReaderRepository extends JpaRepository<Transaction, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Transaction> findTopByTypeAndTradingPairAndStatusOrderByPriceAscCreatedAtAsc(
            OrderType type, String tradingPair, OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Transaction> findTopByTypeAndTradingPairAndStatusOrderByPriceDescCreatedAtAsc(
            OrderType type, String tradingPair, OrderStatus status);
}
