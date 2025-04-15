package com.exchange.matching.domain.repository;

import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.ActivatedOrder;

import java.util.List;
import java.util.Optional;

public interface ActivatedOrderReader {

    Optional<ActivatedOrder> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(OrderType oppositeType, String tradingPair);

    Optional<ActivatedOrder> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(OrderType oppositeType, String tradingPair);

    List<ActivatedOrder> findAll();
}
