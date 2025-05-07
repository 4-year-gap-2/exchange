package com.exchange.order_completed.infrastructure.external;

import com.exchange.order_completed.application.command.ChartCommand;
import com.exchange.order_completed.application.command.CreateMatchedOrderStoreCommand;
import com.exchange.order_completed.application.command.CreateUnmatchedOrderStoreCommand;
import com.exchange.order_completed.application.service.OrderCompletedService;
import com.exchange.order_completed.infrastructure.dto.CompletedOrderChangeEvent;
import com.exchange.order_completed.infrastructure.dto.KafkaMatchedOrderStoreEvent;
import com.exchange.order_completed.infrastructure.dto.KafkaUnmatchedOrderStoreEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.apache.kafka.common.header.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class KafkaEventConsumer {

    private final Counter endToEndCounter;
    private final Timer endToEndTimer;
    private final MeterRegistry meterRegistry;
    private final OrderCompletedService orderCompletedService;

    public KafkaEventConsumer(OrderCompletedService orderCompletedService, MeterRegistry meterRegistry) {
        this.orderCompletedService = orderCompletedService;
        this.meterRegistry = meterRegistry;

        // 전체 체인 처리 카운터 (A→B→C 전체 TPS)
        this.endToEndCounter = Counter.builder("matching_chain_completed_total")
                .description("Total number of completed event processing chains (A→B→C)")
                .register(meterRegistry);

        // 전체 체인 처리 시간 (A→B→C 전체 처리 시간)
        this.endToEndTimer = Timer.builder("matching_chain_processing_time")
                .description("Total time from message creation to final processing (A→B→C)")
                .register(meterRegistry);
    }

    @KafkaListener(
            topics = {"matching-to-order_completed.execute-order-matched"},
            containerFactory = "matchedOrderKafkaListenerContainerFactory")
    public void consumeMatchedMessage(List<ConsumerRecord<String, KafkaMatchedOrderStoreEvent>> records, Acknowledgment ack) {
        System.out.println("총 메시지 수: " + records.size());

        // 타이머 시작
        Timer.Sample sample = Timer.start(meterRegistry);

        //year_month_date
        LocalDate yearMonthDate = LocalDate.now();

        //shard
        // 1, 2, 3 중 하나를 균등 확률로 반환
        int shard = ThreadLocalRandom.current().nextInt(1, 4); // 1 이상 4 미만: 1, 2, 3

        try {
            for (ConsumerRecord<String, KafkaMatchedOrderStoreEvent> record : records) {
                KafkaMatchedOrderStoreEvent value = record.value();
                CreateMatchedOrderStoreCommand command = CreateMatchedOrderStoreCommand.from(value);
                Header deliveryAttemptHeader = record.headers().lastHeader(KafkaHeaders.DELIVERY_ATTEMPT);

                int attempt = deliveryAttemptHeader != null
                        ? Integer.parseInt(new String(deliveryAttemptHeader.value(), StandardCharsets.UTF_8))
                        : 1;

                orderCompletedService.completeMatchedOrder(command, shard, yearMonthDate, attempt);
            }

//            startTime = 9999999999999L - event.getBuyTimestamp();
//            long endToEndDuration = System.currentTimeMillis() - startTime;
        } finally {
            // 타이머 종료 및 기록
            sample.stop(endToEndTimer);
            ack.acknowledge();
            // 전체 체인 처리 시간 기록
//            endToEndTimer.record(endToEndDuration, TimeUnit.MILLISECONDS);
        }
    }

    @KafkaListener(
            topics = {"matching-to-order_completed.execute-order-unmatched"},
            containerFactory = "unmatchedOrderKafkaListenerContainerFactory")
    public void consumeUnmatchedMessage(List<ConsumerRecord<String, KafkaUnmatchedOrderStoreEvent>> records, Acknowledgment ack) {
        System.out.println("총 메시지 수: " + records.size());

//        KafkaUnmatchedOrderStoreEvent event = record.value();
//        long startTime = event.getStartTime();
//        long endToEndDuration = System.currentTimeMillis() - startTime;

        for (ConsumerRecord<String, KafkaUnmatchedOrderStoreEvent> record : records) {
            KafkaUnmatchedOrderStoreEvent value = record.value();
            CreateUnmatchedOrderStoreCommand command = CreateUnmatchedOrderStoreCommand.from(value);
            Header deliveryAttemptHeader = record.headers().lastHeader(KafkaHeaders.DELIVERY_ATTEMPT);

            int attempt = deliveryAttemptHeader != null
                    ? Integer.parseInt(new String(deliveryAttemptHeader.value(), StandardCharsets.UTF_8))
                    : 1;

            orderCompletedService.completeUnmatchedOrder(command, attempt);
        }

        // 전체 체인 처리 시간 기록
//        endToEndTimer.record(endToEndDuration, TimeUnit.MILLISECONDS);

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
