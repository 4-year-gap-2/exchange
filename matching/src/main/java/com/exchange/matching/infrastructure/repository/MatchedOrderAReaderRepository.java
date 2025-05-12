package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entity.UnmatchedOrderA;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface MatchedOrderAReaderRepository extends JpaRepository<UnmatchedOrderA, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UnmatchedOrderA> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(
            OrderType type, String tradingPair);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UnmatchedOrderA> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(
            OrderType type, String tradingPair);
}
