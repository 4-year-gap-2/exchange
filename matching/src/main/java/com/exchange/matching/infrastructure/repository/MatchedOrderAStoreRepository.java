package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entity.UnmatchedOrderA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchedOrderAStoreRepository extends JpaRepository<UnmatchedOrderA, UUID> {
}
