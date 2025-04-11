package com.springcloud.user.application.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class FindUserResult {
    private UUID userId;
    private String username;
    private String phone;


}
