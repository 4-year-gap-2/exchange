package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entity.UnmatchedOrderB;
import com.exchange.matching.domain.repository.ActivatedOrderBStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MatchedOrderBStoreImpl implements ActivatedOrderBStore {

    private final MatchedOrderBStoreRepository matchedOrderBStoreRepository;

    @Override
    public void delete(UnmatchedOrderB oppositeOrder) {
        matchedOrderBStoreRepository.delete(oppositeOrder);
    }

    @Override
    public void save(UnmatchedOrderB activatedOrder) {
        matchedOrderBStoreRepository.save(activatedOrder);
    }

    @Override
    public void deleteAll() {
        matchedOrderBStoreRepository.deleteAll();
    }

    @Override
    public void saveAll(List<UnmatchedOrderB> unmatchedOrderBList) {
        matchedOrderBStoreRepository.saveAll(unmatchedOrderBList);
    }
}
