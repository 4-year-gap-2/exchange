package com.exchange.matching.domain.repository;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionReader {

    Optional<Transaction> findTopByTypeAndTradingPairAndStatusOrderByPriceAscCreatedAtAsc(
            OrderType type, String tradingPair, OrderStatus status);

    Optional<Transaction> findTopByTypeAndTradingPairAndStatusOrderByPriceDescCreatedAtAsc(
            OrderType type, String tradingPair, OrderStatus status);

    List<Transaction> findAll();
}
