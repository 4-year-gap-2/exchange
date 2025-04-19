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

    private final KafkaTemplate<String, KafkaMatchingEvent> matchingEventKafkaTemplate;

    @PostMapping("/v1")
    public ResponseEntity<ResponseDto<String>> matchV1(@RequestBody CreateMatchingRequest createMatchingRequest) {
        matchingEventKafkaTemplate.send("matching-events-tps-v1", KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/v2")
    public ResponseEntity<ResponseDto<String>> matchV2(@RequestBody CreateMatchingRequest createMatchingRequest) {
        matchingEventKafkaTemplate.send("matching-events-tps-v2", KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }

    @PostMapping("/v4")
    public ResponseEntity<ResponseDto<String>> matchV4(@RequestBody CreateMatchingRequest createMatchingRequest) {
        matchingEventKafkaTemplate.send("matching-events-tps-v4", KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }
}
