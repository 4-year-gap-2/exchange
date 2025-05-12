package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entity.MatchedOrder;
import com.exchange.matching.domain.repository.CompletedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MatchedOrderReaderImpl implements CompletedOrderReader {

    private final MatchedOrderReaderRepository matchedOrderReaderRepository;

    @Override
    public List<MatchedOrder> findAll() {
        return matchedOrderReaderRepository.findAll();
    }
}
