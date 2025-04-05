package com.exchange.matching.presentation.external;

import com.exchange.matching.application.dto.ListTransactionResponse;
import com.exchange.matching.application.service.TransactionFacade;
import com.exchange.matching.common.response.ResponseDto;
import com.exchange.matching.domain.entiry.TransactionV1;
import com.exchange.matching.presentation.dto.CreateTransactionRequest;
import com.exchange.matching.presentation.dto.FindTransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionFacade facade;

    @PostMapping
    public ResponseEntity<ResponseDto<TransactionV1>> createTransaction(@RequestBody CreateTransactionRequest request) {
        TransactionV1 savedTransactionV1 = facade.createTransaction(request);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success(savedTransactionV1));
    }

    @GetMapping
    public ResponseEntity<ResponseDto<ListTransactionResponse>> getTransactionsByUserId(@ModelAttribute FindTransactionRequest request,
                                                                                        Pageable pageable) {
        ListTransactionResponse transactionV1s = facade.getTransactionsByUserId(request, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success(transactionV1s));
    }
}

