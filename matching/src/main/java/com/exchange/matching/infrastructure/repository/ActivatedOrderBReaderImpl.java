package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entiry.ActivatedOrderB;
import com.exchange.matching.domain.repository.ActivatedOrderBReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ActivatedOrderBReaderImpl implements ActivatedOrderBReader {

    private final ActivatedOrderBReaderRepository activatedOrderBReaderRepository;

    @Override
    public Optional<ActivatedOrderB> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(OrderType oppositeType, String tradingPair) {
        return activatedOrderBReaderRepository.findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(oppositeType, tradingPair);
    }

    @Override
    public Optional<ActivatedOrderB> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(OrderType oppositeType, String tradingPair) {
        return activatedOrderBReaderRepository.findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(oppositeType, tradingPair);
    }

    @Override
    public List<ActivatedOrderB> findAll() {
        return activatedOrderBReaderRepository.findAll();
    }
}
