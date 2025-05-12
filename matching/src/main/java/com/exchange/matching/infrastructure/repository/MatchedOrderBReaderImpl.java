package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entity.UnmatchedOrderB;
import com.exchange.matching.domain.repository.ActivatedOrderBReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MatchedOrderBReaderImpl implements ActivatedOrderBReader {

    private final MatchedOrderBReaderRepository matchedOrderBReaderRepository;

    @Override
    public Optional<UnmatchedOrderB> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(OrderType oppositeType, String tradingPair) {
        return matchedOrderBReaderRepository.findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(oppositeType, tradingPair);
    }

    @Override
    public Optional<UnmatchedOrderB> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(OrderType oppositeType, String tradingPair) {
        return matchedOrderBReaderRepository.findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(oppositeType, tradingPair);
    }

    @Override
    public List<UnmatchedOrderB> findAll() {
        return matchedOrderBReaderRepository.findAll();
    }
}
