package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import com.exchange.order_completed.domain.entiry.CompletedOrder;
import com.exchange.order_completed.domain.repository.CompletedOrderReader;
import com.exchange.order_completed.domain.repository.CompletedOrderStore;
import com.exchange.order_completed.infrastructure.dto.KafkaBalanceIncreaseEvent;
import com.exchange.order_completed.infrastructure.external.KafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.WriteResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCompletedFacade {

    private final CompletedOrderStore completedOrderStore;
    private final CompletedOrderReader completedOrderReader;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final CassandraTemplate cassandraTemplate;

    public void completeOrder(CreateOrderStoreCommand command) {
        CompletedOrder persistedCompletedOrder = completedOrderReader.findByUserIdAndOrderId(command.userId(), command.orderId());

        if (persistedCompletedOrder != null) {
            log.info("이미 완료된 주문입니다. orderId: {}", command.orderId());
            return;
        }

        CompletedOrder newCompletedOrder = command.toEntity();

        completedOrderStore.save(newCompletedOrder);

        kafkaEventPublisher.publishMessage(KafkaBalanceIncreaseEvent.from(command));
    }

    @Deprecated(forRemoval = true)
    public void completeOrder2(CreateOrderStoreCommand command) {
        CompletedOrder newCompletedOrder = command.toEntity();

        // 경량 트랜잭션 LWT: IF NOT EXISTS 옵션 생성
        InsertOptions options = InsertOptions.builder()
                .withIfNotExists()
                .build();

        // 내부적으로 INSERT ~ IF NOT EXISTS 실행
        WriteResult result = cassandraTemplate.insert(newCompletedOrder, options);

        if (!result.wasApplied()) {
            log.info("이미 완료된 주문입니다. orderId={}", newCompletedOrder.getOrderId());
            return;
        }

        kafkaEventPublisher.publishMessage(KafkaBalanceIncreaseEvent.from(command));
    }

}
