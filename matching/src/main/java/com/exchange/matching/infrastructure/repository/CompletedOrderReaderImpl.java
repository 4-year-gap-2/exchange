package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.CompletedOrder;
import com.exchange.matching.domain.repository.CompletedOrderReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CompletedOrderReaderImpl implements CompletedOrderReader {

    private final CompletedOrderReaderRepository completedOrderReaderRepository;

    @Override
    public List<CompletedOrder> findAll() {
        return completedOrderReaderRepository.findAll();
    }
}
