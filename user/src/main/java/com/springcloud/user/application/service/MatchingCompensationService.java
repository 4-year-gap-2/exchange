package com.springcloud.user.application.service;

import com.springcloud.user.application.command.UserBalanceRollBackCommand;
import com.springcloud.user.application.enums.OrderType;
import com.springcloud.user.common.exception.CustomNotFoundException;
import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.entity.UserBalance;
import com.springcloud.user.domain.repository.UserBalanceRepository;
import com.springcloud.user.domain.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchingCompensationService implements BalanceCompensationService {

    private final UserBalanceRepository userBalanceRepository;

    @Override
    @Transactional
    public void rollBack(UserBalanceRollBackCommand command) {

        String targetCoin = getTargetCoinFromTradingPair(command.getTradingPair(),command.getOrderType());

        UserBalance balance = userBalanceRepository.findUserBalanceWithUserAndCoin(command.getUserId(),targetCoin)
                        .orElseThrow(() -> new IllegalArgumentException());

        if(command.getOrderType().equals(OrderType.SELL)){
            balance.increase(command.getPrice());
        }
        balance.increase(command.getQuantity());
    }

    private String getTargetCoinFromTradingPair(String tradingPair, OrderType orderType) {
        if(orderType.equals(OrderType.SELL)){
            return tradingPair.split("/")[1];
        }
        return tradingPair.split("/")[0];
    }
}
