package com.exchange.matching.domain.repository;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entity.UnmatchedOrderA;

import java.util.List;
import java.util.Optional;

public interface ActivatedOrderReader {

    Optional<UnmatchedOrderA> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(OrderType oppositeType, String tradingPair);

    Optional<UnmatchedOrderA> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(OrderType oppositeType, String tradingPair);

    List<UnmatchedOrderA> findAll();
}
