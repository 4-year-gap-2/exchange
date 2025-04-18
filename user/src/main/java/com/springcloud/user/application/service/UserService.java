package com.springcloud.user.application.service;

import com.springcloud.user.application.command.CreateUserCommand;
import com.springcloud.user.application.command.CreateWalletCommand;
import com.springcloud.user.application.command.LoginUserCommand;
import com.springcloud.user.application.command.UpdateIncrementBalanceCommand;
import com.springcloud.user.application.result.FindUserBalanceResult;
import com.springcloud.user.application.result.FindUserResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public interface UserService {
    FindUserResult signUp(CreateUserCommand command);

    void login(LoginUserCommand command, HttpServletResponse httpServletResponse);

    void createFee(BigDecimal feeValue);

    FindUserBalanceResult createWallet(CreateWalletCommand command, UUID userId);

    FindUserBalanceResult incrementBalance(UpdateIncrementBalanceCommand command);
}
