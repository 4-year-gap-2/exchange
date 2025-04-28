package com.exchange.order_completed.infrastructure.external;

import com.exchange.order_completed.common.response.ResponseDto;
import com.exchange.order_completed.infrastructure.dto.CreateOrderStoreRequest;
import com.exchange.order_completed.infrastructure.dto.KafkaOrderStoreEvent;
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

    private final KafkaTemplate<String, KafkaOrderStoreEvent> kafkaTemplate;
    private static final String TOPIC = "matching-to-order_completed.execute-order-matched";

    @PostMapping
    public ResponseEntity<ResponseDto<String>> store(@RequestBody CreateOrderStoreRequest request) {
        KafkaOrderStoreEvent event = KafkaOrderStoreEvent.builder()
                .tradingPair(request.tradingPair())
                .orderType(request.orderType())
                .price(request.price())
                .quantity(request.quantity())
                .userId(request.userId())
                .orderId(request.orderId())
                .build();

        kafkaTemplate.send(TOPIC, event);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }
}


