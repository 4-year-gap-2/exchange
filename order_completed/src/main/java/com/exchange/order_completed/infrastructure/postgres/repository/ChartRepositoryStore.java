package com.exchange.order_completed.infrastructure.postgres.repository;

import com.exchange.order_completed.domain.postgres.entity.Chart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChartRepositoryStore extends JpaRepository<Chart, UUID> {
}
