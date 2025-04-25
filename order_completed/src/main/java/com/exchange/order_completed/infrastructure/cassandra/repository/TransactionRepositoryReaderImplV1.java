package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.exchange.order_completed.application.query.FindTransactionQuery;
import com.exchange.order_completed.domain.entity.TransactionV1;
import com.exchange.order_completed.domain.repository.TransactionReaderV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryReaderImplV1 implements TransactionReaderV1 {

    private final CassandraOperations cassandraTemplate;

    public Slice<TransactionV1> findByUserIdWithConsistencyLevel(FindTransactionQuery query, Pageable pageable) {
        Select select = QueryBuilder.selectFrom("transactions").all()
                .whereColumn("user_id").isEqualTo(QueryBuilder.literal(query.userId()))
                .whereColumn("year_month").isEqualTo(QueryBuilder.literal(query.yearMonth()));

        SimpleStatement statement = select.build();

        statement = statement
                .setPageSize(pageable.getPageSize())
                .setConsistencyLevel(ConsistencyLevel.QUORUM);

        if (pageable.getPageNumber() > 0 && pageable instanceof CassandraPageRequest cassandraPageRequest) {
            ByteBuffer pagingState = cassandraPageRequest.getPagingState();
            if (pagingState != null) {
                statement = statement.setPagingState(pagingState);
            }
        }

        return cassandraTemplate.slice(statement, TransactionV1.class);
    }
}
