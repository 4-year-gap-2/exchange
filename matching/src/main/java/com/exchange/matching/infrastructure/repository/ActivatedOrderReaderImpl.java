package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entiry.ActivatedOrder;
import com.exchange.matching.domain.repository.ActivatedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ActivatedOrderReaderImpl implements ActivatedOrderReader {

    private final ActivatedOrderReaderRepository activatedOrderReaderRepository;

    @Override
    public Optional<ActivatedOrder> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(OrderType oppositeType, String tradingPair) {
        return activatedOrderReaderRepository.findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(oppositeType, tradingPair);
    }

    @Override
    public Optional<ActivatedOrder> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(OrderType oppositeType, String tradingPair) {
        return activatedOrderReaderRepository.findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(oppositeType, tradingPair);
    }

    @Override
    public List<ActivatedOrder> findAll() {
        return activatedOrderReaderRepository.findAll();
    }
}
