package com.exchange.matching.presentation.external;

import com.exchange.matching.common.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    @PostMapping
    public ResponseEntity<ResponseDto<String>> createHub() {
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success("success"));
    }
}
