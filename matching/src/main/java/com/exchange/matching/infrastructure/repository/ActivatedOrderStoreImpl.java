package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.ActivatedOrder;
import com.exchange.matching.domain.repository.ActivatedOrderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ActivatedOrderStoreImpl implements ActivatedOrderStore {

    private final ActivatedOrderStoreRepository activatedOrderStoreRepository;

    @Override
    public void save(ActivatedOrder activatedOrder) {
        activatedOrderStoreRepository.save(activatedOrder);
    }

    @Override
    public void delete(ActivatedOrder activatedOrder) {
        activatedOrderStoreRepository.delete(activatedOrder);
    }

    @Override
    public void deleteAll() {
        activatedOrderStoreRepository.deleteAll();
    }

    @Override
    public void saveAll(List<ActivatedOrder> activatedOrderList) {
        activatedOrderStoreRepository.saveAll(activatedOrderList);
    }
}
