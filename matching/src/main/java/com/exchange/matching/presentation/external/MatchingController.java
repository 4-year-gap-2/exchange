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

    @PostMapping("/v6")
    public ResponseEntity<ResponseDto<String>> matchV6(@RequestBody CreateMatchingRequest createMatchingRequest) {
        orderDeliveryKafkaTemplate.send("user-to-matching.execute-order-delivery.v6",
                KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/v7")
    public ResponseEntity<ResponseDto<String>> matchV7(@RequestBody CreateMatchingRequest createMatchingRequest) {
        orderDeliveryKafkaTemplate.send("user-to-matching.execute-order-delivery.v7",
                KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }
}
