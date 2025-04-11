package com.exchange.matching.presentation.external;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.service.MatchingFacade;
import com.exchange.matching.common.response.ResponseDto;
import com.exchange.matching.domain.service.MatchingServiceV2;
import com.exchange.matching.presentation.dto.CreateMatchingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {


    private final MatchingFacade matchingFacade;


    @PostMapping
    public ResponseEntity<ResponseDto<String>> createHub(@RequestBody CreateMatchingRequest createMatchingRequest) {


        matchingFacade.match(CreateMatchingCommand.fromRequest(createMatchingRequest));
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }
}
