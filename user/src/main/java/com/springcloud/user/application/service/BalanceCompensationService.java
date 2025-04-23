package com.springcloud.user.application.service;

import com.springcloud.user.application.command.UserBalanceRollBackCommand;
import com.springcloud.user.domain.entity.UserBalance;
import org.springframework.stereotype.Service;

@Service
public interface BalanceCompensationService {
    void rollBack(UserBalanceRollBackCommand command);
}
