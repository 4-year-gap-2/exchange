package com.exchange.order_completed.infrastructure.external;

import com.exchange.order_completed.common.response.ResponseDto;
import com.exchange.order_completed.infrastructure.dto.CreateOrderStoreRequest;
import com.exchange.order_completed.infrastructure.dto.KafkaMatchedOrderStoreEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SandMessageTestController {

    private final KafkaTemplate<String, KafkaMatchedOrderStoreEvent> kafkaTemplate;
    private static final String TOPIC_MATCHED = "matching-to-order_completed.execute-order-matched";
    private static final String TOPIC_UNMATCHED = "matching-to-order_completed.execute-order-unmatched";

    @PostMapping("/matched")
    public ResponseEntity<ResponseDto<String>> storeMatched(@RequestBody CreateOrderStoreRequest request) {
        KafkaMatchedOrderStoreEvent event = KafkaMatchedOrderStoreEvent.builder()
                .tradingPair(request.tradingPair())
                .orderType(request.orderType())
                .price(request.price())
                .quantity(request.quantity())
                .userId(request.userId())
                .orderId(request.orderId())
                .idempotencyId(request.idempotencyId())
                .build();

        kafkaTemplate.send(TOPIC_MATCHED, event);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/unmatched")
    public ResponseEntity<ResponseDto<String>> storeUnmatched(@RequestBody CreateOrderStoreRequest request) {
        KafkaMatchedOrderStoreEvent event = KafkaMatchedOrderStoreEvent.builder()
                .tradingPair(request.tradingPair())
                .orderType(request.orderType())
                .price(request.price())
                .quantity(request.quantity())
                .userId(request.userId())
                .orderId(request.orderId())
                .build();

        kafkaTemplate.send(TOPIC_UNMATCHED, event);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }
}


