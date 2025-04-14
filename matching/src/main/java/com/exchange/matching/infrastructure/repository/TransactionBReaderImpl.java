package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.TransactionB;
import com.exchange.matching.domain.repository.TransactionBReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionBReaderImpl implements TransactionBReader {

    private final TransactionBReaderRepository repository;

    @Override
    public Optional<TransactionB> findTopByTypeAndTradingPairAndStatusOrderByPriceAscCreatedAtAsc(OrderType oppositeType, String tradingPair, OrderStatus orderStatus) {
        return repository.findTopByTypeAndTradingPairAndStatusOrderByPriceAscCreatedAtAsc(oppositeType, tradingPair, orderStatus);
    }

    @Override
    public Optional<TransactionB> findTopByTypeAndTradingPairAndStatusOrderByPriceDescCreatedAtAsc(OrderType oppositeType, String tradingPair, OrderStatus orderStatus) {
        return repository.findTopByTypeAndTradingPairAndStatusOrderByPriceDescCreatedAtAsc(oppositeType, tradingPair, orderStatus);
    }

    @Override
    public List<TransactionB> findAll() {
        return repository.findAll();
    }
}
