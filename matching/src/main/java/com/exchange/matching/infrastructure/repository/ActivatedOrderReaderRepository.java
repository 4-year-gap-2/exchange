package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.ActivatedOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface ActivatedOrderReaderRepository extends JpaRepository<ActivatedOrder, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ActivatedOrder> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(
            OrderType type, String tradingPair);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ActivatedOrder> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(
            OrderType type, String tradingPair);
}
