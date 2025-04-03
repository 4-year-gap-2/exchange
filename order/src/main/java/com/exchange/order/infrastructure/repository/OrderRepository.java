package com.exchange.order.infrastructure.repository;

import com.exchange.matching.domain.entiry.MatchingTest;
import com.exchange.matching.domain.repository.MatchingReader;
import com.exchange.matching.domain.repository.MatchingStore;
import com.exchange.matching.infrastructure.repository.MatchingRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<MatchingTest, UUID>, MatchingRepositoryCustom, MatchingReader, MatchingStore {
}
