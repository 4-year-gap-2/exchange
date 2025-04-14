package com.springcloud.user.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;
@AllArgsConstructor
@Getter
@Builder
public class CreateWalletCommand {
    private UUID coinId;
}
