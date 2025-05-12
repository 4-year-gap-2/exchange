package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entity.UnmatchedOrderA;
import com.exchange.matching.domain.repository.ActivatedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MatchedOrderAReaderImpl implements ActivatedOrderReader {

    private final MatchedOrderAReaderRepository matchedOrderAReaderRepository;

    @Override
    public Optional<UnmatchedOrderA> findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(OrderType oppositeType, String tradingPair) {
        return matchedOrderAReaderRepository.findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(oppositeType, tradingPair);
    }

    @Override
    public Optional<UnmatchedOrderA> findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(OrderType oppositeType, String tradingPair) {
        return matchedOrderAReaderRepository.findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(oppositeType, tradingPair);
    }

    @Override
    public List<UnmatchedOrderA> findAll() {
        return matchedOrderAReaderRepository.findAll();
    }
}
