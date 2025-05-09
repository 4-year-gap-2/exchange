package com.exchange.matching.presentation.external;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.common.response.ResponseDto;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import com.exchange.matching.presentation.dto.CreateMatchingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final KafkaTemplate<String, KafkaMatchingEvent> orderDeliveryKafkaTemplate;

    @PostMapping("/v1")
    public ResponseEntity<ResponseDto<String>> matchV1(@RequestBody CreateMatchingRequest createMatchingRequest) {
        orderDeliveryKafkaTemplate.send("user-to-matching.execute-order-delivery.v1",
                KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/v2")
    public ResponseEntity<ResponseDto<String>> matchV2(@RequestBody CreateMatchingRequest createMatchingRequest) {
        orderDeliveryKafkaTemplate.send("user-to-matching.execute-order-delivery.v2",
                KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/v3")
    public ResponseEntity<ResponseDto<String>> matchV3(@RequestBody CreateMatchingRequest createMatchingRequest) {
        orderDeliveryKafkaTemplate.send("user-to-matching.execute-order-delivery.v3",
                KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/v4")
    public ResponseEntity<ResponseDto<String>> matchV4(@RequestBody CreateMatchingRequest createMatchingRequest) {
        orderDeliveryKafkaTemplate.send("user-to-matching.execute-order-delivery.v4",
                KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/v5")
    public ResponseEntity<ResponseDto<String>> matchV5(@RequestBody CreateMatchingRequest createMatchingRequest) {
        orderDeliveryKafkaTemplate.send("user-to-matching.execute-order-delivery.v5",
                KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/v6a")
    public ResponseEntity<ResponseDto<String>> matchV6A(@RequestBody CreateMatchingRequest createMatchingRequest) {
        orderDeliveryKafkaTemplate.send("user-to-matching.execute-order-delivery.v6a",
                KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/v6b")
    public ResponseEntity<ResponseDto<String>> match6B(@RequestBody CreateMatchingRequest createMatchingRequest) {
        orderDeliveryKafkaTemplate.send("user-to-matching.execute-order-delivery.v6b",
                KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/v6c")
    public ResponseEntity<ResponseDto<String>> match6C(@RequestBody CreateMatchingRequest createMatchingRequest) {
        orderDeliveryKafkaTemplate.send("user-to-matching.execute-order-delivery.v6c",
                KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/v6d")
    public ResponseEntity<ResponseDto<String>> match6D(@RequestBody CreateMatchingRequest createMatchingRequest) {
        orderDeliveryKafkaTemplate.send("user-to-matching.execute-order-delivery.v6d",
                KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }
}
