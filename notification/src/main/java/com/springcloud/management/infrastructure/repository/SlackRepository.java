package com.springcloud.management.infrastructure.repository;

import com.springcloud.management.domain.entity.Slack;
import com.springcloud.management.domain.repository.SlackReader;
import com.springcloud.management.domain.repository.SlackStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlackRepository extends JpaRepository<Slack, UUID>, SlackStore, SlackReader {
}
