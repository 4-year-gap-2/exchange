package com.exchange.order_completed.presentation.external;

import com.exchange.order_completed.application.TimeInterval;
import com.exchange.order_completed.application.service.OrderCompletedService;
import com.exchange.order_completed.application.service.TradeService;
import com.exchange.order_completed.common.UserInfoHeader;
import com.exchange.order_completed.common.response.ResponseDto;
import com.exchange.order_completed.domain.postgres.entity.TradeDataInfo;
import com.exchange.order_completed.presentation.dto.PagedResult;
import com.exchange.order_completed.presentation.dto.TradeDataResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TradeService tradeService;
    private final OrderCompletedService completedService;

    @GetMapping(value = "/chart/{pair}/{interval}")
    public ResponseEntity<ResponseDto<List<TradeDataInfo>>> getChartData(@PathVariable(value = "pair") String pair, @PathVariable(value = "interval") TimeInterval interval) {

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success(tradeService.getTradeInfo(pair,interval)));
    }

    //체결 주문 조회
    @GetMapping("/trade")
    public ResponseEntity<ResponseDto<PagedResult<TradeDataResponse>>> findTradeOrderHistory(
            HttpServletRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam String orderType,  // 필수 파라미터
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate  // 필수 파라미터
    ) {
        UserInfoHeader userInfo = new UserInfoHeader(request);

        Instant cursorInstant = null;
        if (cursor != null) {
            cursorInstant = cursor.atZone(ZoneId.systemDefault()).toInstant();
        }
        PagedResult<TradeDataResponse> pagedResult = completedService.findTradeOrderHistory(userInfo.getUserId(), cursorInstant, size, orderType, startDate, endDate);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.success(pagedResult));

    }
}