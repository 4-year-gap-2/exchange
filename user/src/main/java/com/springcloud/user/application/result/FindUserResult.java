package com.springcloud.user.application.result;

import com.springcloud.user.domain.entity.UserBalance;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class FindUserResult {
    private UUID userId;
    private String username;
    private String phone;
    private String email;
    private List<UserBalance> userBalances;

}
