package com.exchange.matching.domain.repository;

import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.ActivatedOrderB;

import java.util.List;
import java.util.Optional;

public interface ActivatedOrderBReader {
    Optional<ActivatedOrderB> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(OrderType oppositeType, String tradingPair);

    Optional<ActivatedOrderB> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(OrderType oppositeType, String tradingPair);

    List<ActivatedOrderB> findAll();
}
