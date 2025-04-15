package com.springcloud.user.application.service;

import com.springcloud.user.application.command.*;
import com.springcloud.user.application.query.CheckAvailableBalanceQuery;
import com.springcloud.user.application.query.UserBalanceQueryService;
import com.springcloud.user.application.query.UserQueryService;
import com.springcloud.user.application.result.CheckBalanceResult;
import com.springcloud.user.application.result.FindUserBalanceResult;
import com.springcloud.user.application.result.FindUserResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserBalanceCommandService userBalanceCommandService;
    private final ManagementCommandService managementCommandService;
    private final UserBalanceQueryService userBalanceQueryService;


    @Override
    public FindUserResult signUp(CreateUserCommand command) {
        return userCommandService.signUp(command);
    }

    @Override
    public void login(LoginUserCommand command, HttpServletResponse httpServletResponse) {
        userCommandService.login(command, httpServletResponse);
    }

    @Override
    public void createFee(BigDecimal feeValue) {
        managementCommandService.createFee(feeValue);
    }

    @Override
    public FindUserBalanceResult createWallet(CreateWalletCommand command, UUID userId) {
        return userBalanceCommandService.createBalance(command,userId);
    }

    @Override
    public FindUserBalanceResult incrementBalance(UpdateIncrementBalanceCommand command) {
        return userBalanceCommandService.incrementBalance(command);
    }

    @Override
    public CheckBalanceResult checkAvailableBalance(CheckAvailableBalanceQuery query) {
        return userBalanceQueryService.checkAvailableBalance(query);
    }
}
