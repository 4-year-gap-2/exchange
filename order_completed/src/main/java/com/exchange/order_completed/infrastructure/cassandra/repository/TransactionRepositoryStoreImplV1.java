package com.exchange.order_completed.infrastructure.cassandra.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.exchange.order_completed.domain.entiry.TransactionV1;
import com.exchange.order_completed.domain.repository.TransactionStoreV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.EntityWriteResult;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryStoreImplV1 implements TransactionStoreV1 {

    private final CassandraOperations cassandraTemplate;

    @Override
    public TransactionV1 saveWithConsistencyLevel(TransactionV1 transaction) {
        InsertOptions insertOptions = InsertOptions.builder()
                .consistencyLevel(ConsistencyLevel.ANY)
                .build();

        EntityWriteResult<TransactionV1> result = cassandraTemplate.insert(transaction, insertOptions);
        return result.getEntity();
    }
}
