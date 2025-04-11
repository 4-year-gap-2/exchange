package com.springcloud.user.presentation.response;

import com.springcloud.user.application.result.FindUserResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(force = true)
public class CreateUserResponse {

    private final UUID userId;
    private final String username;
    private final String phone;


    public CreateUserResponse(UUID userId, String username, String phone) {
        this.userId = userId;
        this.username = username;
        this.phone = phone;
    }
}
