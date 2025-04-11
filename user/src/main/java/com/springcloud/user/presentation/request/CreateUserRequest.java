package com.springcloud.user.presentation.request;

import com.springcloud.user.application.command.CreateUserCommand;
import lombok.Getter;

@Getter
public class CreateUserRequest {
    private String username;
    private String password;
    private String phone;
    private String bankAccountNumber;

    public CreateUserCommand toCommand() {
        return CreateUserCommand.builder()
                .username(username)
                .password(password)
                .phone(phone)
                .bankAccountNumber(bankAccountNumber)
                .build();
    }
}
