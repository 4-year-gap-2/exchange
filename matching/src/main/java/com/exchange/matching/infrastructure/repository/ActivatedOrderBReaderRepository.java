package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entiry.ActivatedOrderB;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActivatedOrderBReaderRepository extends JpaRepository<ActivatedOrderB, UUID> {

    Optional<ActivatedOrderB> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(
            OrderType type, String tradingPair);

    Optional<ActivatedOrderB> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(
            OrderType type, String tradingPair);
}
