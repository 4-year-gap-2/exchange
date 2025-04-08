package com.exchange.matching.presentation.external;

import com.exchange.matching.application.response.TransactionResponse;
import com.exchange.matching.application.response.ListTransactionResponse;
import com.exchange.matching.application.service.TransactionFacade;
import com.exchange.matching.common.response.ResponseDto;
import com.exchange.matching.presentation.dto.CreateTransactionRequest;
import com.exchange.matching.presentation.dto.FindTransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.UUID;


@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionFacade facade;

    @PostMapping
    public ResponseEntity<ResponseDto<TransactionResponse>> createTransaction(@RequestBody CreateTransactionRequest request) {
        Random random = new Random();

        // 가격: 5000 ~ 10000원 사이의 랜덤 값
        BigDecimal price = BigDecimal.valueOf(5000 + random.nextInt(5001));

        // 수량: 0.1 ~ 1.0 사이의 랜덤 값 (소수점 첫째자리까지)
        BigDecimal amount = BigDecimal.valueOf(0.1 + random.nextDouble() * 0.9)
                .setScale(1, RoundingMode.HALF_UP);

        // 거래 타입: BUY 또는 SELL 랜덤 선택
        String transactionType = random.nextBoolean() ? "BUY" : "SELL";

        // 원본 요청 객체에서 필요한 값을 가져와 새 요청 객체 생성
        CreateTransactionRequest completeRequest = new CreateTransactionRequest(
                UUID.randomUUID(),
                price,
                amount,
                transactionType,
                request.pair(),
                request.dataBaseType()
        );

        TransactionResponse savedTransactionV1 = facade.createTransaction(completeRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success(savedTransactionV1));
    }

    @GetMapping
    public ResponseEntity<ResponseDto<ListTransactionResponse>> getTransactionsByUserId(@ModelAttribute FindTransactionRequest request,
                                                                                        Pageable pageable) {
        ListTransactionResponse transactionV1s = facade.getTransactionsByUserId(request, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success(transactionV1s));
    }
}

