package com.exchange.order_completed.infrastructure.postgesql.repository;

import com.exchange.order_completed.domain.postgresEntity.Chart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartRepositoryReader extends JpaRepository<Chart, UUID> {
}
