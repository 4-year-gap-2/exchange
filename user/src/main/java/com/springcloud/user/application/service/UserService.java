package com.springcloud.user.application.service;

import com.springcloud.user.application.command.*;
import com.springcloud.user.application.result.FindUserBalanceResult;
import com.springcloud.user.application.result.FindUserResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface UserService {
    FindUserResult signUp(CreateUserCommand command);

    void login(LoginUserCommand command, HttpServletResponse httpServletResponse);

//    void createFee(BigDecimal feeValue);

    FindUserBalanceResult createWallet(CreateWalletCommand command, UUID userId);

    FindUserBalanceResult incrementBalance(UpdateIncrementBalanceCommand command);

    void internalDecrementBalance(DecreaseBalanceCommand command);

    void internalIncrementBalance(IncreaseBalanceCommand command);

    Page<FindUserBalanceResult> findBalance(UUID userId, int page, int size);
}
