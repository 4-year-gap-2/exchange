package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entity.MatchedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchedOrderReaderRepository extends JpaRepository<MatchedOrder, UUID> {

}
