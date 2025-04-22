package com.springcloud.user.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class CheckBalanceResponse {
    boolean success;
    String message;
}
