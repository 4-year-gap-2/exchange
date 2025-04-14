package com.exchange.matching.domain.repository;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.TransactionB;

import java.util.List;
import java.util.Optional;

public interface TransactionBReader {

    Optional<TransactionB> findTopByTypeAndTradingPairAndStatusOrderByPriceAscCreatedAtAsc(OrderType oppositeType, String tradingPair, OrderStatus orderStatus);

    Optional<TransactionB> findTopByTypeAndTradingPairAndStatusOrderByPriceDescCreatedAtAsc(OrderType oppositeType, String tradingPair, OrderStatus orderStatus);

    List<TransactionB> findAll();
}
