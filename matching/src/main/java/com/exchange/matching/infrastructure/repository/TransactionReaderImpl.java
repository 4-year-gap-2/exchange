package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.Transaction;
import com.exchange.matching.domain.repository.TransactionReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionReaderImpl implements TransactionReader {

    private final TransactionReaderRepository repository;

    @Override
    public Optional<Transaction> findTopByTypeAndTradingPairAndStatusOrderByPriceAscCreatedAtAsc(OrderType type, String tradingPair, OrderStatus status) {
        return repository.findTopByTypeAndTradingPairAndStatusOrderByPriceAscCreatedAtAsc(type, tradingPair, status);
    }

    @Override
    public Optional<Transaction> findTopByTypeAndTradingPairAndStatusOrderByPriceDescCreatedAtAsc(OrderType type, String tradingPair, OrderStatus status) {
        return repository.findTopByTypeAndTradingPairAndStatusOrderByPriceDescCreatedAtAsc(type, tradingPair, status);
    }

    @Override
    public List<Transaction> findAll() {
        return repository.findAll();
    }
}
