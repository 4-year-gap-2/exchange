package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.MatchingTest;
import com.exchange.matching.domain.repository.MatchingReader;
import com.exchange.matching.domain.repository.MatchingStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchingRepositoryReader extends JpaRepository<MatchingTest, UUID>, MatchingReader{
}
