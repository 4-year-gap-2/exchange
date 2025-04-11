package com.springcloud.user.presentation.request;

import com.springcloud.user.application.command.LoginUserCommand;
import lombok.Getter;

@Getter
public class LoginUserRequest {
    private String username;
    private String password;

    public LoginUserCommand toCommand() {
        return LoginUserCommand.builder()
                .username(username)
                .password(password)
                .build();
    }
}
