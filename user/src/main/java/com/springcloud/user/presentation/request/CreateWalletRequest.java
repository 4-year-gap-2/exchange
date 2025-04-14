package com.springcloud.user.presentation.request;

import com.springcloud.user.application.command.CreateWalletCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateWalletRequest {
    private UUID coinId;

    public CreateWalletCommand toCommand() {
        return CreateWalletCommand.builder()
                .coinId(coinId)
                .build();
    }
}
