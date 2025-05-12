package com.exchange.order_completed.config;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;
import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class CassandraTableBootstrapper {

    private final CassandraAdminTemplate adminTemplate;

    @PostConstruct
    public void createTables() {
        adminTemplate.createTable(true, CqlIdentifier.fromCql("unmatched_order"), UnmatchedOrder.class, new HashMap<>());
        adminTemplate.createTable(true, CqlIdentifier.fromCql("matched_order"), MatchedOrder.class, new HashMap<>());
    }
}
