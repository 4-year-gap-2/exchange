package com.springcloud.user.application.service;

import com.springcloud.user.application.command.*;
import com.springcloud.user.application.query.UserBalanceQueryService;
import com.springcloud.user.application.result.FindUserBalanceResult;
import com.springcloud.user.application.result.FindUserResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserCommandService userCommandService;
    private final UserBalanceCommandService userBalanceCommandService;
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
    public FindUserBalanceResult createWallet(CreateWalletCommand command, UUID userId) {
        return userBalanceCommandService.createBalance(command,userId);
    }

    @Override
    public FindUserBalanceResult incrementBalance(UpdateIncrementBalanceCommand command) {
        return userBalanceCommandService.incrementBalance(command);
    }

    @Override
    public void internalDecrementBalance(DecreaseBalanceCommand command) {
        userBalanceCommandService.internalDecrementBalance(command);
    }

    @Override
    public void internalIncrementBalance(IncreaseBalanceCommand command) {
        userBalanceCommandService.internalIncrementBalance(command);
    }

    @Override
    public Page<FindUserBalanceResult> findBalance(UUID userId, int page, int size) {
        return userBalanceQueryService.findBalance(userId, page, size);
    }
}
