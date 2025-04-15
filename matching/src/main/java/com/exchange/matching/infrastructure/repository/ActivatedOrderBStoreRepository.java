package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.ActivatedOrderB;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivatedOrderBStoreRepository extends JpaRepository<ActivatedOrderB, UUID> {

}
