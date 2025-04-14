package com.springcloud.user.presentation.controller;

import com.springcloud.user.application.command.CreateWalletCommand;
import com.springcloud.user.application.result.FindUserBalanceResult;
import com.springcloud.user.application.result.FindUserResult;
import com.springcloud.user.application.service.UserService;
import com.springcloud.user.common.UserInfoHeader;
import com.springcloud.user.presentation.request.CreateWalletRequest;
import com.springcloud.user.presentation.response.CreateUserBalanceResponse;
import com.springcloud.user.presentation.response.CreateUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Description;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/balances")
public class UserBalanceController {

    private final UserService userService;

    @Description("지갑 생성")
    @PostMapping("/wallet")
    public CreateUserBalanceResponse createWallet(@RequestBody CreateWalletRequest balanceRequest, HttpServletRequest request) {
        // user 정보 변환
        UserInfoHeader userInfo = new UserInfoHeader(request);
        CreateWalletCommand command = balanceRequest.toCommand();
        FindUserBalanceResult result = userService.createWallet(command,userInfo.getUserId());
        CreateUserBalanceResponse response = new CreateUserBalanceResponse(result.getBalancedId(),result.getUserId(),result.getCoinId(),result.getTotalBalance(),result.getAvailableBalance(),result.getWallet());
        return ResponseEntity.ok(response).getBody();
    }

    // 관리자 권한의 잔액 증가
    @PatchMapping("/increment")
    public CreateUserResponse incrementBalance(HttpServletRequest request) throws AccessDeniedException {
        // 권한 체크
        UserInfoHeader userInfo = new UserInfoHeader(request);
        if (!"MASTER".equals(userInfo.getUserRole())) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }

        // 서비스 로직

        //dto 변환하기
        return null;
    }


}
