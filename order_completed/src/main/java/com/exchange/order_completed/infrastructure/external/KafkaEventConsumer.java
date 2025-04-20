package com.exchange.order_completed.infrastructure.external;

import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import com.exchange.order_completed.application.service.OrderCompletedFacade;
import com.exchange.order_completed.infrastructure.dto.KafkaOrderStoreEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final OrderCompletedFacade orderCompletedFacade;

    @KafkaListener(
            topics = "4yearGap.match.orderStore.store",
            groupId = "matching-service",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeMessage(ConsumerRecord<String, KafkaOrderStoreEvent> record, Acknowledgment ack) {
        KafkaOrderStoreEvent event = record.value();
        CreateOrderStoreCommand command = CreateOrderStoreCommand.from(event);
        orderCompletedFacade.saveCompletedOrder(command);
        ack.acknowledge();
    }
}
