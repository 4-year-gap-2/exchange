package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.ActivatedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivatedOrderStoreRepository extends JpaRepository<ActivatedOrder, UUID> {
}
