package com.exchange.order_completed.infrastructure.external;

import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import com.exchange.order_completed.application.service.OrderCompletedFacade;
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

    private final OrderCompletedFacade orderCompletedFacade;
    private static final String TOPIC = "matching-to-order_completed.execute-order-info-save";
    private static final String GROUP_ID = "matching-service";

    @KafkaListener(
            topics = TOPIC,
            groupId = GROUP_ID,
            containerFactory = "completeOrderKafkaListenerContainerFactory")
    public void consumeMessage(ConsumerRecord<String, KafkaOrderStoreEvent> record, Acknowledgment ack, @Header(KafkaHeaders.DELIVERY_ATTEMPT) Integer attempt) {
        KafkaOrderStoreEvent event = record.value();
        CreateOrderStoreCommand command = CreateOrderStoreCommand.from(event);
        orderCompletedFacade.completeOrder(command, attempt);
        ack.acknowledge();
    }
}
