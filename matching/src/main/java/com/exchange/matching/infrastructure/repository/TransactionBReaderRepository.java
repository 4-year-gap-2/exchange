package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.TransactionB;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionBReaderRepository extends JpaRepository<TransactionB, Long> {

    Optional<TransactionB> findTopByTypeAndTradingPairAndStatusOrderByPriceAscCreatedAtAsc(
            OrderType type, String tradingPair, OrderStatus status);

    Optional<TransactionB> findTopByTypeAndTradingPairAndStatusOrderByPriceDescCreatedAtAsc(
            OrderType type, String tradingPair, OrderStatus status);
}
