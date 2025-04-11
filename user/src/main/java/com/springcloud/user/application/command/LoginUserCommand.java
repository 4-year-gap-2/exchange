package com.springcloud.user.application.command;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginUserCommand {
    private String username;
    private String password;
}
