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

    private final KafkaTemplate<String, KafkaMatchingEvent> kafkaTemplate;

    @PostMapping
    public ResponseEntity<ResponseDto<String>> match(@RequestBody CreateMatchingRequest createMatchingRequest) {
        kafkaTemplate.send("matching-events",KafkaMatchingEvent.fromCommand(CreateMatchingCommand.fromRequest(createMatchingRequest)));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }
}
