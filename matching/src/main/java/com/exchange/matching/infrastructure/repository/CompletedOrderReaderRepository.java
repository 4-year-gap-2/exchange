package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.CompletedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompletedOrderReaderRepository extends JpaRepository<CompletedOrder, UUID> {

}
