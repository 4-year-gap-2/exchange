package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entity.UnmatchedOrderA;
import com.exchange.matching.domain.repository.ActivatedOrderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MatchedOrderAStoreImpl implements ActivatedOrderStore {

    private final MatchedOrderAStoreRepository matchedOrderAStoreRepository;

    @Override
    public void save(UnmatchedOrderA unmatchedOrderA) {
        matchedOrderAStoreRepository.save(unmatchedOrderA);
    }

    @Override
    public void delete(UnmatchedOrderA unmatchedOrderA) {
        matchedOrderAStoreRepository.delete(unmatchedOrderA);
    }

    @Override
    public void deleteAll() {
        matchedOrderAStoreRepository.deleteAll();
    }

    @Override
    public void saveAll(List<UnmatchedOrderA> unmatchedOrderAList) {
        matchedOrderAStoreRepository.saveAll(unmatchedOrderAList);
    }
}
