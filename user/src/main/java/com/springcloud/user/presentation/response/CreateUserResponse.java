package com.springcloud.user.presentation.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(force = true)
public class CreateUserResponse {

    private final UUID userId;
    private final String username;
    private final String phone;
    private final String email;

    public CreateUserResponse(UUID userId, String username, String phone, String email) {
        this.userId = userId;
        this.username = username;
        this.phone = phone;
        this.email = email;
    }
}
