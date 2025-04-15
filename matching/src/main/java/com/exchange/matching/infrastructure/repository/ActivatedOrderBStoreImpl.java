package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.ActivatedOrderB;
import com.exchange.matching.domain.repository.ActivatedOrderBStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ActivatedOrderBStoreImpl implements ActivatedOrderBStore {

    private final ActivatedOrderBStoreRepository activatedOrderBStoreRepository;

    @Override
    public void delete(ActivatedOrderB oppositeOrder) {
        activatedOrderBStoreRepository.delete(oppositeOrder);
    }

    @Override
    public void save(ActivatedOrderB activatedOrder) {
        activatedOrderBStoreRepository.save(activatedOrder);
    }

    @Override
    public void deleteAll() {
        activatedOrderBStoreRepository.deleteAll();
    }

    @Override
    public void saveAll(List<ActivatedOrderB> activatedOrderBList) {
        activatedOrderBStoreRepository.saveAll(activatedOrderBList);
    }
}
