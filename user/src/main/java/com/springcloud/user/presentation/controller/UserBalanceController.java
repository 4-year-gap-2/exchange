package com.springcloud.user.presentation.controller;

import com.springcloud.user.application.command.CreateWalletCommand;
import com.springcloud.user.application.command.UpdateIncrementBalanceCommand;
import com.springcloud.user.application.result.FindUserBalanceResult;
import com.springcloud.user.application.service.UserService;
import com.springcloud.user.common.UserInfoHeader;
import com.springcloud.user.infrastructure.dto.MatchCompensatorEvent;
import com.springcloud.user.presentation.request.CreateWalletRequest;
import com.springcloud.user.presentation.request.UpdateIncrementBalanceRequest;
import com.springcloud.user.presentation.response.FindUserBalanceResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Description;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/balances")
public class UserBalanceController {

    private final UserService userService;
    private final KafkaTemplate<String, MatchCompensatorEvent> kafkaTemplate;

    @Description("지갑 생성")
    @PostMapping("/wallet")
    public FindUserBalanceResponse createWallet(@RequestBody CreateWalletRequest balanceRequest, HttpServletRequest request) {
        // user 정보 변환
        UserInfoHeader userInfo = new UserInfoHeader(request);
        CreateWalletCommand command = balanceRequest.toCommand();
        FindUserBalanceResult result = userService.createWallet(command,userInfo.getUserId());
        // FindUserBalanceResponse response = new FindUserBalanceResponse(result.getBalancedId(),result.getUserId(),result.getCoinId(),result.getTotalBalance(),result.getAvailableBalance(),result.getWallet());
        FindUserBalanceResponse response = FindUserBalanceResponse.from(result);
        return ResponseEntity.ok(response).getBody();
    }

    // 관리자 권한의 잔액 증가
    @PatchMapping("/increment")
    public FindUserBalanceResponse incrementBalance(HttpServletRequest request, @RequestBody UpdateIncrementBalanceRequest balanceRequest) throws AccessDeniedException {
        // 권한 체크
        UserInfoHeader userInfo = new UserInfoHeader(request);
        if (!"MASTER".equals(userInfo.getUserRole().name())) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
        // 서비스 로직
        UpdateIncrementBalanceCommand command = new UpdateIncrementBalanceCommand(balanceRequest.getWallet(),balanceRequest.getAmount());
        FindUserBalanceResult result = userService.incrementBalance(command);
        //dto 변환하기
        FindUserBalanceResponse response = FindUserBalanceResponse.from(result);
        return ResponseEntity.ok(response).getBody();
    }

//    @PatchMapping("/decrement")
//    public FindUserBalanceResponse incrementBalance(HttpServletRequest request, @RequestBody ){
//
//    }



}
