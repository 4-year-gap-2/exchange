package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entity.UnmatchedOrderB;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchedOrderBStoreRepository extends JpaRepository<UnmatchedOrderB, UUID> {

}
