package com.exchange.order_completed.infrastructure.external;

import com.exchange.order_completed.application.command.ChartCommand;
import com.exchange.order_completed.application.command.CreateMatchedOrderStoreCommand;
import com.exchange.order_completed.application.command.CreateUnmatchedOrderStoreCommand;
import com.exchange.order_completed.application.service.CurrentPriceService;
import com.exchange.order_completed.application.service.OrderCompletedService;
import com.exchange.order_completed.infrastructure.dto.CompletedOrderChangeEvent;
import com.exchange.order_completed.infrastructure.dto.KafkaMatchedOrderStoreEvent;
import com.exchange.order_completed.infrastructure.dto.KafkaUnmatchedOrderStoreEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.apache.kafka.common.header.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final OrderCompletedService orderCompletedService;
    private final CurrentPriceService currentPriceService;

    @KafkaListener(
            topics = {"matching-to-order_completed.execute-order-matched"},
            containerFactory = "matchedOrderKafkaListenerContainerFactory")
    public void consumeMatchedMessage(List<ConsumerRecord<String, KafkaMatchedOrderStoreEvent>> recordList, Acknowledgment ack) {
        List<CreateMatchedOrderStoreCommand> commandList = new ArrayList<>();

        try {
            for (ConsumerRecord<String, KafkaMatchedOrderStoreEvent> record : recordList) {
                KafkaMatchedOrderStoreEvent value = record.value();
                CreateMatchedOrderStoreCommand buyCommand = CreateMatchedOrderStoreCommand.fromBuyOrderInfo(value);
                CreateMatchedOrderStoreCommand sellCommand = CreateMatchedOrderStoreCommand.fromSellOrderInfo(value);
                commandList.add(buyCommand);
                commandList.add(sellCommand);
            }

            // 체결 주문 처리
            orderCompletedService.completeMatchedOrder(commandList);

            // 현재 가격만 업데이트
            currentPriceService.updateCurrentPrice(commandList);
        } finally {
            ack.acknowledge();
        }
    }

    @KafkaListener(
            topics = {"matching-to-order_completed.execute-order-unmatched"},
            containerFactory = "unmatchedOrderKafkaListenerContainerFactory")
    public void consumeUnmatchedMessage(List<ConsumerRecord<String, KafkaUnmatchedOrderStoreEvent>> records, Acknowledgment ack) {
        for (ConsumerRecord<String, KafkaUnmatchedOrderStoreEvent> record : records) {
            KafkaUnmatchedOrderStoreEvent value = record.value();
            CreateUnmatchedOrderStoreCommand command = CreateUnmatchedOrderStoreCommand.from(value);

            orderCompletedService.completeUnmatchedOrder(command);
        }

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
    }
}
