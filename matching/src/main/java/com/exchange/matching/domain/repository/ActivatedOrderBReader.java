package com.exchange.matching.domain.repository;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entity.UnmatchedOrderB;

import java.util.List;
import java.util.Optional;

public interface ActivatedOrderBReader {
    Optional<UnmatchedOrderB> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(OrderType oppositeType, String tradingPair);

    Optional<UnmatchedOrderB> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(OrderType oppositeType, String tradingPair);

    List<UnmatchedOrderB> findAll();
}
