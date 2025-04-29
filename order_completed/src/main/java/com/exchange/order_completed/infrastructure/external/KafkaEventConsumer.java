package com.exchange.order_completed.infrastructure.external;

import com.exchange.order_completed.application.command.ChartCommand;
import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import com.exchange.order_completed.application.service.OrderCompletedService;
import com.exchange.order_completed.infrastructure.dto.CompletedOrderChangeEvent;
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
    private static final String TOPIC = "matching-to-order_completed.execute-order-info-save";
    private static final String GROUP_ID = "matching-service";

    @KafkaListener(
            topics = TOPIC,
            groupId = GROUP_ID,
            containerFactory = "completeOrderKafkaListenerContainerFactory")
    public void consumeMessage(ConsumerRecord<String, KafkaOrderStoreEvent> record, Acknowledgment ack, @Header(KafkaHeaders.DELIVERY_ATTEMPT) Integer attempt) {
        KafkaOrderStoreEvent event = record.value();
        CreateOrderStoreCommand command = CreateOrderStoreCommand.from(event);
        orderCompletedService.completeOrder(command, attempt);
        ack.acknowledge();
    }

    @KafkaListener(
            topics = "cassandra.exchange.completed_order",
            containerFactory = "chartKafkaListenerContainerFactory")
    public void savaChart(CompletedOrderChangeEvent record) {

        if (!"i".equals(record.getOp())) {
            return ;
        }

        ChartCommand command = ChartCommand.fromEvent(record);

        orderCompletedService.saveChart(command);
        System.out.println("chart saved" + command.getPair());
    }
}
