package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entity.UnmatchedOrderB;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MatchedOrderBReaderRepository extends JpaRepository<UnmatchedOrderB, UUID> {

    Optional<UnmatchedOrderB> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(
            OrderType type, String tradingPair);

    Optional<UnmatchedOrderB> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(
            OrderType type, String tradingPair);
}
