package com.exchange.order_completed.presentation.external;

import com.exchange.order_completed.common.response.ResponseDto;
import com.exchange.order_completed.infrastructure.dto.KafkaMatchedOrderStoreEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class OrderCompletedController {

    private final KafkaTemplate<String, KafkaMatchedOrderStoreEvent> kafkaTemplate;

    // 고정된 UUID 생성
    UUID fixedUserId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID fixedOrderId = UUID.fromString("20ab765f-5326-4366-a0e9-2b6d40087781");

    @GetMapping("/matched")
    public ResponseEntity<ResponseDto<String>> orderMatched() {

        UUID randomUserId = fixedUserId;
        UUID randomOrderId = fixedOrderId;
        UUID randomIdempotencyId = UUID.randomUUID();

        long currentTimeMillis = Instant.now().toEpochMilli();

        KafkaMatchedOrderStoreEvent event = KafkaMatchedOrderStoreEvent.builder()
                .tradingPair("BTC/KRW")
                .orderType("BUY")
                .price(new BigDecimal("36782.50"))
                .quantity(new BigDecimal("0.0001"))
                .userId(randomUserId)
                .orderId(randomOrderId)
                .idempotencyId(randomIdempotencyId)
                .startTimeStamp(currentTimeMillis)
                .build();

        kafkaTemplate.send("matching-to-order_completed.execute-order-matched", String.valueOf(randomOrderId), event);

        return ResponseEntity.ok(ResponseDto.success("success"));
    }

    @GetMapping("/unmatched")
    public ResponseEntity<ResponseDto<String>> orderUnmatched() {

        UUID randomUserId = fixedUserId;
        UUID randomOrderId = UUID.randomUUID();
        UUID randomIdempotencyId = UUID.randomUUID();

        long currentTimeMillis = Instant.now().toEpochMilli();

        KafkaMatchedOrderStoreEvent event = KafkaMatchedOrderStoreEvent.builder()
                .tradingPair("BTC/KRW")
                .orderType("BUY")
                .price(new BigDecimal("36782.50"))
                .quantity(new BigDecimal("10000"))
                .userId(randomUserId)
                .orderId(randomOrderId)
                .idempotencyId(randomIdempotencyId)
                .startTimeStamp(currentTimeMillis)
                .build();

        kafkaTemplate.send("matching-to-order_completed.execute-order-unmatched", String.valueOf(randomOrderId), event);

        return ResponseEntity.ok(ResponseDto.success("success"));
    }
}
