package com.springcloud.user.application.command;

import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
@AllArgsConstructor
public class CreateUserCommand {
    private String username;
    private String password;
    private String phone;
    private String email;
    private String bankAccountNumber;

    public User toEntity(String encodedPassword) {
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .phone(phone)
                .email(email)
                .bankAccountNumber(bankAccountNumber)
                .role(UserRole.MASTER)
                .build();
    }
}


