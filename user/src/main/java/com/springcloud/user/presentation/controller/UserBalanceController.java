package com.springcloud.user.presentation.controller;

import com.springcloud.user.common.UserInfoHeader;
import com.springcloud.user.presentation.response.CreateUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/balances")
public class UserBalanceController {
//    @PostMapping("/wallet")
//    public String createWallet(@RequestBody CreateWalletRequest balanceRequest, HttpServletRequest request) {
//        // user 정보 변환
//        UserInfoHeader userInfo = new UserInfoHeader(request);
//        CreateWalletCommand command = balanceRequest.toCommand();
//        FindUserBalanceResult result = userBalanceService.createWallet(command,userInfo.getUserId(),userInfo.getUserRole());
//        CreateUserResponse response = new CreateUserResponse(result.getUserId(),result.getUsername(),result.getPhone(),result.getEmail());
//
//        return
//    }


}
