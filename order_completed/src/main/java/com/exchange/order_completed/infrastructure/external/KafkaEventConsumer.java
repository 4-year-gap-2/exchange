package com.exchange.order_completed.infrastructure.external;

import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import com.exchange.order_completed.application.service.OrderCompletedService;
import com.exchange.order_completed.infrastructure.dto.KafkaOrderStoreEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final OrderCompletedService orderCompletedService;
    private static final String TOPIC_MATCHED = "matching-to-order_completed.execute-order-matched";
    private static final String TOPIC_UNMATCHED = "matching-to-order_completed.execute-order-unmatched";
    private static final String GROUP_ID = "matching-service";

    @KafkaListener(
            topics = TOPIC_MATCHED,
            groupId = GROUP_ID,
            containerFactory = "completeOrderKafkaListenerContainerFactory")
    public void consumeMatchedMessage(ConsumerRecord<String, KafkaOrderStoreEvent> record, Acknowledgment ack, @Header(KafkaHeaders.DELIVERY_ATTEMPT) Integer attempt) {
        KafkaOrderStoreEvent event = record.value();
        CreateOrderStoreCommand command = CreateOrderStoreCommand.from(event);
        orderCompletedService.completeMatchedOrder(command, attempt);
        ack.acknowledge();
    }

    @KafkaListener(
            topics = TOPIC_UNMATCHED,
            groupId = GROUP_ID,
            containerFactory = "completeOrderKafkaListenerContainerFactory")
    public void consumeUnmatchedMessage(ConsumerRecord<String, KafkaOrderStoreEvent> record, Acknowledgment ack, @Header(KafkaHeaders.DELIVERY_ATTEMPT) Integer attempt) {
        KafkaOrderStoreEvent event = record.value();
        CreateOrderStoreCommand command = CreateOrderStoreCommand.from(event);
        orderCompletedService.completeUnmatchedOrder(command, attempt);
        ack.acknowledge();
    }
}
